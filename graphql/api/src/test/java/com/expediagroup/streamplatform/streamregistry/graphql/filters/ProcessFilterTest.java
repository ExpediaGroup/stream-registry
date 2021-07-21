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

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.*;
import com.expediagroup.streamplatform.streamregistry.model.*;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

public class ProcessFilterTest {

  private static final String REGEX = "match.*";
  private static final String REGEX2 = "match[^_]*";
  private static final String MATCH = "match_this";
  private static final String MATCH2 = "matchthat";
  private static final String FAIL = "fail_this";

  private ProcessFilter processFilter;

  @Test
  public void noFilters() {
    processFilter = new ProcessFilter(null, null, null, null, null);

    Process process = new Process();
    assertTrue(processFilter.test(process));

    process.setKey(matchingProcessKey());
    assertTrue(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    assertTrue(processFilter.test(process));

    process.setZones(zoneKeyList(Collections.singletonList(MATCH)));
    assertTrue(processFilter.test(process));

    process.setInputs(inputList(Collections.singletonList(MATCH)));
    assertTrue(processFilter.test(process));

    process.setOutputs(outputList(Collections.singletonList(MATCH)));
    assertTrue(processFilter.test(process));
  }

  @Test
  public void filterBySpecification() {
    TagQuery tagQuery = TagQuery.builder().nameRegex(REGEX).valueRegex(REGEX).build();

    SpecificationQuery specificationQuery = SpecificationQuery.builder()
        .typeRegex(REGEX)
        .descriptionRegex(REGEX)
        .tags(Collections.singletonList(tagQuery))
        .build();

    processFilter = new ProcessFilter(null, specificationQuery, null, null, null);

    Process process = new Process();
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    assertTrue(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().setType(FAIL);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().setDescription(FAIL);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().getTags().get(0).setName(FAIL);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().getTags().get(0).setValue(FAIL);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().setType(null);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().setDescription(null);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().getTags().get(0).setName(null);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().getTags().get(0).setValue(null);
    assertFalse(processFilter.test(process));

    process.setSpecification(matchingSpecification());
    process.getSpecification().setTags(null);
    assertFalse(processFilter.test(process));
  }

  @Test
  public void filterByProcessKey() {
    ProcessKeyQuery processKeyQuery = ProcessKeyQuery.builder()
        .domainRegex(REGEX)
        .nameRegex(REGEX)
        .build();

    processFilter = new ProcessFilter(processKeyQuery, null, null, null, null);

    Process process = new Process();
    assertFalse(processFilter.test(process));

    process.setKey(matchingProcessKey());
    assertTrue(processFilter.test(process));

    process.setKey(matchingProcessKey());
    process.getKey().setDomain(FAIL);
    assertFalse(processFilter.test(process));

    process.setKey(matchingProcessKey());
    process.getKey().setName(FAIL);
    assertFalse(processFilter.test(process));
  }

  @Test
  public void filterByZoneKey() {
    ZoneKeyQuery zoneKeyQuery1 = ZoneKeyQuery.builder().nameRegex(REGEX).build();
    ZoneKeyQuery zoneKeyQuery2 = ZoneKeyQuery.builder().nameRegex(REGEX2).build();

    processFilter = new ProcessFilter(null, null, Collections.singletonList(zoneKeyQuery1), null, null);

    Process process = new Process();
    assertFalse(processFilter.test(process));

    process.setZones(zoneKeyList(Collections.singletonList(MATCH)));
    assertTrue(processFilter.test(process));

    process.setZones(zoneKeyList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processFilter.test(process));

    process.setZones(zoneKeyList(Arrays.asList(MATCH, FAIL)));
    assertTrue(processFilter.test(process));

    process.setZones(zoneKeyList(Collections.singletonList(FAIL)));
    assertFalse(processFilter.test(process));

    processFilter = new ProcessFilter(null, null, Arrays.asList(zoneKeyQuery1, zoneKeyQuery2), null, null);

    process.setZones(zoneKeyList(Collections.singletonList(MATCH)));
    assertFalse(processFilter.test(process));

    process.setZones(zoneKeyList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processFilter.test(process));

    process.setZones(zoneKeyList(Arrays.asList(MATCH, FAIL)));
    assertFalse(processFilter.test(process));

    process.setZones(zoneKeyList(Collections.singletonList(FAIL)));
    assertFalse(processFilter.test(process));
  }

  @Test
  public void filterByInputs() {
    StreamKeyQuery streamKeyQuery1 = StreamKeyQuery.builder().nameRegex(REGEX).build();
    StreamKeyQuery streamKeyQuery2 = StreamKeyQuery.builder().nameRegex(REGEX2).build();

    processFilter = new ProcessFilter(null, null, null, Collections.singletonList(streamKeyQuery1), null);

    Process process = new Process();
    assertFalse(processFilter.test(process));

    process.setInputs(inputList(Collections.singletonList(MATCH)));
    assertTrue(processFilter.test(process));

    process.setInputs(inputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processFilter.test(process));

    process.setInputs(inputList(Arrays.asList(MATCH, FAIL)));
    assertTrue(processFilter.test(process));

    process.setInputs(inputList(Collections.singletonList(FAIL)));
    assertFalse(processFilter.test(process));

    processFilter = new ProcessFilter(null, null, null, Arrays.asList(streamKeyQuery1, streamKeyQuery2), null);

    process.setInputs(inputList(Collections.singletonList(MATCH)));
    assertFalse(processFilter.test(process));

    process.setInputs(inputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processFilter.test(process));

    process.setInputs(inputList(Arrays.asList(MATCH, FAIL)));
    assertFalse(processFilter.test(process));

    process.setInputs(inputList(Collections.singletonList(FAIL)));
    assertFalse(processFilter.test(process));
  }

  @Test
  public void filterByOutputs() {
    StreamKeyQuery streamKeyQuery1 = StreamKeyQuery.builder().nameRegex(REGEX).build();
    StreamKeyQuery streamKeyQuery2 = StreamKeyQuery.builder().nameRegex(REGEX2).build();

    processFilter = new ProcessFilter(null, null, null, null, Collections.singletonList(streamKeyQuery1));

    Process process = new Process();
    assertFalse(processFilter.test(process));

    process.setOutputs(outputList(Collections.singletonList(MATCH)));
    assertTrue(processFilter.test(process));

    process.setOutputs(outputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processFilter.test(process));

    process.setOutputs(outputList(Arrays.asList(MATCH, FAIL)));
    assertTrue(processFilter.test(process));

    process.setOutputs(outputList(Collections.singletonList(FAIL)));
    assertFalse(processFilter.test(process));

    processFilter = new ProcessFilter(null, null, null, null, Arrays.asList(streamKeyQuery1, streamKeyQuery2));

    process.setOutputs(outputList(Collections.singletonList(MATCH)));
    assertFalse(processFilter.test(process));

    process.setOutputs(outputList(Arrays.asList(MATCH, MATCH2)));
    assertTrue(processFilter.test(process));

    process.setOutputs(outputList(Arrays.asList(MATCH, FAIL)));
    assertFalse(processFilter.test(process));

    process.setOutputs(outputList(Collections.singletonList(FAIL)));
    assertFalse(processFilter.test(process));
  }

  private List<ProcessInput> inputList(List<String> names) {
    return names.stream().map(name -> new ProcessInput(new StreamKey("domain", name, 1), "locality")).collect(Collectors.toList());
  }

  private List<ProcessOutput> outputList(List<String> names) {
    return names.stream().map(name -> new ProcessOutput(new StreamKey("domain", name, 1))).collect(Collectors.toList());
  }

  private List<ZoneKey> zoneKeyList(List<String> zones) {
    return zones.stream().map(ZoneKey::new).collect(Collectors.toList());
  }

  private ProcessKey matchingProcessKey() {
    ProcessKey processKey = new ProcessKey();
    processKey.setDomain(MATCH);
    processKey.setName(MATCH);
    return processKey;
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
