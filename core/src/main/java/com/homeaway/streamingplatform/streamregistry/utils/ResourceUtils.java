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
package com.homeaway.streamingplatform.streamregistry.utils;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for common resource operations
 */
@Slf4j
public abstract class ResourceUtils {

    private ResourceUtils() {}

    /**
     * For use when wanting to return a 404 with a message.
     * @param notFoundMessage the not found message
     * @return the response
     */
    public static Response notFound(String notFoundMessage) {
        return Response.status(Response.Status.NOT_FOUND)
                .type("text/plain")
                .entity(notFoundMessage)
                .build();
    }

    /**
     * Stream not found response.
     *
     * @param streamName the stream name
     * @return the response
     */
    public static Response streamNotFound(String streamName) {
        String msg = "Stream not found " + streamName;
        log.warn(msg);
        return notFound(msg);
    }
}
