/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.*;
import com.expediagroup.streamplatform.streamregistry.model.*;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.*;

public class ProcessBindingFilterTest {

  private static final String REGEX = "match.*";
  private static final String REGEX2 = "match[^_]*";
  private static final String MATCH = "match_this";
  private static final String MATCH2 = "matchthat";
  private static final String FAIL = "fail_this";

  private ProcessBindingFilter processBindingFilter;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void noFilters() {
    processBindingFilter = new ProcessBindingFilter(null, null, null, null, null);

    ProcessBinding processBinding = new ProcessBinding();
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setKey(matchingProcessBindingKey());
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setZone(new ZoneKey(MATCH));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Collections.singletonList(MATCH)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Collections.singletonList(MATCH)));
    assertTrue(processBindingFilter.test(processBinding));
  }

  @Test
  public void filterBySpecification() {
    TagQuery tagQuery = TagQuery.builder().nameRegex(REGEX).valueRegex(REGEX).build();

    SpecificationQuery specificationQuery = SpecificationQuery.builder()
        .typeRegex(REGEX)
        .descriptionRegex(REGEX)
        .tags(Collections.singletonList(tagQuery))
        .build();

    processBindingFilter = new ProcessBindingFilter(null, specificationQuery, null, null, null);

    ProcessBinding processBinding = new ProcessBinding();
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().setType(FAIL);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().setDescription(FAIL);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().getTags().get(0).setName(FAIL);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().getTags().get(0).setValue(FAIL);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().setType(null);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().setDescription(null);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().getTags().get(0).setName(null);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().getTags().get(0).setValue(null);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setSpecification(matchingSpecification());
    processBinding.getSpecification().setTags(null);
    assertFalse(processBindingFilter.test(processBinding));
  }

  @Test
  public void filterByProcessBindingKey() {
    ProcessBindingKeyQuery processBindingKeyQuery = ProcessBindingKeyQuery.builder()
      .domainNameRegex(REGEX)
      .processNameRegex(REGEX)
      .infrastructureZoneRegex(REGEX)
      .build();

    processBindingFilter = new ProcessBindingFilter(processBindingKeyQuery, null, null, null, null);

    ProcessBinding processBinding = new ProcessBinding();
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setKey(matchingProcessBindingKey());
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setKey(matchingProcessBindingKey());
    processBinding.getKey().setDomainName(FAIL);
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setKey(matchingProcessBindingKey());
    processBinding.getKey().setProcessName(FAIL);
    assertFalse(processBindingFilter.test(processBinding));
  }

  @Test
  public void filterByZoneKey() {
    ZoneKeyQuery zoneKeyQuery = ZoneKeyQuery.builder().nameRegex(REGEX).build();

    processBindingFilter = new ProcessBindingFilter(null, null, zoneKeyQuery, null, null);

    ProcessBinding processBinding = new ProcessBinding();
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setZone(new ZoneKey(MATCH));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setZone(new ZoneKey(FAIL));
    assertFalse(processBindingFilter.test(processBinding));
  }

  @Test
  public void filterByInputs() {
    StreamBindingKeyQuery streamBindingKeyQuery1 = StreamBindingKeyQuery.builder().streamNameRegex(REGEX).build();
    StreamBindingKeyQuery streamBindingKeyQuery2 = StreamBindingKeyQuery.builder().streamNameRegex(REGEX2).build();

    processBindingFilter = new ProcessBindingFilter(null, null, null, Collections.singletonList(streamBindingKeyQuery1), null);

    ProcessBinding processBinding = new ProcessBinding();
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Collections.singletonList(MATCH)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Arrays.asList(MATCH, FAIL)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Collections.singletonList(FAIL)));
    assertFalse(processBindingFilter.test(processBinding));

    processBindingFilter = new ProcessBindingFilter(null, null, null, Arrays.asList(streamBindingKeyQuery1, streamBindingKeyQuery2), null);

    processBinding.setInputs(inputList(Collections.singletonList(MATCH)));
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Arrays.asList(MATCH, FAIL)));
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setInputs(inputList(Collections.singletonList(FAIL)));
    assertFalse(processBindingFilter.test(processBinding));
  }

  @Test
  public void filterByOutputs() {
    StreamBindingKeyQuery streamBindingKeyQuery1 = StreamBindingKeyQuery.builder().streamNameRegex(REGEX).build();
    StreamBindingKeyQuery streamBindingKeyQuery2 = StreamBindingKeyQuery.builder().streamNameRegex(REGEX2).build();

    processBindingFilter = new ProcessBindingFilter(null, null, null, null, Collections.singletonList(streamBindingKeyQuery1));

    ProcessBinding processBinding = new ProcessBinding();
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Collections.singletonList(MATCH)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Arrays.asList(MATCH, FAIL)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Collections.singletonList(FAIL)));
    assertFalse(processBindingFilter.test(processBinding));

    processBindingFilter = new ProcessBindingFilter(null, null, null, null, Arrays.asList(streamBindingKeyQuery1, streamBindingKeyQuery2));

    processBinding.setOutputs(outputList(Collections.singletonList(MATCH)));
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Arrays.asList(MATCH, FAIL)));
    assertFalse(processBindingFilter.test(processBinding));

    processBinding.setOutputs(outputList(Collections.singletonList(FAIL)));
    assertFalse(processBindingFilter.test(processBinding));
  }

  private List<ProcessInputStreamBinding> inputList(List<String> names) {
    return names.stream().map(name -> new ProcessInputStreamBinding(
      new StreamBindingKey("domain", name, 1, "zone", "infrastructure"),
      "type", objectMapper.createObjectNode())
    ).collect(Collectors.toList());
  }

  private List<ProcessOutputStreamBinding> outputList(List<String> names) {
    return names.stream().map(name -> new ProcessOutputStreamBinding(
      new StreamBindingKey("domain", name, 1, "zone", "infrastructure"),
      "type", objectMapper.createObjectNode()
    )).collect(Collectors.toList());
  }

  private ProcessBindingKey matchingProcessBindingKey() {
    ProcessBindingKey processBindingKey = new ProcessBindingKey();
    processBindingKey.setDomainName(MATCH);
    processBindingKey.setProcessName(MATCH);
    processBindingKey.setInfrastructureZone(MATCH);
    return processBindingKey;
  }

  private Specification matchingSpecification() {
    Tag tag = new Tag();
    tag.setName(MATCH);
    tag.setValue(MATCH);

    Specification specification = new Specification();
    specification.setDescription(MATCH);
    specification.setTags(Collections.singletonList(tag));
    specification.setType(MATCH);
    return specification;
  }
}
