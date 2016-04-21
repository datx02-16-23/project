####################################################
# This is used by annotations to stream to a running instance
# of LogStreamManager
#
# Needs the packages in requirements.txt
# either install the packages manually or do the following:
#	* [highly recommended] setup a virtual environment via virtualenv
#	* then run
#		pip install -r requirements.txt
#
# You also need to compile Wrapper.java with the following line
# from the sample directory (crappy solution)
# 	javac -cp ../../../build/classes/main:jars/gson.jar:jars/jgroups.jar:. Wrapper.java 
####################################################
w = None
try:
	import jnius_config
	import system
	print system.path
	jnius_config.set_classpath('.','jars/gson.jar','jars/jgroups.jar','../../../build/classes/main')
	from jnius import autoclass

	Wrapper = autoclass('Wrapper')

	w = Wrapper()
except ImportError as e:
	print "Missing Requirements for using wrapper.py:"
	print "\t%s" % e
	print "Not using LogStreamManager"
####################################################