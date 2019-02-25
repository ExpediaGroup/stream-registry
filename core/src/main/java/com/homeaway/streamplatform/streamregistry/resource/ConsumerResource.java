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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.homeaway.streamplatform.streamregistry.db.dao.StreamClientDao;
import com.homeaway.streamplatform.streamregistry.exceptions.ActorNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.RegionNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.homeaway.streamplatform.streamregistry.model.Consumer;

@Slf4j
public class ConsumerResource {
    private final StreamClientDao<Consumer> consumerDao;

    @SuppressWarnings("WeakerAccess")
    public ConsumerResource(StreamClientDao<Consumer> consumerDao) {
        this.consumerDao = consumerDao;
    }

    @GET
    @Path("/{consumerName}")
    @ApiOperation(
        value = "Get Consumer",
        tags = "consumers",
        response = Consumer.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Consumer successfully read", response = Consumer.class),
        @ApiResponse(code = 500, message = "Error Occurred while getting data"),
        @ApiResponse(code = 404, message = "Consumer not found")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getConsumer(
        @ApiParam(value = "Stream Name corresponding to the consumer", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "Consumer Name", required = true) @PathParam("consumerName") String consumerName) {
        try {
            Optional<Consumer> responseConsumer = consumerDao.get(streamName, consumerName);
            if (!responseConsumer.isPresent()) {
                log.warn("Consumer not found: {} for stream={}", consumerName, streamName);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                                String.format("Consumer:%s not found for Stream=%s", consumerName, streamName)))
                        .build();
            }
            return Response.ok().entity(responseConsumer.get()).build();
        } catch (StreamNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                            "Stream:" + streamName + " not found . Please create the Stream before trying to pulling  a consumer"))
                    .build();
        } catch (RuntimeException e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            "Error while getting consumer=" + consumerName,
                            e.getCause() !=null ? e.getMessage() + e.getCause().getMessage() : e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/")
    @ApiOperation(
        value = "Get All consumers",
        response = Consumer.class,
        tags = "consumers")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Consumer(s) successfully read", response = Consumer.class),
        @ApiResponse(code = 500, message = "Error Occurred while getting data"),
        @ApiResponse(code = 404, message = "Stream not found")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllConsumers(
        @ApiParam(value = "Stream Name corresponding to the consumer", required = true) @PathParam("streamName") String streamName) {
        try {
            List<Consumer> listConsumer = consumerDao.getAll(streamName);
            return Response.ok().entity(listConsumer).build();
        } catch (StreamNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                            "Stream:" + streamName + " not found . Please create the Stream before trying to get all Consumers"))
                    .build();
        } catch (RuntimeException exception) {
            log.error("Error occurred while getting data from Stream Registry.", exception);
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry", exception);
        }
    }

    @PUT
    @ApiOperation(
        value = "Upsert a consumer with streamName in stream-registry",
        tags = "consumers",
        response = Consumer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Consumer upserted", response = Consumer.class),
        @ApiResponse(code = 404, message = "Stream not found"),
        @ApiResponse(code = 412, message = "unsupported region"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Path("/{consumerName}/regions/{region}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response upsertConsumer(
        @ApiParam(value = "Stream Name corresponding to the consumer", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "Consumer Name", required = true) @PathParam("consumerName") String consumerName,
        @ApiParam(value = "Consumer region. All region values available at /regions endpoint",
            required = true) @PathParam("region") String region) {
        try {
            Optional<Consumer> consumer = consumerDao.update(streamName, consumerName, region);
            log.info(" Consumer upserted, consumerName: " + consumerName);
            return Response.ok().entity(consumer.get()).build();
        } catch (StreamNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                            "Stream:" + streamName + " not found . Please create the Stream before registering a Consumer",
                            e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Input is wrong.", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),
                            "Input Validation failed.",
                            e.getMessage()))
                    .build();
        } catch (RegionNotFoundException re) {
            log.error("Region not supported " + region);
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),
                            "Unsupported region: " + re.getRegion() + ". Hit /regions to get the list of all supported regions",
                            re.getCause() !=null ? re.getMessage() + re.getCause().getMessage() : re.getMessage()))
                    .build();
        } catch (ClusterNotFoundException ce) {
            log.error("Cluster not available for stream-key:{} ", ce.getClusterName());
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),
                            "Cluster not available for " + ce.getClusterName(),
                            ce.getCause() !=null ? ce.getMessage() + ce.getCause().getMessage() : ce.getMessage()))
                    .build();
        } catch (RuntimeException e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            "Error while upserting consumer=" + consumerName,
                            e.getCause() !=null ? e.getMessage() + e.getCause().getMessage() : e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @ApiOperation(value = "Deletes Consumer within a stream", tags = "consumers")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Consumer deleted successfully"),
        @ApiResponse(code = 404, message = "Consumer not found"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data")})
    @Path("/{consumerName}")
    @Timed
    public Response deleteConsumer(
        @ApiParam(value = "Stream Name corresponding to the consumer", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "Consumer Name", required = true) @PathParam("consumerName") String consumerName) {
        try {
            consumerDao.delete(streamName, consumerName);
        } catch (StreamNotFoundException se) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                            "Stream:" + streamName + " not found . Please create the Stream before trying to delete the consumer"))
                    .build();
        } catch (ActorNotFoundException ce) {
            log.warn("Consumer not found: " + consumerName);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                            "Consumer:" + consumerName + " not found.",
                            ce.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            "Error while deleting consumer=" + consumerName,
                            e.getCause() !=null ? e.getMessage() + e.getCause().getMessage() : e.getMessage()))
                    .build();
        }
        log.info("Consumer successfully deleted: " + consumerName);
        return Response
            .ok()
            .type("text/plain")
            .entity("Consumer deleted " + consumerName)
            .build();
    }
}
