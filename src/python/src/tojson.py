from var import VarTable,Var,get_vars,find_reads
from json import dump

translate_types = {
    'list' : 'array'
}

def initJson(watch):
    init = {'header' : { 'annotatedVariables' : {}, 'version' : 0.0 }, 'body' : []}
    for var in watch:
        t_json = translate_types[var.type_]
        var.type_ = t_json
        init['header']['annotatedVariables'][var.name] = var.get_json() 
    return init

def getOperationObject():
    return {'operation' : None, 'operationBody' : {}}

class ToJson(object):
    def __init__(self,watch):
        self.jsonBuffer = initJson(watch)
        self.table = VarTable()
        self.ids = [w.name for w in watch]

    def toJson(self,statement):
        variable = self.table.evaluate_indices(statement)
        if not isinstance(variable,tuple):
            if isinstance(variable,str):
                return {'identifier' : variable}
            else:
                return None
        else:
            return {'identifier' : variable[0], 'index' : list(variable[1:]) } 

    def putOperation(self,name):
        obj = getOperationObject()
        self.jsonBuffer['body'].append(obj)
        obj['operation'] = name
        return obj

    def putWrite(self,src,src_val,dst):
        obj = self.putOperation('write')
        obj['operationBody']['source'] = self.toJson(src)
        obj['operationBody']['target'] = self.toJson(dst)
        obj['operationBody']['value'] = src_val

    def putRead(self,statement,value):
        obj = self.putOperation('read')
        obj['operationBody']['source'] = self.toJson(statement)
        obj['operationBody']['value'] = value

    def putInit(self,dst,value):
        obj = self.putOperation('init')
        obj['operationBody']['target'] = self.toJson(dst)
        # handle multi-dimensional
        obj['operationBody']['size'] = len(value) if isinstance(value,list) else 1
        obj['operationBody']['value'] = value

    def dumpJson(self,outfile):
        with open(outfile,'wb') as f:
            dump(self.jsonBuffer,f)
            f.close()

    # make separate read/write cases
    def convert(self,output,outfile):
        print output
        for line in output:
            isNewValue = self.table.update(line)
            if line['type'] == 'write':
                (src,dst) = (line['src'],line['dst'])
                var = get_vars(dst) + get_vars(src)
                if [i for i in self.ids if i in var]:
                    if not isNewValue or [i for i in self.ids if i in get_vars(src)]:
                        self.putWrite(src,line['src_val'],dst)
                    else:
                        self.putInit(dst,line['src_val'])
            elif line['type'] == 'read':
                reads = find_reads(line['statement'])
                for read in reads:
                    self.putRead(read,self.table.evaluate(read))
        self.dumpJson(outfile)