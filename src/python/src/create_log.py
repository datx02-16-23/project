from codegen import to_source
from transformer import WriteTransformer,ReadTransformer,PassTransformer
from ast import NodeTransformer,parse,Assign,Name,Str
from printnode import ast_visit as printnode
from distutils.dir_util import copy_tree
from os.path import abspath
from os import remove

class MainTransformer(NodeTransformer):
	def __init__(self,transformers,logwriter_nodes):
		self.transformers = transformers
		self.logwriter_nodes = logwriter_nodes

	def visit_Module(self,node):
		for tr in self.transformers:
			tr.visit(node)

		self.logwriter_nodes.reverse()
		for n in self.logwriter_nodes:
			node.body.insert(0,n)

		return node

def setup_env(settings,v_env):
	copy_tree(settings['rootdir'],v_env)
	# This only supports flat layout of files
	with open(v_env+'__init__.py','wb') as f:
		f.write('')
		f.close()

def transform(node,logwriter_nodes):
	transformers = [PassTransformer(), WriteTransformer(), ReadTransformer()]
	MainTransformer(transformers,logwriter_nodes).visit(node)

# Move this out of create_log? Perhaps into run.py
def load_logwriter(logwriter,output):
	with open(logwriter,'r') as f:
		n = parse(f.read())
		f.close()
		for node in n.body:
			if (isinstance(node,Assign) and 
				isinstance(node.targets[0],Name) and 
				node.targets[0].id == 'outfile'):
				node.value = Str(output)
		return n.body

# Maybe convert this into a "main" function
# Also rethink the execution of this function
# - v_env+... must be fail-safe
# execfile must be fail-safe
# given settings variable should be sanity-checked
def visualize(settings):
	# Setup rootdir of visualization folder given source root directory
	v_env = '%svisualize/' % settings['rootdir']
	setup_env(settings,v_env)

	# Generate ast's from given files copied into visualization folder
	nodes = []
	for f in settings['files']:
		path = v_env+f
		fRead = open(path, "r")
		nodes.append( {'path' : path, 'parse' : parse(fRead.read())} )
		fRead.close()

	# Replace lines in files to be visualized with
	# function calls from operations.py
	open(settings['output'],'w').close()
	for node in nodes:
		f = open(node['path'],'wb')
		transform(node['parse'],load_logwriter(abspath('operations.py'),abspath(settings['output'])))
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