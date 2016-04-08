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