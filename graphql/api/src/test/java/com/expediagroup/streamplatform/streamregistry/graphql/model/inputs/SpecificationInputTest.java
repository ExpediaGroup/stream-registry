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
package com.expediagroup.streamplatform.streamregistry.graphql.model.inputs;

import com.expediagroup.streamplatform.streamregistry.model.Principal;
import com.expediagroup.streamplatform.streamregistry.model.Security;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SpecificationInputTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private final List<SecurityInput> securityInputs = Collections.singletonList(
    SecurityInput.builder().role("admin").principals(
      Collections.singletonList(PrincipalInput.builder().name("user").build())
    ).build()
  );
  private final Specification emptySecurity = new Specification(
    "description", Collections.emptyList(), "type",
    mapper.createObjectNode(), Collections.emptyList()
  );
  private final Specification withSecurity = new Specification(
    "description", Collections.emptyList(), "type", mapper.createObjectNode(),
    Collections.singletonList(
      new Security("admin", Collections.singletonList(new Principal("user")))
    )
  );

  @Test
  public void nullSecurityToEmptyList() {
    assertThat(getSpecificationInputWithSecurity(null).asSpecification(), is(emptySecurity));
  }

  @Test
  public void emptySecurityToEmptyList() {
    assertThat(getSpecificationInputWithSecurity(Collections.emptyList()).asSpecification(), is(emptySecurity));
  }

  @Test
  public void validSecurityList() {
    assertThat(getSpecificationInputWithSecurity(securityInputs).asSpecification(), is(withSecurity));

  }

  private SpecificationInput getSpecificationInputWithSecurity(List<SecurityInput> security) {
    return SpecificationInput.builder()
      .description("description")
      .tags(Collections.emptyList())
      .type("type")
      .configuration(mapper.createObjectNode())
      .security(security)
      .build();
  }
}
