outfile = '/home/johan/Dropbox/It/Kandidat Datx02/project/src/python/src/output.py'

def put(statement):
    with open(outfile, 'a') as f:
        f.write('%s,' % str(statement))
        f.close()

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
nodes = write(('A', 'B', 'C', 'D', 'E', 'F', 'G'), ('var', 'nodes'), ('A', 'B', 'C', 'D', 'E', 'F', 'G'))
distances = write({'B': {'A': 5, 'D': 1, 'G': 2}, 'A': {'B': 5, 'D': 3, 'E': 12, 'F': 5}, 'D': {'B': 1, 'G': 1, 'E': 1, 'A': 3}, 'G': {'B': 2, 'D': 1, 'C': 2}, 'C': {'G': 2, 'E': 1, 'F': 16}, 'E': {'A': 12, 'D': 1, 'C': 1, 'F': 2}, 'F': {'A': 5, 'E': 2, 'C': 16}}, ('var', 'distances'), {'B': {'A': 5, 'D': 1, 'G': 2}, 'A': {'B': 5, 'D': 3, 'E': 12, 'F': 5}, 'D': {'B': 1, 'G': 1, 'E': 1, 'A': 3}, 'G': {'B': 2, 'D': 1, 'C': 2}, 'C': {'G': 2, 'E': 1, 'F': 16}, 'E': {'A': 12, 'D': 1, 'C': 1, 'F': 2}, 'F': {'A': 5, 'E': 2, 'C': 16}})
unvisited = write({node: None for node in nodes}, ('var', 'unvisited'), {node: None for node in nodes})
visited = write({}, ('var', 'visited'), {})
current = write('B', ('var', 'current'), 'B')
currentDistance = write(0, ('var', 'currentDistance'), 0)
unvisited[current] = write(('var', 'currentDistance'), ('subscript', ('var', 'unvisited'), ('var', 'current')), currentDistance)
while read(True, True):
    for (neighbour, distance) in distances[current].items():
        distance = write(distance, ('var', 'distance'), distance)
        neighbour = write(neighbour, ('var', 'neighbour'), neighbour)
        if (read(('var', 'neighbour'), neighbour) not in read(('var', 'unvisited'), unvisited)):
            continue
        newDistance = write(('binop', '+', ('var', 'currentDistance'), ('var', 'distance')), ('var', 'newDistance'), currentDistance + distance)
        if ((read(('subscript', ('var', 'unvisited'), ('var', 'neighbour')), unvisited[neighbour]) is read(('var', 'None'), None)) or (read(('subscript', ('var', 'unvisited'), ('var', 'neighbour')), unvisited[neighbour]) > read(('var', 'newDistance'), newDistance))):
            unvisited[neighbour] = write(('var', 'newDistance'), ('subscript', ('var', 'unvisited'), ('var', 'neighbour')), newDistance)
    visited[current] = write(('var', 'currentDistance'), ('subscript', ('var', 'visited'), ('var', 'current')), currentDistance)
    del unvisited[current]
    if (not read(('var', 'unvisited'), unvisited)):
        break
    candidates = write([node for node in unvisited.items() if node[1]], ('var', 'candidates'), [node for node in unvisited.items() if node[1]])
    (current, currentDistance) = sorted(candidates, key=lambda x: x[1])[0]
print read(('var', 'visited'), visited)