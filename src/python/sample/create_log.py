from codegen import to_source
from ast import NodeTransformer,parse,Assign,Name,Str
from printnode import ast_visit as printnode
from distutils.dir_util import copy_tree
from os.path import abspath
from os import remove

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

# given settings variable should be sanity-checked
def create_env(settings):
	# Setup rootdir of visualization folder given source root directory
	copy_tree(settings['rootdir'],settings['v_env'])

	# Generate ast's from given files copied into visualization folder
	nodes = []
	for f in settings['files']:
		path = settings['v_env']+f
		fRead = open(path, "r")
		nodes.append( {'path' : path, 'parse' : parse(fRead.read())} )
		fRead.close()

	# Replace lines in files to be visualized with
	# function calls from operations.py
	mt = MainTransformer(settings['transformers'],settings['operations'])
	for node in nodes:
		f = open(node['path'],'w')
		mt.visit(node['parse'])
		f.write(to_source(node['parse']))
		f.close()

def format_log(output_buffer):
	# encapsulate the output in a list
	output = "output = [%s]" % output_buffer[:len(output_buffer)-1] # remove trailing ","
	# Should use native temporary module
	tmp = open('tmp.py','w')
	tmp.write(output)
	tmp.close()
	output = __import__('tmp').output
	remove('tmp.py')
	return output

def alias(operation,aliases):
	if isinstance(operation['operationBody'],dict):
		for k,statement in operation['operationBody'].iteritems():
			if isinstance(statement,dict) and 'identifier' in statement:
				name = statement['identifier']
				statement['identifier'] = aliases[name] if name in aliases else name

def is_init(operation):
	return ('index' not in operation['operationBody']['target'] and
			'index' not in operation['operationBody']['source'])

class LogPostProcessor(object):
	def __init__(self,output_file):
		output_read = open(output_file,'r')
		output_buffer = output_read.read()
		output_read.close()
		self.output = format_log(output_buffer)

	def process(self,variables):
		self.names = [variable.name for variable in variables]
		self.fix_occurrences()
		return self.output

	def fix_occurrences(self):
		aliases = {}
		for operation in self.output:
			if (operation['operation'] == 'write' and is_init(operation)):
				target = operation['operationBody']['target']['identifier']
				if (operation['operationBody']['source']['identifier'] in self.names and
					target not in aliases and 
					target not in self.names):
					aliases[target] = operation['operationBody']['source']['identifier']
					self.names.append(target)
			alias(operation,aliases)