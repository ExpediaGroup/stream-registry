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
package com.homeaway.streamplatform.streamregistry.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.homeaway.streamplatform.streamregistry.exceptions.InvalidClusterException;
import com.homeaway.streamplatform.streamregistry.model.JsonCluster;
import com.homeaway.streamplatform.streamregistry.service.ClusterService;

@Api(value = "Stream-registry API", description = "Stream Registry API, a centralized governance tool for managing streams.")
@Path("/v0/clusters")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ClusterResource extends BaseResource {
    private final ClusterService clusterService;

    public ClusterResource(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @PUT
    @ApiOperation(
        value = "Upsert Clusters",
        notes = "Create/Update a cluster",
        tags = "clusters")
    @ApiResponses(value = { @ApiResponse(code = 202, message = "Request accepted"),
        @ApiResponse(code = 400, message = "Validation Exception while creating a cluster"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @Timed
    public Response upsertCluster(@ApiParam(value = "Cluster Object", name = "Cluster Object", required = true) JsonCluster clusterParam) {
        try {
            log.info("Cluster Object {}", clusterParam);

            clusterService.upsertCluster(clusterParam);
            return Response.status(Response.Status.ACCEPTED).build();
        }  catch (InvalidClusterException e) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, e);
        }
    }

    @GET
    @Path("/")
    @ApiOperation(
        value = "Get All Clusters",
        notes = "Gets all the clusters",
        tags = "clusters")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Cluster information"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data"),
        @ApiResponse(code = 404, message = "Cluster not found") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllClusters(@ApiParam(value = "clusterName") @QueryParam("clusterName") String clusterName,
        @ApiParam(value = "vpc") @QueryParam("vpc") String vpc,
        @ApiParam(value = "env") @QueryParam("env") String env,
        @ApiParam(value = "hint") @QueryParam("hint") String hint,
        @ApiParam(value = "type") @QueryParam("type") String type) {
        try {
            List<JsonCluster> clusterList = clusterService.getAllClusters();

            if(clusterName != null && !clusterName.isEmpty()) {
                clusterList = clusterList.stream()
                    .filter(e -> e.getClusterValue().getClusterName().equalsIgnoreCase(clusterName))
                    .collect(Collectors.toList());
            }

            if(vpc != null && !vpc.isEmpty()) {
                clusterList = clusterList.stream()
                    .filter(e -> e.getClusterKey().getVpc().equalsIgnoreCase(vpc))
                    .collect(Collectors.toList());
            }

            if(env != null && !env.isEmpty()) {
                clusterList = clusterList.stream()
                    .filter(e -> e.getClusterKey().getEnv().equalsIgnoreCase(env))
                    .collect(Collectors.toList());
            }

            if(hint != null && !hint.isEmpty()) {
                clusterList = clusterList.stream()
                    .filter(e -> e.getClusterKey().getHint().equalsIgnoreCase(hint))
                    .collect(Collectors.toList());
            }

            if(type != null && !type.isEmpty()) {
                clusterList = clusterList.stream()
                    .filter(e -> e.getClusterKey().getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            }

            return Response.status(Response.Status.OK.getStatusCode()).entity(clusterList).build();
        } catch (IllegalStateException e) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, e);
        }
    }
}
