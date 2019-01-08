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
package com.homeaway.streamingplatform.streamregistry.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.homeaway.streamingplatform.streamregistry.db.dao.AbstractDao;
import com.homeaway.streamingplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamingplatform.streamregistry.db.dao.impl.RegionDaoImpl;
import com.homeaway.streamingplatform.streamregistry.model.Hint;

public class RegionResourceIT extends BaseResourceIT {

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRegions(){
        RegionDao regionDao = new RegionDaoImpl(configuration.getEnv(), infraManager);
        RegionResource resource = new RegionResource(regionDao);
        Response response = resource.getRegions();

        Collection<Hint> hints = (Collection<Hint>)response.getEntity();

        Assert.assertEquals(new HashSet<>(Arrays.asList(US_EAST_REGION)),
                hints.stream().filter((hint) -> hint.getHint().equalsIgnoreCase(AbstractDao.PRIMARY_HINT)).findFirst().get().getVpcs());
        Assert.assertEquals(new HashSet<>(Arrays.asList(US_EAST_REGION)),
                hints.stream().filter((hint) -> hint.getHint().equalsIgnoreCase(BaseResourceIT.OTHER_HINT)).findFirst().get().getVpcs());
        Assert.assertEquals(new HashSet<>(Arrays.asList(US_WEST_REGION)),
                hints.stream().filter((hint) -> hint.getHint().equalsIgnoreCase(BaseResourceIT.SOME_HINT)).findFirst().get().getVpcs());
    }
}
