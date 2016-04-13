outfile = '/home/johan/Dropbox/It/Kandidat Datx02/project/src/python/src/output.py'
import socket
import json
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(('localhost', 8000))

def to_json(statement):
    return {'test': None}

def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()
    statement_json = to_json(statement)
    sock.send(json.dumps(statement_json))

def write(src, dst, source):
    put({'type': 'write', 'src': src, 'dst': dst, 'src_val': source})
    return source

def read(statement, source):
    put({'type': 'read', 'statement': statement, 'value': source})
    return source

def link(*params):

    def wrap(func):

        def call(*args):
            for (param, arg) in zip(params, args):
                put({'type': 'link', 'param': param, 'arg': arg})
            return func(*tuple((arg['value'] for arg in args)))
        return call
    return wrap
a = write(3, ('var', 'a'), 3)