outfile = '/home/johan/Documents/Chalmers/Kandidat/python/parsing/transform/base3/output.py'

def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()

def write(src, dst, source):
    put({'type': 'write', 'src': src, 'dst': dst, 'src_val': eval(str(source))})
    return source

def read(statement, source):
    put({'type': 'read', 'statement': statement, 'value': source})
    return source
a = write([1, 2, 3], ('var', 'a'), [1, 2, 3])
a[len(a) - 1] = write(('subscript', ('var', 'a'), 0), ('subscript', ('var', 'a'), ('binop', '-', len(a), 1)), a[0])
if (read(('subscript', ('var', 'a'), 0), a[0]) > 2):
    a[0]+=1