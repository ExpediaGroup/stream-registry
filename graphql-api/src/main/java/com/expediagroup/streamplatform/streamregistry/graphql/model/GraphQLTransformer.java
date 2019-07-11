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
package com.expediagroup.streamplatform.streamregistry.graphql.model;

import java.util.function.Function;

import lombok.RequiredArgsConstructor;

import com.hotels.beans.BeanUtils;
import com.hotels.beans.model.FieldTransformer;
import com.hotels.beans.transformer.Transformer;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@RequiredArgsConstructor
public class GraphQLTransformer {
  private final Transformer transformer;

  public GraphQLTransformer(
      Service<Domain, Domain.Key> domainService,
      Service<Schema, Schema.Key> schemaService) {
    this(transformer(domainService, schemaService));
  }

  public GraphQLDomain transform(Domain domain) {
    return transformer.transform(domain, GraphQLDomain.class);
  }

  public GraphQLSchema transform(Schema schema) {
    return transformer.transform(schema, GraphQLSchema.class);
  }

  public GraphQLStream transform(Stream stream) {
    return transformer.transform(stream, GraphQLStream.class);
  }

  private static Transformer transformer(
      Service<Domain, Domain.Key> domainService,
      Service<Schema, Schema.Key> schemaService) {
    BeanUtils beanUtils = new BeanUtils();
    Transformer transformer = beanUtils.getTransformer();

    Function<Domain.Key, GraphQLDomain> domainFunction = key -> transformer
        .transform(domainService.get(key), GraphQLDomain.class);
    Function<Schema.Key, GraphQLSchema> schemaFunction = key -> transformer
        .transform(schemaService.get(key), GraphQLSchema.class);

    return transformer
        .withFieldTransformer(new FieldTransformer<>("tags", GraphQLKeyValue::toMap))
        .withFieldTransformer(new FieldTransformer<>("configuration", GraphQLKeyValue::toMap))
        .withFieldTransformer(new FieldTransformer<>("domain", domainFunction))
        .withFieldTransformer(new FieldTransformer<>("schema", schemaFunction));
  }
}
