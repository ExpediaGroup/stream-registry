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

import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.DOMAIN_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.SCHEMA_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.SPECIFICATION_TYPE;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Component
public class SchemaAccessControlledResourceMapper implements AccessControlledResourceMapper {

  @Override
  public boolean canMap(Object domainObject) {
    return domainObject instanceof Schema;
  }

  @Override
  public AccessControlledResource map(Object domainObject) {
    Schema schema = (Schema) domainObject;
    return new AccessControlledResource(
        Map.of(
            DOMAIN_NAME, schema.getKey().getDomain(),
            SCHEMA_NAME, schema.getKey().getName()
        ),
        Map.of(SPECIFICATION_TYPE, schema.getSpecification().getType()));
  }
}