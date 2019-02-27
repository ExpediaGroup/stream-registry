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
package com.homeaway.streamplatform.streamregistry.resource;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.homeaway.streamplatform.streamregistry.model.JsonCluster;
import com.homeaway.streamplatform.streamregistry.utils.JsonModelBuilder;

public class ClusterResourceIT extends BaseResourceIT {

    @Test
    public void test_upsert_new_cluster() throws InterruptedException {
        String clusterName = "test_upsert_new_cluster";
        JsonCluster expectedCluster = JsonModelBuilder.buildJsonCluster(clusterName);
        clusterResource.upsertCluster(expectedCluster);

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response cluster = clusterResource.getCluster(clusterName);

        Assert.assertEquals(expectedCluster.getClusterValue(), cluster.getEntity());
    }

    @Test
    public void test_upsert_new_cluster_with_exception() throws InterruptedException {
        JsonCluster expectedCluster = JsonModelBuilder.buildJsonCluster(null);

        Response response = clusterResource.upsertCluster(expectedCluster);

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Assert.assertEquals(Response.Status.BAD_REQUEST, response.getStatus());
    }

}
