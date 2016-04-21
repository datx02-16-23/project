from context import sample
from sample import annotations
from os.path import abspath

if __name__ == '__main__':
	# API for create_settings and Variable needed
	# to create LOG.
	#
	# annotations.create_settings Parameters
	#	root_directory, - root directory of program
	#	files, 			- files to observe
	#	variables, 		- variables to observe
	#	main_file, 		- main file of program
	#	output			- where to wite LOG output
	# annotations.Variable Constructor
	# 	name, 			- name of variable
	#	rawType, 		- type of variable
	#	attributes=None, 
	#	abstractType=None
	settings = annotations.create_settings(
		abspath('./test'),
		['main.py','subf/depend.py'],
		sample.annotations.Variable('c','list'),
		'main.py',
		abspath('output.json')
	)
	annotations.run(settings)