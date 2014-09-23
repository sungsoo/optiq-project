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
package net.hydromatic.optiq.rules.java.impl;

import net.hydromatic.linq4j.expressions.BlockBuilder;
import net.hydromatic.linq4j.expressions.Expression;

import net.hydromatic.optiq.rules.java.AggAddContext;

import java.util.List;

/**
 * Implementation of {@link net.hydromatic.optiq.rules.java.AggAddContext}.
 */
public abstract class AggAddContextImpl extends AggResultContextImpl
    implements AggAddContext {
  public AggAddContextImpl(BlockBuilder block, List<Expression> accumulator) {
    super(block, accumulator);
  }

  public final List<Expression> arguments() {
    return rowTranslator().translateList(rexArguments());
  }
}
