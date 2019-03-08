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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

import org.apache.avro.SchemaParseException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;

import com.homeaway.streamplatform.streamregistry.db.dao.StreamClientDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.exceptions.*;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.utils.StreamRegistryUtils;
import com.homeaway.streamplatform.streamregistry.utils.StreamRegistryUtils.EntriesPage;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Api(value = "Stream-registry API", description = "Stream Registry API, a centralized governance tool for managing streams.")
@Path("/v0/streams")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class StreamResource extends BaseResource{

    private static final int DEFAULT_STREAMS_PAGE_NUMBER = 0;

    private static final int DEFAULT_STREAMS_PAGE_SIZE = 10;

    private final StreamDao streamDao;
    private final StreamClientDao<Producer> producerDao;
    private final StreamClientDao<Consumer> consumerDao;

    public StreamResource(StreamDao streamDao, StreamClientDao<Producer> producerDao, StreamClientDao<Consumer> consumerDao) {
        this.streamDao = streamDao;
        this.producerDao = producerDao;
        this.consumerDao = consumerDao;
    }

    @PUT
    @ApiOperation(
        value = "Upsert stream",
        notes = "Create/Update a stream and its meta-data",
        tags = "streams")
    @ApiResponses(value = { @ApiResponse(code = 202, message = "Stream registration request accepted"),
        @ApiResponse(code = 400, message = "Validation Exception while creating a stream."),
        @ApiResponse(code = 412, message = "Exception occurred as requested cluster is not supported"),
        @ApiResponse(code = 500, message = "Error occured while registering a Stream") })
    @Path("/{streamName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response upsertStream(@ApiParam(value = "stream name", required = true) @PathParam("streamName") String streamName,
                                 @ApiParam(value = "stream entity", required = true) Stream stream) {
        try {
            if (stream.getName() == null || !stream.getName().equals(streamName)) {
                throw new InvalidStreamException(String.format("Stream name provided in path param [%s] does not match that of the stream body [%s]",
                        streamName, stream.getName()));
            }
            streamDao.upsertStream(stream);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (InvalidStreamException | SchemaManagerException | StreamCreationException | UnsupportedOperationException | InvalidReplicationFactorException e) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, e);
        } catch (ClusterNotFoundException e) {
            return buildErrorMessage(Response.Status.PRECONDITION_FAILED, e);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @PUT
    @ApiOperation(
            value = "Validate stream schema compatibility",
            notes = "Validate compatibility of stream's schemas against an implementation of SchemaManager",
            tags = "streams")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Compatibility check succeeded"),
            @ApiResponse(code = 400, message = "Compatibility check failed!"),
            @ApiResponse(code = 500, message = "Error occurred while validating schemas") })
    @Path("/{streamName}/{schemaType}/compatibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response validateStreamCompatibility(@ApiParam(value = "stream entity", required = true) Stream stream,
                                                @ApiParam(value = "stream name", required = true) @PathParam("streamName") String streamName,
                                                @ApiParam(value = "schema type", allowableValues = "default", required = true) @PathParam("schemaType") String schemaType) {
        try {
            if (!streamName.equals(stream.getName())) {
                throw new InvalidStreamException(String.format("Stream name provided in path param [%s] does not match that of the stream body [%s]",
                        streamName, stream.getName()));
            }
            streamDao.validateSchemaCompatibility(stream);
            return Response.ok()
                    .type("text/plain")
                    .entity("Schema Validation Successful").build();
        } catch (InvalidStreamException | SchemaParseException | SchemaValidationException e) {
            return buildErrorMessage(Response.Status.BAD_REQUEST, e);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @Path("/{streamName}")
    @ApiOperation(
        value = "Get stream",
        notes = "Returns a single stream resource",
        tags = "streams",
        response = Stream.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "successful operation", response = Stream.class),
        @ApiResponse(code = 404, message = "Stream not found"),
        @ApiResponse(code = 500, message = "Error occured while getting a Stream.") })
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getStream(@ApiParam(value = "Stream name", required = true) @PathParam("streamName") String streamName) {
        try {
            Stream stream = streamDao.getStream(streamName);
            return Response.ok().entity(stream).build();
        } catch (StreamNotFoundException e) {
            return buildErrorMessage(Response.Status.NOT_FOUND, e);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @ApiOperation(
        value = "Get all streams",
        notes = "Get all streams from stream registry",
        tags = "streams",
        response = EntriesPage.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Stream(s) read successfully", response = EntriesPage.class),
        @ApiResponse(code = 500, message = "Error Occurred while getting data")})
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllStreams(
        @ApiParam(
            value = "Page number (zero based), default: 0",
            defaultValue = "" + DEFAULT_STREAMS_PAGE_NUMBER)
        @QueryParam("pageNumber") Optional<Integer> pageNumber,
        @ApiParam(
            value = "Page size",
            defaultValue = "" + DEFAULT_STREAMS_PAGE_SIZE)
        @QueryParam("pageSize") Optional<Integer> pageSize) {
        try {
            final int pSize = pageSize.orElse(DEFAULT_STREAMS_PAGE_SIZE);
            final int pNumber = pageNumber.orElse(DEFAULT_STREAMS_PAGE_NUMBER);
            final List<Stream> allStreams = streamDao.getAllStreams();
            final int totalSize = allStreams.size();

            List<Stream> streamsPage = Optional.ofNullable(StreamRegistryUtils
                .paginate(
                    allStreams,
                    pSize)
                .get(pNumber))
                .orElse(Collections.emptyList());

            return Response.ok()
                    .entity(StreamRegistryUtils.toEntriesPage(streamsPage, totalSize, pSize, pNumber))
                    .build();
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @DELETE
    @ApiOperation(value = "Delete stream with path param stream name", tags = "streams")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Stream deleted successfully"),
            @ApiResponse(code = 404, message = "Stream not found"),
            @ApiResponse(code = 500, message = "Error Occurred while deleting the stream")})
    @Path("/{streamName}")
    @Timed
    public Response deleteStream(@ApiParam(value = "Stream object that needs to be deleted from the Stream Registry", required = true)
    @PathParam("streamName") String streamName) {
        try {
            streamDao.deleteStream(streamName);
            return Response.ok()
                    .entity(String.format("Stream=%s deleted successfully", streamName))
                    .build();
        } catch (StreamNotFoundException e) {
            return buildErrorMessage(Response.Status.NOT_FOUND, e);
        } catch (RuntimeException e) {
            return buildErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }


    @Path("/{streamName}/producers")
    public ProducerResource getProducerResource(){
        return new ProducerResource(producerDao);
    }

    @Path("/{streamName}/consumers")
    public ConsumerResource getConsumerResource(){
        return new ConsumerResource(consumerDao);
    }

}
