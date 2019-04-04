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
import java.util.Optional;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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

import com.homeaway.streamplatform.streamregistry.exceptions.ActorNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.RegionNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.service.StreamClientService;

@Slf4j
public class ProducerResource extends BaseResource{

    private final StreamClientService<Producer> producerDao;

    @SuppressWarnings("WeakerAccess")
    public ProducerResource(StreamClientService<Producer> producerDao) {
        this.producerDao = producerDao;
    }

    @PUT
    @ApiOperation(
        value = "Register producer",
        notes = "Register a producer with a stream. Typically made at beginning of app life-cycle.",
        tags = "producers",
        response = Producer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Producer information", response = Producer.class),
        @ApiResponse(code = 400, message = "Stream not found"),
        @ApiResponse(code = 412, message = "Exception occurred as requested cluster is not supported"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Path("/{producerName}/regions/{region}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response upsertProducer(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "name of the producer", required = true) @PathParam("producerName") String producerName,
        @ApiParam(value = "name of the region. All region values available at /regions endpoint",
            required = true) @PathParam("region") String region) {
        try {
            Optional<Producer> producer = producerDao.update(streamName, producerName, region);
            log.info("Producer={} upserted for stream={}", producerName, streamName);
            return Response.ok().entity(producer.get()).build();
        } catch (StreamNotFoundException  e) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, e);
        } catch (ClusterNotFoundException | RegionNotFoundException e) {
            return buildErrorMessage(Response.Status.PRECONDITION_FAILED, e);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @Path("/{producerName}")
    @ApiOperation(
        value = "Get producer",
        notes = "Get a producer associated with the stream",
        tags = "producers",
        response = Producer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Producer information", response = Producer.class),
        @ApiResponse(code = 404, message = "Producer not found"),
        @ApiResponse(code = 400, message = "Stream not found"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getProducer(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "name of the producer", required = true) @PathParam("producerName") String producerName) {
        try {
            Optional<Producer> responseProducer = producerDao.get(streamName, producerName);
            if (!responseProducer.isPresent()) {
                throw new ActorNotFoundException(String.format("Producer=%s not found for Stream=%s", producerName, streamName));
            }
            return Response.ok().entity(responseProducer.get()).build();
        } catch (ActorNotFoundException se) {
            return buildErrorMessage(Response.Status.NOT_FOUND, se);
        } catch (StreamNotFoundException se) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, se);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @DELETE
    @ApiOperation(
        value = "De-register producer",
        notes = "De-Registers a producer from a stream. Typically made at end of app life-cycle.",
        tags = "producers")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Producer successfully deleted"),
        @ApiResponse(code = 400, message = "Stream or Producer not found"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Path("/{producerName}")
    @Timed
    public Response deleteProducer(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "name of the producer", required = true) @PathParam("producerName") String producerName) {
        try {
            producerDao.delete(streamName, producerName);
            log.info("Producer={} of stream={} successfully deleted.", producerName, streamName);
            return Response
                    .ok()
                    .type("text/plain")
                    .entity(String.format("Producer=%s deleted.", producerName))
                    .build();
        } catch (StreamNotFoundException | ActorNotFoundException e) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, e);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @Path("/")
    @ApiOperation(
        value = "Get producers",
        notes = "Gets a list of producers for a given stream",
        tags = "producers",
        response = Producer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Producer information", response = Producer.class),
        @ApiResponse(code = 500, message = "Error Occurred while getting data"),
        @ApiResponse(code = 400, message = "Stream not found") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllProducers(
        @ApiParam(value = "Stream Name corresponding to the producer", required = true) @PathParam("streamName") String streamName) {
        try {
            List<Producer> listProducer = producerDao.getAll(streamName);
            return Response.ok().entity(listProducer).build();
        } catch (StreamNotFoundException e) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, e);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

}