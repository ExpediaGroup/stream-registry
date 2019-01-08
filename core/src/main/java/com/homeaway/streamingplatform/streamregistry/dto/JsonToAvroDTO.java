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
package com.homeaway.streamingplatform.streamregistry.dto;

import java.util.Calendar;

import javax.ws.rs.BadRequestException;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.OperationType;
import com.homeaway.digitalplatform.streamregistry.Schema;
import com.homeaway.digitalplatform.streamregistry.Tags;
import com.homeaway.streamingplatform.streamregistry.model.Stream;

public class JsonToAvroDTO {

    public static AvroStream convertJsonToAvro(Stream jsonStream, OperationType operationType) {

        com.homeaway.streamingplatform.streamregistry.model.Schema jsonKeySchema = jsonStream.getLatestKeySchema();
        com.homeaway.streamingplatform.streamregistry.model.Schema jsonValueSchema = jsonStream.getLatestValueSchema();
        com.homeaway.streamingplatform.streamregistry.model.Tags jsonTags = jsonStream.getTags();

        if (jsonKeySchema == null || jsonValueSchema == null
            || jsonStream.getOwner() == null
            || jsonTags == null || jsonTags.getProductId() == -1) {
            throw new BadRequestException("Input Validation Failed. Mandatory Input Fields: keySchema, valueSchema, tags.productId, owner");
        }

        Schema keySchema = Schema.newBuilder()
                .setSchemaString(jsonKeySchema.getSchemaString())
                .setId(jsonKeySchema.getId())
                .setSubjectId(jsonKeySchema.getVersion())
                .setCreated(String.valueOf(Calendar.getInstance().getTime()))
                .setUpdated(String.valueOf(Calendar.getInstance().getTime()))
                .build();


        Schema valueSchema = Schema.newBuilder()
                .setSchemaString(jsonValueSchema.getSchemaString())
                .setId(jsonValueSchema.getId())
                .setSubjectId(jsonValueSchema.getVersion())
                .setCreated(String.valueOf(Calendar.getInstance().getTime()))
                .setUpdated(String.valueOf(Calendar.getInstance().getTime()))
                .build();
        Tags tags = Tags.newBuilder().setProductId(jsonTags.getProductId())
            .setPortfolioId(jsonTags.getPortfolioId())
            .setBrand(jsonTags.getBrand())
            .setAssetProtectionLevel(jsonTags.getAssetProtectionLevel())
            .setComponentId(jsonTags.getComponentId())
            .setHint(jsonTags.getHint())
            .build();

        AvroStream avroStream= AvroStream.newBuilder()
                .setName(jsonStream.getName())
                .setLatestKeySchema(keySchema)
                .setLatestValueSchema(valueSchema)
                .setOperationType(operationType)
                .setOwner(jsonStream.getOwner())
                .setUpdated(System.currentTimeMillis())
                .setGithubUrl(jsonStream.getGithubUrl())
                .setIsDataNeededAtRest(jsonStream.getIsDataNeededAtRest())
                .setIsAutomationNeeded(jsonStream.getIsAutomationNeeded())
                .setTags(tags)
                .setVpcList(jsonStream.getVpcList())
                .setReplicatedVpcList(jsonStream.getReplicatedVpcList())
                .setTopicConfig(jsonStream.getTopicConfig())
                .setPartitions(jsonStream.getPartitions())
                .setReplicationFactor(jsonStream.getReplicationFactor())
                .build();

        if (jsonStream.getSchemaCompatibility() != null)
            avroStream.setSchemaCompatibility(jsonStream.getSchemaCompatibility());

        return avroStream;

    }

}