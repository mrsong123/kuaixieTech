#!/usr/bin/env python3
"""Check literal INSERT column lists against required fields in reference/proposed DDL.
Does not connect to MySQL or prove SQL semantics. Dynamic column lists are counted as skipped.
"""
from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
def split(s):
 parts=[];start=depth=0;q=None
 for i,c in enumerate(s):
  if q:
   if c==q and (i==0 or s[i-1]!='\\'):q=None
  elif c in "'\"`":q=c
  elif c=='(':depth+=1
  elif c==')':depth-=1
  elif c==',' and depth==0:parts.append(s[start:i]);start=i+1
 parts.append(s[start:]);return parts
def required_columns(sql):
 required={}
 for m in re.finditer(r'CREATE TABLE\s+(?:IF NOT EXISTS\s+)?`?(\w+)`?\s*\(',sql,re.I):
  i=m.end();depth=1;q=None
  while i<len(sql) and depth:
   c=sql[i]
   if q:
    if c==q and sql[i-1]!='\\':q=None
   elif c in "'\"`":q=c
   elif c=='(':depth+=1
   elif c==')':depth-=1
   i+=1
  fields=[];pk=set()
  for decl in split(sql[m.end():i-1]):
   decl=re.sub(r"COMMENT\s+'(?:[^'\\]|\\.)*'",'',decl,flags=re.I).strip()
   col=re.match(r'`?(\w+)`?\s+(?:CHAR|VARCHAR|TEXT|BIGINT|INT|TINYINT|SMALLINT|DECIMAL|DOUBLE|FLOAT|JSON|DATETIME|TIMESTAMP|DATE|BOOLEAN)\b',decl,re.I)
   if col:fields.append((col[1],bool(re.search(r'NOT\s+NULL|PRIMARY\s+KEY',decl,re.I)),bool(re.search(r'DEFAULT|AUTO_INCREMENT|GENERATED',decl,re.I))))
   elif re.match(r'PRIMARY\s+KEY',decl,re.I):pk.update(re.findall(r'`?(\w+)`?',decl[decl.find('(')+1:decl.find(')')]))
  required[m[1]]={k for k,non,default in fields if (non or k in pk) and not default}
 return required
def audit(required, sources):
 issues=[];count=skipped=0
 for name,text in sources:
  for m in re.finditer(r'INSERT\s+(?:IGNORE\s+)?INTO\s+`?(t_\w+)`?\s*\(([^)]+)\)',text,re.I):
   names=m[2].strip()
   if not re.fullmatch(r'[\w`,\s]+',names):skipped+=1;continue
   count+=1;given=set(re.findall(r'\w+',names));missing=required.get(m[1],set())-given
   if missing:issues.append((name,text.count('\n',0,m.start())+1,m[1],sorted(missing)))
 return count,skipped,issues
if __name__=='__main__':
 sql=(ROOT/'db/reference/current-schema.sql').read_text()+'\n'+'\n'.join(p.read_text() for p in sorted((ROOT/'db/migrations').glob('*.proposed.sql')))
 paths=list((ROOT/'src/main/java/com/kuaixietech').rglob('*.java'))+list((ROOT/'src/main/resources/mapper').rglob('*.xml'))
 count,skipped,issues=audit(required_columns(sql),[(str(p.relative_to(ROOT)),p.read_text()) for p in paths])
 print(f'{count} literal INSERTs checked; {skipped} dynamic lists skipped; {len(issues)} missing required-column findings; no database connection')
 for name,line,table,missing in issues:print(f'{name}:{line}: {table}: '+', '.join(missing))
 raise SystemExit(1 if issues else 0)
