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
package com.lz.druid.sql.dialect.oracle.ast.stmt;

import com.lz.druid.sql.ast.statement.SQLAssignItem;
import com.lz.druid.sql.dialect.oracle.visitor.OracleASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class OracleAlterSessionStatement extends OracleStatementImpl implements OracleAlterStatement {

    private List<SQLAssignItem> items = new ArrayList<SQLAssignItem>();

    @Override
    public void accept0(OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, items);
        }
        visitor.endVisit(this);
    }

    public List<SQLAssignItem> getItems() {
        return items;
    }

    public void setItems(List<SQLAssignItem> items) {
        this.items = items;
    }

}
