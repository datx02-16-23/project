########################################################################
# To be injected into nodes
########################################################################
outfile = None
outport = 8000
annotated_variables = None

import socket
import json

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(('localhost', outport))

########################################################################
# Expression Evaluation
########################################################################
def is_variable(expr):
    return isinstance(expr,tuple) and expr[0] == 'var'

def eval_variable(expr):
    if not is_variable(expr):
        raise Exception('%s not a variable!' % str(expr))
    return expr[2]

def get_indices(expr):
    if not isinstance(expr,tuple):
        return ('undefined',expr)
    return get_indices_(expr[0],expr[1:])

def get_indices_(name,value):
    if name == 'subscript':
        var = get_indices(value[0])
        indices = value[1:]
        return (var,) + indices
    elif name == 'var':
        return value

def resolve_subscript(var,indices):
    for i in indices:
        var = var[i]
    return var

def get_value(expr):
    if not isinstance(expr,tuple):
        return expr
    return get_value_(expr[0],expr[1:])

def get_value_(name,value):
    if name == 'var':
        return value[1]
    elif name == 'subscript':
        var = get_value(value[0])
        return resolve_subscript(var,value[1:])

def contains_variable(expr,variable_name):
    if not isinstance(expr,tuple):
        return False
    elif is_variable(expr): 
        return expr[1] == variable_name
    else:
        for node in expr:
            if contains_variable(node,variable_name):
                return True
    return False
########################################################################
# Json
########################################################################
def get_operation(name):
    return {'operation' : name, 'operationBody' : {}}

def to_json(statement):
    indices = get_indices(statement)
    name = None
    if isinstance(indices,tuple):
        name = indices[0] if indices[1] == 'Store' else 'undefined'
    if name:
        indices = indices[1:]
    statement_json = {
        'identifier' : name,
        'index' : indices
    }
    return statement_json
########################################################################
# Log Methods
########################################################################
def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()
    sock.send(json.dumps(statement))

def write(src,dst):
    operation = get_operation('write')
    value = get_value(src)
    print 'write ',src,dst
    print value
    operation['operationBody'] = {
        'source' : to_json(src), 'target' : to_json(dst), 'value' : value
    }
    return value

def read(statement):
    print 'read ',statement
    value = get_value(statement)
    put({'type' : 'read', 'source' :  statement, 'value' : value})
    return value

def link(*params):
	def wrap(func):
		def call(*args):
			for param,arg in zip(params,args):
				put({'type' : 'link', 'param' : param, 'arg' : arg})
			return func(*tuple(arg['value'] for arg in args))
		return call
	return wrap
########################################################################