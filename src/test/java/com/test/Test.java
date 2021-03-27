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

    public static void printSql(String sql) {
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
            add(new Cell("fullName"));
            add(new Cell("dataType"));
        }};
        List<List<Cell>> body = new ArrayList<List<Cell>>();
        for (TableStat.Column column : list) {
            List<Cell> cells = new ArrayList<Cell>();
            cells.add(new Cell(column.getTable()));
            cells.add(new Cell(column.getName()));
            cells.add(new Cell(column.isSelect() + ""));
            cells.add(new Cell(column.isWhere() + ""));
            cells.add(new Cell(column.isJoin() + ""));
            cells.add(new Cell(column.isGroupBy() + ""));
            cells.add(new Cell(column.isHaving() + ""));
            cells.add(new Cell(column.getFullName() + ""));
            cells.add(new Cell(column.getDataType() + ""));
            body.add(cells);
        }
        String a = new ConsoleTable.ConsoleTableBuilder().addHeaders(header).addRows(body).build().toString();
        System.out.println(a);
    }

    @org.junit.Test
    public void test() {
        String sql = "         SELECT DISTINCT(date(?)) FROM lt_borrow_bill\n" +
                "        where `unique_code` = ? AND status IN (?,?) and is_delete = 0 group by date(?) order by gmt_plan_repayment ";
        printSql(sql);
    }


    @org.junit.Test
    public void test1() {
        String sql = " SELECT bill_amount-credit_capital-credit_rate+overdue_amount+sum_overdue+breach_amount-credit_breach+differ_amount AS need_pay_bill_amount,\n" +
                "        gmt_plan_repayment,bill_nper,STATUS AS bill_status,overdue_status,bank_capital-credit_bank_capital AS capital_amount,\n" +
                "        bill_amount-bank_amount+overdue_amount+sum_overdue+breach_amount-credit_breach-credit_premium+differ_amount AS premium,\n" +
                "        bill_amount-bank_amount-sum_premium-credit_premium+overdue_amount+breach_amount-sum_breach-credit_premium AS need_pay_premium,\n" +
                "        repayment_amount AS paid_amount,bank_rate-credit_bank_rate AS rate_amount, gmt_repay\n" +
                "        FROM lt_borrow_bill WHERE is_delete = 0 AND borrow_id =?";
        printSql(sql);
    }

    @org.junit.Test
    public void test2() {
        String sql = " SELECT bill_amount-credit_capital-credit_rate+overdue_amount+sum_overdue+breach_amount-credit_breach+differ_amount AS need_pay_bill_amount,\n" +
                "        gmt_plan_repayment,bill_nper,STATUS AS bill_status,overdue_status, capital_amount-credit_capital as capital_amount,\n" +
                "        repayment_amount AS paid_amount, (rate_amount-credit_rate) as rate_amount, gmt_repay, (rate_amount-credit_rate) isEarlySettlementRateAmount,\n" +
                "        is_early_settlement isEarlySettlement,penal_sum penalSum,(overdue_amount+sum_overdue+breach_amount-credit_breach) overdueAmount\n" +
                "        FROM lt_borrow_bill WHERE is_delete = 0 AND borrow_id = ?";
        printSql(sql);
    }

    @org.junit.Test
    public void test3() {
        String sql = "   UPDATE lt_code_record SET gmt_modified = NOW(),is_check = 1 WHERE id=?";
        printSql(sql);
    }

    @org.junit.Test
    public void test4() {
        String sql = " SELECT\n" +
                "        *\n" +
                "        FROM lt_code_record WHERE is_delete = 0 AND send_account=?\n" +
                "        AND type=? ORDER BY ID DESC LIMIT 1";
        printSql(sql);
    }

    @org.junit.Test
    public void test5() {
        String sql = "  SELECT\n" +
                "        COUNT(*)\n" +
                "        FROM lt_code_record\n" +
                "        WHERE is_delete = 0\n" +
                "        AND send_account=?\n" +
                "        AND type=?\n" +
                "        AND gmt_create  >=  \n" +
                "        CAST(CAST(SYSDATE()AS DATE)AS DATETIME) ";
        printSql(sql);
    }



    @org.junit.Test
    public void test6() {
        String sql = "   SELECT SUM(amount) FROM lt_expect_lend_time\n" +
                "        WHERE is_delete=0\n" +
                "        AND supplier_code = ?\n" +
                "        AND quota_type = ?\n" +
                "            AND rate_type = ?\n" +
                "        AND DATE_FORMAT(expect_time,'%Y-%m-%d')=DATE_FORMAT(?,'%Y-%m-%d') ";
        printSql(sql);
    }


}
