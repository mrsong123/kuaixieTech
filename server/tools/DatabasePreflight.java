import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Source-file utility: only JDBC metadata reads; never imports DDL or queries business rows.
 * Run from server/ after audit_schema.py --contract /tmp/kuaixie-contract.tsv.
 * java --class-path /path/to/mysql-connector-j.jar tools/DatabasePreflight.java /tmp/kuaixie-contract.tsv
 */
class DatabasePreflight {
  static String required(String name) {
    String value=System.getenv(name);
    if(value==null||value.isBlank()) throw new IllegalArgumentException("Configure "+name+" in environment settings");
    return value;
  }
  public static void main(String[] args) throws Exception {
    if(args.length!=1)throw new IllegalArgumentException("Provide the contract TSV path");
    String url=required("DB_URL");
    if(!url.startsWith("jdbc:mysql:"))throw new IllegalArgumentException("DB_URL must be a MySQL JDBC URL");
    Properties props=new Properties();
    props.setProperty("user",required("DB_USER"));
    props.setProperty("password",required("DB_PASSWORD"));
    props.setProperty("connectTimeout","10000");
    props.setProperty("socketTimeout","10000");
    try(Connection db=DriverManager.getConnection(url,props)) {
      db.setReadOnly(true);
      db.setAutoCommit(false);
      String catalog=db.getCatalog();
      if(catalog==null||Set.of("mysql","sys","information_schema","performance_schema").contains(catalog))
        throw new IllegalArgumentException("Select a business database in DB_URL");
      int tables=0,missing=0;
      for(String line:Files.readAllLines(Path.of(args[0]))) {
        String[] parts=line.split("\t",2);
        String table=parts[0];Set<String> columns=new HashSet<>();
        // JDBC treats underscores as wildcards. Exact TABLE_NAME comparison is essential.
        try(ResultSet rs=db.getMetaData().getColumns(catalog,null,table,null)) {
          while(rs.next())if(table.equals(rs.getString("TABLE_NAME")))columns.add(rs.getString("COLUMN_NAME"));
        }
        if(columns.isEmpty()){System.out.println("Missing table: "+table);missing++;continue;}
        tables++;
        for(String column:parts[1].split(","))if(!columns.contains(column)) {
          System.out.println("Missing column: "+table+"."+column);missing++;
        }
      }
      db.rollback();
      System.out.println("Metadata-only preflight: "+tables+" tables found, "+missing+" missing tables/columns");
      if(missing>0)System.exit(1);
    }catch(SQLException error) {
      // Driver messages can include endpoints; never print URL, usernames or credentials.
      System.err.println("Database preflight failed: SQLState="+error.getSQLState()+", code="+error.getErrorCode());
      System.exit(2);
    }
  }
}
