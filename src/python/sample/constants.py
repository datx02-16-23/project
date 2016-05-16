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

SETTINGS_GENLOGENV  	= 'v_env'
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
######################################################################
# TRANSFORMERS
######################################################################
TRANSFORMER_NAME_PASS  = 'link'
TRANSFORMER_NAME_WRITE = 'write'
TRANSFORMER_NAME_READ  = 'read'
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
JSON_OPERATION_TYPE_WRITE = 'write'
JSON_OPERATION_TYPE_READ  = 'read'
JSON_OPERATION_BODY   = 'operationBody'
JSON_OPERATION_SOURCE = 'source'
JSON_OPERATION_TARGET = 'target'
JSON_OPERATION_VALUE  = 'value'
JSON_OPERATION_INDEX  = 'index'
JSON_OPERATION_BEGINLINE = 'beginLine'
JSON_OPERATION_ENDLINE   = 'endLine'
#################################
# JSON_VARIABLE
#################################
JSON_VARIABLE_IDENTIFIER 	= 'identifier'
JSON_VARIABLE_RAWTYPE    	= 'rawType'
JSON_VARIABLE_RAWTYPE_INDEPENDENTVAR = 'independentElement'
JSON_VARIABLE_ABSTRACTTYPE 	= 'abstractType'
JSON_VARIABLE_ATTRIBUTES  	= 'attributes'
#################################
# JSON_HEADER
#################################
JSON_HEADER = 'header'

JSON_HEADER_VERSION = 'version'
JSON_HEADER_ANNOTATEDVARIABLES = 'annotatedVariables'
JSON_HEADER_SOURCES = 'sources'