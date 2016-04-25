# This file executes the different modules, resulting in a output.json file
import sys
from os import path,system
from create_log import create_env,LogPostProcessor
from ast import parse,Assign,List,Name,Str,NodeVisitor
from transformer import WriteTransformer,ReadTransformer,PassTransformer
from json import dump

print "================ Python Annotations ================"
print "Loading wrapper.py..."
from wrapper import w
currrent_path = path.dirname(path.abspath(__file__))
print "Appending %s to sys.path" % currrent_path
sys.path.insert(0,currrent_path)

class OperationsLoader(NodeVisitor):
	""" Goes through operations.py file and inserts 
	values into assignments, setting neccessary variables """

	outfile = 'outfile'
	annotated_variables = 'annotated_variables'

	def __init__(self,operations_path):
		self.operations = None
		with open(operations_path,'r') as f:
			self.operations = parse(f.read())

	def load(self,output_path,variables):
		self.settings = {
			OperationsLoader.outfile : Str(output_path),
			OperationsLoader.annotated_variables : List(elts=
				[Str(variable.name) for variable in variables]
			)
		}
		self.visit(self.operations)
		return self.operations

	def visit_Assign(self,node):
		target = node.targets[0]
		if isinstance(target,Name):
			if target.id == OperationsLoader.outfile:
				node.value = self.settings[OperationsLoader.outfile]
			elif target.id == OperationsLoader.annotated_variables:
				node.value = self.settings[OperationsLoader.annotated_variables]
	
	def visit_FunctionDef(self,node): return

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
		self.rawType = translate(rawType)
		self.attributes = attributes
		self.abstractType = abstractType if abstractType is not None else self.rawType

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
			sources[name] = read.read().split('\n')
	return sources

def create_header(version,variables,files):
	annotatedVariables = {}
	for variable in variables:
		annotatedVariables[variable.name] = variable.get_json()
	return {
		'version' : version,
		'annotatedVariables' : annotatedVariables,
		'sources' : create_sources(files)
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
		'output' : output + "/output.json"
	}
	return settings

def dump_json(header,body,output_path):
	with open(output_path,'w') as f:
		final_output = {
			'header' : header,
			'body' : body
		}
		dump(final_output,f)

def run(settings):
	"""
	run
		settings - 	takes a settings dictionary,
					can be correctly formatted by calling create_settings
					with valid parameters
	"""
	print "Loading operations.py..."
	ol = OperationsLoader(currrent_path+'/operations.py')
	settings['operations'] = ol.load(settings['output'],settings['observe'])
	
	print "Loading Transformers (parsers)..."
	settings['transformers'] = [PassTransformer('link'), WriteTransformer('write'), ReadTransformer('read')]
	settings['v_env'] = '%s/visualize/' % currrent_path
	settings['main'] = settings['v_env'] + path.basename(settings['main'])

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
	processor = LogPostProcessor(settings['output']) 
	output 	  = processor.process(settings['observe'])
	
	dump_json(header,output,settings['output'])
	# with open(settings['output'],'w+') as f:
	# 	final_output = {
	# 		'header' : header,
	# 		'body' : output
	# 	}
	# 	dump(final_output,f)

	print "Removing visualization environment..."
	system('rm -rf %s' % settings['v_env'])

	print "Done! Output stored at:\n%s" % settings['output']