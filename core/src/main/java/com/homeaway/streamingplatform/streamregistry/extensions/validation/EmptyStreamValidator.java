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
package com.homeaway.streamingplatform.streamregistry.extensions.validation;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.homeaway.streamingplatform.streamregistry.exceptions.InvalidStreamException;
import com.homeaway.streamingplatform.streamregistry.model.Stream;

@SuppressWarnings("unused")
@Slf4j
public class EmptyStreamValidator implements StreamValidator {

    @Override
    public void configure(Map<String, ?> configs) {
    }

    @Override
    public boolean isStreamValid(Stream stream) throws InvalidStreamException {
        return true;
    }

    @Override
    public String getValidationAssertion() {
        return null;
    }
}