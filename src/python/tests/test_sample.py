from context import sample
from sample import run
from os.path import abspath

if __name__ == '__main__':
	#  sample.run.create_settings Parameters
	#	root_directory, files, variables, main_file, output
	# sample.run.Variable Constructor
	# 	name, rawType, attributes=None, abstractType=None
	settings = run.create_settings(
		abspath('./'),
		['main.py'],
		sample.run.Variable('c','array'),
		'main.py',
		abspath('output.json')
	)
	run.run(settings)