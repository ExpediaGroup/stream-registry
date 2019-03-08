/* Copyright (c) 2018-2019 Expedia Group.
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
package com.homeaway.streamplatform.streamregistry.resource;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import io.dropwizard.jersey.errors.ErrorMessage;

@Slf4j
public class BaseResource {

    protected Response buildErrorMessage(Response.Status status, Exception e) {
        ErrorMessage errorMessage = new ErrorMessage(status.getStatusCode(),
                e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : null);

        log.error(errorMessage.toString(), e);
        return Response.status(status)
                .entity(errorMessage)
                .build();
    }

}
