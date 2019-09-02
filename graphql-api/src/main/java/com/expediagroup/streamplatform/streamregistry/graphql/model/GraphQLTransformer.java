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

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotels.beans.BeanUtils;
import com.hotels.beans.model.FieldTransformer;
import com.hotels.beans.transformer.Transformer;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RequiredArgsConstructor
public class GraphQLTransformer {

  private final Transformer transformer;

  public GraphQLTransformer() {
    this(transformer());
  }

  public GraphQLDomain transform(Domain domain) {
    return transformer.transform(domain, GraphQLDomain.class);
  }

  public <T, K> K transform(T sourceObj, Class<? extends K> targetClass) {
    return transformer.transform(sourceObj, targetClass);
  }

  public GraphQLSchema transform(Schema schema) {
    return transformer.transform(schema, GraphQLSchema.class);
  }

  public GraphQLStream transform(Stream stream) {
    return transformer.transform(stream, GraphQLStream.class);
  }

  private static Transformer transformer() {
    return new BeanUtils().getTransformer().withFieldTransformer(new FieldTransformer<>("configuration", ObjectNode::deepCopy));
  }
}
