#!/usr/bin/env python3
"""Field-level delivery ledger. SQL mentions are evidence candidates, never acceptance.
--require-complete is intentionally nonzero until every schema field has an explicit,
reviewable business test receipt. No DB connection or writes are performed.
"""
import argparse
import csv
import json
from pathlib import Path
import re
from audit_schema import schema

SERVER=Path(__file__).resolve().parents[1]
REPO=SERVER.parent

def main():
 p=argparse.ArgumentParser();p.add_argument('--write',action='store_true');p.add_argument('--require-complete',action='store_true');args=p.parse_args()
 domains=json.loads((SERVER/'db/business/domains.json').read_text());owners={}
 for domain in domains:
  for table in domain['tables']:
   if table in owners:raise SystemExit('Duplicate business owner: '+table)
   owners[table]=domain
 tables={};origin={};descriptions={}
 for file in [SERVER/'db/reference/current-schema.sql',*sorted((SERVER/'db/migrations').glob('*.proposed.sql'))]:
  text=file.read_text();current=None
  for line in text.splitlines():
   declaration=re.search(r'CREATE TABLE(?: IF NOT EXISTS)? `?(t_\w+)`?',line)
   if declaration:current=declaration[1]
   field=re.match(r"\s*`?(\w+)`?\s+(?:CHAR|VARCHAR|TEXT|MEDIUMTEXT|LONGTEXT|INT|BIGINT|TINYINT|DECIMAL|JSON|DATETIME|TIMESTAMP|BOOLEAN|DATE)\b.*?COMMENT\s+'([^']*)'",line)
   if current and field:descriptions[current+'.'+field[1]]=field[2]
  for table,fields in schema(text).items():
   if table in tables:raise SystemExit('Duplicate schema: '+table)
   tables[table]=fields;origin[table]='supplied' if 'reference' in file.parts else 'proposed'
 if set(tables)!=set(owners):raise SystemExit('Unmapped tables: '+str(sorted(set(tables)-set(owners)))+'; stale mappings: '+str(sorted(set(owners)-set(tables))))
 # Entities and empty generated BaseMapper/ServiceImpl scaffolds do not count as executable SQL evidence.
 sources=[]
 for folder in [SERVER/'src/main/java',SERVER/'src/main/resources/mapper']:
  for file in folder.rglob('*'):
   if file.suffix not in {'.java','.xml'}:continue
   text=file.read_text();text=re.sub(r'/\*.*?\*/|<!--.*?-->', '',text,flags=re.S)
   if re.search(r'\b(?:SELECT|INSERT|UPDATE|DELETE)\b',text,re.I):sources.append((str(file.relative_to(REPO)),text))
 receipts_file=SERVER/'db/business/acceptance.json'
 receipts=json.loads(receipts_file.read_text()) if receipts_file.exists() else {}
 known_fields={table+'.'+field for table,fields in tables.items() for field in fields}
 if set(receipts)-known_fields:raise SystemExit('Acceptance references unknown fields: '+str(sorted(set(receipts)-known_fields)))
 rows=[];unverified=0
 for table,fields in sorted(tables.items()):
  domain=owners[table]
  candidates=[(file,text) for file,text in sources if re.search(r'\b'+re.escape(table)+r'\b',text)]
  for field in sorted(fields):
   evidence=[file for file,text in candidates if re.search(r'\b'+re.escape(field)+r'\b',text)]
   receipt=receipts.get(table+'.'+field,{})
   verified=all(receipt.get(key) for key in ('frontend','admin','backend','test','result')) and receipt.get('result')=='passed'
   if verified:
    # Acceptance must point to existing artifacts, not just an arbitrary passed flag.
    for key in ('frontend','admin','backend','test'):
     artifact=(REPO/receipt[key]).resolve()
     if not artifact.is_relative_to(REPO) or not artifact.is_file():raise SystemExit('Missing or external acceptance artifact: '+receipt[key])
   else:unverified+=1
   usage='system-derived' if field in {'id','created_at','updated_at','reviewed_at','paid_at','resolved_at'} else 'restricted' if re.search(r'password|secret|token|cipher|openid|unionid|id_card|passport|snapshot_url|storage_url',field) else 'business'
   rows.append({'table':table,'column':field,'description':descriptions.get(table+'.'+field,field),'usage':usage,'origin':origin[table],'domain':domain['id'],'frontendIntent':domain['frontend'],'adminIntent':domain['admin'],'sqlCandidates':evidence,'status':'verified' if verified else 'pending','acceptance':receipt})
 output={'tableCount':len(tables),'suppliedTableCount':sum(x=='supplied' for x in origin.values()),'fieldCount':len(rows),'unverifiedFieldCount':unverified,'note':'SQL candidates are not proof of endpoint reachability, field semantics or complete business behaviour. Pending fields must not be called complete.','domains':domains,'fields':rows}
 json_text=json.dumps(output,ensure_ascii=False,indent=2)+'\n'
 destination=SERVER/'src/main/resources/business-coverage.json'
 if args.write:
  destination.write_text(json_text)
  with (REPO/'docs/DATABASE_BUSINESS_COVERAGE.csv').open('w',newline='',encoding='utf-8-sig') as f:
   w=csv.writer(f);w.writerow(['表','字段','字段说明','使用方式','结构来源','业务域','前台业务目标','后台业务目标','候选 SQL 文件（非闭环证明）','验收状态'])
   for row in rows:w.writerow([row['table'],row['column'],row['description'],row['usage'],row['origin'],row['domain'],row['frontendIntent'],row['adminIntent'],';'.join(row['sqlCandidates']),row['status']])
 elif not destination.exists() or destination.read_text()!=json_text:raise SystemExit('Business coverage ledger stale; run with --write')
 print(f'{len(tables)} tables; {len(rows)} fields; {unverified} fields lack complete acceptance receipts')
 if args.require_complete and unverified:raise SystemExit(1)

if __name__=='__main__':main()
