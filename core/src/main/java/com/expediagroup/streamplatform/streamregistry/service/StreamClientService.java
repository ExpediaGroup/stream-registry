/* Copyright (c) 2018-2019 Expedia, Inc.
 * All rights reserved.  http://www.expediagroup.com

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
package com.expediagroup.streamplatform.streamregistry.service;

import java.util.List;
import java.util.Optional;

import com.expediagroup.streamplatform.streamregistry.exceptions.ActorNotFoundException;
import com.expediagroup.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.exceptions.RegionNotFoundException;
import com.expediagroup.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.expediagroup.streamplatform.streamregistry.model.StreamClient;

public interface StreamClientService<T extends StreamClient> {

    /**
     * Insert or Update a Producer/Consumer of a Stream
     * @param streamName - the name of the Stream to insert or update the Producer/Consumer for
     * @param actorName - the name of the Producer/Consumer for the Stream
     * @param region - the region housing the Stream
     * @return a Producer or Consumer Object
     * @throws StreamNotFoundException - When a stream is not available for the given streamName
     * @throws RegionNotFoundException - When the input region is not supported
     * @throws ClusterNotFoundException - When a cluster could not be found for the given region.
     */
    Optional<T> update(String streamName, String actorName, String region) throws StreamNotFoundException, RegionNotFoundException, ClusterNotFoundException;

    /**
     * Pull the producer/consumer for the given name.
     * @param streamName - the name of the Stream for getting the available Producers/Consumers
     * @param actorName - the name of the Producer/Consumer
     * @return a Producer or Consumer Object
     * @throws StreamNotFoundException - Stream not available for the given name
     */
    Optional<T> get(String streamName, String actorName) throws StreamNotFoundException;

    /**
     * Delete the producer/consumer for the give name
     * @param streamName - the name of the Stream for deleting the Producers/Consumers against
     * @param actorName - the name of the Producer/Consumer to be deleted
     * @throws StreamNotFoundException - When the given stream is not available
     * @throws ActorNotFoundException - When the given Producer/Consumer is not available
     */
    void delete(String streamName, String actorName) throws StreamNotFoundException, ActorNotFoundException;

    /**
     * Pull all the Producers or consumers for a given stream.
     * @param streamName - the name of the Stream for retrieving the Producers/Consumers against
     * @return All of the Producers/Consumers for the Stream as a List
     * @throws StreamNotFoundException - when the given stream is not available.
     */
    List<T> getAll(String streamName) throws StreamNotFoundException;

}
