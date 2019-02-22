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
import javax.ws.rs.PUT;
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

import com.homeaway.streamplatform.streamregistry.db.dao.SourceDao;
import com.homeaway.streamplatform.streamregistry.exceptions.SourceNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.UnsupportedSourceTypeException;
import com.homeaway.streamplatform.streamregistry.model.Source;
import com.homeaway.streamplatform.streamregistry.utils.ResourceUtils;

@Api(value = "Source resource API", description = "Sources is used to manage Source Change Data Capture events")
@Path("/v0/sources")
@Slf4j
public class SourceResource {

    private final SourceDao sourceDao;

    public SourceResource(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @PUT
    @ApiOperation(
            value = "Insert source",
            notes = "Register source in the Stream registry",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source insert request accepted"),
            @ApiResponse(code = 400, message = "SourceType is not supported. Refer to /sourceTypes for supported types"),
            @ApiResponse(code = 500, message = "Error inserting new source")})
    @Path("/{source}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response insert(@ApiParam(value = "source entity", required = true) Source source) {
        try {
            sourceDao.inserting(source);
        } catch (UnsupportedSourceTypeException e) {
            Response.status(Response.Status.NOT_FOUND)
                    .entity("SourceType not found. Refer to /sourceTypes to find supported types")
                    .build();
        } catch (Exception e) {
            log.error("Error inserting source - {}", source.getSourceName(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
                .status(Response.Status.ACCEPTED)
                .build();
    }

    @PUT
    @ApiOperation(
            value = "Update source",
            notes = "Update source in the Stream registry",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source update request accepted"),
            @ApiResponse(code = 400, message = "SourceType is not supported. Refer to /sourceTypes for supported types"),
            @ApiResponse(code = 500, message = "Error inserting new source")})
    @Path("/{source}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response update(@ApiParam(value = "source entity", required = true) Source source) {
        try {
            sourceDao.updating(source);
        } catch (UnsupportedSourceTypeException e) {
            Response.status(Response.Status.NOT_FOUND)
                    .entity("SourceType not found. Refer to /sourceTypes to find supported types")
                    .build();
        } catch (Exception e) {
            log.error("Error inserting source - {}", source.getSourceName(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
                .status(Response.Status.ACCEPTED)
                .build();
    }

    @GET
    @Path("/{sourceName}")
    @ApiOperation(
            value = "Get source",
            notes = "Get the source for the given sourceName",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Returns Source Entity", response = Source.class),
            @ApiResponse(code = 404, message = "No source found for sourceName"),
            @ApiResponse(code = 500, message = "Error Occurred retrieving source information")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response get(@ApiParam(value = "name of the source", required = true)
                        @PathParam("sourceName") String sourceName) {
        try {
            Optional<Source> source = sourceDao.get(sourceName);
            if (source.isPresent()) {
                return Response.ok().entity(source.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Error getting source - {}", sourceName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{sourceName}/status")
    @ApiOperation(
            value = "Get source status",
            notes = "Get the current status of the source for the given sourceName",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Returns Source Entity", response = Source.class),
            @ApiResponse(code = 404, message = "No source found for sourceName"),
            @ApiResponse(code = 500, message = "Error Occurred retrieving source information")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getStatus(@ApiParam(value = "name of the source", required = true)
                              @PathParam("sourceName") String sourceName) {
        try {
            String status = sourceDao.getStatus(sourceName);
            return Response.ok().entity(status).build();
        } catch (SourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error getting source - {}", sourceName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{sourceName}/starting")
    @ApiOperation(
            value = "Start source",
            notes = "Start source for the given sourceName",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source starting request accepted"),
            @ApiResponse(code = 404, message = "No source found for sourceName"),
            @ApiResponse(code = 500, message = "Error Occurred retrieving source information")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response start(@ApiParam(value = "name of the source", required = true)
                                @PathParam("sourceName") String sourceName) {
        try {
            sourceDao.starting(sourceName);
        } catch (SourceNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Source not found")
                    .build();
        } catch (Exception e) {
            log.error("Error starting source - {}", sourceName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
                .status(Response.Status.ACCEPTED)
                .build();
    }

    @PUT
    @Path("/{sourceName}/pausing")
    @ApiOperation(
            value = "Pause source",
            notes = "Pause source for the given sourceName",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source pausing request accepted"),
            @ApiResponse(code = 404, message = "No source found for sourceName"),
            @ApiResponse(code = 500, message = "Error Occurred retrieving source information")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response pause(@ApiParam(value = "name of the source", required = true)
                          @PathParam("sourceName") String sourceName) {
        try {
            sourceDao.pausing(sourceName);
        } catch (SourceNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Source not found")
                    .build();
        } catch (Exception e) {
            log.error("Error pausing source - {}", sourceName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
                .status(Response.Status.ACCEPTED)
                .build();
    }

    @PUT
    @Path("/{sourceName}/resuming")
    @ApiOperation(
            value = "Resume source",
            notes = "Resume source for the given sourceName",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source resuming request accepted"),
            @ApiResponse(code = 404, message = "No source found for sourceName"),
            @ApiResponse(code = 500, message = "Error Occurred retrieving source information")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response resume(@ApiParam(value = "name of the source", required = true)
                                 @PathParam("sourceName") String sourceName) {
        try {
            sourceDao.resuming(sourceName);
        } catch (SourceNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Source not found")
                    .build();
        } catch (Exception e) {
            log.error("Error resuming source - {}", sourceName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
                .status(Response.Status.ACCEPTED)
                .build();
    }

    @PUT
    @Path("/{sourceName}/stopping")
    @ApiOperation(
            value = "Stop source",
            notes = "Stop source for the given sourceName",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source stopping request accepted"),
            @ApiResponse(code = 404, message = "No source found for sourceName"),
            @ApiResponse(code = 500, message = "Error Occurred retrieving source information")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response stop(@ApiParam(value = "name of the source", required = true)
                               @PathParam("sourceName") String sourceName) {
        try {
            sourceDao.stopping(sourceName);
        } catch (SourceNotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Source not found")
                    .build();
        } catch (Exception e) {
            log.error("Error resuming source - {}", sourceName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response
                .status(Response.Status.ACCEPTED)
                .build();
    }

    @DELETE
    @ApiOperation(
            value = "Delete source",
            notes = "Deletes the source with given sourceName",
            tags = "sources")
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Source deleting request accepted"),
            @ApiResponse(code = 404, message = "Source not found"),
            @ApiResponse(code = 500, message = "Error Occurred while getting data")})
    @Path("/{sourceName}")
    @Timed
    public Response deleteSource(@ApiParam(value = "name of the source", required = true) @PathParam("sourceName") String sourceName) {
        try {
            sourceDao.deleting(sourceName);
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
            @ApiResponse(code = 500, message = "Error Occurred while getting data")})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllSourcesByStream() {
        try {
            return Response.ok().entity(sourceDao.getAll()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}