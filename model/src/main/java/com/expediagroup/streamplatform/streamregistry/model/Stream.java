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
package com.expediagroup.streamplatform.streamregistry.model;

import static com.expediagroup.streamplatform.streamregistry.model.SchemaCompatibility.FULL_TRANSITIVE;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Stream {
  /**
   * name of the Stream
   */
  @NonNull String name;

  /**
   * Schema Compatibility type for the stream.
   */
  @NonNull @Default SchemaCompatibility schemaCompatibility = FULL_TRANSITIVE;

  /**
   * Key Schema details of the latest Subject in Schema-registry
   */
  @NonNull Schema latestKeySchema;

  /**
   * Value Schema details of the latest Subject in Schema-registry
   */
  @NonNull Schema latestValueSchema;

  /**
   * Owner of the stream
   */
  @NonNull String owner;

  /**
   * Created timestamp of the stream in stream-registry
   */
  @NonNull @Default Long created = -1L;

  /**
   * Updated timestamp of the stream in stream-registry
   */
  @NonNull @Default Long updated = -1L;

  /**
   * gitHub url of the SMP project
   */
  @NonNull @Default String githubUrl = null;

  /**
   * Is data need in long term storage devices like Hadoop , S3 etc.
   */
  @Default boolean isDataNeededAtRest = false;

  /**
   * Is Automation Needed? i.e. Switch to deploy mirror makers
   */
  @Default boolean isAutomationNeeded = true;

  /**
   * Tags of the stream for cost evaluation, cluster identification, Portfolio identification etc.
   */
  @NonNull Tags tags;

  /**
   * List of VPC's the stream is available in
   */
  @NonNull @Default List<String> vpcList = null;

  /**
   * List of VPC's the stream is automatically replicated to
   */
  @NonNull @Default List<String> replicatedVpcList = null;

  /**
   * Topic Configuration map
   */
  @NonNull @Default Map<String, String> topicConfig = null;

  /**
   * Number of Partitions
   */
  int partitions;

  /**
   * Replication Factor
   */
  int replicationFactor;
}
