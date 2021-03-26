package com.test;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.fastjson.JSON;
import table.Cell;
import util.ConsoleTable;

import java.util.*;

public class Test {

    public static void main(String[] args) {
        String sql = "update  lz_test_user set username=？ WHERE username=? AND  IS_DELETE = 0";

        // 新建 MySQL Parser
        SQLStatementParser parser = new MySqlStatementParser(sql);
        // 使用Parser解析生成AST，这里SQLStatement就是AST
        SQLStatement statement = parser.parseStatement();
        // 使用visitor来访问AST
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        statement.accept(visitor);
        // 从visitor中拿出你所关注的信息
        Collection<TableStat.Column> list = visitor.getColumns();

        List<Cell> header = new ArrayList<Cell>() {{
            add(new Cell("tableName"));
            add(new Cell("column"));
            add(new Cell("select"));
            add(new Cell("where"));
            add(new Cell("join"));
            add(new Cell("groupBy"));
            add(new Cell("having"));
            add(new Cell("attributes"));
            add(new Cell("fullName"));
            add(new Cell("primaryKey"));
            add(new Cell("unique"));
            add(new Cell("dataType"));
        }};
        List<List<Cell>> body = new ArrayList<List<Cell>>();
        for(TableStat.Column column: list){
           List<Cell> cells   = new ArrayList<Cell>();
            cells.add(new Cell(column.getTable()));
            cells.add(new Cell(column.getName()));
            cells.add(new Cell(column.isSelect() + ""));
            cells.add(new Cell(column.isWhere() +""));
            cells.add(new Cell(column.isJoin() +""));
            cells.add(new Cell(column.isGroupBy()+""));
            cells.add(new Cell(column.isHaving()+""));
            cells.add(new Cell(JSON.toJSONString(column.getAttributes())));
            cells.add(new Cell(column.getFullName() +""));
            cells.add(new Cell(column.isPrimaryKey() +""));
            cells.add(new Cell(column.isUnique() +""));
            cells.add(new Cell(column.getDataType() +""));
            body.add(cells);

          //  System.out.println(JSON.toJSONString(column));
        }
        String a = new ConsoleTable.ConsoleTableBuilder().addHeaders(header).addRows(body).build().toString();
        System.out.println(a);

    }


}
