########################################################################
# To be injected into nodes
########################################################################
outfile = None
outport = 8000

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
        return expr
    return get_indices_(expr[0],expr[1:])

def get_indices_(name,value):
    if name == 'subscript':
        variable = get_indices(value[0])
        indices = [get_indices(i) if not is_variable(i) else eval_variable(i) for i in value[1:]]
        return [variable] + indices
    elif name == 'var':
        return value

def get_value(expr):
    if not isinstance(expr,tuple):
        return expr
    elif expr[0] == 'var':
        return expr[2]
    elif expr[0] == 'subscript':
        return get_value(expr[1])
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
    operation['operationBody'] = {
        'source' : to_json(src), 'target' : to_json(dst), 'value' : get_value(src)
    }
    put(operation)
    return get_value(src)

def read(statement):
    put({'type' : 'read', 'source' :  statement, 'value' : get_value(source)})
    return source

def link(*params):
	def wrap(func):
		def call(*args):
			for param,arg in zip(params,args):
				put({'type' : 'link', 'param' : param, 'arg' : arg})
			return func(*tuple(arg['value'] for arg in args))
		return call
	return wrap
########################################################################