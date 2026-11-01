#!/usr/bin/env python3
"""Deterministically document declared Java HTTP contracts and literal SDK callers.

No application startup, database access, outbound network calls, or acceptance claims.
This source scanner is deliberately checked against the existing route auditor.
"""
import argparse
import csv
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[2]
JAVA=ROOT/'server/src/main/java'
OUT=ROOT/'docs/api'
VERBS={'Get':'GET','Post':'POST','Put':'PUT','Patch':'PATCH','Delete':'DELETE'}

def without_comments(text):
    pattern=r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|/\*.*?\*/|//[^\n]*'
    return re.sub(pattern,lambda m:re.sub(r'[^\n]',' ',m[0]) if m[0].startswith(('/',)) else m[0],text,flags=re.S)

def closing(text,start,left='(',right=')'):
    depth=0;quote=None;escaped=False
    for i in range(start,len(text)):
        c=text[i]
        if quote:
            if escaped:escaped=False
            elif c=='\\':escaped=True
            elif c==quote:quote=None
            continue
        if c in ['"',"'",'`']:quote=c;continue
        if c==left:depth+=1
        elif c==right:
            depth-=1
            if depth==0:return i
    raise ValueError('Unbalanced source delimiter near '+text[start:start+60])

def split_top(text,separator=','):
    parts=[];start=0;stack=[];quote=None;escaped=False
    pairs={')':'(',']':'[','}':'{','>':'<'}
    for i,c in enumerate(text):
        if quote:
            if escaped:escaped=False
            elif c=='\\':escaped=True
            elif c==quote:quote=None
            continue
        if c in ['"',"'",'`']:quote=c;continue
        if c in '([{<':stack.append(c)
        elif c in ')]}>' and stack and stack[-1]==pairs[c]:stack.pop()
        elif c==separator and not stack:parts.append(text[start:i].strip());start=i+1
    parts.append(text[start:].strip())
    return [p for p in parts if p]

def annotations(text):
    result=[];remaining=[];at=0
    for match in re.finditer(r'@[A-Za-z_][\w.]*',text):
        if match.start()<at:continue
        remaining.append(text[at:match.start()]);end=match.end()
        pos=end
        while pos<len(text) and text[pos].isspace():pos+=1
        if pos<len(text) and text[pos]=='(':end=closing(text,pos)+1
        result.append(text[match.start():end]);at=end
    remaining.append(text[at:])
    return result,' '.join(''.join(remaining).split())

def field(text):
    attrs,plain=annotations(text)
    match=re.match(r'(.+?)\s+([A-Za-z_$][\w$]*)$',plain)
    if not match:raise ValueError('Unsupported field/parameter '+text)
    return {'name':match[2],'type':match[1],'annotations':attrs}

def location(file,text,start):
    return {'file':str(file.relative_to(ROOT)),'line':text.count('\n',0,start)+1}

def types_in(file,text):
    package=re.search(r'\bpackage\s+([\w.]+)\s*;',text)[1]
    found=[]
    for match in re.finditer(r'\b(class|interface|record|enum)\s+([A-Za-z_$][\w$]*)',text):
        kind,name=match.groups();start=match.end()
        if kind=='record':
            param_start=text.find('(',start);param_end=closing(text,param_start)
            fields=[field(p) for p in split_top(text[param_start+1:param_end])]
            brace=text.find('{',param_end)
        else:fields=[];brace=text.find('{',start)
        if brace<0:continue
        end=closing(text,brace,'{','}')
        parents=[p for p in found if p['brace']<match.start()<p['end']]
        names=[p['name'] for p in parents]+[name]
        entry={'name':name,'qualifiedName':package+'.'+'.'.join(names),'kind':kind,'fields':fields,
               'source':location(file,text,match.start()),'brace':brace,'end':end}
        if kind=='enum':entry['values']=re.findall(r'\b[A-Z][A-Z0-9_]*\b',text[brace+1:end].split(';',1)[0])
        found.append(entry)
    return found

def md_escape(value):return str(value).replace('|','\\|').replace('\n',' ')
def source_link(source):return '['+Path(source['file']).name+':'+str(source['line'])+'](../../'+source['file']+'#L'+str(source['line'])+')'
def anchor(name):return re.sub(r'[^a-z0-9_-]','-',name.lower())

def sdk_contracts():
    path=ROOT/'client/packages/sdk/src/client.ts';text=path.read_text();found=[];dynamic=[]
    for match in re.finditer(r'\brequest<',text):
        end=closing(text,match.end()-1,'<','>');pos=end+1
        if pos>=len(text) or text[pos]!='(':continue
        call_end=closing(text,pos);args=split_top(text[pos+1:call_end])
        if len(args)<2 or not re.fullmatch(r"'(GET|POST|PUT|PATCH|DELETE)'",args[0]):continue
        method=args[0][1:-1];response=text[match.end():end]
        if not re.fullmatch(r"'[^']+'",args[1]) and not re.fullmatch(r"[A-Za-z_$][\w$]*\s*\?\s*'[^']+'\s*:\s*'[^']+'",args[1]):
            dynamic.append({'method':method,'pathExpression':args[1],'responseType':response,'source':location(path,text,match.start())})
            continue
        for route in re.findall(r"'(/[^']*)'",args[1]):
            found.append({'method':method,'path':route,'responseType':response,
                          'source':location(path,text,match.start())})
    types_path=ROOT/'client/packages/sdk/src/types.ts';source=types_path.read_text();definitions=[]
    for match in re.finditer(r'export\s+(interface|type)\s+(\w+)\b',source):
        if match[1]=='interface':
            begin=source.find('{',match.end());end=closing(source,begin,'{','}')+1
        else:
            after=source[match.end():];parts=split_top(after,';')
            # All current exported aliases terminate with a top-level semicolon.
            first=parts[0] if parts else after
            end=match.end()+after.find(first)+len(first)+1
        definitions.append({'name':match[2],'kind':match[1],'declaration':source[match.start():end].strip(),
                            'source':location(types_path,source,match.start())})
    return found,definitions,dynamic

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    sources={p:without_comments(p.read_text()) for p in sorted(JAVA.rglob('*.java'))}
    schemas={}
    for path,text in sources.items():
        for entry in types_in(path,text):schemas[entry['qualifiedName']]=entry
    simple={}
    for name in schemas:simple.setdefault(name.rsplit('.',1)[-1],[]).append(name)
    def resolve(name,file,controller):
        name=re.sub(r'\s+','',name)
        if name in schemas:return name
        package=re.search(r'\bpackage\s+([\w.]+)\s*;',sources[file])[1]
        candidates=[package+'.'+controller+'.'+name,package+'.'+name]
        for imported in re.findall(r'\bimport\s+(?:static\s+)?([\w.*]+)\s*;',sources[file]):
            if imported.endswith('.*'):candidates.append(imported[:-2]+'.'+name)
            elif name==imported.rsplit('.',1)[-1]:candidates.append(imported)
            elif name.startswith(imported.rsplit('.',1)[-1]+'.'):candidates.append(imported.rsplit('.',1)[0]+'.'+name)
        for item in candidates:
            if item in schemas:return item
        return simple[name][0] if len(simple.get(name,[]))==1 else None
    endpoint_list=[];used=set();sdk,ts_types,dynamic_sdk=sdk_contracts()
    for path,text in sources.items():
        if '@RestController' not in text:continue
        controller=re.search(r'\bclass\s+(\w+)',text)[1];class_at=text.find('class '+controller)
        base_match=re.search(r'@RequestMapping\s*\(\s*"([^"]*)"',text[:class_at]);base=base_match[1] if base_match else ''
        for mapping in re.finditer(r'@(Get|Post|Put|Patch|Delete)Mapping\b',text):
            end=mapping.end();pos=end
            while pos<len(text) and text[pos].isspace():pos+=1
            mapping_args=''
            if pos<len(text) and text[pos]=='(':end=closing(text,pos)+1;mapping_args=text[pos+1:end-1]
            relative=re.match(r'\s*(?:(?:value|path)\s*=\s*)?"([^"]*)"',mapping_args)
            if mapping_args.strip() and not relative:raise ValueError('Unsupported mapping '+str(path)+mapping_args)
            signature=re.search(r'\bpublic\s+([\w.<>, ?\[\]]+)\s+(\w+)\s*\(',text[end:])
            if not signature:raise ValueError('Cannot parse method after '+str(path)+mapping.group())
            param_start=end+signature.end()-1;param_end=closing(text,param_start)
            params=[field(p) for p in split_top(text[param_start+1:param_end])]
            for parameter in params:
                attr=' '.join(parameter['annotations'])
                parameter['location']='body' if '@RequestBody' in attr else 'path' if '@PathVariable' in attr else 'query' if '@RequestParam' in attr else 'header' if '@RequestHeader' in attr else 'multipart' if '@RequestPart' in attr else 'framework'
                declared=re.search(r'@(?:RequestParam|PathVariable|RequestHeader|RequestPart)\s*\(\s*(?:(?:value|name)\s*=\s*)?"([^"]+)"',attr)
                parameter['wireName']=declared[1] if declared else parameter['name']
                default=re.search(r'defaultValue\s*=\s*"([^"]*)"',attr)
                if default:parameter['default']=default[1]
                parameter['requiredByBinding']=parameter['location'] in ['body','path','query','header','multipart'] and 'required=false' not in attr.replace(' ','') and not default
                schema=resolve(parameter['type'],path,controller)
                if schema and schemas[schema]['kind'] in ['record','enum']:
                    parameter['schema']=schema;parameter['fields']=schemas[schema]['fields'];used.add(schema)
            route=base+(relative[1] if relative else '')
            public=route.removeprefix('/api')
            alias=public if re.match(r'^/(public|rpc|portal|support|me|auth)(/|$)',public) else None
            row={'method':VERBS[mapping[1]],'path':route,'gatewayAlias':alias,'controller':controller,'handler':signature[2],
                 'parameters':params,'responseJavaType':signature[1].strip(),'source':location(path,text,mapping.start()),
                 'mappingAttributes':mapping_args,'contractStatus':'declared; provider and full business acceptance not implied'}
            row['responseSchemas']=[]
            row['sdkResponses']=[item for item in sdk if item['method']==row['method'] and item['path']==public]
            row['accessHint']='public' if route.startswith(('/api/public/','/api/auth/')) else 'provider_signature_callback' if route.startswith('/api/payment-callback/') else 'authenticated; service ownership/admin guard applies'
            if '/admin/' in route or '/rpc/admin_' in route:row['accessHint']='administrator; service guard applies'
            for token in re.findall(r'[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*',row['responseJavaType']):
                resolved=resolve(token,path,controller)
                if resolved and schemas[resolved]['kind'] in ['record','enum']:
                    used.add(resolved)
                    if resolved not in row['responseSchemas']:row['responseSchemas'].append(resolved)
            endpoint_list.append(row)
    # Independent route parser guards against silently omitting declarations.
    spec=importlib.util.spec_from_file_location('audit_routes',ROOT/'server/tools/audit_routes.py');audit=importlib.util.module_from_spec(spec);spec.loader.exec_module(audit)
    expected=[]
    for path,text in sources.items():
        if '@RestController' in text:expected.extend(audit.routes(path.read_text()))
    actual=[(r['method'],r['path'].removeprefix('/api')) for r in endpoint_list]
    if sorted(actual)!=sorted(expected):raise ValueError('Contract scanner differs from independent controller route auditor')
    if len(actual)!=len(set(actual)):raise ValueError('Duplicate endpoint declaration')
    # Include component records/enums recursively, retaining source rather than guessing runtime map keys.
    pending=list(used)
    while pending:
        name=pending.pop();entry=schemas[name];file=ROOT/entry['source']['file']
        for component in entry['fields']:
            for token in re.findall(r'[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*',component['type']):
                resolved=resolve(token,file,name.split('.')[-2] if '.' in name else name)
                if resolved and schemas[resolved]['kind'] in ['record','enum'] and resolved not in used:used.add(resolved);pending.append(resolved)
    exported={name:{k:v for k,v in schemas[name].items() if k not in ['brace','end']} for name in sorted(used)}
    known={(r['method'],r['path'].removeprefix('/api')) for r in endpoint_list}
    missing=[item for item in sdk if (item['method'],item['path']) not in known]
    source_hash=hashlib.sha256()
    for file in sorted([*sources,ROOT/'client/packages/sdk/src/client.ts',ROOT/'client/packages/sdk/src/types.ts']):source_hash.update(str(file.relative_to(ROOT)).encode()+b'\0'+file.read_bytes())
    endpoint_list.sort(key=lambda row:(row['path'],row['method']))
    index={'sourceSha256':source_hash.hexdigest(),'scope':'source declarations only; not complete runtime acceptance',
           'endpointCount':len(endpoint_list),'schemaCount':len(exported),'sdkCallCount':len(sdk),
           'sdkMissingBackendCount':len(missing),'dynamicSdkCalls':dynamic_sdk,
           'endpoints':endpoint_list,'sdkMissingBackend':missing}
    outputs={'HTTP_INDEX.json':json.dumps(index,ensure_ascii=False,indent=2)+'\n',
             'JAVA_TYPES.json':json.dumps(exported,ensure_ascii=False,indent=2)+'\n',
             'SDK_TYPES.json':json.dumps(ts_types,ensure_ascii=False,indent=2)+'\n'}
    buffer=io.StringIO();writer=csv.writer(buffer,lineterminator='\n');writer.writerow(['method','canonical_path','gateway_alias','controller','handler','access_hint','request_parameters_json','response_java_type','sdk_response_types','source_file','source_line'])
    for row in endpoint_list:writer.writerow([row['method'],row['path'],row['gatewayAlias'] or '',row['controller'],row['handler'],row['accessHint'],json.dumps(row['parameters'],ensure_ascii=False),row['responseJavaType'],'; '.join(item['responseType'] for item in row['sdkResponses']),row['source']['file'],row['source']['line']])
    outputs['HTTP_INDEX.csv']=buffer.getvalue()
    lines=['# 全量 HTTP 声明与参数','',f"由 `python3 server/tools/generate_api_reference.py` 生成；{len(endpoint_list)}个真实控制器路由、{len(exported)}个请求/响应record或enum。JSON含全部字段、注解、默认值与定位。",'',
           '只枚举已声明入口；不代表全业务或供应商验收。权限提示是路由提示，最终还要经过Security与service guard。泛型Map/Object响应不虚构固定字段；若存在SDK类型，列出其声明并链接[SDK字段附录](SDK_TYPES.md)，仍以服务端返回为准。[Java record字段附录](JAVA_TYPES.md)。','']
    current=None
    for row in endpoint_list:
        if current!=row['controller']:current=row['controller'];lines+=['## '+current,'']
        lines+=['### `'+row['method']+' '+row['path']+'`','',source_link(row['source'])+'；'+row['accessHint']+'。',
                '',('网关兼容路径：`'+row['gatewayAlias']+'`。') if row['gatewayAlias'] else '服务端无该路径的去 `/api` 网关兼容声明，使用规范路径。','',
                '| 位置 | 名称 | Java类型 / 字段 | 绑定必需 | 默认 |','| --- | --- | --- | --- | --- |']
        if not row['parameters']:lines+=['| — | — | 无参数 | — | — |']
        for param in row['parameters']:
            shape=param['type']
            if param.get('schema'):shape+=' → ['+param['schema']+'](JAVA_TYPES.md#'+anchor(param['schema'])+')'
            if param.get('fields'):shape+=': '+', '.join(c['name']+':'+c['type'] for c in param['fields'])
            lines+=['| '+' | '.join(md_escape(v) for v in [param['location'],param['wireName'],shape,param['requiredByBinding'],param.get('default','—')])+' |']
        lines+=['','返回 Java 类型：`'+row['responseJavaType']+'`。']
        for schema in row['responseSchemas']:lines+=['响应字段：['+schema+'](JAVA_TYPES.md#'+anchor(schema)+')。']
        for sdk_row in row['sdkResponses']:lines+=['SDK调用方声明：`'+sdk_row['responseType']+'`；'+source_link(sdk_row['source'])+'。']
        lines+=['']
    lines+=['## SDK调用但未找到控制器声明','',f'{len(missing)}条调用（含模板条件分支，按调用点计数），不能作为可用接口。','', '| 方法 | 路径 | SDK源 |','| --- | --- | --- |']
    lines += ['| '+row['method']+' | `'+row['path']+'` | '+source_link(row['source'])+' |' for row in missing]
    lines+=['','## 动态 SDK 路径','', '以下是拼接/动态路径，不能当作声明路由；RPC实际键见[RPC索引](RPC_INDEX.json)。','']
    for row in dynamic_sdk:lines+=['- `'+row['method']+' '+row['pathExpression']+'`；'+source_link(row['source'])+'。']
    outputs['HTTP_INDEX.md']='\n'.join(lines).rstrip()+'\n'
    lines=['# Java 请求与响应字段','', '从controller参数与返回值追踪的record/enum，保留字段类型及声明约束。`@Valid`仅在对应入口声明时触发Bean Validation；service还可能校验条件必填/权限/状态。未标@NotNull不等于业务允许空值。金额精度、UUID和状态以附列注解及业务章节为准。','']
    for name,entry in exported.items():
        lines+=['<a id="'+anchor(name)+'"></a>','## '+name,'',source_link(entry['source'])+'；'+entry['kind']+'。','']
        if entry['kind']=='enum':lines+=['值：`'+'`, `'.join(entry['values'])+'`。',''];continue
        lines+=['| 字段 | 类型 | 声明注解 |','| --- | --- | --- |']
        for component in entry['fields']:lines+=['| '+' | '.join(md_escape(v) for v in [component['name'],component['type'],' '.join(component['annotations']) or '—'])+' |']
        lines+=['']
    outputs['JAVA_TYPES.md']='\n'.join(lines).rstrip()+'\n'
    lines=['# SDK调用方类型字段','', '以下为前端TypeScript声明的逐项原文，便于对照泛型Map响应。它们不是额外后端保证；`BackendRecord`/索引签名仍是开放字段，真实SQL选择列、业务状态及当前响应优先。','']
    for entry in ts_types:lines+=['## '+entry['name'],'',source_link(entry['source']),'','```typescript',entry['declaration'],'```','']
    outputs['SDK_TYPES.md']='\n'.join(lines).rstrip()+'\n'
    for name,content in outputs.items():
        path=OUT/name
        if args.check:
            if not path.exists() or path.read_text()!=content:raise SystemExit('Stale generated API documentation: '+str(path.relative_to(ROOT)))
        else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(content)
    print(json.dumps({k:index[k] for k in ['endpointCount','schemaCount','sdkCallCount','sdkMissingBackendCount']},ensure_ascii=False))

if __name__=='__main__':main()
