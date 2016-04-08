def get_op(tr_op):
	return {
		'+' : lambda x,y:x+y,
		'-' : lambda x,y:x-y,
		'*' : lambda x,y:x*y,
		'/' : lambda x,y:x/y
	}[tr_op]

class Variable(object):
	def __init__(self,name,type_,attributes,x):
		pass
			
class VariableTable(object):
	def __init__(self):
		self.table = {}

	def update(self,src_expression,dst_expression):
		pass
		#1 evaluate src_expression (if able)
		#2 evaluate dst_expression (if able)
		#3 store src_value in dst_location

	def evaluate(self,expression):
		if not isinstance(expression,tuple):
			return expression
		(head,r) = (expression[0],expression[1:])
		return self.evaluate_(head,r)

	def evaluate_(self,head,r):
		if head == 'var':
			return self.evaluate_var(r[0])
		elif head == 'subscript':
			return self.evaluate_subscript(r)
		elif head == 'binop':
			return self.evaluate_binop(r)
		else:
			raise InvalidExpression('%s%s' % (head,r))

	def evaluate_var(self,var):
		if var not in self.table:
			raise InvalidExpression('%s not in table' % var)
		else:
			return self.table[var]

	def evaluate_subscript(self,subscript):
		(var,indices) = (self.evaluate(subscript[0]),subscript[1:])
		indices_ = []
		for i in indices:
			indices_.append(self.evaluate(i))
		for i in indices_:
			var = var[i]
		return var

	def evaluate_binop(self,binop):
		op = get_op(binop[0])
		l  = self.evaluate(binop[1])
		r  = self.evaluate(binop[2])
		return op(l,r)

class InvalidExpression(Exception): pass

######################## Tests ########################
# table
# t = VariableTable()
# t.table['x'] = [[1,2,3],[4,5,6]]
# t.table['y'] = 5
# print 'eval : ',t.evaluate(('var','x'))
# print 'eval : ',t.evaluate(('subscript',('var','x'),('subscript',('var','x'),0,0)))
# print 'eval : ',t.evaluate(('binop','+',('var','y'),2))