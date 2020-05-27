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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.SchemaConverter;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;

@Component
public class SchemaRepository
    extends DefaultRepository<Schema, SchemaKey, Entity.SchemaKey, DefaultSpecification>
    implements com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository {
  SchemaRepository(EntityView view, EventSender sender, SchemaConverter converter) {
    super(view, sender, converter, Entity.SchemaKey.class);
  }
}
