# This file executes the different modules, resulting in a output.json file
from os import path,system
from create_log import visualize
from ast import parse,Assign,Name,Str
from transformer import WriteTransformer,ReadTransformer,PassTransformer

def load_logwriter(operations,output):
	for node in operations.body:
		if (isinstance(node,Assign) and 
			isinstance(node.targets[0],Name) and 
			node.targets[0].id == 'outfile'):
			node.value = Str(output)
	return operations.body

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

# either write to port or outfile
if __name__ == '__main__':
	# where to output execution of program
	output = path.abspath('output.py')
	open(output,'w').close()

	operations_read = None
	with open('operations.py','r') as f:
		operations_read = f.read()
	operations = load_logwriter(parse(operations_read),output)

	transformers = [PassTransformer('link'), WriteTransformer('write'), ReadTransformer('read')]
	# settings
	# rootdir - where programfiles is located
	# files - what files should be visualized
	# output - where to store executed statements during run-time
	# observe - what variables to observe during execution
	settings = {
		'rootdir' : path.abspath('./test'),
		'v_env' : './testvisualize/',
		'files' : ['main.py'],
		'operations' : operations,
		'transformers' : transformers,
		'observe' : [Variable('a','list',abstractType='adjacencymatrix',attributes={'size' : [3,3]})]}
	# create visulization environment
	visualize(settings)
	# run userprogram in visualization environment
	execfile(path.abspath('./testvisualize/main.py'))
	# right now run cleanup script until a better solution is found
	# system('sh cleanup.sh')