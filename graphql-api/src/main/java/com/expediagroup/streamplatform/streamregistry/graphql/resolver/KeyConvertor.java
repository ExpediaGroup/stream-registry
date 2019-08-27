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
package com.expediagroup.streamplatform.streamregistry.graphql.resolver;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

public class KeyConvertor {

  public static GraphQLDomain.Key convert(Domain.Key key) {
    return new GraphQLDomain.Key(key.getName());
  }

  public static GraphQLSchema.Key convert(Schema.Key key) {
    return new GraphQLSchema.Key(key.getName(), convert(key.getDomain()));
  }

  public static Domain.Key convert(GraphQLDomain.Key key) {
    return new Domain.Key(key.getName());
  }

  public static Schema.Key convert(GraphQLSchema.Key schema) {
    return new Schema.Key(schema.getName(), convert(schema.getDomain()));
  }
}
