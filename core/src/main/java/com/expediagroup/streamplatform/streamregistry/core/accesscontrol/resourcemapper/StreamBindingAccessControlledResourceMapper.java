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
package com.expediagroup.streamplatform.streamregistry.core.accesscontrol.resourcemapper;

import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.DOMAIN_NAME;
import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.INFRASTRUCTURE_NAME;
import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.SPECIFICATION_TYPE;
import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.STREAM_NAME;
import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.STREAM_VERSION;
import static com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AcessControlledResourceFields.ZONE_NAME;
import static java.lang.String.valueOf;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

@Component
public class StreamBindingAccessControlledResourceMapper implements AccessControlledResourceMapper {

  @Override
  public boolean canMap(Object domainObject) {
    return domainObject instanceof StreamBinding;
  }

  @Override
  public AccessControlledResource map(Object domainObject) {
    StreamBinding streamBinding = (StreamBinding) domainObject;
    return new AccessControlledResource(
        Map.of(
            DOMAIN_NAME, streamBinding.getKey().getStreamDomain(),
            STREAM_NAME, streamBinding.getKey().getStreamName(),
            STREAM_VERSION, valueOf(streamBinding.getKey().getStreamVersion()),
            ZONE_NAME, streamBinding.getKey().getInfrastructureZone(),
            INFRASTRUCTURE_NAME, streamBinding.getKey().getInfrastructureName(),
            STREAM_NAME, streamBinding.getKey().getStreamName()
        ),
        Map.of(SPECIFICATION_TYPE, streamBinding.getSpecification().getType()));
  }
}
