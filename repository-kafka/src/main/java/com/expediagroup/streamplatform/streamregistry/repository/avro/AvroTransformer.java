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
package com.expediagroup.streamplatform.streamregistry.repository.avro;

import lombok.RequiredArgsConstructor;

import com.hotels.beans.BeanUtils;
import com.hotels.beans.model.FieldMapping;
import com.hotels.beans.model.FieldTransformer;
import com.hotels.beans.transformer.Transformer;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvroTransformer {
  private final Transformer tranformer;

  public AvroTransformer() {
    this(new BeanUtils()
        .getTransformer()
        .withFieldMapping(new FieldMapping("domainKey", "domain"))
        .withFieldTransformer(new FieldTransformer<>("domain", AvroDomainConversion::modelKey))
        .withFieldMapping(new FieldMapping("domain", "domainKey"))
        .withFieldTransformer(new FieldTransformer<>("domainKey", AvroDomainConversion::avroKey))
        .withFieldMapping(new FieldMapping("schemaKey", "schema"))
        .withFieldTransformer(new FieldTransformer<>("schema", AvroSchemaConversion::modelKey))
        .withFieldMapping(new FieldMapping("schema", "schemaKey"))
        .withFieldTransformer(new FieldTransformer<>("schemaKey", AvroSchemaConversion::avroKey)));
  }

  public <T, R> R transform(T sourceObj, Class<R> targetClass) {
    return tranformer.transform(sourceObj, targetClass);
  }
}
