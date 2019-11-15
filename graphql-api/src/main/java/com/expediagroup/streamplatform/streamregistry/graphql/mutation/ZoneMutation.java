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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation;

import com.expediagroup.streamplatform.streamregistry.graphql.GraphQLApiType;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

public interface ZoneMutation extends GraphQLApiType {
  Zone insert(ZoneKeyInput key, SpecificationInput specification);

  Zone update(ZoneKeyInput key, SpecificationInput specification);

  Zone upsert(ZoneKeyInput key, SpecificationInput specification);

  Boolean delete(ZoneKeyInput key);

  Zone updateStatus(ZoneKeyInput key, StatusInput status);
}
