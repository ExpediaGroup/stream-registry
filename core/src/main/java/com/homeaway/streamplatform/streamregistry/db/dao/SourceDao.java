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

import io.dropwizard.lifecycle.Managed;

import com.homeaway.streamplatform.streamregistry.exceptions.SourceNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.UnsupportedSourceTypeException;
import com.homeaway.streamplatform.streamregistry.model.Source;

/**
 * Source type
 */
public interface SourceDao extends Managed {

    void inserting(Source source) throws UnsupportedSourceTypeException;

    void updating(Source source) throws SourceNotFoundException;

    Optional<Source> get(String sourceName);

    void starting(String sourceName) throws SourceNotFoundException;

    void pausing(String sourceName) throws SourceNotFoundException;

    void resuming(String sourceName) throws SourceNotFoundException;

    void stopping(String sourceName) throws SourceNotFoundException;

    String getStatus(String sourceName) throws SourceNotFoundException;

    void deleting(String sourceName);

    List<Source> getAll();

}
