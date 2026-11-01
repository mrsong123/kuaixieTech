import json
import re
import unittest
from pathlib import Path

from schema_comments import (tables,annotate,without_seed_data,COMMENT,mask_comments,
                             CATALOG,BASELINE,FULL,ANNOTATED,inventory,products)

def tokens(sql):
    clean=COMMENT.sub('',mask_comments(sql))
    return re.findall(r"'(?:''|\\.|[^'])*'|`[^`]+`|\w+|[^\s]",clean)

class SchemaCommentsTest(unittest.TestCase):
    def test_quotes_json_checks_and_generated_column_are_not_split(self):
        sql="CREATE TABLE x(id BIGINT PRIMARY KEY, v JSON CHECK(JSON_VALID(v)), active TINYINT GENERATED ALWAYS AS (CASE WHEN id IN(1,2) THEN 1 ELSE NULL END) STORED);"
        table=tables(sql)[0]
        self.assertEqual(['id','v','active'],[c['name'] for c in table['columns']])
        metadata={'x':{'comment':'示例表','columns':{'id':'主键','v':'JSON对象','active':'生成状态'}}}
        result=annotate(sql,metadata)
        self.assertEqual(tokens(sql),tokens(result))
        self.assertEqual(result,annotate(result,metadata))

    def test_quote_and_line_comment_escaping_preserves_definitions(self):
        sql="CREATE TABLE t(id INT DEFAULT 1, name VARCHAR(30) DEFAULT '--a,b' -- old\n);"
        metadata={'t':{'comment':"表的'说明",'columns':{'id':'编号','name':"用户的'名称\\路径"}}}
        result=annotate(sql,metadata)
        self.assertEqual(tokens(sql),tokens(result))
        self.assertTrue(all(c['comment'] for c in tables(result)[0]['columns']))

    def test_seed_removal_keeps_triggers_and_does_not_remove_keywords_in_strings(self):
        sql="CREATE TABLE a(v VARCHAR(30) DEFAULT 'INSERT INTO x;');\nINSERT INTO a VALUES('x;y');\nDELIMITER //\nCREATE TRIGGER tr BEFORE INSERT ON a FOR EACH ROW BEGIN SET @x=1; END//\nDELIMITER ;"
        result,count=without_seed_data(sql)
        self.assertEqual(1,count)
        self.assertIn("DEFAULT 'INSERT INTO x;'",result)
        self.assertIn('CREATE TRIGGER tr BEFORE INSERT ON a',result)
        self.assertNotIn("VALUES('x;y')",result)

    def test_real_schema_has_exact_complete_comment_catalog(self):
        catalog=json.loads(CATALOG.read_text())
        data=inventory()
        self.assertEqual(113,len(data))
        self.assertEqual(1186,sum(len(t['columns']) for t in data))
        self.assertEqual(set(catalog),{t['name'] for t in data})
        for table in data:
            self.assertEqual(set(catalog[table['name']]['columns']),{c['name'] for c in table['columns']})
            for value in [catalog[table['name']]['comment'],*catalog[table['name']]['columns'].values()]:
                self.assertRegex(value,r'[\u3400-\u9fff]')
                self.assertLessEqual(len(value),1000)

    def test_full_schema_includes_all_old_and_new_tables_and_no_seed_inserts(self):
        full=FULL.read_text()
        parsed=tables(full)
        self.assertEqual(113,len(parsed))
        self.assertEqual(1186,sum(len(t['columns']) for t in parsed))
        self.assertTrue(all(t['comment'] and all(c['comment'] for c in t['columns']) for t in parsed))
        self.assertFalse(re.search(r'^\s*INSERT(?:\s+IGNORE)?\s+INTO\b',mask_comments(full),re.M|re.I))
        self.assertIn('CREATE TRIGGER trg_t_reward_ledger_once',full)

    def test_original_reference_is_only_annotated_without_definition_changes(self):
        self.assertEqual(tokens(BASELINE.read_text()),tokens(ANNOTATED.read_text()))
        self.assertEqual(82,len(tables(ANNOTATED.read_text())))

    def test_generated_outputs_are_reproducible_without_writing_source(self):
        original=BASELINE.read_bytes()
        for path,content in products(json.loads(CATALOG.read_text())).items():
            self.assertEqual(path.read_text(),content,str(path))
        self.assertEqual(original,BASELINE.read_bytes())

if __name__=='__main__': unittest.main()
