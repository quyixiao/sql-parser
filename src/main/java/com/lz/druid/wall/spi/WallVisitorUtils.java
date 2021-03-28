package com.lz.druid.wall.spi;

import com.lz.druid.sql.ast.SQLExpr;
import com.lz.druid.sql.ast.SQLName;
import com.lz.druid.sql.ast.SQLObject;
import com.lz.druid.sql.ast.expr.*;
import com.lz.druid.sql.ast.statement.*;
import com.lz.druid.sql.ast.expr.*;
import com.lz.druid.sql.ast.statement.*;
import com.lz.druid.sql.visitor.SQLEvalVisitor;
import com.lz.druid.sql.visitor.SQLEvalVisitorUtils;
import com.lz.druid.sql.visitor.functions.Nil;
import com.lz.druid.util.FnvHash;
import com.lz.druid.util.JdbcUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import static com.lz.druid.sql.visitor.SQLEvalVisitor.EVAL_VALUE;

@Slf4j
public class WallVisitorUtils {


    public final static String   HAS_TRUE_LIKE = "hasTrueLike";

    private static ThreadLocal<WallConditionContext>    wallConditionContextLocal    = new ThreadLocal<WallConditionContext>();
    private static ThreadLocal<WallTopStatementContext> wallTopStatementContextLocal = new ThreadLocal<WallTopStatementContext>();

    public static WallConditionContext getWallConditionContext() {
        return wallConditionContextLocal.get();
    }



    public static boolean isSimpleCountTableSource(WallVisitor visitor, SQLSelect select) {
        SQLSelectQuery query = select.getQuery();

        if (query instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) query;

            boolean allawTrueWhere = false;

            if (queryBlock.getWhere() == null) {
                allawTrueWhere = true;
            } else {
                Object whereValue = getValue(visitor, queryBlock.getWhere());
                if (whereValue == Boolean.TRUE) {
                    allawTrueWhere = true;
                } else if (whereValue == Boolean.FALSE) {
                    return false;
                }
            }
            boolean simpleCount = false;
            if (queryBlock.getSelectList().size() == 1) {
                SQLExpr selectItemExpr = queryBlock.getSelectList().get(0).getExpr();
                if (selectItemExpr instanceof SQLAggregateExpr) {
                    if (((SQLAggregateExpr) selectItemExpr)
                            .methodNameHashCod64() == FnvHash.Constants.COUNT) {
                        simpleCount = true;
                    }
                }
            }

            if (allawTrueWhere && simpleCount) {
                return true;
            }
        }

        return false;
    }


    public static Object getValue(WallVisitor visitor, SQLExpr x) {
        if (x != null && x.getAttributes().containsKey(EVAL_VALUE)) {
            return getValueFromAttributes(visitor, x);
        }

        if (x instanceof SQLBinaryOpExpr) {
            return getValue(visitor, (SQLBinaryOpExpr) x);
        }

        if (x instanceof SQLBinaryOpExprGroup) {
            return getValue(visitor, (SQLBinaryOpExprGroup) x);
        }

        if (x instanceof SQLBooleanExpr) {
            return ((SQLBooleanExpr) x).getBooleanValue();
        }

        if (x instanceof SQLNumericLiteralExpr) {
            return ((SQLNumericLiteralExpr) x).getNumber();
        }

        if (x instanceof SQLCharExpr) {
            return ((SQLCharExpr) x).getText();
        }

        if (x instanceof SQLNCharExpr) {
            return ((SQLNCharExpr) x).getText();
        }

        if (x instanceof SQLNotExpr) {
            Object result = getValue(visitor, ((SQLNotExpr) x).getExpr());
            if (result instanceof Boolean) {
                return !((Boolean) result).booleanValue();
            }
        }

        if (x instanceof SQLQueryExpr) {

            if (isSimpleCountTableSource(visitor, ((SQLQueryExpr) x).getSubQuery())) {
                return Integer.valueOf(1);
            }

            if (isSimpleCaseTableSource(visitor, ((SQLQueryExpr) x).getSubQuery())) {
                SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) ((SQLQueryExpr) x).getSubQuery().getQuery();
                SQLCaseExpr caseExpr = (SQLCaseExpr) queryBlock.getSelectList().get(0).getExpr();

                Object result = getValue(caseExpr);

                if (visitor != null && !visitor.getConfig().isCaseConditionConstAllow()) {
                    boolean leftIsName = false;
                    if (x.getParent() instanceof SQLBinaryOpExpr) {
                        SQLExpr left = ((SQLBinaryOpExpr) x.getParent()).getLeft();
                        if (left instanceof SQLName) {
                            leftIsName = true;
                        }
                    }

                    if (!leftIsName && result != null) {
                        addViolation(visitor, ErrorCode.CONST_CASE_CONDITION, "const case condition", caseExpr);
                    }
                }

                return result;
            }
        }

        String dbType = null;
        if (visitor != null) {
            dbType = visitor.getDbType();
        }

        if (x instanceof SQLMethodInvokeExpr //
                || x instanceof SQLBetweenExpr //
                || x instanceof SQLInListExpr //
                || x instanceof SQLUnaryExpr //
        ) {
            return eval(visitor, dbType, x, Collections.emptyList());
        }

        if (x instanceof SQLCaseExpr) {

            if (visitor != null && !visitor.getConfig().isCaseConditionConstAllow()) {
                SQLCaseExpr caseExpr = (SQLCaseExpr) x;

                boolean leftIsName = false;
                if (caseExpr.getParent() instanceof SQLBinaryOpExpr) {
                    SQLExpr left = ((SQLBinaryOpExpr) caseExpr.getParent()).getLeft();
                    if (left instanceof SQLName) {
                        leftIsName = true;
                    }
                }

                if (!leftIsName && caseExpr.getValueExpr() == null && caseExpr.getItems().size() > 0) {
                    SQLCaseExpr.Item item = caseExpr.getItems().get(0);
                    Object conditionVal = getValue(visitor, item.getConditionExpr());
                    Object itemVal = getValue(visitor, item.getValueExpr());
                    if (conditionVal instanceof Boolean && itemVal != null) {
                        addViolation(visitor, ErrorCode.CONST_CASE_CONDITION, "const case condition", caseExpr);
                    }
                }
            }

            return eval(visitor, dbType, x, Collections.emptyList());
        }

        return null;
    }

    public static Object getValue(SQLExpr x) {
        return getValue(null, x);
    }


    public static Object getValueFromAttributes(WallVisitor visitor, SQLObject sqlObject) {
        if (sqlObject == null) {
            return null;
        }

        if (visitor != null && visitor.getConfig().isConditionLikeTrueAllow()
                && sqlObject.getAttributes().containsKey(HAS_TRUE_LIKE)) {
            return null;
        }
        return sqlObject.getAttribute(EVAL_VALUE);
    }


    public static boolean isSimpleCaseTableSource(WallVisitor visitor, SQLSelect select) {
        SQLSelectQuery query = select.getQuery();

        if (query instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) query;

            boolean allawTrueWhere = false;

            if (queryBlock.getWhere() == null) {
                allawTrueWhere = true;
            } else {
                Object whereValue = getValue(visitor, queryBlock.getWhere());
                if (whereValue == Boolean.TRUE) {
                    allawTrueWhere = true;
                } else if (whereValue == Boolean.FALSE) {
                    return false;
                }
            }
            boolean simpleCase = false;
            if (queryBlock.getSelectList().size() == 1) {
                SQLExpr selectItemExpr = queryBlock.getSelectList().get(0).getExpr();
                if (selectItemExpr instanceof SQLCaseExpr) {
                    simpleCase = true;
                }
            }

            if (allawTrueWhere && simpleCase) {
                return true;
            }
        }

        return false;
    }


    private static void addViolation(WallVisitor visitor, int errorCode, String message, SQLObject x) {
        visitor.addViolation(new IllegalSQLObjectViolation(errorCode, message, visitor.toSQL(x)));
    }


    public static Object eval(WallVisitor wallVisitor, String dbType, SQLObject sqlObject, List<Object> parameters) {
        SQLEvalVisitor visitor = SQLEvalVisitorUtils.createEvalVisitor(dbType);
        visitor.setParameters(parameters);
        visitor.registerFunction("rand", Nil.instance);
        visitor.registerFunction("sin", Nil.instance);
        visitor.registerFunction("cos", Nil.instance);
        visitor.registerFunction("asin", Nil.instance);
        visitor.registerFunction("acos", Nil.instance);
        sqlObject.accept(visitor);

        if (sqlObject instanceof SQLNumericLiteralExpr) {
            return ((SQLNumericLiteralExpr) sqlObject).getNumber();
        }
        return getValueFromAttributes(wallVisitor, sqlObject);
    }

    public static boolean isSimpleCountTableSource(WallVisitor visitor, SQLTableSource tableSource) {
        if (!(tableSource instanceof SQLSubqueryTableSource)) {
            return false;
        }

        SQLSubqueryTableSource subQuery = (SQLSubqueryTableSource) tableSource;

        return isSimpleCountTableSource(visitor, subQuery.getSelect());
    }

    public static void loadResource(Set<String> names, String resource) {
        try {
            boolean hasResource = false;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                Enumeration<URL> e = Thread.currentThread().getContextClassLoader().getResources(resource);
                while (e.hasMoreElements()) {
                    URL url = e.nextElement();
                    InputStream in = null;
                    try {
                        in = url.openStream();
                        readFromInputStream(names, in);

                        hasResource = true;
                    } finally {
                        JdbcUtils.close(in);
                    }
                }
            }

            // for aliyun odps
            if (!hasResource) {
                if (!resource.startsWith("/")) {
                    resource = "/" + resource;
                }

                InputStream in = null;
                try {
                    in = WallVisitorUtils.class.getResourceAsStream(resource);
                    if (in != null) {
                        readFromInputStream(names, in);
                    }
                } finally {
                    JdbcUtils.close(in);
                }
            }
        } catch (IOException e) {
            log.error("load oracle deny tables errror", e);
        }
    }

    private static void readFromInputStream(Set<String> names, InputStream in) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.length() > 0) {
                    line = line.toLowerCase();
                    names.add(line);
                }
            }
        } finally {
            JdbcUtils.close(reader);
        }
    }


    public static class WallTopStatementContext {

        private boolean fromSysTable    = false;
        private boolean fromSysSchema   = false;

        private boolean fromPermitTable = false;

        public boolean fromSysTable() {
            return fromSysTable;
        }

        public void setFromSysTable(boolean fromSysTable) {
            this.fromSysTable = fromSysTable;
        }

        public boolean fromSysSchema() {
            return fromSysSchema;
        }

        public void setFromSysSchema(boolean fromSysSchema) {
            this.fromSysSchema = fromSysSchema;
        }

        public boolean fromPermitTable() {
            return fromPermitTable;
        }

        public void setFromPermitTable(boolean fromPermitTable) {
            this.fromPermitTable = fromPermitTable;
        }
    }


    public static class WallConditionContext {

        private boolean partAlwayTrue   = false;
        private boolean partAlwayFalse  = false;
        private boolean constArithmetic = false;
        private boolean xor             = false;
        private boolean bitwise         = false;

        public boolean hasPartAlwayTrue() {
            return partAlwayTrue;
        }

        public void setPartAlwayTrue(boolean partAllowTrue) {
            this.partAlwayTrue = partAllowTrue;
        }

        public boolean hasPartAlwayFalse() {
            return partAlwayFalse;
        }

        public void setPartAlwayFalse(boolean partAlwayFalse) {
            this.partAlwayFalse = partAlwayFalse;
        }

        public boolean hasConstArithmetic() {
            return constArithmetic;
        }

        public void setConstArithmetic(boolean constArithmetic) {
            this.constArithmetic = constArithmetic;
        }

        public boolean hasXor() {
            return xor;
        }

        public void setXor(boolean xor) {
            this.xor = xor;
        }

        public boolean hasBitwise() {
            return bitwise;
        }

        public void setBitwise(boolean bitwise) {
            this.bitwise = bitwise;
        }

    }



    public static String form(String name) {
        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }

        if (name.startsWith("'") && name.endsWith("'")) {
            name = name.substring(1, name.length() - 1);
        }

        if (name.startsWith("`") && name.endsWith("`")) {
            name = name.substring(1, name.length() - 1);
        }

        name = name.toLowerCase();
        return name;
    }

}
