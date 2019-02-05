/* Copyright (c) 2018 Expedia Group.
 * All rights reserved.  http://www.homeaway.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.homeaway.streamplatform.streamregistry.configuration;

import java.util.Map;

import lombok.Data;

/**
 * properties of the Producer that would produce events to the StreamRegistry's Underlying Compacted Topic where each Stream is stored.
 */
@Data
public class KafkaProducerConfig {

    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";

    Map<String, String> kafkaProducerProperties;

}
