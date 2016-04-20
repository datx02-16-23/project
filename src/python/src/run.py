# This file executes the different modules, resulting in a output.json file
from os import path,system
from create_log import create_env,format_log
from ast import parse,Assign,Name,Str
from transformer import WriteTransformer,ReadTransformer,PassTransformer
from json import dump
####################################################
# Activate the virtualenv vdepend for this to work
####################################################
# import jnius_config
# jnius_config.set_classpath('.','gson.jar','jgroups.jar','../../../build/classes/main')
# from jnius import autoclass

# Wrapper = autoclass('Wrapper')

# w = Wrapper()
####################################################
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
		self.abstractType = abstractType if abstractType is not None else rawType

	def get_json(self):
		return {
			'identifier' : self.name,
			'rawType' : self.rawType,
			'abstractType' : self.abstractType,
			'attributes' : self.attributes
		}

def create_header(version,variables):
	annotatedVariables = {}
	for variable in variables:
		annotatedVariables[variable.name] = variable.get_json()
	return {
		'version' : version,
		'annotatedVariables' : annotatedVariables
	}

if __name__ == '__main__':
	# where to output json
	output = path.abspath('output.json')
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
		'observe' : [Variable('a','array',abstractType='array',attributes={'size' : [3]})]}

	# create visulization environment
	create_env(settings)

	# run userprogram in visualization environment
	# w.send(create_header(2.0,settings['observe']))
	execfile(path.abspath('./testvisualize/main.py'))

	# create a valid json output file from buffer
	output_buffer = format_log(output)
	with open(output,'w+') as f:
		final_output = {
			'header' : create_header(2.0,settings['observe']),
			'body' : output_buffer
		}
		dump(final_output,f)
	# right now run cleanup script until a better solution is found
	# system('sh cleanup.sh')