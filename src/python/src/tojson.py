from expr import Variable,VariableTable,InvalidExpression
from json import dump

def translate_types(type_):
    types_ = {
        'list' : 'array'
    }
    if type_ not in types_:
        return type_
    else:
        return types_[type_]

def initJsonBuffer(watch):
    jsonBuffer = {'header' : { 'annotatedVariables' : {}, 'version' : 0.0 }, 'body' : []}
    for var in watch:
        t_json = translate_types(var.type_)
        var.type_ = t_json
        jsonBuffer['header']['annotatedVariables'][var.name] = var.get_json()
    return jsonBuffer

def getOperationObject():
    return {'operation' : None, 'operationBody' : {}}

class ToJson(object):
    def __init__(self,watch):
        self.jsonBuffer = initJsonBuffer(watch)
        self.ids = [w.name for w in watch]
        self.table = VariableTable()

    def toJson(self,statement):
        indices = self.table.evaluate_indices(statement)
        if len(indices) == 1:
            return {'identifier' : indices[0]}
        else:
            return {'identifier' : indices[0], 'index' : indices[1:]}

    def putOperation(self,name):
        obj = getOperationObject()
        self.jsonBuffer['body'].append(obj)
        obj['operation'] = name
        return obj

    def putWrite(self,src,src_val,dst):
        obj = self.putOperation('write')
        try:
            obj['operationBody']['source'] = self.toJson(src)
        except InvalidExpression:
            obj['operationBody']['source'] = 'undefined'
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

    # make separate read/write cases
    def convert_(self,output,outfile):
        for line in output:
            if line['type'] == 'write':
                self.table.update(line['src_val'],line['dst'])
                self.putWrite(line['src'],line['src_val'],line['dst'])
            elif line['type'] == 'read':
                pass
        return self.jsonBuffer

def dumpJson(outfile,jsonBuffer):
    with open(outfile,'wb') as f:
        dump(jsonBuffer,f)
        f.close()

def convert(output_path,outfile_path,watch):
    tj = ToJson(watch)
    pm = __import__(output_path)
    output = pm.output
    print tj.convert_(output,outfile_path)

# from create_log import format_log
# format_log('output.py')
convert('output','output.json',[])