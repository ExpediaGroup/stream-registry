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
package com.homeaway.streamingplatform.extensions.schema;

import java.util.Map;

import com.homeaway.streamingplatform.exceptions.SchemaException;
import com.homeaway.streamingplatform.exceptions.SchemaManagerException;

public class SchemaManagerIT {

    public static class ValidSchemaManager implements SchemaManager {


        @Override
        public SchemaReference registerSchema(String subject, String schema) throws SchemaManagerException {
            return new SchemaReference("subject", 0, 0);
        }

        @Override
        public boolean checkCompatibility(String subject, String schema) throws SchemaException {
            return true;
        }

        @Override
        public void configure(Map<String, Object> configs) {

        }
    }
}
