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
package com.homeaway.streamplatform.streamregistry.resource;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.homeaway.streamplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamplatform.streamregistry.model.Hint;

@Api(value = "Stream-registry API", description = "Stream Registry API, a centralized governance tool for managing streams.")
@Path("/v0/regions")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class RegionResource {

    private final RegionDao regionDao;

    public RegionResource(RegionDao regionDao) {
        this.regionDao = regionDao;
    }

    @GET
    @ApiOperation(
        value = "Get all supported regions for each Hint in this environment",
        tags = "regions",
        response = Collection.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns all available regions", response = List.class) })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getRegions() {
        Collection<Hint> hintRegionMap = regionDao.getHints();
        return Response.status(200).entity(hintRegionMap).build();
    }

}
