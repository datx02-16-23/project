from easygui import enterbox,fileopenbox,diropenbox,msgbox,choicebox
from os import path
from context import sample
from sample import annotations

if __name__ == '__main__':
	msgbox("Select Root Directory of program")
	root_directory = diropenbox("Root Directory")
	
	files = []
	while True:
		msgbox("Select File to observe, Cancel if done")
		f = fileopenbox("File")
		if f == '.':
			break
		else:
			files.append(path.basename(f))
	
	variables = []
	while True:
		name = enterbox("Name of Variable to observe")
		if name is None:
			break
		types = ['array']
		type_ = choicebox("Type of Variable", choices=types)
		variables.append(annotations.Variable(name,type_))

	msgbox("Main File of program")
	main_file = path.basename(fileopenbox("Main File"))

	msgbox("Output Directory")
	output = diropenbox("Output")
	output = "%s/output.json" % output

	settings = annotations.create_settings(root_directory,files,variables,main_file,output)
	annotations.run(settings)