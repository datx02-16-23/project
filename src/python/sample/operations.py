########################################################################
# To be injected into nodes
########################################################################
from inspect import getfile,currentframe
from os.path import basename
from json import dumps
from wrapper import w
outfile = None
annotated_variables = None

########################################################################
# Expression Evaluation
########################################################################
def is_variable(expr):
    return isinstance(expr,tuple) and expr[0] == 'var'

def get_variable(expr):
    if not isinstance(expr,tuple):
        return 'undefined'
    elif is_variable(expr):
        return expr[1]
    else:
        for exp in expr:
            exp = get_variable(exp)
            if exp != 'undefined': return exp

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
def get_operation(name,value,begin_line,end_line):
    return {
        'operation' : name, 
        'source' : basename(getfile(currentframe())),
        'operationBody' : {'value' : value},
        'beginLine' : begin_line - 1,
        'endLine' : end_line - 1
    }

def to_json(statement):
    indices = get_indices(statement)
    subscripted = indices[0]
    name = subscripted[0] if isinstance(subscripted,tuple) else subscripted
    indices = list(indices[1:]) if isinstance(subscripted,tuple) else None
    statement_json = {
        'identifier' : name
    }
    if indices:
        statement_json['index'] = indices
    return statement_json
########################################################################
# Log Methods
########################################################################
def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()
    if w is not None:
        print w.send(dumps({'body' : [statement], 'header' : None}))

def annotated_in(expr):
    return get_variable(expr) in annotated_variables

def write(src,dst,begin_line,end_line):
    value = get_value(src)
    if annotated_in(src) or annotated_in(dst):
        operation = get_operation('write',value,begin_line,end_line)
        operation['operationBody']['source'] = to_json(src)
        operation['operationBody']['target'] = to_json(dst)
        put(operation)
    return value

def read(statement,begin_line,end_line):
    value = get_value(statement)
    if annotated_in(statement):
        operation = get_operation('read',value,begin_line,end_line)
        operation['operationBody']['source'] = to_json(statement)
        put(operation)
    return value

# Implement for json
def link(*params):
    def wrap(func):
        def call(*args):
            for param,arg in zip(params,args):
                if get_variable(arg) in annotated_variables:
                    annotated_variables.append(get_variable(param))
                write(arg,param,0,0)
            return func(*tuple(get_value(arg) for arg in args))
        return call
    return wrap
########################################################################