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

		# the nodes will be inserted in backwards order since inserted at index 0
		# therefore reverse
		self.operations.reverse()
		for n in self.operations:
			node.body.insert(0,n)

		return node

def setup_env(rootdir,v_env):
	copy_tree(rootdir,v_env)
	# This only supports flat layout of files?
	with open(v_env+'__init__.py','w') as f:
		f.write('')
		f.close()

# given settings variable should be sanity-checked
def create_env(settings):
	# Setup rootdir of visualization folder given source root directory
	setup_env(settings['rootdir'],settings['v_env'])

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

def format_log(file_path):
	output = None
	with open(file_path,'r') as f:
		output = f.read()
		# encapsulate the output in a list
		output = "output = [%s]" % output[:len(output)-1] # remove trailing ","
		# Feels like a hacky way of formatting
		tmp = open('tmp.py','w')
		tmp.write(output)
		tmp.close()
		output = __import__('tmp').output
		remove('tmp.py')
		f.close()
	return output