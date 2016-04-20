########################################################################
# To be injected into nodes
########################################################################
from json import dumps
outfile = None
annotated_variables = None

####################################################
# Activate the virtualenv vdepend for this to work
####################################################
# import jnius_config
# jnius_config.set_classpath('.','gson.jar','jgroups.jar','../../../build/classes/main')
# from jnius import autoclass

# Wrapper = autoclass('Wrapper')

# w = Wrapper()
####################################################
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
    subscripted = indices[0]
    name = subscripted[0] if isinstance(subscripted,tuple) else subscripted
    indices = list(indices[1:]) if isinstance(subscripted,tuple) else None
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
    # print w.send(dumps({'body' : [statement], 'header' : None}))

def write(src,dst):
    operation = get_operation('write')
    value = get_value(src)
    operation['operationBody'] = {
        'source' : to_json(src), 'target' : to_json(dst), 'value' : [value]
    }
    put(operation)
    return value

def read(statement):
    operation = get_operation('read')
    value = get_value(statement)
    operation['operationBody'] = {
        'source' : to_json(statement), 'value' : [value]
    }
    put(operation)
    return value

# Implement for json
def link(*params):
    def wrap(func):
        def call(*args):
            for param,arg in zip(params,args):
                write(arg,param)
            return func(*tuple(get_value(arg) for arg in args))
        return call
    return wrap
########################################################################