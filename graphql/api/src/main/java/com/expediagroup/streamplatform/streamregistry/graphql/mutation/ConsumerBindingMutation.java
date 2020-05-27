/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation;

import com.expediagroup.streamplatform.streamregistry.graphql.GraphQLApiType;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

public interface ConsumerBindingMutation extends GraphQLApiType {
  ConsumerBinding insert(ConsumerBindingKeyInput key, SpecificationInput specification);

  ConsumerBinding update(ConsumerBindingKeyInput key, SpecificationInput specification);

  ConsumerBinding upsert(ConsumerBindingKeyInput key, SpecificationInput specification);

  Boolean delete(ConsumerBindingKeyInput key);

  ConsumerBinding updateStatus(ConsumerBindingKeyInput key, StatusInput status);
}
