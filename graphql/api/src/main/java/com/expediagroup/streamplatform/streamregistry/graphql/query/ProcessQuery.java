/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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

import java.util.List;
import java.util.Optional;

import com.expediagroup.streamplatform.streamregistry.graphql.GraphQLApiType;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProcessKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ProcessKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.Process;

public interface ProcessQuery extends GraphQLApiType {
  Optional<Process> byKey(ProcessKeyInput key);

  Iterable<Process> byQuery(ProcessKeyQuery key, SpecificationQuery specification,
                           List<ZoneKeyQuery> zones, List<StreamKeyQuery> inputs, List<StreamKeyQuery> outputs);
}
