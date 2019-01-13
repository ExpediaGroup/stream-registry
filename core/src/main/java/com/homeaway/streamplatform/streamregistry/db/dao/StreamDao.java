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
import java.util.Optional;

import com.homeaway.streamplatform.streamregistry.model.Stream;

// TODO - Need javadoc
// TODO - Probably might want to break this up into stream-bindings, clusters, sources, sinks and replications. Currently this is all a bit of a monolith
public interface StreamDao {

    void upsertStream(Stream stream);

    Optional<Stream> getStream(String streamName);

    void deleteStream(String streamName);

    List<Stream> getAllStreams();

    boolean validateStreamCompatibility(Stream stream);

}
