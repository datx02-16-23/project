from context import sample
from sample import interceptor
from os.path import abspath

if __name__ == '__main__':
	# API for create_settings and Variable needed
	# to create LOG.
	#
	# interceptor.create_settings Parameters
	#	root_directory, - root directory of program
	#	files, 			- files to observe
	#	variables, 		- variables to observe
	#	main_file, 		- main file of program
	#	output			- where to wite LOG output
	# interceptor.Variable Constructor
	# 	name, 			- name of variable
	#	rawType, 		- type of variable
	#	attributes=None, 
	#	abstractType=None
	settings = interceptor.create_settings(
		abspath('./test'),
		['/main.py'],
		sample.interceptor.Variable('graph','list',attributes={'size' : [3,5]}),
		'/main.py',
		abspath('./test')
	)
	interceptor.run(settings)