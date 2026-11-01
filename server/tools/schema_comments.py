#!/usr/bin/env python3
"""Parse CREATE TABLE definitions without changing SQL; build reviewed Chinese comments."""
import json
import argparse
import hashlib
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TYPES = r'CHAR|VARCHAR|TINYTEXT|MEDIUMTEXT|LONGTEXT|TEXT|TINYINT|SMALLINT|INT|BIGINT|DECIMAL|DOUBLE|FLOAT|JSON|DATETIME|TIMESTAMP|DATE|BOOLEAN'

def mask_comments(text):
    out = list(text)
    i, quote = 0, None
    while i < len(text):
        c = text[i]
        if quote:
            if c == '\\': i += 2; continue
            if c == quote:
                if i+1 < len(text) and text[i+1] == quote: i += 2; continue
                quote = None
        elif c in "'\"`": quote = c
        elif text.startswith('--', i) or c == '#':
            end = text.find('\n', i)
            if end < 0: end = len(text)
            out[i:end] = ' ' * (end-i); i = end; continue
        elif text.startswith('/*', i):
            end = text.find('*/', i+2)
            if end < 0: raise ValueError('Unclosed SQL comment')
            end += 2
            out[i:end] = ['\n' if c == '\n' else ' ' for c in text[i:end]]
            i = end; continue
        i += 1
    return ''.join(out)

def boundaries(text, start, stop_at_close=False):
    depth, quote, i = 0, None, start
    while i < len(text):
        c = text[i]
        if quote:
            if c == '\\': i += 2; continue
            if c == quote:
                if i+1 < len(text) and text[i+1] == quote: i += 2; continue
                quote = None
        elif c in "'\"`": quote = c
        elif c == '(': depth += 1
        elif c == ')':
            if depth == 0 and stop_at_close: return i
            depth -= 1
        elif c == ';' and depth == 0 and not stop_at_close: return i
        i += 1
    raise ValueError('Unclosed SQL definition')

def split_columns(text, start, end):
    depth, quote, begin, i = 0, None, start, start
    while i < end:
        c = text[i]
        if quote:
            if c == '\\': i += 2; continue
            if c == quote:
                if i+1 < end and text[i+1] == quote: i += 2; continue
                quote = None
        elif c in "'\"`": quote = c
        elif c == '(': depth += 1
        elif c == ')': depth -= 1
        elif c == ',' and depth == 0:
            yield begin, i
            begin = i+1
        i += 1
    yield begin, end

COMMENT = re.compile(r"\bCOMMENT\s*(?:=\s*)?'((?:''|\\.|[^'])*)'", re.I)

def tables(sql):
    clean = mask_comments(sql)
    result = []
    for match in re.finditer(r'CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?(\w+)`?\s*\(', clean, re.I):
        start = match.end()
        end = boundaries(clean, start, True)
        finish = boundaries(clean, end+1)
        columns = []
        for left, right in split_columns(clean, start, end):
            part = clean[left:right]
            field = re.match(r'\s*`?(\w+)`?\s+('+TYPES+r')\b', part, re.I)
            if not field: continue
            comment = COMMENT.search(part)
            columns.append({'name':field[1], 'definition':part.strip(), 'comment':comment[1] if comment else '', 'start':left,'end':right})
        comment = COMMENT.search(clean[end+1:finish])
        result.append({'name':match[1], 'comment':comment[1] if comment else '', 'columns':columns, 'body_start':start,'body_end':end,'end':finish})
    return result

def inventory():
    result = []
    for path in [ROOT/'db/reference/current-schema.sql', *sorted((ROOT/'db/migrations').glob('*.proposed.sql'))]:
        for table in tables(path.read_text()):
            table['source'] = str(path.relative_to(ROOT.parent))
            result.append(table)
    return result

CATALOG = ROOT/'db/schema-comments.json'
BASELINE = ROOT/'db/reference/current-schema.sql'
ANNOTATED = ROOT/'db/reference/current-schema.annotated.sql'
FULL = ROOT/'db/deploy/kuaixieTech_full_schema_commented.sql'
DICTIONARY = ROOT/'db/DATA_DICTIONARY.md'

def quoted(value):
    if not value.strip() or len(value)>1000: raise ValueError('Invalid comment length')
    return "'"+value.replace('\\','\\\\').replace("'","''")+"'"

def annotate(sql, catalog):
    changes = []
    clean = mask_comments(sql)
    for table in tables(sql):
        metadata = catalog[table['name']]
        if set(metadata['columns']) != {c['name'] for c in table['columns']}:
            raise ValueError('Comment/column mismatch: '+table['name'])
        for column in table['columns']:
            left, right = column['start'], column['end']
            part = clean[left:right]
            existing = COMMENT.search(part)
            value = quoted(metadata['columns'][column['name']])
            if existing:
                changes.append((left+existing.start(),left+existing.end(),'COMMENT '+value))
            else:
                at = left+len(part.rstrip())
                changes.append((at,at,' COMMENT '+value))
        left, right = table['body_end']+1, table['end']
        existing = COMMENT.search(clean[left:right])
        value = quoted(metadata['comment'])
        if existing: changes.append((left+existing.start(),left+existing.end(),'COMMENT='+value))
        else: changes.append((right,right,' COMMENT='+value))
    for left,right,value in sorted(changes,reverse=True): sql=sql[:left]+value+sql[right:]
    return sql

def without_seed_data(sql):
    """Remove top-level INSERT seeds, preserving original trigger definitions verbatim."""
    changes=[]
    clean=mask_comments(sql)
    delimiter=clean.upper().find('DELIMITER ')
    for match in re.finditer(r'^\s*INSERT(?:\s+IGNORE)?\s+INTO\b',clean,re.M|re.I):
        if delimiter>=0 and match.start()>delimiter:
            raise ValueError('INSERT inside delimiter block requires explicit review')
        end=boundaries(clean,match.end())+1
        changes.append((match.start(),end,'\n-- 原始参考 SQL 中的种子 INSERT 已从完整结构版排除。\n'))
    for left,right,value in sorted(changes,reverse=True): sql=sql[:left]+value+sql[right:]
    return sql,len(changes)

def products(catalog):
    baseline=BASELINE.read_text()
    annotated=annotate(baseline,catalog)
    clean,removed=without_seed_data(annotated)
    guard=("\n-- 完整结构版仅允许空库；请让SQL客户端遇错停止，不要使用 --force。\n"
           "SET @kuaixie_full_schema_guard = IF(\n"
           " (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE())=0,\n"
           " 'SELECT ''Empty database confirmed'' AS initialization_status',\n"
           " 'SELECT * FROM information_schema.KUAIXIE_FULL_SCHEMA_REQUIRES_EMPTY_DATABASE');\n"
           "PREPARE kuaixie_full_schema_guard_stmt FROM @kuaixie_full_schema_guard;\n"
           "EXECUTE kuaixie_full_schema_guard_stmt;\n"
           "DEALLOCATE PREPARE kuaixie_full_schema_guard_stmt;\n"
           "SET @kuaixie_full_schema_guard = NULL;\n")
    clean,count=re.subn(r'(?im)^USE\s+`suixinggou`;\s*$',lambda m:m[0]+'\n'+guard,clean,count=1)
    if count!=1: raise ValueError('Expected original database selection')
    digest=hashlib.sha256(baseline.encode()).hexdigest()
    header=("-- 数据库：suixinggou；随行带货交易撮合平台业务库。\n"
            "-- 完整结构版：原有82表 + 提议新增31表 = 113表，含表及字段中文COMMENT。\n"
            "-- 仅供新建空库审阅初始化；已有业务库请使用增量版，不要重复导入本文件。\n"
            "-- 保留原始索引、外键及触发器定义；不含原始种子数据和业务数据。\n"
            "-- MySQL没有库级COMMENT语法，数据库用途在此及DATA_DICTIONARY.md中说明。\n"
            "-- 本工具不连接数据库；未验证原始触发器或实际MySQL执行。\n"
            f"-- 原始文件SHA256：{digest}；排除种子INSERT：{removed}条。\n\n")
    from build_deployment_sql import render
    incremental,_=render()
    full=(header+clean+'\n\n-- 以下为同库增量结构，不重复导入原始表。\n'+incremental).rstrip()+'\n'
    rows=inventory()
    doc=['# 数据库中文数据字典\n',
         '数据库名：`suixinggou`。用途：随行带货交易撮合平台业务库，涵盖用户、商品、行程、订单、托管资金、店铺、保险、售后、配置与运营。MySQL不支持库级COMMENT；不重命名现有数据库。\n',
         '范围：原有82表、提议新增31表，共113表/1186字段。仅补充备注，不改变表名、字段名、金额或业务数据；011另行提议将订单status扩宽至32字符。全部仍待真实环境完整验收。\n',
         '增量版用于已有库升级，不包含原有82表的重建，也不会更新已经存在表的COMMENT；完整结构版含全部表，适用于新空库审阅初始化。原始上传SQL保持原样，带备注的原始82表副本另存reference/current-schema.annotated.sql。\n',
         '给真实旧库补备注应根据其实际SHOW CREATE TABLE生成逐项审阅的专用迁移；本次不使用旧快照MODIFY覆盖线上字段属性，不执行任何SQL。\n']
    for table in rows:
        metadata=catalog[table['name']]
        kind='原有表' if table['source'].endswith('reference/current-schema.sql') else '提议新增表'
        rowsource=table['source']
        doc.extend([f"\n## `{table['name']}` — {metadata['comment']}\n",f'来源：{kind}；`{rowsource}`。\n',
                    '| 字段 | 定义（类型、默认值及列级约束） | 中文备注 |','| --- | --- | --- |'])
        for col in table['columns']:
            definition=COMMENT.sub('',col['definition']).strip()
            if table['name']=='t_orders' and col['name']=='status':
                definition=re.sub(r'VARCHAR\(16\)','VARCHAR(32)',definition,flags=re.I)+'（011后；原始为16字符）'
            definition=re.sub(r'\s+',' ',definition).replace('|','\\|').replace('`','\\`')
            comment=metadata['columns'][col['name']].replace('|','\\|')
            doc.append(f'| `{col["name"]}` | `{definition}` | {comment} |')
    return {ANNOTATED:'-- 原始82表的中文备注副本；包含原始种子和触发器，不用于直接重复导入已有业务库。\n'+annotated,FULL:full,DICTIONARY:'\n'.join(doc)+'\n'}

if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write',action='store_true')
    parser.add_argument('--check',action='store_true')
    args=parser.parse_args()
    if args.write or args.check:
        catalog=json.loads(CATALOG.read_text())
        if set(catalog)!={t['name'] for t in inventory()}: raise ValueError('Table comment coverage mismatch')
        for path in sorted((ROOT/'db/migrations').glob('*.proposed.sql')):
            updated=annotate(path.read_text(),catalog)
            if args.write: path.write_text(updated)
            elif updated!=path.read_text(): raise ValueError('Migration comments stale: '+str(path))
        if args.write:
            from build_deployment_sql import render,TARGET
            TARGET.write_text(render()[0])
        for path,content in products(catalog).items():
            if args.write: path.write_text(content)
            elif not path.exists() or path.read_text()!=content: raise ValueError('Generated comments stale: '+str(path))
        print('113 tables / 1186 columns have reviewed comments; original reference unchanged; no database connection')
    else:
        data = inventory()
        Path('/tmp/schema-comment-inventory.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
        print(f'{len(data)} tables; {sum(len(t["columns"]) for t in data)} columns; '+str(sum(not c['comment'] for t in data for c in t['columns']))+' missing column comments')
