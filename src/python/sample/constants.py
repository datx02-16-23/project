"""
author: Johan Gerdin 2016
This file contains constants used by the pylogger module.
Some has a simplified name which should be used with care since they could result
in ambiguity if used irresponsibly. They are however appropriate if they should be
used many times in a file resulting in very long lines of code. For example, BODY is suitable:
operation[BODY] instead of operation[JSON_OPERATION_BODY]
"""

######################################################################
# PYTHON SPECIFICS
######################################################################
from copy import deepcopy
import __builtin__
BUILTINS = dir(__builtin__)
BUILTINS_LOWERCASE = [deepcopy(builtin).lower() for builtin in BUILTINS]
######################################################################
# SETTINGS
######################################################################
SETTINGS_ROOTDIR 	 = 'rootdir'
SETTINGS_FILES 		 = 'files'
SETTINGS_VARIABLES 	 = 'observe'
SETTINGS_MAIN 		 = 'main'
SETTINGS_OUTPUT 	 = 'output'
SETTINGS_OUTPUT_PATH = "/output.json"

SETTINGS_PYLOGGERENV  	= 'v_env'
SETTINGS_TRANSFORMERS 	= 'transformers'
SETTINGS_OPERATIONS 	= 'operations'
#################################
# NODE
#################################
NODE_PATH = 'path'
NODE_AST  = 'parse'
######################################################################
# OPERATIONS
######################################################################
OPERATIONS_OUTFILE = 'outfile'
OPERATIONS_ANNOTATEDVARIABLES = 'annotated_variables'
OPERATIONS_PATH = '/operations.py'
######################################################################
# EXPR
######################################################################
EXPR_UNDEFINED 	= 'undefined'
EXPR_VAR 		= 'var'
EXPR_SUBSCRIPT 	= 'subscript'
EXPR_STORE      = 'Store'
######################################################################
# TRANSFORMERS
######################################################################
TRANSFORMER_NAME_PASS  = 'link'
TRANSFORMER_NAME_WRITE = 'write'
TRANSFORMER_NAME_READ  = 'read'
from ast import Subscript,Name
TRANSFORMER_SUBSCRIPT = Subscript.__name__
TRANSFORMER_NAME = Name.__name__
######################################################################
# JSON
######################################################################
#################################
# JSON_BODY
#################################
JSON_BODY = 'body'
#################################
# JSON_OPERATION
#################################
JSON_OPERATION_TYPE   = 'operation'
JSON_OPERATION_TYPE_WRITE  = WRITE  = 'write'
JSON_OPERATION_TYPE_READ   = READ   = 'read'
JSON_OPERATION_TYPE_PASS   = PASS   = 'pass'
JSON_OPERATION_BODY   	   = BODY   = 'operationBody'
JSON_OPERATION_BODY_SOURCE = SOURCE = 'source'
JSON_OPERATION_BODY_TARGET = TARGET = 'target'
JSON_OPERATION_BODY_INDEX  = INDEX  = 'index'
JSON_OPERATION_BODY_VALUE  = 'value'
JSON_OPERATION_BEGINLINE = 'beginLine'
JSON_OPERATION_ENDLINE   = 'endLine'
#################################
# JSON_VARIABLE
#################################
JSON_VARIABLE_IDENTIFIER 		= IDENTIFIER 	= 'identifier'
JSON_VARIABLE_RAWTYPE    		= RAWTYPE 		= 'rawType'
JSON_VARIABLE_RAWTYPE_DEFAULT 	= RAWTYPE_DEFAULT = 'independentElement'
JSON_VARIABLE_ABSTRACTTYPE 		= ABSTRACTTYPE 	= 'abstractType'
JSON_VARIABLE_ATTRIBUTES  		= ATTRIBUTES 	='attributes'
#################################
# JSON_HEADER
#################################
JSON_HEADER = 'header'

JSON_HEADER_VERSION = 'version'
JSON_HEADER_ANNOTATEDVARIABLES = 'annotatedVariables'
JSON_HEADER_SOURCES = 'sources'