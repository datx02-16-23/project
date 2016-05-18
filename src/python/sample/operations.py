########################################################################
# To be injected into nodes
########################################################################
from constants import *
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
    return isinstance(expr,tuple) and expr[0] == EXPR_VAR

def get_variable(expr):
    if not isinstance(expr,tuple):
        return EXPR_UNDEFINED
    elif is_variable(expr):
        return expr[1]
    else:
        for exp in expr:
            exp = get_variable(exp)
            if exp != EXPR_UNDEFINED: return exp

def get_indices(expr):
    if not isinstance(expr,tuple):
        return (EXPR_UNDEFINED,expr)
    return get_indices_(expr[0],expr[1:])

def get_indices_(name,value):
    if name == EXPR_SUBSCRIPT:
        var = get_indices(value[0])
        indices = value[1:]
        return (var,) + indices
    elif name == EXPR_VAR:
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
    if name == EXPR_VAR:
        return value[1]
    elif name == EXPR_SUBSCRIPT:
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
        JSON_OPERATION_TYPE : name, 
        JSON_OPERATION_BODY_SOURCE : basename(getfile(currentframe())),
        JSON_OPERATION_BODY : {JSON_OPERATION_BODY_VALUE : value},
        JSON_OPERATION_BEGINLINE : begin_line - 1,
        JSON_OPERATION_ENDLINE : end_line - 1
    }

def to_json(statement):
    indices = get_indices(statement)
    subscripted = indices[0]
    name = subscripted[0] if isinstance(subscripted,tuple) else subscripted
    indices = list(indices[1:]) if isinstance(subscripted,tuple) else None
    statement_json = {
        JSON_VARIABLE_IDENTIFIER : name
    }
    if indices:
        statement_json[JSON_OPERATION_BODY_INDEX] = indices
    return statement_json
########################################################################
# Log Methods
########################################################################
def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()
    if w is not None:
        print w.send(dumps({JSON_BODY : [statement], JSON_HEADER : None}))

def annotated_in(expr):
    return get_variable(expr) in annotated_variables

def write(src,dst,begin_line,end_line):
    value = get_value(src)
    if annotated_in(src) or annotated_in(dst):
        operation = get_operation(JSON_OPERATION_TYPE_WRITE,value,begin_line,end_line)
        source = to_json(src)
        if source[JSON_VARIABLE_IDENTIFIER] != EXPR_UNDEFINED:
            operation[JSON_OPERATION_BODY][JSON_OPERATION_BODY_SOURCE] = source
        operation[JSON_OPERATION_BODY][JSON_OPERATION_BODY_TARGET] = to_json(dst)
        put(operation)
    return value

def read(statement,begin_line,end_line):
    value = get_value(statement)
    if annotated_in(statement):
        operation = get_operation(JSON_OPERATION_TYPE_READ,value,begin_line,end_line)
        operation[JSON_OPERATION_BODY][JSON_OPERATION_BODY_SOURCE] = to_json(statement)
        put(operation)
    return value

def link(*params):
    def wrap(func):
        def call(*args):
            for param,arg in zip(params,args):
                v_arg = get_variable(arg)
                v_param = get_variable(param)
                if v_param != v_arg:
                    if v_arg in annotated_variables and v_param not in annotated_variables:
                        annotated_variables.append(v_param)
                    linenos = params[-2:]
                    operation = get_operation(JSON_OPERATION_TYPE_PASS,get_value(arg),*linenos)
                    operation[BODY][SOURCE] = to_json(arg)
                    operation[BODY][TARGET] = to_json(param)
                    put(operation)
            return func(*tuple(get_value(arg) for arg in args))
        return call
    return wrap
########################################################################