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
package com.homeaway.streamplatform.streamregistry.db.dao.impl;

import java.util.*;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.db.dao.AbstractDao;
import com.homeaway.streamplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamplatform.streamregistry.model.Hint;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.resource.BaseResourceIT;

public class RegionDaoImplTest {

    private final InfraManager mockInfraManager = Mockito.mock(InfraManager.class);
    private String devEnv = "dev";
    private RegionDao dao = new RegionDaoImpl(devEnv, mockInfraManager);

    private ClusterKey createClusterKey(String env, String hint, String vpc) {
        return ClusterKey.newBuilder()
                .setEnv(env)
                .setHint(hint)
                .setVpc(vpc)
                .build();
    }

    @Test
    public void testGetHintRegionMap() {
        Map<ClusterKey, ClusterValue> allClusters = new HashMap<>();

        // primary supported regions
        allClusters.put(createClusterKey(devEnv, AbstractDao.PRIMARY_HINT, "aus-dts-1"), null);
        allClusters.put(createClusterKey(devEnv, AbstractDao.PRIMARY_HINT, "us-east-vpc"), null);
        allClusters.put(createClusterKey(devEnv, AbstractDao.PRIMARY_HINT, "us-west-vpc"), null);
        allClusters.put(createClusterKey(devEnv, AbstractDao.PRIMARY_HINT, "us-east-vpc-3"), null);
        allClusters.put(createClusterKey("test", AbstractDao.PRIMARY_HINT, "us-west-vpc-2"), null);

        // other-hint supported regions
        allClusters.put(ClusterKey.newBuilder().setEnv(devEnv).setHint(BaseResourceIT.OTHER_HINT).setVpc("us-east-vpc").setType("producer").build(), null);
        allClusters.put(ClusterKey.newBuilder().setEnv(devEnv).setHint(BaseResourceIT.OTHER_HINT).setVpc("us-east-vpc").setType("consumer").build(), null);

        // tiered clusters.
        allClusters.put(ClusterKey.newBuilder().setEnv(devEnv).setHint("tier-1").setVpc("us-east-vpc").build(), null);
        allClusters.put(ClusterKey.newBuilder().setEnv(devEnv).setHint("tier-2").setVpc("us-east-vpc").build(), null);
        allClusters.put(ClusterKey.newBuilder().setEnv(devEnv).setHint("tier-3").setVpc("aus-dts-1").build(), null);
        allClusters.put(ClusterKey.newBuilder().setEnv(devEnv).setHint("tier-4").setVpc("aus-dts-1").build(), null);
        allClusters.put(ClusterKey.newBuilder().setEnv(devEnv).setHint("tier-5").setVpc("aus-dts-1").build(), null);

        Mockito.when(mockInfraManager.getAllClusters()).thenReturn(allClusters);

        Collection<Hint> hintRegionMap = dao.getHints();

        // 5 tiered hints, primary, other-hint
        Assert.assertEquals(hintRegionMap.size(), 7);

        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase(AbstractDao.PRIMARY_HINT)).count(), 1);
        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase(AbstractDao.PRIMARY_HINT)).findFirst().get().getVpcs(),
                new HashSet<>(Arrays.asList("aus-dts-1", "us-east-vpc", "us-west-vpc", "us-east-vpc-3")));
        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase(BaseResourceIT.OTHER_HINT)).findFirst().get().getVpcs(),
                new HashSet<>(Arrays.asList("us-east-vpc")));
        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase("tier-1")).findFirst().get().getVpcs(),
                new HashSet<>(Arrays.asList("us-east-vpc")));
        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase("tier-2")).findFirst().get().getVpcs(),
                new HashSet<>(Arrays.asList("us-east-vpc")));
        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase("tier-3")).findFirst().get().getVpcs(),
                new HashSet<>(Arrays.asList("aus-dts-1")));
        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase("tier-4")).findFirst().get().getVpcs(),
                new HashSet<>(Arrays.asList("aus-dts-1")));
        Assert.assertEquals(hintRegionMap.stream().filter((hint) -> hint.getHint().equalsIgnoreCase("tier-5")).findFirst().get().getVpcs(),
                new HashSet<>(Arrays.asList("aus-dts-1")));
    }

}
