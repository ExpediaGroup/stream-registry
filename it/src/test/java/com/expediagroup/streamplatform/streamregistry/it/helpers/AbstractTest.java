/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import junit.framework.TestCase;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;

import org.junit.Before;

import com.expediagroup.streamplatform.streamregistry.it.StreamRegistryIT;

public abstract class AbstractTest {
    protected ITestDataFactory factory = new ITestDataFactory(getClass().getSimpleName().toLowerCase());

    protected Client client = StreamRegistryIT.client;

    @Before
    public final void before() {
        createRequiredDatastoreState();
    }

    public abstract void createRequiredDatastoreState();

    public Response assertMutationSucceeds(Mutation mutation) {
        try {
            return client.invoke(mutation);
        } catch (RuntimeException e) {
            TestCase.fail("Mutation failed");
        }
        return null;
    }

    public Mutation assertMutationFails(Mutation mutation) {
        if (mutation.getClass().getSimpleName().toLowerCase().contains("insert")) {
            assertRequiresObjectIsAbsent(mutation);
        }
        if (mutation.getClass().getSimpleName().toLowerCase().contains("update")) {
            assertRequiresObjectIsPresent(mutation);
        }
        return mutation;
    }

    public void assertRequiresObjectIsAbsent(Mutation m) {
        try {
            client.invoke(m);
            TestCase.fail("Expected a ValidationException");
        } catch (RuntimeException e) {
            assertEquals("Can't create because it already exists", e.getMessage());
        }
    }

    public void setFactorySuffix(String suffix) {
        factory = new ITestDataFactory(suffix);
        createRequiredDatastoreState();
    }

    public void assertRequiresObjectIsPresent(Mutation m) {
        try {
            client.invoke(m);
            TestCase.fail("Expected a ValidationException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().startsWith("Can't update"));
        }
    }
}
