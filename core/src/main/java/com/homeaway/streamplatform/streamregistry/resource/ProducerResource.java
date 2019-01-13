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

import javax.ws.rs.BadRequestException;
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
import com.homeaway.streamplatform.streamregistry.exceptions.ProducerNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.UnknownRegionException;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.utils.ResourceUtils;

@Slf4j
public class ProducerResource {

    private final StreamDao streamDao;

    private final StreamClientDao<Producer> producerDao;

    @SuppressWarnings("WeakerAccess")
    public ProducerResource(StreamDao streamDao, StreamClientDao<Producer> producerDao) {
        this.streamDao = streamDao;
        this.producerDao = producerDao;
    }

    @PUT
    @ApiOperation(
        value = "Register producer",
        notes = "Register a producer with a stream. Typically made at beginning of app life-cycle.",
        tags = "producers",
        response = Producer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Producer information", response = Producer.class),
        @ApiResponse(code = 404, message = "Stream not found"),
        @ApiResponse(code = 412, message = "unsupported region"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Path("/{producerName}/regions/{region}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response upsertProducer(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "name of the producer", required = true) @PathParam("producerName") String producerName,
        @ApiParam(value = "name of the region. All region values available at /regions endpoint",
            required = true) @PathParam("region") String region) {
        try {
            Optional<Stream> stream = streamDao.getStream(streamName);
            if (!stream.isPresent()) {
                return ResourceUtils.streamNotFound(streamName);
            }
            Optional<Producer> producer = producerDao.update(streamName, producerName, region);
            if (producer.isPresent()) {
                log.info(" Producer upserted, producerName: " + producerName);
                return Response.ok().entity(producer.get()).build();
            }
        } catch (IllegalArgumentException e) {
            log.error("Input is wrong.", e);
            throw new BadRequestException("Input Validation failed. Message=" + e.getMessage(), e);
        } catch (UnknownRegionException re) {
            log.warn("Region not supported " + region);
            return Response.status(Response.Status.PRECONDITION_FAILED)
                .type("text/plain")
                .entity("Unsupported region: " + re.getRegion() + ". Hit /regions to get the list of all supported regions")
                .build();
        } catch (ClusterNotFoundException ce) {
            log.warn("Region {} is not supported for the stream {}", region, streamName);
            return Response.status(Response.Status.PRECONDITION_FAILED)
                .type("text/plain")
                .entity("This region: " + region + " is not available for the stream")
                .build();
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            throw new InternalServerErrorException("Error occurred while updating the Producer in Stream Registry", e);
        }
        return null;
    }

    @GET
    @Path("/{producerName}")
    @ApiOperation(
        value = "Get producer",
        notes = "Get a producer associated with the stream",
        tags = "producers",
        response = Producer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns Producer information", response = Producer.class),
        @ApiResponse(code = 404, message = "Stream or Producer not found"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getProducer(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "name of the producer", required = true) @PathParam("producerName") String producerName) {
        Optional<Stream> stream = streamDao.getStream(streamName);
        try {
            if (!stream.isPresent()) {
                return ResourceUtils.streamNotFound(streamName);
            }
            Optional<Producer> responseProducer = producerDao.get(streamName, producerName);
            if (!responseProducer.isPresent()) {
                log.warn("Producer Not Found: " + producerName);
                return ResourceUtils.notFound("Producer not found " + producerName);
            }
            return Response.ok().entity(responseProducer.get()).build();
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry", e);
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry", e);
        }
    }

    @DELETE
    @ApiOperation(
        value = "De-register producer",
        notes = "De-Registers a producer from a stream. Typically made at end of app life-cycle.",
        tags = "producers")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Producer successfully deleted"),
        @ApiResponse(code = 404, message = "Stream or Producer not found"),
        @ApiResponse(code = 500, message = "Error Occurred while getting data") })
    @Path("/{producerName}")
    @Timed
    public Response deleteProducer(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
        @ApiParam(value = "name of the producer", required = true) @PathParam("producerName") String producerName) {
        try {
            producerDao.delete(streamName, producerName);
        } catch (StreamNotFoundException se) {
            return ResourceUtils.streamNotFound(streamName);
        } catch (ProducerNotFoundException pe) {
            log.warn("Producer not found ", producerName);
            return ResourceUtils.notFound("Producer not found " + pe.getProducerName());
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
            .ok()
            .type("text/plain")
            .entity("Producer deleted " + producerName)
            .build();
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
        @ApiResponse(code = 404, message = "Stream not found") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllProducers(
        @ApiParam(value = "Stream Name corresponding to the producer", required = true) @PathParam("streamName") String streamName) {
        try {
            Optional<Stream> stream = streamDao.getStream(streamName);
            if (!stream.isPresent()) {
                return ResourceUtils.streamNotFound(streamName);
            }
            List<Producer> listProducer = producerDao.getAll(streamName);
            return Response.ok().entity(listProducer).build();
        } catch (Exception e) {
            log.error("Error occurred while getting data from Stream Registry.", e);
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry");
        }
    }

}
