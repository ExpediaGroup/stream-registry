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
package com.homeaway.streamplatform.streamregistry.model;

import java.util.List;

import lombok.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Producer.ProducerBuilder.class)
@EqualsAndHashCode(callSuper = true)
@Data
public class Producer extends StreamClient {

    @Builder
    public Producer(String name, List<RegionStreamConfig> regionStreamConfigList) {
        super(name, regionStreamConfigList);
    }

    @Override
    public String toString() {
        return "Producer{" +
            "name='" + name + '\'' +
            ", regionStreamConfigList=" + regionStreamConfigList +
            '}';
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ProducerBuilder {
    }
}
