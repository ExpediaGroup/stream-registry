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
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProcessBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.*;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;

public interface ProcessBindingQuery extends GraphQLApiType {
  Optional<ProcessBinding> byKey(ProcessBindingKeyInput key);

  Iterable<ProcessBinding> byQuery(ProcessBindingKeyQuery key, SpecificationQuery specification,
                           ZoneKeyQuery zone, List<ConsumerBindingKeyQuery> inputs, List<ProducerBindingKeyQuery> outputs);
}
