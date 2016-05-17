from context import sample
from sample import pylogger
from os.path import abspath

if __name__ == '__main__':
	# API for create_settings and Variable needed
	# to create PEL.
	#
	# pylogger.create_settings Parameters
	#	root_directory, - root directory of program
	#	files, 			- files to observe
	#	variables, 		- variables to observe
	#	main_file, 		- main file of program
	#	output			- where to wite PEL output
	# pylogger.Variable Constructor
	# 	name, 			- name of variable
	#	rawType, 		- type of variable
	#	attributes=None, 
	#	abstractType=None
	settings = pylogger.create_settings(
		abspath('./test'),
		['/main.py'],
		[sample.pylogger.Variable('vec','list')],
		'/main.py',
		abspath('./test')
	)
	pylogger.run(settings)