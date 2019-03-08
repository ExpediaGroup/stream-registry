/* Copyright (c) 2018-2019 Expedia Group.
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
import java.util.Optional;

import com.homeaway.streamplatform.streamregistry.exceptions.ActorNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.RegionNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.homeaway.streamplatform.streamregistry.model.StreamClient;

public interface StreamClientDao<T extends StreamClient> {

    /**
     * Insert or Update a Producer/Consumer of a Stream
     * @param streamName
     * @param actorName
     * @param region
     * @return a Producer or Consumer Object
     * @throws StreamNotFoundException - When a stream is not available for the given streamName
     * @throws RegionNotFoundException - When the input region is not supported
     * @throws ClusterNotFoundException - When a cluster could not be found for the given region.
     */
    Optional<T> update(String streamName, String actorName, String region) throws StreamNotFoundException, RegionNotFoundException, ClusterNotFoundException;

    /**
     * Pull the producer/consumer for the given name.
     * @param streamName
     * @param actorName
     * @return
     * @throws StreamNotFoundException - Stream not available for the given name
     */
    Optional<T> get(String streamName, String actorName) throws StreamNotFoundException;

    /**
     * Delete the producer/consumer for the give name
     * @param streamName
     * @param actorName
     * @throws StreamNotFoundException - When the given stream is not available
     * @throws ActorNotFoundException - When the given Producer/Consumer is not available
     */
    void delete(String streamName, String actorName) throws StreamNotFoundException, ActorNotFoundException;

    /**
     * Pull all the Producers or consumers for a given stream.
     * @param streamName
     * @return
     * @throws StreamNotFoundException - when the given stream is not available.
     */
    List<T> getAll(String streamName) throws StreamNotFoundException;

}
