# This file executes the different modules, resulting in a output.json file
import sys
from os import path,system
from create_log import create_env,format_log
from ast import parse,Assign,Name,Str
from transformer import WriteTransformer,ReadTransformer,PassTransformer
from json import dump
from wrapper import w

currrent_path = path.dirname(path.abspath(__file__))

def load_logwriter(operations,output):
	operations_read = None
	with open(operations,'r') as f:
		operations_read = f.read()
	operations_parse = parse(operations_read)
	for node in operations_parse.body:
		if (isinstance(node,Assign) and 
			isinstance(node.targets[0],Name) and 
			node.targets[0].id == 'outfile'):
			node.value = Str(output)
	return operations_parse.body

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

def create_sources(files):
	sources = {}
	for f in files:
		name = path.basename(f)
		with open(f,'r') as read:
			sources[name] = {"sourceLines" : read.read().split('\n')}
	return sources

def create_header(version,variables,files):
	annotatedVariables = {}
	for variable in variables:
		annotatedVariables[variable.name] = variable.get_json()
	return {
		'version' : version,
		'annotatedVariables' : annotatedVariables,
		'metadata' : {
			'sources' : create_sources(files)
		}
	}

# create_settings
# root_directory - where program is located
# files - what files should be visualized
# variables - output what variables to observe during execution
# main_file - main file to be executed
# output - where to store the final LOG file
def create_settings(root_directory, files, variables, main_file, output):
	settings = {
		'rootdir' : root_directory,
		'files' : files,
		'observe' : variables if isinstance(variables,list) else [variables],
		'main' : main_file,
		'output' : output
	}
	return settings

def run(settings):
	# additional currently non-user settings
	settings['operations'] = load_logwriter(currrent_path+'/operations.py',settings['output'])
	settings['transformers'] = [PassTransformer('link'), WriteTransformer('write'), ReadTransformer('read')]
	settings['v_env'] = '%s/visualize/' % currrent_path
	settings['main'] = settings['v_env'] + settings['main']
	# create visulization environment
	create_env(settings)
	# run userprogram in visualization environment
	files = [settings['rootdir']+'/'+f for f in settings['files']]
	header = create_header(2.0,settings['observe'],files)
	if w is not None:
		w.send(header)
	open(settings['output'],'w').close()
	execfile(settings['main'],globals())

	# create a valid json output file from buffer
	output_buffer = format_log(settings['output'])
	with open(settings['output'],'w+') as f:
		final_output = {
			'header' : header,
			'body' : output_buffer
		}
		dump(final_output,f)

	system('rm -rf %s/visualize' % currrent_path)