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
package com.homeaway.streamingplatform.streamregistry.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import com.homeaway.digitalplatform.streamregistry.SchemaCompatibility;

@JsonDeserialize(builder = Stream.StreamBuilder.class)
@Builder
@Data
public class Stream {

    /**
     * name of the Stream
     */
    @NotNull
    String name;

    /**
     * Schema Compatibility type for the stream.
     */
    @Builder.Default
    SchemaCompatibility schemaCompatibility = SchemaCompatibility.TRANSITIVE_FULL;

    /**
     * Key Schema details of the latest Subject in Schema-registry
     */
    @NotNull
    Schema latestKeySchema;

    /**
     * Value Schema details of the latest Subject in Schema-registry
     */
    @NotNull
    Schema latestValueSchema;

    /**
     * Owner of the stream
     */
    @NotNull
    String owner;

    /**
     * Created timestamp of the stream in stream-registry
     */
    @Builder.Default
    Long created = -1L;

    /**
     * Updated timestamp of the stream in stream-registry
     */
    @Builder.Default
    Long updated = -1L;

    /**
     * gitHub url of the SMP project
     */
    @Builder.Default
    String githubUrl = null;

    /**
     * Is data need in long term storage devices like Hadoop , S3 etc.
     */
    @Builder.Default
    Boolean isDataNeededAtRest = false;

    /**
     * Is Automation Needed? i.e. Switch to deploy mirror makers
     */
    @Builder.Default
    Boolean isAutomationNeeded = true;

    /**
     * Tags of the stream for cost evaluation, cluster identification, Portfolio identification etc.
     */
    @NotNull
    Tags tags;

    /**
     * List of VPC's the stream is available in
     */
    @Builder.Default
    List<String> vpcList = null;

    /**
     * List of VPC's the stream is automatically replicated to
     */
    @Builder.Default
    List<String> replicatedVpcList = null;

    /**
     * Topic Configuration map
     */
    @Builder.Default
    Map<String, String> topicConfig = null;

    /**
     * Number of Partitions
     */
    int partitions;

    /**
     * Replication Factor
     */
    int replicationFactor;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class StreamBuilder {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Stream stream = (Stream) o;
        return Objects.equals(name, stream.name) &&
            schemaCompatibility == stream.schemaCompatibility &&
            Objects.equals(latestKeySchema, stream.latestKeySchema) &&
            Objects.equals(latestValueSchema, stream.latestValueSchema) &&
            Objects.equals(owner, stream.owner) &&
            Objects.equals(created, stream.created) &&
            Objects.equals(updated, stream.updated) &&
            Objects.equals(githubUrl, stream.githubUrl) &&
            Objects.equals(isDataNeededAtRest, stream.isDataNeededAtRest) &&
            Objects.equals(isAutomationNeeded, stream.isAutomationNeeded) &&
            Objects.equals(tags, stream.tags) &&
            Objects.equals(vpcList, stream.vpcList) &&
            Objects.equals(replicatedVpcList, stream.replicatedVpcList) &&
            Objects.equals(topicConfig, stream.topicConfig) &&
            Objects.equals(partitions, stream.partitions) &&
            Objects.equals(replicationFactor, stream.replicationFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, schemaCompatibility, latestKeySchema, latestValueSchema, owner, created, updated,
            isDataNeededAtRest, isAutomationNeeded, tags, vpcList, replicatedVpcList, topicConfig, partitions, replicationFactor);
    }

    @Override
    public String toString() {
        return "Stream{" +
            "name='" + name + '\'' +
            ", schemaCompatibility=" + schemaCompatibility +
            ", latestKeySchema=" + latestKeySchema +
            ", latestValueSchema=" + latestValueSchema +
            ", owner='" + owner + '\'' +
            ", created=" + created +
            ", updated=" + updated +
            ", githubUrl=" + githubUrl +
            ", isDataNeededAtRest=" + isDataNeededAtRest +
            ", isAutomationNeeded=" + isAutomationNeeded +
            ", tags=" + tags +
            ", vpcList=" + vpcList +
            ", replicatedVpcList=" + vpcList +
            ", topicConfig=" + topicConfig +
            ", partitions=" + partitions +
            ", replicationFactor=" + replicationFactor +
            '}';
    }
}
