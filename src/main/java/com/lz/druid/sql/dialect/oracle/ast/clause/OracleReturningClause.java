/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lz.druid.sql.dialect.oracle.ast.clause;

import com.lz.druid.sql.ast.SQLExpr;
import com.lz.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.lz.druid.sql.dialect.oracle.visitor.OracleASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class OracleReturningClause extends OracleSQLObjectImpl {

    private List<SQLExpr> items = new ArrayList<SQLExpr>();
    private List<SQLExpr> values = new ArrayList<SQLExpr>();

    public List<SQLExpr> getItems() {
        return items;
    }

    public void addItem(SQLExpr item) {
        if (item != null) {
            item.setParent(this);
        }
        this.items.add(item);
    }

    public List<SQLExpr> getValues() {
        return values;
    }

    public void addValue(SQLExpr value) {
        if (value != null) {
            value.setParent(this);
        }
        this.values.add(value);
    }

    @Override
    public void accept0(OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, items);
            acceptChild(visitor, values);
        }
        visitor.endVisit(this);
    }

    public OracleReturningClause clone() {
        OracleReturningClause x = new OracleReturningClause();

        for (SQLExpr item : items) {
            SQLExpr item2 = item.clone();
            item2.setParent(x);
            x.items.add(item2);
        }

        for (SQLExpr v : values) {
            SQLExpr v2 = v.clone();
            v2.setParent(x);
            x.values.add(v2);
        }

        return x;
    }
}
