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

import java.util.Map;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.db.dao.ClusterDao;
import com.homeaway.streamplatform.streamregistry.utils.ResourceUtils;

@Api(value = "Stream-registry API", description = "Stream Registry API, a centralized governance tool for managing streams.")
@Path("/v0/clusters")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ClusterResource {
    private final ClusterDao clusterDao;

    public ClusterResource(ClusterDao clusterDao) {
        this.clusterDao = clusterDao;
    }

//    @PUT
//    @ApiOperation(
//        value = "Upsert Clusters",
//        notes = "Create/Update a cluster",
//        tags = "clusters")
//    @ApiResponses(value = { @ApiResponse(code = 202, message = "Request accepted"),
//        @ApiResponse(code = 400, message = "Validation Exception while creating a cluster"),
//        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
//    @Path("/{clusterName}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Timed
//    public Response upsertCluster(@ApiParam(value = "cluster name", required = true) @PathParam("clusterName") String clusterName,
//        @ApiParam(value = "cluster entity", required = true) ClusterValue clusterValue) {
//
//    }

//    upsertCluster


    @GET
    @Path("/")
    @ApiOperation(
        value = "Get All Clusters",
        notes = "Gets all the clusters",
        tags = "clusters",
        response = ClusterValue.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Cluster information", response = ClusterValue.class),
        @ApiResponse(code = 500, message = "Error Occurred while getting data"),
        @ApiResponse(code = 404, message = "Stream not found") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllClusters() {
        try {
            Map<ClusterKey, ClusterValue> clusterMap = clusterDao.getAllClusters();
            return Response.ok().entity(clusterMap).build();
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry");
        }
    }


    @GET
    @Path("/{clusterName}")
    @ApiOperation(
        value = "Get a Cluster",
        notes = "Returns a single cluster",
        tags = "clusters",
        response = ClusterValue.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Cluster information", response = ClusterValue.class),
        @ApiResponse(code = 500, message = "Error Occurred while getting data"),
        @ApiResponse(code = 404, message = "Stream not found") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getCluster(@ApiParam(value = "clusterName", required = true) @PathParam ("clusterName") String clusterName) {
        try {
            Optional<ClusterValue> clusterValue = clusterDao.getCluster(clusterName);

            if(!clusterValue.isPresent()) {
                return ResourceUtils.notFound("Cluster not found" + clusterName);
            }

            return Response.ok().entity(clusterValue).build();
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry");
        }
    }
}
