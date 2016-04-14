########################################################################
# To be injected into nodes
########################################################################
outfile = None
outport = 8000

import socket
import json

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(('localhost', outport))

def get_operation(name):
    return {'operation' : name, 'operationBody' : {}}

def to_json(statement):
    operation = get_operation(statement['type'])
    return operation

def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()
    statement_json = to_json(statement)
    sock.send(json.dumps(statement_json))

def write(src,dst,source):
    put({'type' : 'write', 'src' : src, 'dst' : dst, 'src_val' : source })
    return source

def read(statement,source):
    put({'type' : 'read', 'statement' :  statement, 'value' : source})
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