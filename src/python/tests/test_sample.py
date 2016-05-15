from context import sample
from sample import genlog
from os.path import abspath

if __name__ == '__main__':
	# API for create_settings and Variable needed
	# to create LOG.
	#
	# genlog.create_settings Parameters
	#	root_directory, - root directory of program
	#	files, 			- files to observe
	#	variables, 		- variables to observe
	#	main_file, 		- main file of program
	#	output			- where to wite LOG output
	# genlog.Variable Constructor
	# 	name, 			- name of variable
	#	rawType, 		- type of variable
	#	attributes=None, 
	#	abstractType=None
	settings = genlog.create_settings(
		abspath('./test'),
		['/main.py'],
		sample.genlog.Variable('graph','list',attributes={'size' : [3,5]}),
		'/main.py',
		abspath('./test')
	)
	genlog.run(settings)