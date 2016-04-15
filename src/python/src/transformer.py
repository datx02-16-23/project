# from ast import Subscript,Tuple,List, Num,Str,Name, Call,Assign, Store,Load
# from ast import dump,fix_missing_locations,copy_location,parse,NodeTransformer
# from ast import Add
from ast import *
from codegen import to_source as ts
from printnode import ast_visit as printnode
from copy import deepcopy
######################## Utilities ##########################
DEFINED_OPERATIONS = ['write','read','link']

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
		return isinstance(node.elts[0],Str) and node.elts[0].s == 'var'
#############################################################
class ExpressionTransformer(NodeTransformer):

	# ('subscript', to, indices..)
	def visit_Subscript(self,node):
		value  = self.visit(node.value)
		index  = self.visit(node.slice)
		expand = None
		if is_variable(value):
			expand = add_tuple(Tuple(elts=[value]),index)
			expand.elts = [Str('subscript')] + expand.elts
		else:
			expand = add_tuple(value,index)
		return copy_location(expand,node)

	# ('var',node.id, 'Store') if Store is context
	# ('var',node.id, node) if Load is context
	def visit_Name(self,node):
		if node.id == 'False' or node.id == 'True':
			return node
		value = [node] if isinstance(node.ctx,Load) else [Str('Store')]
		return copy_location(
			Tuple(elts=
				[Str(s='var'),Str(s=node.id)] + value
			)
		,node)

	#!! Not Supported !!
	# # ('binop', op, left, right)
	# def visit_BinOp(self,node):
	# 	return copy_location(
	# 		Tuple(elts=[
	# 			Str('binop'),
	# 			translate_op(type(node.op)),
	# 			self.visit(node.left),
	# 			self.visit(node.right)
	# 		])
	# 	,node)
	def visit_BinOp(self,node):	return node

	# these two need implementing
	def visit_DictComp(self,node): return node
	
	def visit_ListComp(self,node): return node

class OperationTransformer(NodeTransformer):
	def __init__(self,name):
		self.name = name
		self.expr_transformer = ExpressionTransformer()

	# args_ - [ast.Node]
	def create_call(self,args_):
		return Call(
			func=Name(id=self.name, ctx=Load()),
			args=args_,
			keywords=[]
		)

	# Following nodes should generally be avoided
	# or should be handled diffrently in future
	def visit_Call(self,node): return node

	def visit_Assign(self,node): return node

	def visit_AugAssign(self,node): return node

	def visit_Delete(self,node): return node

	def visit_FunctionDef(self,node):
		for field in node.body:
			self.visit(field)
		return node

class WriteTransformer(OperationTransformer):
	def __init__(self,name):
		super(WriteTransformer,self).__init__(name)

	def visit_Assign(self,node):
		if len(node.targets) == 1 and (isinstance(node.targets[0],Name) or isinstance(node.targets[0],Subscript)):
			src = self.expr_transformer.visit(deepcopy(node.value))
			dst = self.expr_transformer.visit(deepcopy(node.targets[0]))
			write = self.create_call([src,dst])
			node.value = write
		return node

	def insert_write(self,target,mod):
		mod.body.insert(0,
			Assign(
				targets = [target],
				value 	= self.create_call([target,self.expr_transformer.visit(target)])
			)
		)

	# def visit_For(self,node):
	# 	for i,field in enumerate(node.body):
	# 		node.body[i] = self.visit(field)
	# 	# inject an (for example) i = write(i,['var','i'])
	# 	# to keep track of i during loop, not a very good solution though
	# 	if isinstance(node.target,Name):
	# 		self.insert_write(node.target,node)
	# 	else:
	# 		for target in node.target.elts:
	# 			self.insert_write(target,node)
	# 	return node

class ReadTransformer(OperationTransformer):
	def __init__(self,name):
		super(ReadTransformer,self).__init__(name)

	def create_read(self,node):
		stmt = self.expr_transformer.visit(deepcopy(node))
		read = self.create_call([stmt,node])
		return copy_location(read,node)

	def visit_Subscript(self,node): 
		return self.create_read(node)

	def visit_Name(self,node):
		return self.create_read(node)

	def visit_For(self,node):
		for line in node.body:
			self.visit(line)
		return node

class PassTransformer(OperationTransformer):
	def __init__(self,name):
		super(PassTransformer,self).__init__(name)
		self.function_defs = []
		self.function_bodies = []

	def extract_params(self,args):
		reg_args = []
		for arg in args.args:
			reg_args.append(Str(arg.id))
		return reg_args
	def visit_Module(self,node):
		for child in iter_child_nodes(node):
			self.visit(child)
		for body in self.function_bodies:
			for field in body:
				self.visit(field)

	def visit_FunctionDef(self,node):
		self.function_defs.append(node.name)
		node.decorator_list.append(Call(
			func=Name(id=self.name),
			args=self.extract_params(node.args),
			keywords=[]
		))
		self.function_bodies.append(node.body)
		return node

	def visit_Call(self,node):
		if not isinstance(node.func,Attribute) and node.func.id in self.function_defs:
			for i,arg in enumerate(node.args):
				node.args[i] = Dict(
					values=[arg, self.expr_transformer.visit(deepcopy(arg))],
					keys=[Str('value'),Str('arg')]
				)
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