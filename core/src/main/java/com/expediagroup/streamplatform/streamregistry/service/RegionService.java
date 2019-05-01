/* Copyright (c) 2018-Present Expedia Group.
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

import java.util.Collection;
import java.util.Set;

import com.expediagroup.streamplatform.streamregistry.model.Hint;

// TODO consider splitting this interface into a ClusterService and NamespaceDao
/**
 * The interface for the Region dao.
 */
public interface RegionService {
    // TODO hint name needs to become clustername or stream-binding
    // TODO this method needs to move to a ClusterService
    /**
     * Returns a Collection of {@link Hint}s
     *
     * @return Collection of hints
     */
    Collection<Hint> getHints();

    // TODO regions need to become namespaces
    // TODO there needs to be knowledge of a parent namespace
    // TODO there needs to be knowledge of the current namespace
    /**
     * Get supported regions for a specific {@link Hint} and environment.
     *
     * @param hint {@link Hint}
     * @return supported regions
     */
    Set<String> getSupportedRegions(String hint);
}
