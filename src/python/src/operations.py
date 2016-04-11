########################################################################
# To be injected into nodes
########################################################################
outfile = None

def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()

def write(src,dst,source):
    put({'type' : 'write', 'src' : src, 'dst' : dst, 'src_val' : source })
    return source

def read(statement,source):
    put({'type' : 'read', 'statement' :  statement, 'value' : source})
    return source
########################################################################