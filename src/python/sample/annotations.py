# This file executes the different modules, resulting in a output.json file
import sys
from os import path,system
from create_log import create_env,format_log
from ast import parse,Assign,Name,Str
from transformer import WriteTransformer,ReadTransformer,PassTransformer
from json import dump

print "================ Python Annotations ================"
print "Loading wrapper.py..."
from wrapper import w
currrent_path = path.dirname(path.abspath(__file__))
print "Appending %s to sys.path" % currrent_path
sys.path.insert(0,currrent_path)

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

def translate(rawType):
	types = {
		'list' : 'array'
	}
	if rawType in types:
		return types[rawType]
	else:
		return rawType
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

def create_settings(root_directory, files, variables, main_file, output):
	"""
	create_settings
		root_directory 	- root directory of program
		files 			- files to be observed
		variables 		- variables to be observed
		main_file 		- main file of program
		output 			- where to store LOG output
	"""
	settings = {
		'rootdir' : root_directory,
		'files' : files,
		'observe' : variables if isinstance(variables,list) else [variables],
		'main' : main_file,
		'output' : output
	}
	return settings

def run(settings):
	"""
	run
		settings - 	takes a settings dictionary,
					can be correctly formatted by calling create_settings
					with valid parameters
	"""
	print "Loading operations.py..."
	settings['operations'] = load_logwriter(currrent_path+'/operations.py',settings['output'])
	
	print "Loading Transformers (parsers)..."
	settings['transformers'] = [PassTransformer('link'), WriteTransformer('write'), ReadTransformer('read')]
	settings['v_env'] = '%s/visualize/' % currrent_path
	settings['main'] = settings['v_env'] + settings['main']

	print "Creating temporary visualization environment & adding to sys.path at:\n%s" % settings['v_env']
	sys.path.insert(0,settings['v_env'])
	create_env(settings)
	
	print "Creating Header..."
	files = [settings['rootdir']+'/'+f for f in settings['files']]
	header = create_header(2.0,settings['observe'],files)
	if w is not None:
		print "Sending header to LogStreamManager"
		w.send(header)
	
	print "Creating/Overwriting output file at:%s" % settings['output']
	open(settings['output'],'w').close()
	
	print "Running main file:\n%s" % settings['main']
	execfile(settings['main'],globals())

	print "Formatting json buffer from output..."
	output_buffer = format_log(settings['output'])
	with open(settings['output'],'w+') as f:
		final_output = {
			'header' : header,
			'body' : output_buffer
		}
		dump(final_output,f)

	print "Removing visualization environment..."
	system('rm -rf %s' % settings['v_env'])

	print "Done! Output stored at:\n%s" % settings['output']