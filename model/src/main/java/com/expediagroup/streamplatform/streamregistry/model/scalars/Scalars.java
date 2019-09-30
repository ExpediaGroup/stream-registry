/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.model.scalars;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

public final class Scalars {

  private Scalars() {}

  public static GraphQLScalarType objectNodeScalar() {
    return scalar("ObjectNode", new ObjectNodeCoercing());
  }

  private static GraphQLScalarType scalar(String name, Coercing<?, ?> coercing) {
    return GraphQLScalarType
        .newScalar()
        .name(name)
        .description(name + " Scalar")
        .coercing(coercing)
        .build();
  }
}
