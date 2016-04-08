from expr import Variable
from os import path
from create_log import visualize

if __name__ == '__main__':
	settings = {
		'rootdir' : path.abspath('./test'), 
		'files' : ['main.py'],
		'exec' : 'main.py',
		'watch' : [Variable('a','list',None,None)]}
	visualize(settings)
	# import ast
	
	# f = open('operations.py','r')
	# n = ast.parse(f.read())
	# f.close()

	# from printnode import ast_visit as p
	# for b in n.body:
	# 	p(b)