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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.homeaway.streamplatform.streamregistry.db.dao.StreamClientDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.ConsumerNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.UnknownRegionException;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.utils.ResourceUtils;

@Slf4j
public class ConsumerResource {
    private final StreamDao streamDao;
    private final StreamClientDao<Consumer> consumerDao;

    @SuppressWarnings("WeakerAccess")
    public ConsumerResource(StreamDao streamDao, StreamClientDao<Consumer> consumerDao) {
        this.streamDao = streamDao;
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
            Optional<Stream> stream = streamDao.getStream(streamName);
            if (!stream.isPresent()) {
                log.warn("Stream not found: {}", streamName);
                return ResourceUtils.notFound("Stream not found: " + streamName );
            }
            Optional<Consumer> responseConsumer = consumerDao.get(streamName, consumerName);
            if (!responseConsumer.isPresent()) {
                log.warn("Consumer not found: {}", consumerName);
                return ResourceUtils.notFound("consumer not found: " + consumerName);
            }
            return Response.ok().entity(responseConsumer.get()).build();
        } catch (Exception exception) {
            log.error("Error occurred while getting data from Stream Registry.", exception);
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry", exception);
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
            Optional<Stream> stream = streamDao.getStream(streamName);
            if (!stream.isPresent()) {
                log.warn("Stream not found " + streamName);
                return ResourceUtils.notFound("Stream not found " + streamName);
            }
            List<Consumer> listConsumer = consumerDao.getAll(streamName);
            return Response.ok().entity(listConsumer).build();
        } catch (Exception exception) {
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
            Optional<Stream> stream = streamDao.getStream(streamName);
            if (!stream.isPresent()) {
                return ResourceUtils.streamNotFound(streamName);
            }
            Optional<Consumer> consumer = consumerDao.update(streamName, consumerName, region);
            if (consumer.isPresent()) {
                log.info(" Consumer upserted, consumerName: " + consumerName);
                return Response.ok().entity(consumer.get()).build();
            }
        } catch (IllegalArgumentException e) {
            log.error("Input is wrong.", e);
            throw new IllegalArgumentException("Input Validation failed. Message=" + e.getMessage(), e);
        } catch (UnknownRegionException re) {
            log.warn("Region not supported " + region);
            return Response.status(Response.Status.PRECONDITION_FAILED)
                .type("text/plain")
                .entity("Unsupported region: " + re.getRegion() + ". Query /regions to get the list of all supported regions")
                .build();
        } catch (ClusterNotFoundException ce) {
            log.warn("Region {} is not supported for the stream {}",region, streamName);
            return Response.status(Response.Status.PRECONDITION_FAILED)
                .type("text/plain")
                .entity("This region: " + region + " is not available for the stream: "+streamName)
                .build();
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry", e);
        }
        return null;
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
            return ResourceUtils.streamNotFound(se.getStreamName());
        } catch (ConsumerNotFoundException ce) {
            log.warn("Consumer not found: " + consumerName);
            return ResourceUtils.notFound("Consumer not found: " + ce.getConsumerName());
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        log.info("Consumer successfully deleted: " + consumerName);
        return Response
            .ok()
            .type("text/plain")
            .entity("Consumer deleted " + consumerName)
            .build();
    }
}
