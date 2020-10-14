/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.accesscontrol;

import java.io.Serializable;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.accesscontrol.AccessControlAuthoriser;
import com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AccessType;
import com.expediagroup.streamplatform.streamregistry.core.accesscontrol.resourcemapper.AccessControlledResourceMapper;

@Component
@Slf4j
public class PermissionEvaluatorImpl implements PermissionEvaluator {

  private final AccessControlAuthoriser accessControlAuthoriser;
  private final List<AccessControlledResourceMapper> accessControlledResourceMappers;
  private final AccessTypeMapper accessTypeMapper;

  public PermissionEvaluatorImpl(AccessControlAuthoriser accessControlAuthoriser, List<AccessControlledResourceMapper> accessControlledResourceMappers, AccessTypeMapper accessTypeMapper) {
    this.accessControlAuthoriser = accessControlAuthoriser;
    this.accessControlledResourceMappers = accessControlledResourceMappers;
    this.accessTypeMapper = accessTypeMapper;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
                               Object permission) {

    AccessControlledResourceMapper accessControlledResourceMapper = accessControlledResourceMappers
        .stream().filter(anAccessControlledResourceMapper -> anAccessControlledResourceMapper
            .canMap(targetDomainObject)).findFirst().orElseThrow(() -> new IllegalStateException("no AccessControlledResourceMapper found"));

    AccessControlledResource accessControlledResource = accessControlledResourceMapper.map(targetDomainObject);
    AccessType accessType = accessTypeMapper.map(permission);

    return accessControlAuthoriser.hasAccess(accessControlledResource, accessType, authentication);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
                               String targetType, Object permission) {
    return true;
  }
}
