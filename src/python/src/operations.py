########################################################################
# To be injected into nodes
########################################################################
outfile = None

# Need to isolate this from reseting each time operations is loaded
def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()

# Might wanna make these the same but change in-code syntax
def write(src,dst,source):
    # Right now using hack to make deep copy of source
    put({'type' : 'write', 'src' : src, 'dst' : dst, 'src_val' : source })
    return source

def read(statement,source):
    #put('--read--\nstatement : %s\n' % str(statement))
    put({'type' : 'read', 'statement' :  statement, 'value' : source})
    return source
########################################################################