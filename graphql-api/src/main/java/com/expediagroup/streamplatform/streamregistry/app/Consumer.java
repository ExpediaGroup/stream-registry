package com.expediagroup.streamplatform.streamregistry.app;
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
import com.expediagroup.streamplatform.streamregistry.app.keys.*;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;


import lombok.Data;

@Data
@Entity
public class Consumer {

  @EmbeddedId
  private ConsumerKey key;
  private Specification specification;
  private Status status;
  //private String streamId; //Resolves to Stream
  //private String zoneId; //Resolves to Zone
}