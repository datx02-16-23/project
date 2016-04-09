def get_op(tr_op):
	return {
		'+' : lambda x,y:x+y,
		'-' : lambda x,y:x-y,
		'*' : lambda x,y:x*y,
		'/' : lambda x,y:x/y
	}[tr_op]

class Variable(object):
	def __init__(self,name,rawType,attributes=None,abstractType=None):
		self.name = name
		self.rawType = rawType
		self.attributes = attributes
		self.abstractType = abstractType

	def get_json(self):
		return {
			'identifier' : self.name,
			'rawType' : self.rawType,
			'abstractType' : self.abstractType,
			'attributes' : self.attributes
		}
			
class VariableTable(object):
	def __init__(self):
		self.table = {}

	def update(self,src_val,dst):
		indices = self.evaluate_indices(dst)
		var_name = indices[0]
		new_value = False
		if len(indices) == 1:
			new_value = var_name in self.table
			self.table[var_name] = src_val
		else:
			var = self.table[var_name]
			loc = indices[1:len(indices) - 1]
			index = indices[len(indices) - 1]
			for i in loc:
				var = var[i]
			var[index] = src_val
		return new_value

	def evaluate_indices(self,expression):
		if not isinstance(expression,tuple):
			raise InvalidExpression(expression)
		return self.evaluate_indices_(expression[0],expression[1:])

	def evaluate_indices_(self,l,r):
		if l == 'var':
			return [r[0]]
		elif l == 'subscript':
			indices = [self.evaluate(ri) for ri in r[1:]]
			return self.evaluate_indices(r[0]) + indices
		else:
			return [self.evaluate((l,r))]

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
# t.table['y'] = 1
# print t.evaluate_indices(('subscript',('var','x'),0,('binop','+',1,1)))
# t.table['a'] = [[1,2,3],[4,5,6]]
# t.table['b'] = [0,1,2]
# print 'eval : ',t.evaluate(('subscript', ('var', 'a'), 0, ('binop', '+', ('subscript', ('var', 'b'), 0), ('subscript', ('var', 'b'), 0))))