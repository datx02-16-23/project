# from ast import Subscript,Tuple,List, Num,Str,Name, Call,Assign, Store,Load
# from ast import dump,fix_missing_locations,copy_location,parse,NodeTransformer
# from ast import Add
from ast import *
from codegen import to_source as ts
from printnode import ast_visit as printnode
######################## Utilities ##########################
DEFINED_OPERATIONS = ['write','read']

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

	def visit_Name(self,node):
		if node.id == 'False' or node.id == 'True':
			return node
		return copy_location(
			Tuple(elts=
				[Str(s='var'),Str(s=node.id)]
			)
		,node)

	def visit_BinOp(self,node):
		return copy_location(
			Tuple(elts=[
				Str('binop'),
				translate_op(type(node.op)),
				self.visit(node.left),
				self.visit(node.right)
			])
		,node)

	def visit_Call(self,node):
		return node

class OperationTransformer(NodeTransformer):
	def __init__(self,name):
		self.name = name
		self.expr_transformer = ExpressionTransformer()

	# args_ - ast.Node
	def create(self,args_):
		return Call(
			func=Name(id=self.name, ctx=Load()),
			args=args_,
			keywords=[]
		)

	def visit_Call(self,node):
		if node.func.id in DEFINED_OPERATIONS:
			return node
		else:
			for arg in node.args:
				self.expr_transformer.visit(arg)

	# OperationTransformer shouldnt know about WriteTransformer
	# should be a better solution
	def visit_Assign(self,node): return node

	def visit_AugAssign(self,node): return node
		
class WriteTransformer(OperationTransformer):
	def __init__(self):
		super(WriteTransformer,self).__init__('write')

	def visit_Assign(self,node):
		src = self.expr_transformer.visit(node.value)
		# need a deep copy of node.targets[0] (the left-most part
		# of the assignment) since we dont want to change that statement
		# this is an eval-hack, possibly more safe solution exists
		dst = self.expr_transformer.visit(eval(dump(node.targets[0])))
		write = self.create([src,dst,node.value])
		node.value = write
		return node
		# return copy_location(
		# 	Assign(
		# 		targets=node.targets,
		# 		value=write
		# 	)
		# ,node)

	def insert(self,target,mod):
		mod.body.insert(0,
			Assign(
				targets = [target],
				value 	= self.create([target,self.expr_transformer.visit(target)])
			)
		)

	def visit_For(self,node):
		for i,field in enumerate(node.body):
			node.body[i] = self.visit(field)
		# inject an (for example) i = write(i,['var','i'])
		# to keep track of i during loop, not a very good solution though
		if isinstance(node.target,Name):
			self.insert(node.target,node)
		else:
			for target in node.target.elts:
				self.insert(target,node)
		return node

class ReadTransformer(OperationTransformer):
	def __init__(self):
		super(ReadTransformer,self).__init__('read')

	def create_read(self,node):
		stmt = self.expr_transformer.visit(node)
		read = self.create([stmt,node])
		return copy_location(read,node)

	def visit_Subscript(self,node): return self.create_read(node)

	def visit_Name(self,node): return self.create_read(node)

	def visit_For(self,node):
		self.visit(node.body)
		return node

######################## Tests ########################
def test_stmt(stmt):
	wt = WriteTransformer()
	rt = ReadTransformer()
	node = parse(stmt).body[0]
	node = wt.visit(node)
	node = rt.visit(node)
	ts_node = ts(node)
	print 'after : ',ts_node
	return ts_node

# # # assignment
# test_stmt("a = 1")						# integer
# test_stmt("a = 'asd'") 					# string
# test_stmt("a = False") 					# boolean
# test_stmt("a = [1,2,3]")				# array
# test_stmt("a = [[[1,2,3]]]")			# array
# test_stmt("a[0] = [1,2,3]")				# array dst
# test_stmt("a[0][1] = [1,2,3]")			# array dst
# test_stmt("a[0][b[0]+b[1]] = [1,2,3]")	# array dst
# test_stmt("a[len(a) - 1] = a[0]") 		# array dst,function call
# test_stmt("a[0] += 3")

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