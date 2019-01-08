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
import java.util.Objects;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StreamClient {

    /**
     * Name of the producer or Consumer
     */
    @NotNull
    String name;

    /**
     * Details of the producer or Consumer - region, cluster name, kafka configuration
     */
    @NotNull
    List<RegionStreamConfig> regionStreamConfigList;

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        StreamClient streamClient = (StreamClient) o;
        return Objects.equals(name, streamClient.name) &&
            Objects.equals(regionStreamConfigList, streamClient.regionStreamConfigList);
    }

    @Override public int hashCode() {

        return Objects.hash(super.hashCode(), name, regionStreamConfigList);
    }
}
