# This file executes the different modules, resulting in a output.json file
import sys
from os import path,system
from create_log import create_env,LogPostProcessor
from ast import parse,Assign,List,Name,Str,NodeVisitor
from transformer import WriteTransformer,ReadTransformer,PassTransformer
from json import dump
from constants import *

print "================ Python Logger ================"
print "Loading wrapper.py..."
from wrapper import w
currrent_path = path.dirname(path.abspath(__file__))
print "Appending %s to sys.path" % currrent_path
sys.path.insert(0,currrent_path)

class OperationsLoader(NodeVisitor):
	""" Goes through operations.py file and inserts 
	values into assignments, setting neccessary variables """

	def __init__(self,operations_path):
		self.operations = None
		with open(operations_path,'r') as f:
			self.operations = parse(f.read())

	def load(self,output_path,variables):
		self.settings = {
			OPERATIONS_OUTFILE : Str(output_path),
			OPERATIONS_ANNOTATEDVARIABLES : List(elts=
				[Str(variable.name) for variable in variables]
			)
		}
		self.visit(self.operations)
		return self.operations

	def visit_Assign(self,node):
		target = node.targets[0]
		if isinstance(target,Name):
			if target.id == OPERATIONS_OUTFILE:
				node.value = self.settings[OPERATIONS_OUTFILE]
			elif target.id == OPERATIONS_ANNOTATEDVARIABLES:
				node.value = self.settings[OPERATIONS_ANNOTATEDVARIABLES]
	
	def visit_FunctionDef(self,node): return

def translate(rawType):
	types = {
		'list' : 'array',
		'array' : 'array'
	}
	if rawType in types:
		return types[rawType]
	else:
		return JSON_VARIABLE_RAWTYPE_DEFAULT

class Variable(object):
	def __init__(self,name,rawType,attributes=None,abstractType=None):
		self.name = name
		self.rawType = translate(rawType)
		self.attributes = attributes
		self.abstractType = abstractType if abstractType is not None else self.rawType

	def get_json(self):
		return {
			JSON_VARIABLE_IDENTIFIER : self.name,
			JSON_VARIABLE_RAWTYPE : self.rawType,
			JSON_VARIABLE_ABSTRACTTYPE : self.abstractType,
			JSON_VARIABLE_ATTRIBUTES : self.attributes
		}

	def __repr__(self):
		return "(Name = %s, Type = %s)" % (self.name,self.rawType)

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
		JSON_HEADER_VERSION : version,
		JSON_HEADER_ANNOTATEDVARIABLES : annotatedVariables,
		JSON_HEADER_SOURCES : create_sources(files)
	}

def format_path(path):
	return path if not path.startswith('/') else path[1:]

def format_variables(variables):
	variables_ = []
	if not isinstance(variables,list):
		variables = [variables]
	for variable in variables:
		if isinstance(variable,dict):
			variables_.append(Variable(**variable))
		elif isinstance(variable,Variable):
			variables_.append(variable)
	return variables_

def create_settings(root_directory, files, variables, main_file, output):
	"""
	create_settings
		root_directory 	- root directory of program		"path/to/root"
		files 			- files to be observed			[files,...]
		variables 		- variables to be observed		[Variable,...]
		main_file 		- main file of program			"relative/path/from/root/to/main"
		output 			- where to store PEL output 	"path/to/store/output"
	"""
	files = [format_path(f) for f in files]
	variables = format_variables(variables)
	settings = {
		SETTINGS_ROOTDIR : root_directory if root_directory.endswith('/') else root_directory+'/',
		SETTINGS_FILES : files,
		SETTINGS_VARIABLES : variables,
		SETTINGS_MAIN : format_path(main_file),
		SETTINGS_OUTPUT : output + SETTINGS_OUTPUT_PATH
	}
	return settings

def dump_json(header,body,output_path):
	with open(output_path,'w') as f:
		final_output = {
			JSON_HEADER : header,
			JSON_BODY : body
		}
		dump(final_output,f)

def run(settings):
	"""
	run
		settings - 	takes a settings dictionary,
					can be correctly formatted by calling create_settings
					with valid parameters
	"""
	print settings
	print "Loading operations.py..."
	ol = OperationsLoader(currrent_path+OPERATIONS_PATH)
	settings[SETTINGS_OPERATIONS] = ol.load(settings[SETTINGS_OUTPUT],settings[SETTINGS_VARIABLES])
	
	print "Loading Transformers (parsers)..."
	settings[SETTINGS_TRANSFORMERS] = [PassTransformer(TRANSFORMER_NAME_PASS), WriteTransformer(TRANSFORMER_NAME_WRITE), ReadTransformer(TRANSFORMER_NAME_READ)]
	settings[SETTINGS_PYLOGGERENV] = '%s/visualize/' % currrent_path
	settings[SETTINGS_MAIN] = settings[SETTINGS_PYLOGGERENV] + path.basename(settings[SETTINGS_MAIN])
	
	if settings[SETTINGS_PYLOGGERENV] not in sys.path:
		print "Creating temporary visualization environment & adding to sys.path at:\n%s" % settings[SETTINGS_PYLOGGERENV]
		sys.path.insert(0,settings[SETTINGS_PYLOGGERENV])
	create_env(settings)
	
	print "Creating Header..."
	files = [settings[SETTINGS_ROOTDIR]+f for f in settings[SETTINGS_FILES]]
	header = create_header(2.0,settings[SETTINGS_VARIABLES],files)
	if w is not None:
		print "Sending header to LogStreamManager"
		w.send(header)
	
	print "Creating/Overwriting output file at:%s" % settings[SETTINGS_OUTPUT]
	open(settings[SETTINGS_OUTPUT],'w').close()
	
	print "Running main file:\n%s" % settings[SETTINGS_MAIN]
	execfile(settings[SETTINGS_MAIN],globals())
	
	print "Removing %s from sys.path" % settings[SETTINGS_PYLOGGERENV]
	sys.path.remove(settings[SETTINGS_PYLOGGERENV])

	print "Formatting json buffer from output..."
	processor = LogPostProcessor(settings[SETTINGS_OUTPUT]) 
	output 	  = processor.process(settings[SETTINGS_VARIABLES])
	dump_json(header,output,settings[SETTINGS_OUTPUT])

	print "Removing visualization environment..."
	system('rm -rf %s' % settings[SETTINGS_PYLOGGERENV])

	print "Done! Output stored at:\n%s" % settings[SETTINGS_OUTPUT]