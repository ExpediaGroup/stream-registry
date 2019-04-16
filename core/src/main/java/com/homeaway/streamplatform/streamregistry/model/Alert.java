/* Copyright (c) 2018-Present Expedia Group.
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

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Alert.AlertBuilder.class)
@Builder
@Data
public class Alert {

    @Builder
    public Alert(String type, String method) {
        this.type = type;
        this.method = method;
    }

    /**
     * Region where the Producer/Consumer is deployed.
     */
    @NotNull
    String type;

    /**
     * Name of the cluster that Producer/Consumer is communicating to.
     */
    @NotNull
    String method;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AlertBuilder {}
}
