from context import sample
from sample import annotations
from os.path import abspath

if __name__ == '__main__':
	#  sample.annotations.create_settings Parameters
	#	root_directory, files, variables, main_file, output
	# sample.annotations.Variable Constructor
	# 	name, rawType, attributes=None, abstractType=None
	settings = annotations.create_settings(
		abspath('./'),
		['main.py'],
		sample.annotations.Variable('c','array'),
		'main.py',
		abspath('output.json')
	)
	annotations.run(settings)