/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.expediagroup.streamplatform.streamregistry.core.services.SchemaService;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.graphql.InputHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

@RunWith(MockitoJUnitRunner.class)
public class SchemaMutationImplTest {

  @Mock
  private SchemaService schemaService;

  @Mock
  private SchemaView schemaView;

  private SchemaMutationImpl schemaMutation;

  @Before
  public void before() throws Exception {
    schemaMutation = new SchemaMutationImpl(schemaService, schemaView);
  }

  @Test
  public void updateStatusWithEntityStatusEnabled() {
    ReflectionTestUtils.setField(schemaMutation, "entityStatusEnabled", true);
    SchemaKeyInput key = getSchemaInputKey();
    Optional<Schema> schema = Optional.of(getSchema(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(schemaView.get(any())).thenReturn(schema);
    when(schemaService.updateStatus(any(), any())).thenReturn(schema);

    Schema result = schemaMutation.updateStatus(key, statusInput);

    verify(schemaView, times(1)).get(key.asSchemaKey());
    verify(schemaService, times(1)).updateStatus(schema.get(), statusInput.asStatus());
    assertEquals(schema.get(), result);
  }

  @Test
  public void updateStatusWithEntityStatusDisabled() {
    ReflectionTestUtils.setField(schemaMutation, "entityStatusEnabled", false);
    SchemaKeyInput key = getSchemaInputKey();
    Optional<Schema> schema = Optional.of(getSchema(key));
    StatusInput statusInput = InputHelper.statusInput();

    when(schemaView.get(any())).thenReturn(schema);

    Schema result = schemaMutation.updateStatus(key, statusInput);

    verify(schemaView, times(1)).get(key.asSchemaKey());
    verify(schemaService, never()).updateStatus(schema.get(), statusInput.asStatus());
    assertEquals(schema.get(), result);
  }

  private SchemaKeyInput getSchemaInputKey() {
    return SchemaKeyInput.builder()
      .domain("domain")
      .name("schema")
      .build();
  }

  private Schema getSchema(SchemaKeyInput key) {
    return new Schema(key.asSchemaKey(), StateHelper.specification(), StateHelper.status());
  }
}
