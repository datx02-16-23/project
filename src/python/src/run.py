from expr import Variable
from os import path
from create_log import visualize

if __name__ == '__main__':
	output = path.abspath('output.py')
	settings = {
		'rootdir' : path.abspath('./test'), 
		'files' : ['main.py'],
		'output' : output,
		'watch' : [Variable('a','list',None,None)]}
	visualize(settings)
	# execfile(path.abspath('/vi'))
	# ToJson().convert(output,path.abspath('output.json'))
	# import ast
	
	# f = open('operations.py','r')
	# n = ast.parse(f.read())
	# f.close()

	# from printnode import ast_visit as p
	# for b in n.body:
	# 	p(b)