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
package com.expediagroup.streamplatform.streamregistry.core.schema;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.avro.Schema;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import com.expediagroup.streamplatform.streamregistry.core.exception.SchemaManagerException;

@RequiredArgsConstructor
public class HttpSchemaManager implements SchemaManager {
  private final RestTemplate rest;
  private final UriTemplateHandler handler;

  public HttpSchemaManager(String schemaRegistryUrl) {
    this(new RestTemplate(), new DefaultUriBuilderFactory(schemaRegistryUrl));
  }

  @Override
  public SchemaReference registerSchema(String subject, Schema schema) throws SchemaManagerException {
    RequestEntity<Request> request = RequestEntity
        .post(handler.expand("/subjects/{subject}/versions", subject))
        .body(new Request(schema.toString()));
    ResponseEntity<RegisterResponse> response = rest.exchange(request, RegisterResponse.class);
    return new SchemaReference(subject, response.getBody().getId(), -1); //TODO version
    //TODO handle exceptions -> SchemaManagerException
  }

  @Override
  public boolean checkCompatibility(String subject, Schema schema) {
    RequestEntity<Request> request = RequestEntity
        .post(handler.expand("/compatibility/subjects/{subject}/versions/latest", subject))
        .body(new Request(schema.toString()));
    ResponseEntity<CompatibilityResponse> response = rest.exchange(request, CompatibilityResponse.class);
    return response.getBody().isCompatible();
  }

  @Value
  private static class Request {
    String schema;
  }

  @Value
  private static class RegisterResponse {
    int id;
  }

  @Value
  private static class CompatibilityResponse {
    @JsonProperty("is_compatible")
    boolean compatible;
  }
}
