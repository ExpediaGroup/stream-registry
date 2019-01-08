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

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Hint {

    /**
     * Stream's Hint, which would be used to determine the cluster
     *
     * Example: primary (default), logs etc.,
     */
    String hint;

    /**
     * Set of VPCs where the kafka cluster is available
     *
     * Example: us-east-1-vpc-defa0000, us-west-2-vpc-0000cafe
     */
    Set<String> vpcs;
}
