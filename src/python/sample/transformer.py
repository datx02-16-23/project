from constants import *
from ast import *
from codegen import to_source as ts
from printnode import ast_visit as printnode
from copy import deepcopy
######################## Utilities ##########################
def translate_op(op):
	tr_op = {
		Add: 	Str('+'),
	    Sub: 	Str('-'),
	    Mult:	Str('*'),
	    Div: 	Str('/'),

	}[op]
	return tr_op

def add_tuple(t1,t2):
	if not isinstance(t1,Tuple) and not isinstance(t2,Tuple):
		return Tuple(elts=[t1,t2])
	elif isinstance(t1,Tuple):
		if isinstance(t2,Tuple):
			return Tuple(elts=t1.elts+t2.elts)
		else:
			return Tuple(elts=t1.elts+[t2])
	else:
		return Tuple(elts=[t1]+t2.elts)

def is_variable(node):
	if not isinstance(node,Tuple):
		return False
	else:
		return isinstance(node.elts[0],Str) and node.elts[0].s == EXPR_VAR

def is_builtin(name):
	name_ = deepcopy(name)
	name_.lower()
	return name_ in BUILTINS_LOWERCASE

def get_lineno(node):
	lineno = [node.lineno]
	for child in iter_child_nodes(node):
		if hasattr(child,'lineno'):
			lineno.append(child.lineno)
			lineno = lineno + get_lineno(child)
	return lineno
#############################################################
class Expression(object):
	DEFINED_NAMES = []

	def __init__(self,name):
		self.name = Str(name)

	def get_expression(self,node):
		raise NotImplementedError

	def get_expression_(self,elts):
		return Tuple(elts=elts)

class SubscriptExpression(Expression):
	Expression.DEFINED_NAMES.append(EXPR_SUBSCRIPT)
	def __init__(self):
		super(SubscriptExpression,self).__init__(EXPR_SUBSCRIPT)

	# (EXPR_SUBSCRIPT, to, indices..)
	def get_expression(self,node):
		if is_variable(node.value) or isinstance(node.value,List):
			node.value = super(SubscriptExpression,self).get_expression_([self.name,node.value])
		expression = add_tuple(node.value,node.slice)
		return expression

class NameExpression(Expression):
	Expression.DEFINED_NAMES.append(EXPR_VAR)
	def __init__(self):
		super(NameExpression,self).__init__(EXPR_VAR)

	# (EXPR_VAR,node.id, EXPR_STORE) if Store is context
	# (EXPR_VAR,node.id, node) if Load is context
	def get_expression(self,node):
		if is_builtin(node.id):
			return node
		value = node if isinstance(node.ctx,Load) else Str(EXPR_STORE)
		return super(NameExpression,self).get_expression_([self.name,Str(node.id),value])


class ExpressionTransformer(NodeTransformer):
	NAME = "expression_transformer"
	SUPPORTED_NODES = [TRANSFORMER_SUBSCRIPT,TRANSFORMER_NAME]

	def is_generated_expression(self,node):
		if isinstance(node,Tuple) and len(node.elts) > 0:
			return node.elts[0] in Expression.DEFINED_NAMES
		else:
			return False

	def visit_Subscript(self,node):
		node.value = self.visit(node.value)
		expression = SubscriptExpression().get_expression(node)
		return copy_location(expression,node)

	def visit_Name(self,node):
		expression = NameExpression().get_expression(node)
		return copy_location(expression,node)

	def generic_visit(self,node):
		if type(node).__name__ in self.SUPPORTED_NODES:
			super(ExpressionTransformer,self).visit(node)
		else:
			return node

	def visit_BinOp(self,node): return node
	def visit_Call(self,node): return node
	def visit_Delete(self,node): return node
	def visit_DictComp(self,node): return node
	def visit_ListComp(self,node): return node

class OperationTransformer(NodeTransformer):
	DEFINED_OPERATIONS = []

	def __init__(self,name):
		self.name = name
		OperationTransformer.DEFINED_OPERATIONS.append(name)
		self.expr_transformer = ExpressionTransformer()

	# args_ - [ast.Node]
	def create_call(self,args_,node):
		lineno = get_lineno(node)
		if lineno:
			begin_line = Num(min(lineno))
			end_line = Num(max(lineno))
			args_ = args_ + [begin_line,end_line]
		return Call(
			func=Name(id=self.name, ctx=Load()),
			args=args_,
			keywords=[]
		)

	def visit_Call(self,node):
		for arg in node.args:
			self.visit(arg)
		return node

	def visit_FunctionDef(self,node):
		for field in node.body:
			self.visit(field)
		return node

	# Following nodes should generally be avoided 
	# if not specifically needed for implementation
	def visit_Assign(self,node): return node
	def visit_AugAssign(self,node): return node
	def visit_Delete(self,node): return node
	def visit_DictComp(self,node): return node
	def visit_ListComp(self,node): return node	
	def visit_IfExp(self,node): return node

class WriteTransformer(OperationTransformer):
	def __init__(self,name):
		super(WriteTransformer,self).__init__(name)

	def visit_Assign(self,node):
		if len(node.targets) == 1 and (isinstance(node.targets[0],Name) or isinstance(node.targets[0],Subscript)):
			src = self.expr_transformer.visit(deepcopy(node.value))
			dst = self.expr_transformer.visit(deepcopy(node.targets[0]))
			write = self.create_call([src,dst],node)
			node.value = write
		return node

class ReadTransformer(OperationTransformer):
	def __init__(self,name):
		super(ReadTransformer,self).__init__(name)

	def create_read(self,node):
		stmt = self.expr_transformer.visit(deepcopy(node))
		read = self.create_call([stmt],node)
		return copy_location(read,node)

	def visit_Subscript(self,node): 
		return self.create_read(node)

	def visit_Name(self,node):
		return self.create_read(node)

	def visit_For(self,node):
		for line in node.body:
			self.visit(line)
		return node

	def visit_Assign(self,node):
		node.value = self.visit(node.value)
		return node

	def visit_Tuple(self,node):
		if self.expr_transformer.is_generated_expression(node):
			return node
		for e in node.elts:
			self.visit(e)

class PassTransformer(OperationTransformer):
	def __init__(self,name):
		super(PassTransformer,self).__init__(name)
		self.function_defs = []
		self.function_bodies = []

	def extract_params(self,args):
		reg_args = []
		for arg in args.args:
			reg_args.append(self.expr_transformer.visit(arg))
		return reg_args

	def visit_Module(self,node):
		for child in iter_child_nodes(node):
			self.visit(child)
		for body in self.function_bodies:
			for field in body:
				self.visit(field)
		return node

	def visit_FunctionDef(self,node):
		self.function_defs.append(node.name)
		node.decorator_list.append(self.create_call(self.extract_params(node.args),node))
		self.function_bodies.append(node.body)
		return node

	def visit_Call(self,node):
		if not isinstance(node.func,Attribute) and node.func.id in self.function_defs:
			for i,arg in enumerate(node.args):
				node.args[i] = self.expr_transformer.visit(deepcopy(arg))
		else:
			for child in iter_child_nodes(node):
				self.visit(child)
		return node

	def visit_Assign(self,node):
		for child in iter_child_nodes(node):
			self.visit(child)
		return node

######################## Tests ########################
# pt = PassTransformer()
# def test_stmt(stmt):
# 	wt = WriteTransformer()
# 	rt = ReadTransformer()
# 	node = parse(stmt).body[0]
# 	print 'before : ',stmt
# 	node = pt.visit(node)
# 	node = wt.visit(node)
# 	node = rt.visit(node)
# 	ts_node = ts(node)
# 	print 'after : ',ts_node
# 	return ts_node

# # assignment
# test_stmt("a = 1")						# integer
# test_stmt("a = 'asd'") 					# string
# test_stmt("a = False") 					# boolean
# test_stmt("a = [1,2,3]")				# array
# test_stmt("a = [[[1,2,3]]]")			# array
# test_stmt("tmp = a[i]")

# # indexing
# test_stmt("a[0] = [1,2,3]")				# array dst
# test_stmt("a[0][1] = [1,2,3]")			# array dst
# test_stmt("a[0][b[0]+b[1]] = [1,2,3]")	# array dst
# test_stmt("a[len(a) - 1] = a[0]") 		# array dst,function call
# test_stmt("a[0] += 3")
# test_stmt("a[j+1]")
# test_stmt("a[j+1][x+3]")

# # binop
# test_stmt("x = a + b")
# test_stmt("x = a + 3")

# # if
# test_stmt("if a: return True")
# test_stmt("if a[0]: return True")
# test_stmt("if a[0][1]: return True")
# test_stmt("if a[0][1] + a[2][4] == 3: return True")
# test_stmt("if a[0][1] + x + 3 == 3: return True")

# # for
# test_stmt("for i in range(0,10): x = 3")
# test_stmt("for i,j in (range(0,10),range(0,10)): x = 3")
# test_stmt("for i in [1,2,3]: print i")

# # function calls
# test_stmt("a = rand_array(10,50)")

# function declaration
# test_stmt("def foo(): return 0")
# test_stmt("def assign(value,target,i): target[i] = value")
# test_stmt("assign(0,array,0)")