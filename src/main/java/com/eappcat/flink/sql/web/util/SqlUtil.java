package com.eappcat.flink.sql.web.util;

import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.flink.sql.parser.impl.FlinkSqlParserImpl;
import org.apache.flink.sql.parser.validate.FlinkSqlConformance;

import static org.apache.calcite.avatica.util.Quoting.BACK_TICK;

/**
 * SQL验证相关函数
 * @author Xuan Yue Bo
 */
public class SqlUtil {

    public static Boolean isValidSql(String sql) {
        try {
            parseSql(sql);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // check if the sql is a valid sql. cannot check UDF code.
    public static SqlNodeList parseSql(String sql) throws Exception {
        SqlParser parser = SqlParser.create(sql, SqlParser.configBuilder()
                .setParserFactory(FlinkSqlParserImpl.FACTORY)
                .setQuoting(BACK_TICK)
                .setUnquotedCasing(Casing.TO_UPPER)
                .setQuotedCasing(Casing.UNCHANGED)
                .setConformance(FlinkSqlConformance.DEFAULT)
                .build()
        );
        return parser.parseStmtList();
    }


    public static void main(String[] args) {
        String sql = "DROP TABLE IF EXISTS test;\n\nCREATE TABLE test (\n    id  INT,\n    content STRING,\n    dt TIMESTAMP(3),\n    primary key(id)  NOT ENFORCED\n) WITH (\n  'connector.type' = 'jdbc',\n  'connector.url' = 'jdbc:mysql://192.168.9.138:3306/kdbd?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true',\n  'connector.username' = 'kdbd',\n  'connector.password' = 'kdbd',\n  'connector.driver' = 'com.mysql.jdbc.Driver',\n  'connector.table' = 'test',\n  'connector.write.flush.interval' = '10s'\n);\n\nDROP TABLE IF EXISTS test2;\n\nCREATE TABLE test2 (\n    id  INT,\n    content STRING,\n    dt TIMESTAMP(3),\n    primary key(id)  NOT ENFORCED\n) WITH (\n  'connector.type' = 'jdbc',\n  'connector.url' = 'jdbc:mysql://192.168.9.138:3306/kdbd?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true',\n  'connector.username' = 'kdbd',\n  'connector.password' = 'kdbd',\n  'connector.driver' = 'com.mysql.jdbc.Driver',\n  'connector.table' = 'test2',\n  'connector.write.flush.interval' = '10s'\n);\n\n\nINSERT INTO test2 (id,content,dt)\nSELECT * FROM test;\n\n";
//        String sql = "select * from tbl;";

        try {
            SqlNodeList sqls = parseSql(sql);
            System.out.println(sqls.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
