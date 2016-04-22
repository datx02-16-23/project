USER GUIDE
* (Recommended) Create a virtual environment via virtualenv, e.g.
	virtualenv vannotations
Next activate your virtual environment, by running the following:
	. vannotations/bin/activate
* Run
	pip install -r requirements.txt
Running pip outside of virtual environment should work as well but is not 
recommended since this will install easygui on machines global python dist.
- If easygui causes errors python-tk might be missing.
  	sudo apt-get install python-tk
  will install python-tk if using Ubuntu

* Have a runnable program ready and the run
	python test_gui.py

* Use output.json with visualization.	