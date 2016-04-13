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
from random import random

@link('matrix', 'm', 'n')
def dijkstra(matrix, m, n):
    k = write('undefined', ('var', 'k'), int(input('Enter the source vertex')))
    cost = write([[0 for x in range(m)] for x in range(1)], ('var', 'cost'), [[0 for x in range(m)] for x in range(1)])
    offsets = write([], ('var', 'offsets'), [])
    offsets.append(k)
    elepos = write(0, ('var', 'elepos'), 0)
    for j in range(m):
        j = write(j, ('var', 'j'), j)
        cost[0][j] = write(('subscript', ('var', 'matrix', matrix), ('var', 'k', k), ('var', 'j', j)), ('subscript', ('var', 'cost', cost), 0, ('var', 'j', j)), matrix[k][j])
    mini = write(999, ('var', 'mini'), 999)
    for x in range(m - 1):
        x = write(x, ('var', 'x'), x)
        mini = write(999, ('var', 'mini'), 999)
        for j in range(m):
            j = write(j, ('var', 'j'), j)
            if ((read(('subscript', ('var', 'cost', cost), 0, ('var', 'j', j)), cost[0][j]) <= read(('var', 'mini', mini), mini)) and (read(('var', 'j', j), j) not in read(('var', 'offsets', offsets), offsets))):
                mini = write(('subscript', ('var', 'cost', cost), 0, ('var', 'j', j)), ('var', 'mini'), cost[0][j])
                elepos = write(('var', 'j', j), ('var', 'elepos'), j)
        offsets.append(elepos)
        for j in range(m):
            j = write(j, ('var', 'j'), j)
            if (read(('subscript', ('var', 'cost', cost), 0, ('var', 'j', j)), cost[0][j]) > read(('subscript', ('var', 'cost', cost), 0, ('var', 'elepos', elepos)), cost[0][elepos]) + read(('subscript', ('var', 'matrix', matrix), ('var', 'elepos', elepos), ('var', 'j', j)), matrix[elepos][j])):
                cost[0][j] = write(('binop', '+', ('subscript', ('var', 'cost', cost), 0, ('var', 'elepos', elepos)), ('subscript', ('var', 'matrix', matrix), ('var', 'elepos', elepos), ('var', 'j', j))), ('subscript', ('var', 'cost', cost), 0, ('var', 'j', j)), cost[0][elepos] + matrix[elepos][j])
    print ('The shortest path', read(('var', 'offsets', offsets), offsets))
    print ('The cost to various vertices in order', read(('var', 'cost', cost), cost))

@link()
def main():
    print 'Dijkstras algorithum graph using matrix representation \n'
    n = write('undefined', ('var', 'n'), int(input('number of elements in row')))
    m = write('undefined', ('var', 'm'), int(input('number of elements in column')))
    matrix = write([[0 for x in range(m)] for x in range(n)], ('var', 'matrix'), [[0 for x in range(m)] for x in range(n)])
    for i in range(n):
        i = write(i, ('var', 'i'), i)
        for j in range(m):
            j = write(j, ('var', 'j'), j)
            matrix[i][j] = write('undefined', ('subscript', ('var', 'matrix', matrix), ('var', 'i', i), ('var', 'j', j)), int(random() * 100))
    print read(('var', 'matrix', matrix), matrix)
    dijkstra({'value': matrix, 'arg': ('var', 'matrix', matrix)}, {'value': n, 'arg': ('var', 'n', n)}, {'value': m, 'arg': ('var', 'm', m)})
main()