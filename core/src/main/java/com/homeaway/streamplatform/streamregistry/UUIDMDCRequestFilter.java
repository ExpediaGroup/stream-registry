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
package com.homeaway.streamplatform.streamregistry;

import java.util.UUID;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.slf4j.MDC;

/**
 * UUIDMDCRequestFilter class whose filter method would be called for each HttpRequest.
 */
public class UUIDMDCRequestFilter implements ContainerRequestFilter {
    public static final String MDC_REQUEST_UUID ="request-UUID";

    /**
     * Adds an key-value in MDC for each http-request which can be used for log and request tracking
     * @param requestContext
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        MDC.put(MDC_REQUEST_UUID, UUID.randomUUID().toString());
    }

}
