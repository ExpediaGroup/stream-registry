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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation;

import java.util.List;

import com.expediagroup.streamplatform.streamregistry.graphql.GraphQLApiType;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.*;
import com.expediagroup.streamplatform.streamregistry.model.Process;

public interface ProcessMutation extends GraphQLApiType {
  Process insert(ProcessKeyInput key, SpecificationInput specification,
                 List<ZoneKeyInput> zones, List<ProcessInInput> inputs, List<ProcessOutInput> outputs);

  Process update(ProcessKeyInput key, SpecificationInput specification,
                 List<ZoneKeyInput> zones, List<ProcessInInput> inputs, List<ProcessOutInput> outputs);

  Process upsert(ProcessKeyInput key, SpecificationInput specification,
                 List<ZoneKeyInput> zones, List<ProcessInInput> inputs, List<ProcessOutInput> outputs);

  Boolean delete(ProcessKeyInput key);

  Process updateStatus(ProcessKeyInput key, StatusInput status);
}
