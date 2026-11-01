import unittest
from audit_required_inserts import required_columns,audit
class RequiredInsertTests(unittest.TestCase):
 def test_missing_receipt_precision_is_rejected(self):
  schema=required_columns('CREATE TABLE t_receipts(id CHAR(36) PRIMARY KEY,payment_minor DECIMAL(24,0) NOT NULL,payment_minor_digits INT NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);')
  count,skipped,issues=audit(schema,[('receipts.java','INSERT INTO t_receipts(id,payment_minor) VALUES(?,?)')]);self.assertEqual(count,1);self.assertEqual(issues[0][3],['payment_minor_digits'])
 def test_default_auto_increment_and_nullable_columns_can_be_omitted(self):
  schema=required_columns("CREATE TABLE t_demo(id BIGINT AUTO_INCREMENT PRIMARY KEY,amount DECIMAL(12,2) NOT NULL,status VARCHAR(20) NOT NULL DEFAULT 'pending',note TEXT NULL);")
  self.assertEqual(audit(schema,[('x','INSERT INTO t_demo(amount) VALUES(?)')])[2],[])
 def test_table_primary_key_is_implicitly_required(self):
  schema=required_columns('CREATE TABLE t_demo(id CHAR(36),note TEXT,PRIMARY KEY(id));');self.assertEqual(schema['t_demo'],{'id'})
 def test_dynamic_column_list_is_not_claimed_checked(self):
  self.assertEqual(audit({'t_demo':{'id'}},[('x','INSERT INTO t_demo("+columns+") VALUES(?)')])[:2],(0,1))
 def test_decimal_commas_and_comment_text_do_not_change_required_fields(self):
  schema=required_columns("CREATE TABLE t_demo(id CHAR(36) NOT NULL COMMENT 'DEFAULT is just text',amount DECIMAL(12,2) DEFAULT 0,description TEXT NULL COMMENT 'comma, in a comment');");self.assertEqual(schema['t_demo'],{'id'})
if __name__=='__main__':unittest.main()
