package com.test;

import com.lz.druid.sql.ast.SQLStatement;
import com.lz.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.lz.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.lz.druid.sql.parser.SQLStatementParser;
import com.lz.druid.stat.TableStat;
import table.Cell;
import util.ConsoleTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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


    @org.junit.Test
    public void test7() {
        String sql = "    SELECT COUNT(1)\n" +
                "        FROM lt_sms_send_log lssl left join lt_sms_template lst on lst.id = lssl.template_id\n" +
                "        WHERE lssl.is_delete = 0\n" +
                "        AND lssl.phones = ?\n" +
                "        AND lssl.status = 0\n" +
                "        AND lssl.gmt_create   >= ?\n" +
                "        AND lssl.gmt_create   <=  ? \n" +
                "        AND lst.name=?";
        printSql(sql);
    }


    @org.junit.Test
    public void test8() {
        String sql = "   SELECT COUNT(1)\n" +
                "        FROM lt_sms_send_log lssl left join lt_sms_template lst on lst.id = lssl.template_id\n" +
                "        WHERE lssl.is_delete = 0\n" +
                "        AND lssl.phones = ? \n" +
                "        AND lssl.status = 0 \n" +
                "        AND lssl.gmt_create   >= ? \n" +
                "        AND lssl.gmt_create  <= ? \n" +
                "        AND lst.name not in ('SMS_LOGIN_REGISTER_CODE') ";
        printSql(sql);
    }


    @org.junit.Test
    public void test9() {
        String sql = "   SELECT\n" +
                "        lsb.id rid, lsb.gmt_create, lsb.gmt_modified, lsb.gmt_repay, lsb.borrow_no, lsb.unique_code, lsb.borrow_nper,\n" +
                "        lsb.borrow_use, lsb.amount, lsb.status, lsb.review_status, lsb.risk_order_no, lsb.arrival_amount, lsb.gmt_arrival,\n" +
                "        lsb.ups_no, lsb.gmt_first_repayment, lsb.gmt_plan_repayment, lsb.card_number, lsb.card_name, lsb.gmt_close,\n" +
                "        lsb.close_reason, lsb.overdue_status, lsb.current_overdue_status, lsb.overdue_day, lsb.rate_amount, lsb.overdue_amount,\n" +
                "        lsb.repay_amount, lsb.sum_rate, lsb.penal_sum, lsb.sum_overdue, lsb.sum_rebate, lsb.is_early_settlement, lsb.remark,\n" +
                "        lsb.differ_amount, lsb.nper_amount, lsb.paid_nper, lsb.overdue_nper, lsb.credit_rate, lsb.credit_overdue, lsb.credit_capital,\n" +
                "        lsb.latitude, lsb.longitude, lsb.province, lsb.city, lsb.county, lsb.address, lsb.success_num, lsb.year_rate, lsb.overdue_rate,\n" +
                "        lsb.supplier_code, lsb.supplier_order_no, lsb.app_type, lsb.contract_no, lsb.bank_rate, lsb.spv_rate, lsb.prem_rate, lsb.extend_json\n" +
                "        FROM\n" +
                "        lt_stage_borrow lsb LEFT JOIN\n" +
                "        lt_user_account lua ON\n" +
                "        lsb.unique_code = lua.unique_code\n" +
                "        AND lsb.supplier_code = lsb.supplier_code\n" +
                "        WHERE lua.is_delete = 0 AND lsb.is_delete = 0 AND lsb.unique_code = ? and lsb.review_status IN (-1, -2) ORDER BY lsb.gmt_create DESC LIMIT 1  ";
        printSql(sql);
    }

    @org.junit.Test
    public void test10() {
        String sql = " SELECT id as rid,borrow_no,amount,borrow_nper as total_nper,gmt_arrival as gmt_borrow,year_rate,\n" +
                "        gmt_plan_repayment gmt_plan_repay,contract_no,current_overdue_status,gmt_arrival, supplier_order_no,\n" +
                "        rate_amount,supplier_code supplierCode,zlx_label zlxLabel,premium\n" +
                "        FROM lt_stage_borrow sb WHERE is_delete = 0 AND status in (?,?) AND unique_code = ?  ";
        printSql(sql);
    }

    @org.junit.Test
    public void test11() {
        String sql = "  SELECT COUNT(1) FROM lt_repayment WHERE is_delete = 0 and unique_code = ?\n" +
                "            AND DATE_FORMAT(gmt_create,'%Y-%m-%d')   >= DATE_FORMAT(?,'%Y-%m-%d')\n" +
                "            AND DATE_FORMAT(gmt_create,'%Y-%m-%d')  <=  DATE_FORMAT(?,'%Y-%m-%d')\n" +
                "        and borrow_ids in (SELECT id FROM lt_stage_borrow WHERE is_delete = 0 AND unique_code = ?) ";
        printSql(sql);
    }

    @org.junit.Test
    public void test12() {
        String sql = "   update lt_repayment set gmt_modified=NOW(),status=?\n" +
                "            ,trade_no=?\n" +
                "            ,pay_trade_no=?\n" +
                "            ,repay_date =?\n" +
                "        where id=?";
        printSql(sql);
    }

    @org.junit.Test
    public void test13() {
        String sql = "   SELECT\n" +
                "        d.id,d.gmt_modified,d.repay_type,d.repay_no,d.unique_code,d.repayment_amount,d.actual_amount,\n" +
                "        d.capital_amount,d.rate_amount,d.overdue_amount,d.breach_amount,d.penal_sum,d.borrow_id,d.bill_id,\n" +
                "        d.repayment_id,d.pay_trade_no,d.trade_no,d.user_coupon_id,d.coupon_amount,d.rebate_amount,\n" +
                "        d.card_number,d.card_name,d.bank_capital,d.bank_rate,d.prem_amount,d.insurance_amount,\n" +
                "        d.income_amount,d.differ_amount,d.coupon_no,d.wd_amount,\n" +
                "        re.repay_date as gmt_create\n" +
                "        FROM lt_repayment_detail d\n" +
                "        LEFT JOIN lt_repayment re ON d.repay_no = re.repay_no\n" +
                "        where d.is_delete = 0 AND d.bill_id = ? ";
        printSql(sql);
    }


    @org.junit.Test
    public void test14() {
        String sql = "   SELECT *  FROM lt_repayment WHERE is_delete = 0 and unique_code = ?\n" +
                "            AND DATE_FORMAT(gmt_create,'%Y-%m-%d')   >=  DATE_FORMAT(?,'%Y-%m-%d')\n" +
                "            AND DATE_FORMAT(gmt_create,'%Y-%m-%d')  <=  DATE_FORMAT(?,'%Y-%m-%d')\n" +
                "        and borrow_ids in (SELECT id FROM lt_stage_borrow WHERE is_delete = 0 AND unique_code = ?)\n" +
                "        ORDER BY gmt_create DESC\n" +
                "        LIMIT ?,?";
        printSql(sql);
    }


    @org.junit.Test
    public void test15() {
        String sql = "   SELECT\n" +
                "        la.id,la.gmt_create,la.gmt_modified,la.creator,la.modifier,la.name,la.image_url,la.column_id,la.url,\n" +
                "        la.status,la.gmt_valid_start,la.gmt_valid_end,la.img_desc,la.sort,la.img_name\n" +
                "        FROM lt_advertisement la LEFT JOIN lt_column lc ON la.column_id = lc.id AND lc.column_no = ? \n" +
                "        WHERE la.is_delete = 0 AND lc.is_delete = 0 AND la.status = 1\n" +
                "        AND gmt_valid_start <=  NOW() AND gmt_valid_end  >=   NOW()\n" +
                "            AND la.name = ? \n" +
                "        ORDER BY sort ASC";
        printSql(sql);
    }


    @org.junit.Test
    public void test16() {
        String sql = "     SELECT COUNT(1) from lt_bank lb LEFT JOIN lt_bank_safe lbs\n" +
                "        ON lb.id = lbs.bank_id\n" +
                "        WHERE lb.bank_code = ? AND lb.is_delete = 0 AND lbs.is_delete = 0 and  lb.pay_channel = ? \n" +
                "        AND (lb.is_valid = 0 OR (lbs.gmt_start_time  <=  now() AND lbs.gmt_end_time  >=   now()))\n";
        printSql(sql);
    }


    @org.junit.Test
    public void test17() {
        String sql = "   SELECT * from lt_bank where is_delete = 0 AND is_valid = 1 and  pay_channel = ?\n" +
                "        AND id NOT IN (SELECT bank_id from lt_bank_safe where is_delete = 0 AND  pay_channel =? \n" +
                "        AND gmt_start_time  <=  ? AND gmt_end_time   >=  ? ) \n";
        printSql(sql);
    }

    @org.junit.Test
    public void test19() {
        String sql = "   SELECT\n" +
                "        COUNT(*)\n" +
                "        FROM\n" +
                "        lt_bank_safe\n" +
                "        WHERE\n" +
                "        NOW()  >=  gmt_start_time\n" +
                "        AND NOW()  <  gmt_end_time\n" +
                "        AND bank_id = ? \n" +
                "        AND is_delete = 0 ";
        printSql(sql);
    }

    @org.junit.Test
    public void test20() {
        String sql = "   SELECT policy_no as policyNo, begin_effect_time as beginEffectTime FROM lt_policy WHERE " +
                " subCode = '200' and date_add(begin_effect_time,INTERVAL 1 DAY)   <=   now() AND order_no = ? ";
        printSql(sql);
    }

  @org.junit.Test
    public void test21() {
        String sql = "  UPDATE lt_quota_detail SET  used_amount=used_amount1 + ? ,nums=nums+?\n" +
                "        WHERE username = ? \n" +
                "        AND is_delete = 0 \n" +
                "        AND (used_amount + ? <= ?) ";
        printSql(sql);
    }


  @org.junit.Test
    public void test22() {
        String sql = " SELECT * from lt_xs_dc_bill where is_delete = 0 and  margin_type = 'B' and borrow_id = ? limit 1 ";
        printSql(sql);
    }

    @org.junit.Test
    public void test23() {
        String sql = " UPDATE lt_quota_detail\n" +
                "        SET\n" +
                "        gmt_modified = NOW(),\n" +
                "        used_amount = used_amount - ?,\n" +
                "        nums=?-?\n" +
                "        WHERE date =?\n" +
                "        AND is_delete = 0\n" +
                "        AND supplier_code = ?\n" +
                "        AND quota_type =?\n" +
                "        AND rate_type =? ";
        printSql(sql);
    }


    @org.junit.Test
    public void test24() {
        String sql = " UPDATE lt_quota_detail\n" +
                "        SET\n" +
                "        gmt_modified = NOW(),\n" +
                "        used_amount = used_amount - ? ,\n" +
                "        nums=nums-1\n" +
                "        WHERE id = ? \n" +
                "        AND is_delete = 0";
        printSql(sql);
    }

    @org.junit.Test
    public void test25() {
        String sql = "  SELECT\n" +
                "        *\n" +
                "        FROM lt_quota_detail\n" +
                "        where is_delete = 0\n" +
                "        AND date = ? \n" +
                "        AND supplier_code = ? \n" +
                "        AND quota_type = ? \n" +
                "        AND rate_type = ? \n" +
                "        limit 0,1 ";
        printSql(sql);
    }

    @org.junit.Test
    public void test26() {
        String sql = "  SELECT\n" +
                "        *\n" +
                "        FROM lt_quota_detail\n" +
                "        WHERE is_delete= 0\n" +
                "        AND supplier_code = ? \n" +
                "        AND quota_type = ? \n" +
                "        AND rate_type = ? \n" +
                "        AND date(date) > date(?)\n" +
                "        AND amount >=  ? + used_amount\n" +
                "        ORDER BY date ASC\n" +
                "        limit 0,1";
        printSql(sql);
    }


    @org.junit.Test
    public void test27() {
        String sql = "   SELECT\n" +
                "        *\n" +
                "        FROM lt_rate_type\n" +
                "        WHERE is_delete = 0 AND supplier_code = ? \n" +
                "        AND gmt_start   <=  ?  NOW() AND gmt_end   >=   NOW() ";
        printSql(sql);
    }

    @org.junit.Test
    public void test28() {
        String sql = "   SELECT\n" +
                "        * \n" +
                "        FROM lt_app_upgrade\n" +
                "        WHERE is_delete = 0\n" +
                "        AND status = 1 AND type =?  AND app_code= ? \n" +
                "        AND\n" +
                "        ?  BETWEEN min_version AND max_version\n" +
                "        ORDER BY version_code DESC\n" +
                "        LIMIT 1 ";
        printSql(sql);
    }




    @org.junit.Test
    public void test29() {
        String sql = "   update lt_channel_list cl,lt_user_phone up\n" +
                "        set cl.freeze_status = 1, cl.gmt_modified = now()\n" +
                "        where cl.mobile = up.user_name and up.unique_code = ? \n" +
                "        and up.is_delete = 0 and cl.is_delete = 0; ";
        printSql(sql);
    }


    @org.junit.Test
    public void test30() {
        String sql = "    UPDATE lt_user_bankcard SET gmt_modified = NOW(),is_main = ? \n" +
                "        WHERE unique_code = ? and card_number = ? and status =? ";
        printSql(sql);
    }

    @org.junit.Test
    public void test31() {
        String sql = " SELECT\n" +
                "        lub.id,lub.gmt_create,lub.gmt_modified,lub.unique_code,lub.mobile,lub.bank_code,lub.card_number,lub.is_main,lub.status,\n" +
                "        lb.bank_icon,lb.bank_name,lb.is_valid,lb.invalid_desc,lb.baofoo_is_valid\n" +
                "        from lt_user_account lua\n" +
                "        LEFT JOIN lt_user_bankcard lub\n" +
                "        on lua.unique_code = lub.unique_code\n" +
                "        LEFT JOIN lt_fund_supplier lfs\n" +
                "        on lua.supplier_code = lfs.supplier_code and lub.pay_channel = lfs.pay_channel\n" +
                "        LEFT JOIN lt_bank lb\n" +
                "        ON ? = lub.bank_code\n" +
                "        where lua.is_delete = 0 and lub.is_delete = 0 AND lfs.is_delete = 0 AND lb.is_delete = 0\n" +
                "        AND lua.unique_code = ? AND lub.is_main = 1 and lub.`status` = 1 LIMIT 1  ";
        printSql(sql);
    }

    @org.junit.Test
    public void test32() {
        String sql = "  SELECT\n" +
                "          t1.bank_code bankCode,\n" +
                "          t1.mobile mobile,\n" +
                "          t1.bank_name bankName,\n" +
                "          t1.card_number cardNumber,\n" +
                "          (select t2.bank_icon from lt_bank t2 WHERE t2.bank_code = t1.bank_code and t2.is_delete=0 and t2.pay_channel = ? limit 1) bankIcon,\n" +
                "          t1.pay_channel payChannel\n" +
                "        FROM lt_user_bankcard t1\n" +
                "        WHERE t1.is_delete = 0 and t1.status = 1 and t1.pay_channel   <>  ? and t1.unique_code = ? GROUP BY t1.card_number\n";
        printSql(sql);
    }

    @org.junit.Test
    public void test33() {
        String sql = "  " +
                "SELECT\n" +
                "        t1.bank_code bankCode,\n" +
                "        t1.mobile mobile,\n" +
                "        t1.bank_name bankName,\n" +
                "        t1.card_number cardNumber,\n" +
                "        t2.bank_icon bankIcon,\n" +
                "        t2.bank_name bankName,\n" +
                "        t1.id rid,\n" +
                "        t2.id bankId,\n" +
                "        t2.is_valid isValid,\n" +
                "        t2.invalid_desc invalidDesc,\n" +
                "        t1.pay_channel payChannel\n" +
                "        FROM lt_user_bankcard t1 left join lt_bank t2 on t1.bank_code = ?  and  t2.pay_channel = t1.pay_channel and t2.is_delete = 0\n" +
                "        WHERE t1.is_delete = 0 and t1.status = 1 and t1.pay_channel =? and t1.unique_code = ?\n";
        printSql(sql);
    }

    @org.junit.Test
    public void test34() {
        String sql = "   select * from lt_user_bankcard lub\n" +
                "        left join lt_fund_supplier lfs on lub.pay_channel = lfs.pay_channel and lub.unique_code = ? \n" +
                "        where lfs.supplier_code = ? and lub.is_delete = 0 and lfs.is_delete = 0 and lub.is_main = 1\n" +
                "        limit 1 ";
        printSql(sql);
    }

    @org.junit.Test
    public void test35() {
        String sql = "  select lub.card_number cardNumber,lub.mobile mobile,lub.bank_code bankCode,\n" +
                "        lub.bank_name bankName,lb.bank_icon bankIcon, lub.card_seq cardSeq, lub.is_sign_agreementpay signStatus\n" +
                "        from lt_user_bankcard lub\n" +
                "        left join lt_fund_supplier lfs on lub.pay_channel = lfs.pay_channel and lub.unique_code = ? \n" +
                "        left join lt_bank lb on lub.bank_code = lb.bank_code and lb.pay_channel = lub.pay_channel\n" +
                "        where lfs.supplier_code = ? and lub.is_delete = 0 and lfs.is_delete = 0 and lub.status =? \n ";
        printSql(sql);
    }
    @org.junit.Test
    public void test36() {
        String sql = "   SELECT\n" +
                "        COUNT(a.id)\n" +
                "        FROM\n" +
                "        riskt_aichong_orders AS a\n" +
                "        LEFT JOIN riskt_aichong_order_info AS b ON a.id = b.order_id\n" +
                "        WHERE\n" +
                "        b.status = 0\n" +
                "        AND a.order_no =? \n" +
                "        AND DATE_FORMAT(a.order_time, '%Y-%m') = ? ";
        printSql(sql);
    }

    @org.junit.Test
    public void test37() {
        String sql = "   SELECT\n" +
                "         DATE_FORMAT(a.order_time,'%Y-%m') AS order_time,\n" +
                "        COUNT(a.id) AS order_sum,\n" +
                "        SUM( CASE\n" +
                "        WHEN a.refunded = 'N' THEN\n" +
                "        1\n" +
                "        ELSE\n" +
                "        0\n" +
                "        END\n" +
                "        ) AS success_order,\n" +
                "        SUM(\n" +
                "        CASE\n" +
                "        WHEN a.refunded = 'N' THEN\n" +
                "        total_price\n" +
                "        ELSE\n" +
                "        0\n" +
                "        END\n" +
                "        ) AS success_price,\n" +
                "        SUM(\n" +
                "        CASE\n" +
                "        WHEN a.refunded = 'Y' THEN\n" +
                "        1\n" +
                "        ELSE\n" +
                "        0\n" +
                "        END\n" +
                "        )  AS refunded_order,\n" +
                "      SUM(\n" +
                "        CASE\n" +
                "        WHEN a.refunded = 'Y' THEN\n" +
                "        total_price\n" +
                "        ELSE\n" +
                "        0\n" +
                "        END\n" +
                "        ) AS refunded_price\n" +
                "        FROM\n" +
                "        riskt_baibu_orders AS a\n" +
                "        WHERE\n" +
                "        a.order_no =? \n" +
                "       GROUP BY\n" +
                "         DATE_FORMAT(a.order_time,'%Y-%m')\n" +
                "        ORDER BY  DATE_FORMAT(a.order_time,'%Y-%m') DESC\n" +
                "        LIMIT 6  ";
        printSql(sql);
    }

    @org.junit.Test
    public void test38() {
        String sql = "  SELECT\n" +
                "        COUNT(a.id)\n" +
                "        FROM\n" +
                "        riskt_baibu_orders AS a\n" +
                "        LEFT JOIN riskt_baibu_order_info AS b ON a.id = b.order_id\n" +
                "        WHERE\n" +
                "        b.status = 0\n" +
                "        AND a.order_no = ? \n" +
                "        AND DATE_FORMAT(a.order_time, '%Y-%m') = ?  ";
        printSql(sql);
    }

    @org.junit.Test
    public void test39() {
        String sql = "   insert into arc_borrow \n" +
                "        \t(order_no,consumer_no,user_name,cert_type,cert_no) \n" +
                "        values\n" +
                "        \t(?,?,?,?,?)\n ";
        printSql(sql);
    }

    @org.junit.Test
    public void test40() {
        String sql = "   update arc_borrow set\n" +
                "            order_no = ?,\n" +
                "            consumer_no = ?,\n" +
                "            user_name = ? \n" +
                "        where id = ? ";
        printSql(sql);
    }

 @org.junit.Test
    public void test41() {
        String sql = "          SELECT\n" +
                "        consumerNo\n" +
                "        FROM\n" +
                "            (\n" +
                "                SELECT\n" +
                "                    *\n" +
                "                FROM\n" +
                "                    `arc_borrow_pay_info`\n" +
                "                WHERE\n" +
                "                    `consumerNo` IN (?,?)\n" +
                "        and `status` IN (0,1,2,4)\n" +
                "        and   update_time>DATE_SUB(curdate(),INTERVAL  ? DAY)\n" +
                "                ORDER BY\n" +
                "                    id DESC\n" +
                "            ) tb\n" +
                "        GROUP BY\n" +
                "            `consumerNo` ";
        printSql(sql);
    }


 @org.junit.Test
    public void test42() {
        String sql = "         select * \n" +
                "        from arc_borrow_pay_info\n" +
                "        where consumerNo = ? \n" +
                "        and (status = 0 or status = 1)\n" +
                "\n" +
                "            and (scene = '100' or scene = '101' or scene = '102' or scene = '103' or scene = '50')\n" +
                "\n" +
                "            and (scene = '110' or scene = '111' or scene = '112' or scene = '113')\n" +
                "\n" +
                "        order by id  ";
        printSql(sql);
    }


 @org.junit.Test
    public void test43() {
        String sql = "         select * , \n" +
                "        (select d.item_value from arc_sys_dict_detail d LEFT JOIN arc_sys_dict s on d.parent_id  = s.id\n" +
                "\t\twhere s.type_code = 'PRODUCT_TYPE' and d.item_code = borrow_type) as borrowTypeStr\n" +
                "        from arc_borrow\n" +
                "          where \n" +
                "                 order_no like CONCAT(CONCAT('%', ?), '%')\n" +
                "                and user_name like CONCAT(CONCAT('%', ?), '%')\n" +
                "                and real_name like CONCAT(CONCAT('%',? ), '%')\n" +
                "                and phone like CONCAT(CONCAT('%', ? ), '%')\n";
        printSql(sql);
    }



    // todo quyixiao
 @org.junit.Test
    public void test44() {
        String sql = "   SELECT * FROM lz_test_user WHERE  real_name LIKE CONCAT('%',?,'%') AND  IS_DELETE = 0  ";
        printSql(sql);
    }


    @org.junit.Test
    public void test45() {
        String sql = "   SELECT\n" +
                "            COUNT(*) AS order_size,\n" +
                "            SUM(amount) AS amount_sum\n" +
                "        FROM\n" +
                "            riskt_bailin_electricity_orders\n" +
                "        WHERE\n" +
                "            (\n" +
                "                order_status = '待付款'\n" +
                "                OR order_status = '待发货'\n" +
                "                OR order_status = '已完成'\n" +
                "            )\n" +
                "        AND state = 10\n" +
                "        AND consumer_no=?    ";
        printSql(sql);
    }


    @org.junit.Test
    public void test46() {
        String sql = "  SELECT\n" +
                "            COUNT(*) AS refund_count,\n" +
                "            SUM(refund_amount) AS refund_amount\n" +
                "        FROM\n" +
                "            `riskt_bailin_electricity_orders`\n" +
                "        WHERE\n" +
                "            refund_success = 1\n" +
                "        AND state = 10\n" +
                "        AND consumer_no=?   ";
        printSql(sql);
    }
  @org.junit.Test
    public void test47() {
        String sql = "     SELECT\n" +
                "\ttb.order_time,\n" +
                "\ttb.order_sum - tb.refunded_order AS success_order,\n" +
                "\ttb.amount_sum - tb.refunded_price AS success_price,\n" +
                "\ttb.order_sum,\n" +
                "\ttb.refunded_order,\n" +
                "\ttb.refunded_price\n" +
                "FROM\n" +
                "\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tDATE_FORMAT(a.order_time, '%Y-%m') AS order_time,\n" +
                "\t\t\tSUM(\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\tWHEN a.order_status = '待付款'\n" +
                "\t\t\t\tOR a.order_status = '待收货'\n" +
                "\t\t\t\tOR a.order_status = '已完成' THEN\n" +
                "\t\t\t\t\t1\n" +
                "\t\t\t\tELSE\n" +
                "\t\t\t\t\t0\n" +
                "\t\t\t\tEND\n" +
                "\t\t\t) AS order_sum,\n" +
                "\t\t\tSUM(\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\tWHEN a.order_status = '待付款'\n" +
                "\t\t\t\tOR a.order_status = '待收货'\n" +
                "\t\t\t\tOR a.order_status = '已完成' THEN\n" +
                "\t\t\t\t\tamount\n" +
                "\t\t\t\tELSE\n" +
                "\t\t\t\t\t0\n" +
                "\t\t\t\tEND\n" +
                "\t\t\t) AS amount_sum,\n" +
                "\t\t\tSUM(\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\tWHEN a.refund_success = 1 THEN\n" +
                "\t\t\t\t\t1\n" +
                "\t\t\t\tELSE\n" +
                "\t\t\t\t\t0\n" +
                "\t\t\t\tEND\n" +
                "\t\t\t) AS refunded_order,\n" +
                "\t\t\tSUM(\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\tWHEN a.refund_success = 1 THEN\n" +
                "\t\t\t\t\tamount\n" +
                "\t\t\t\tELSE\n" +
                "\t\t\t\t\t0\n" +
                "\t\t\t\tEND\n" +
                "\t\t\t) AS refunded_price\n" +
                "\t\tFROM\n" +
                "\t\t\triskt_bailin_electricity_orders AS a\n" +
                "\t\tWHERE\n" +
                "\t\t\ta.order_no = ? \n" +
                "\t\tGROUP BY\n" +
                "\t\t\tDATE_FORMAT(a.order_time, '%Y-%m')\n" +
                "\t\tORDER BY\n" +
                "\t\t\tDATE_FORMAT(a.order_time, '%Y-%m') DESC\n" +
                "\t\tLIMIT 6\n" +
                "\t) tb  ";
        printSql(sql);
    }
  @org.junit.Test
    public void test48() {
        String sql = "   SELECT\n" +
                "        COUNT(a.id)\n" +
                "        FROM\n" +
                "        riskt_bailin_electricity_orders AS a\n" +
                "        LEFT JOIN riskt_bailin_electricity_orders_info AS b ON a.order_id = b.order_id\n" +
                "        WHERE\n" +
                "        b.refund_success = 0\n" +
                "        AND a.order_no = ? \n" +
                "        AND DATE_FORMAT(a.order_time, '%Y-%m') = ? ";
        printSql(sql);
    }

    @org.junit.Test
    public void test49() {
        String sql = "   SELECT DISTINCT(consumer_no) FROM `arc_event_add`  WHERE `bssid` =?  ";
        printSql(sql);
    }

   @org.junit.Test
    public void test50() {
        String sql = "    select\n" +
                "        event_field.tb_name,event_field.id,event_field.field_name,event_field.field_code,event_field.field_type,event_field.field_desc,event_field.data_type,event_field.create_user_id,event_field.create_user_name,event_field.state,event_field.remark,event_field.create_time,event_field.update_time\n" +
                "        from\n" +
                "        arc_event_manage manage\n" +
                "        left join\n" +
                "        arc_event_type_field type_field\n" +
                "        on type_field.type_id = manage.event_type_id\n" +
                "        left join\n" +
                "        arc_event_field event_field\n" +
                "        on type_field.field_id = event_field.id\n" +
                "        where manage.scene = ?  ";
        printSql(sql);
    }
    @org.junit.Test
    public void test51() {
        String sql = "     select ef.tb_name,ef.id,ef.field_name,ef.field_code,ef.field_type,ef.field_desc,ef.data_type,ef.create_user_id,ef.create_user_name,ef.state,ef.remark,ef.create_time,ef.update_time\n" +
                "        from arc_event_field ef left join arc_event_field_scene efs on ef.`id` = efs.`field_id` where ef.state = '10' and efs.state = '10' and scene = ? \n  ";
        printSql(sql);
    }
    @org.junit.Test
    public void test52() {
        String sql = "     SELECT\n" +
                "        manage.id,manage.scene,manage.event_name,manage.partner_id,manage.app_id,manage.product_id,manage.event_type_id,manage.opt_flag,manage.create_user_id,manage.create_user_name,manage.state,manage.remark,manage.create_time,manage.update_time,\n" +
                "        partner.partner_name,manage.is_decision,\n" +
                "        app.app_name,\n" +
                "        product.product_name,\n" +
                "        monitor.trigger_way,monitor.period\n" +
                "        FROM\n" +
                "        arc_event_manage manage\n" +
                "        LEFT JOIN\n" +
                "        arc_event_partner partner ON manage.partner_id = partner.id\n" +
                "        LEFT JOIN\n" +
                "        arc_event_app app ON manage.app_id = app.id\n" +
                "        LEFT JOIN\n" +
                "        arc_event_product product ON manage.product_id = product.id\n" +
                "        LEFT JOIN\n" +
                "        riskt_event_monitor monitor ON manage.scene = monitor.scene where \n" +
                "                 manage.event_type_id = ?\n" +
                "                and manage.event_name like concat('%',?,'%')\n" +
                "                and manage.product_id = ? \n" +
                "                and manage.scene =? \n" +
                "            and manage.state = 10\n" +
                "        order by manage.state asc,manage.id desc\n";
        printSql(sql);
    }

}
