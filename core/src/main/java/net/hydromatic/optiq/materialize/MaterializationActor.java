/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hydromatic.optiq.materialize;

import net.hydromatic.optiq.jdbc.OptiqRootSchema;
import net.hydromatic.optiq.jdbc.OptiqSchema;

import org.eigenbase.reltype.RelDataType;
import org.eigenbase.util.Util;

import com.google.common.collect.*;

import java.util.*;

/**
 * Actor that manages the state of materializations in the system.
 */
class MaterializationActor {
  // Not an actor yet -- TODO make members private and add request/response
  // queues

  final Map<MaterializationKey, Materialization> keyMap = Maps.newHashMap();

  final Map<QueryKey, MaterializationKey> keyBySql = Maps.newHashMap();

  final List<MaterializationService.TileKey> tileKeys = Lists.newArrayList();

  /** A query materialized in a table, so that reading from the table gives the
   * same results as executing the query. */
  static class Materialization {
    final MaterializationKey key;
    final OptiqRootSchema rootSchema;
    OptiqSchema.TableEntry materializedTable;
    final String sql;
    final RelDataType rowType;

    /** Creates a materialization.
     *
     * @param key  Unique identifier of this materialization
     * @param materializedTable Table that currently materializes the query.
     *                          That is, executing "select * from table" will
     *                          give the same results as executing the query.
     *                          May be null when the materialization is created;
     *                          materialization service will change the value as
     * @param sql  Query that is materialized
     * @param rowType Row type
     */
    Materialization(MaterializationKey key,
        OptiqRootSchema rootSchema,
        OptiqSchema.TableEntry materializedTable,
        String sql,
        RelDataType rowType) {
      this.key = key;
      this.rootSchema = rootSchema;
      this.materializedTable = materializedTable; // may be null
      this.sql = sql;
      this.rowType = rowType;
    }
  }

  /** A materialization can be re-used if it is the same SQL, on the same
   * schema, with the same path for resolving functions. */
  static class QueryKey {
    final String sql;
    final OptiqSchema schema;
    final List<String> path;

    QueryKey(String sql, OptiqSchema schema, List<String> path) {
      this.sql = sql;
      this.schema = schema;
      this.path = path;
    }

    @Override public boolean equals(Object obj) {
      return obj == this
          || obj instanceof QueryKey
          && sql.equals(((QueryKey) obj).sql)
          && schema.equals(((QueryKey) obj).schema)
          && path.equals(((QueryKey) obj).path);
    }

    @Override public int hashCode() {
      return Util.hashV(sql, schema, path);
    }
  }
}

// End MaterializationActor.java