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
package com.expediagroup.streamplatform.streamregistry.graphql.query;

import java.util.Optional;

import com.expediagroup.streamplatform.streamregistry.graphql.GraphQLApiType;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

public interface SchemaQuery extends GraphQLApiType {
  Optional<Schema> byKey(SchemaKeyInput key);

  Iterable<Schema> byQuery(SchemaKeyQuery key, SpecificationQuery specification);
}
