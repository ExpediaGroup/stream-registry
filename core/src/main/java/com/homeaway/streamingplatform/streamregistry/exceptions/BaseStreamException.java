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
package com.homeaway.streamingplatform.streamregistry.exceptions;

import com.homeaway.streamingplatform.streamregistry.model.Stream;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseStreamException extends RuntimeException {
    private static final long serialVersionUID = 3286563758640098316L;
    protected final String streamName;

    public BaseStreamException(Throwable cause, String streamName) {
        super(cause);
        this.streamName = streamName;
    }

    public BaseStreamException(String streamName) {
        this.streamName = streamName;
    }

    public BaseStreamException(Stream stream) {
        this.streamName = stream.getName();
    }

    public String getStreamName() {
        return streamName;
    }

}
