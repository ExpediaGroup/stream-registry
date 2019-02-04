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

import static com.homeaway.streamplatform.streamregistry.model.SourceType.SOURCE_TYPES;

import java.util.List;
import java.util.Optional;

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

import com.homeaway.streamplatform.streamregistry.db.dao.SourceDao;
import com.homeaway.streamplatform.streamregistry.exceptions.SourceNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.UnsupportedSourceType;
import com.homeaway.streamplatform.streamregistry.model.Source;
import com.homeaway.streamplatform.streamregistry.utils.ResourceUtils;

@Slf4j
public class SourceResource {

    private final SourceDao sourceDao;

    @SuppressWarnings("WeakerAccess")
    public SourceResource(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @PUT
    @ApiOperation(
            value = "Register source with a stream",
            notes = "Register a source with a stream",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source was upserted successfully"),
            @ApiResponse(code = 500, message = "Error Occurred while getting data")})
    @Path("/{source}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response putSource(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
                              @ApiParam(value = "source entity", required = true) Source source) {
        try {
            boolean isSupportedType = SOURCE_TYPES.stream()
                    .anyMatch(sourceType -> source.getSourceType().equalsIgnoreCase(sourceType));

            if (isSupportedType) {
                sourceDao.upsert(source);
            } else {
                throw new UnsupportedSourceType(source.getSourceType());
            }
        } catch (UnsupportedSourceType e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Unsupported source type. Please refer to /sourcetypes for supported types")
                    .build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e)
                    .build();
        }
        return Response
                .status(Response.Status.ACCEPTED)
                .build();
    }

    @GET
    @Path("/{sourceName}")
    @ApiOperation(
            value = "Get source",
            notes = "Get the source associated with this stream",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Returns Source information", response = Source.class),
            @ApiResponse(code = 404, message = "Stream or Source not found"),
            @ApiResponse(code = 500, message = "Error Occurred while getting data")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getSource(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
                              @ApiParam(value = "name of the source", required = true) @PathParam("sourceName") String sourceName) {

        try {
            Optional<Source> source = sourceDao.get(streamName, sourceName);

            if (!source.isPresent()) {
                return ResourceUtils.notFound(sourceName);
            }
            return Response.ok().entity(source.get()).build();
        } catch (Exception e) {
            log.error("Error occurred while deleting data from Stream Registry.", e);
            throw new InternalServerErrorException("Error occurred while getting source", e);
        }

    }

    @DELETE
    @ApiOperation(
            value = "De-register source",
            notes = "De-Registers a source from a stream",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Source successfully deleted"),
            @ApiResponse(code = 404, message = "Stream or source not found"),
            @ApiResponse(code = 500, message = "Error Occurred while getting data")})
    @Path("/{sourceName}")
    @Timed
    public Response deleteSource(@ApiParam(value = "name of the stream", required = true) @PathParam("streamName") String streamName,
                                 @ApiParam(value = "name of the source", required = true) @PathParam("sourceName") String sourceName) {
        try {
            sourceDao.delete(streamName, sourceName);
        } catch (SourceNotFoundException pe) {
            log.warn("Source not found ", sourceName);
            return ResourceUtils.notFound("Source not found " + sourceName);
        } catch (Exception e) {
            log.error("Error occurred while deleting data from Stream Registry.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
                .ok()
                .type("text/plain")
                .entity("Source deleted " + sourceName)
                .build();
    }

    @GET
    @Path("/")
    @ApiOperation(
            value = "Get all sources for a given Stream",
            notes = "Gets a list of sources for a given stream",
            tags = "sources",
            response = Source.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Returns all sources for a given stream", response = List.class),
            @ApiResponse(code = 500, message = "Error Occurred while getting data"),
            @ApiResponse(code = 404, message = "Stream not found")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllSourcesByStream(
            @ApiParam(value = "Stream Name corresponding to the source", required = true) @PathParam("streamName") String streamName) {
        try {
            List<Source> sources = sourceDao.getAll(streamName);

            if (sources.size() == 0) {
                Response.status(Response.Status.NOT_FOUND.getStatusCode(), "Stream not found").build();
            }
            return Response.status(Response.Status.OK.getStatusCode()).entity(sources).build();
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while getting data from Stream Registry");
        }
    }

}
