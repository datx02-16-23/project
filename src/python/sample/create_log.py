from codegen import to_source
from ast import NodeTransformer,parse,Assign,Name,Str
from printnode import ast_visit as printnode
from distutils.dir_util import copy_tree
from os.path import abspath
from os import remove
from constants import *

class MainTransformer(NodeTransformer):
	def __init__(self,transformers,operations):
		self.transformers = transformers
		self.operations = operations

	def visit_Module(self,node):
		for tr in self.transformers:
			tr.visit(node)

		# the nodes will be inserted in backwards order 
		# since inserted at index 0 therefore reverse list
		operations_body = self.operations.body
		operations_body.reverse()
		for n in operations_body:
			node.body.insert(0,n)

		return node

# Generate ast's from given files copied into visualization folder
def generate_nodes(files,v_env):
	nodes = []
	for f in files:
		path = v_env + f
		with open(path,'r') as read:
			node = parse(read.read())
			nodes.append({NODE_PATH : path, NODE_AST : node})
	return nodes

# Replace statements in files to be visualized with
# function calls to methods supplied by operations.py
def transform(nodes,main_transformer):
	for node in nodes:
		with open(node[NODE_PATH],'w') as node_source:
			main_transformer.visit(node[NODE_AST])
			node_source.write(to_source(node[NODE_AST]))

# given settings variable should be sanity-checked
def create_env(settings):
	# Setup rootdir of visualization folder given source root directory
	copy_tree(settings[SETTINGS_ROOTDIR],settings[SETTINGS_GENLOGENV])
	nodes = generate_nodes(settings[SETTINGS_FILES],settings[SETTINGS_GENLOGENV])
	mt = MainTransformer(settings[SETTINGS_TRANSFORMERS],settings[SETTINGS_OPERATIONS])
	transform(nodes,mt)

def format_log(output_buffer):
	# encapsulate the output in a list
	output = "output = [%s]" % output_buffer[:len(output_buffer)-1] # remove trailing ","
	# Should use native temporary module?
	tmp = open('tmp.py','w')
	tmp.write(output)
	tmp.close()
	output = __import__('tmp').output
	remove('tmp.py')
	return output

def alias(operation,aliases):
	if isinstance(operation[JSON_OPERATION_BODY],dict):
		for k,statement in operation[JSON_OPERATION_BODY].iteritems():
			if isinstance(statement,dict) and JSON_VARIABLE_IDENTIFIER in statement:
				name = statement[JSON_VARIABLE_IDENTIFIER]
				statement[JSON_VARIABLE_IDENTIFIER] = aliases[name] if name in aliases else name

def is_init(operation):
	if JSON_OPERATION_SOURCE in operation[JSON_OPERATION_BODY]:
		return (JSON_OPERATION_INDEX not in operation[JSON_OPERATION_BODY][JSON_OPERATION_TARGET] and
				JSON_OPERATION_INDEX not in operation[JSON_OPERATION_BODY][JSON_OPERATION_SOURCE])
	else:
		return JSON_OPERATION_INDEX not in operation[JSON_OPERATION_BODY][JSON_OPERATION_TARGET]

class LogPostProcessor(object):
	def __init__(self,output_file):
		with open(output_file,'r') as output_read:
			output_buffer = output_read.read()
			self.output = format_log(output_buffer)
		# output_read = open(output_file,'r')
		# output_buffer = output_read.read()
		# output_read.close()
		# self.output = format_log(output_buffer)

	def process(self,variables):
		self.names = [variable.name for variable in variables]
		self._fix_occurrences()
		return self.output

	def _fix_occurrences(self):
		aliases = {}
		for operation in self.output:
			if (operation[JSON_OPERATION_TYPE] == JSON_OPERATION_TYPE_WRITE and is_init(operation)):
				target = operation[JSON_OPERATION_BODY][JSON_OPERATION_TARGET][JSON_VARIABLE_IDENTIFIER]
				if (JSON_OPERATION_SOURCE in operation[JSON_OPERATION_BODY] and
					operation[JSON_OPERATION_BODY][JSON_OPERATION_SOURCE][JSON_VARIABLE_IDENTIFIER] in self.names and
					# target not in aliases and 
					target not in self.names):
					aliases[target] = operation[JSON_OPERATION_BODY][JSON_OPERATION_SOURCE][JSON_VARIABLE_IDENTIFIER]
					# self.names.append(target)
			alias(operation,aliases)