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
package com.homeaway.streamplatform.streamregistry.db.dao;

import java.util.List;
import java.util.Map;

import com.homeaway.streamplatform.streamregistry.exceptions.StreamSourceException;
import com.homeaway.streamplatform.streamregistry.model.StreamSource;

/**
 * This interface is implemented by a stream provider to provide
 * stream platform and/or company specific requirements related to
 * stream sources.
 */
public interface StreamSourceManager {
    /**
     * Returns a list of supported source types.
     * The actual list will vary by stream provider.
     *
     * @param cluster the target cluster for provisioned source types
     * @return the available source types for the given cluster
     * @throws StreamSourceException This is thrown when         the operation could not complete.
     */
    List<String> getSourceTypes(String cluster) throws StreamSourceException;

    /**
     * Creates the stream source in the underlying stream platform.
     * ConfigMaps guide the creation of the stream.
     *
     * @param sourceName   the name of the source to inserting.
     * @param type         the type of the source to inserting. Must be one of the supported types.
     * @param clusterName  the name of the cluster that the stream should be created on.
     * @param configMap the map of configmaps.
     * @return the created stream source
     * @throws StreamSourceException This is thrown when         the operation could not complete.
     */
    StreamSource createStreamSource(String sourceName, String type,
                                    String clusterName,
                                    Map<String, String> configMap)
            throws StreamSourceException;

    /**
     * Retrieves a StreamSource from the underlying stream provider.
     *
     * @param sourceName the name of the source to retrieve
     * @return the requested stream source
     * @throws StreamSourceException This is thrown when         the operation could not complete.
     */
    StreamSource getStreamSource(String sourceName)
            throws StreamSourceException;

    /**
     * Updates the stream source in the underlying stream provider
     * with the supplied config maps.
     *
     * @param sourceName   the name of the stream source to updating.
     * @param configMap the map of config maps -- applicable overrides apply.
     * @return the updated stream source
     * @throws StreamSourceException This is thrown when         the operation could not complete.
     */
    StreamSource updateStreamSource(String sourceName,
                                    Map<String, Map> configMap)
            throws StreamSourceException;

    /**
     * Deletes the stream source in the given stream platform.
     *
     * @param sourceName the name of the stream source to deleting
     * @return the deleted stream source
     * @throws StreamSourceException This is thrown when         the operation could not complete.
     */
    StreamSource deleteStreamSource(String sourceName) throws StreamSourceException;

    /**
     * Restart stream source.
     *
     * @param sourceName the source name
     * @return the stream source
     * @throws StreamSourceException the stream source exception
     */
    StreamSource restartStreamSource(String sourceName) throws StreamSourceException;

    /**
     * Pause stream source.
     *
     * @param sourceName the source name
     * @return the stream source
     * @throws StreamSourceException the stream source exception
     */
    StreamSource pauseStreamSource(String sourceName) throws StreamSourceException;

    /**
     * Resume stream source.
     *
     * @param sourceName the source name
     * @return the stream source
     * @throws StreamSourceException the stream source exception
     */
    StreamSource resumeStreamSource(String sourceName) throws StreamSourceException;


    /**
     * Gets the current status of the Source
     *
     * @param sourceName the source name
     * @return the status
     * @throws StreamSourceException the stream source exception
     */
    String getStatus(String sourceName) throws StreamSourceException;


}
