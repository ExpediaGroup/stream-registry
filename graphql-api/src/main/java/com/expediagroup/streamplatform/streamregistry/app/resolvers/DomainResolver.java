package com.expediagroup.streamplatform.streamregistry.app.resolvers;
/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.expediagroup.streamplatform.streamregistry.app.Domain;
import com.expediagroup.streamplatform.streamregistry.app.Schema;
import com.expediagroup.streamplatform.streamregistry.app.Status;
import com.expediagroup.streamplatform.streamregistry.app.services.Services;

@Component
public class DomainResolver implements GraphQLResolver<Domain> {

  private Services services;

  public DomainResolver(Services services) {
    this.services = services;
  }

  public List<Schema> schemas(Domain domain) {
    List<Schema> out = new ArrayList<>();
    for (Schema v : services.getSchemaService().readAll()) {
      if (v.getKey().getDomain().equals(domain.getKey().getName())) {
        out.add(v);
      }
    }
    return out;
  }

  public Status status(Domain domain) {
    return domain.getStatus() == null ? new Status() : domain.getStatus();
  }
}