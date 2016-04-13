from expr import Variable,VariableTable,InvalidExpression,get_variables
from json import dump
from create_log import format_log
from copy import deepcopy

def union(l1,l2):
    u = []
    for l in l1:
        if l in l2:
            u.append(l)
    return u

def translate_types(type_):
    types_ = {
        'list' : 'array'
    }
    if type_ not in types_:
        return type_
    else:
        return types_[type_]

def initJsonBuffer(observe):
    jsonBuffer = {'header' : { 'annotatedVariables' : {}, 'version' : 0.0 }, 'body' : []}
    for var in observe:
        t_json = translate_types(var.rawType)
        var.rawType = t_json
        jsonBuffer['header']['annotatedVariables'][var.name] = var.get_json()
    return jsonBuffer

def getOperationObject():
    return {'operation' : None, 'operationBody' : {}}

class ToJson(object):
    def __init__(self,observe):
        self.jsonBuffer = initJsonBuffer(observe)
        self.ids = [w.name for w in observe]
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

    # var = get_varialble rethink
    def convert_(self,output):
        for line in output:
            if line['type'] == 'write':
                # copy src_val so that changing src_val in table wont
                # affect src_val written to jsonBuffer
                (new_value,dst) = self.table.update(deepcopy(line['src_val']),line['dst'])
                if dst in self.ids:
                    if new_value:
                        self.putInit(line['dst'],line['src_val'])
                    else:
                        self.putWrite(line['src'],line['src_val'],line['dst'])
            elif line['type'] == 'read':
                var = get_variables(line['statement'])
                if union(var,self.ids):
                    self.putRead(line['statement'],line['value'])
            elif line['type'] == 'link':
                var = get_variables(line['arg']['arg'])
                if union(var,self.ids):
                    if line['param'] not in self.ids:
                        self.ids.append(line['param'])
                    self.putWrite(line['arg']['arg'],line['arg']['value'],('var',line['param']))
        return self.jsonBuffer

def dumpJson(outfile,jsonBuffer):
    with open(outfile,'wb') as f:
        dump(jsonBuffer,f)
        f.close()

def convert(output_path,outfile_path,observe):
    tj = ToJson(observe)
    output = format_log(output_path)
    dumpJson(outfile_path,tj.convert_(output))