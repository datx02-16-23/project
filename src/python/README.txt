USER GUIDE
* (Recommended) Create a virtual environment via virtualenv 
Next activate your virtual environment:
	. [path to environment]/bin/activate
* Run
	pip install -r requirements.txt
Running pip outside of virtual environment should work as well but is not 
recommended since this will install easygui on machines global python dist.
- If easygui causes errors python-tk might be missing.

* Have a runnable program ready and the run tests/test_gui.py to specify settings.

* Use output.json with visualization.