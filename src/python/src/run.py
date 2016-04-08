# This file executes the different modules, resulting in a output.json file
from expr import Variable
from os import path
from create_log import visualize
from tojson import convert

if __name__ == '__main__':
	# where to output execution of program
	output = path.abspath('output.py')
	# settings
	# rootdir - where programfiles is located
	# files - what files should be visualized
	# output - where to store executed statements during run-time
	# watch - what variables to observe during execution
	settings = {
		'rootdir' : path.abspath('./test'), 
		'files' : ['main.py'],
		'output' : output,
		'watch' : [Variable('a','list',None,None)]}
	# create visulization environment
	visualize(settings)
	# run userprogram in visualization environment
	execfile(path.abspath('./testvisualize/main.py'))
	# convert output to json
	convert(output,path.abspath('output.json'),[Variable('a','list',None,None)])