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
package com.expediagroup.streamplatform.streamregistry.core.accesscontrol.domain;

public interface AcessControlledResourceFields {

  public static final String STREAM_NAME = "stream-name";
  public static final String STREAM_VERSION = "stream-version";

  public static final String SCHEMA_NAME = "schema-name";
  public static final String DOMAIN_NAME = "domain-name";
  public static final String ZONE_NAME = "zone-name";
  public static final String CONSUMER_NAME = "consumer-name";
  public static final String PRODUCER_NAME = "producer-name";
  public static final String INFRASTRUCTURE_NAME = "infrastructure-name";

  public static final String SPECIFICATION_TYPE = "specification-type";

}
