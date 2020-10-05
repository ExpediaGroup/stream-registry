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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static com.expediagroup.streamplatform.streamregistry.state.graphql.type.CustomType.OBJECTNODE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import okhttp3.OkHttpClient;

import com.apollographql.apollo.ApolloClient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultApolloClientFactoryTest {
  @Mock private ApolloClient.Builder builder;

  private final String streamRegistryUrl = "streamRegistryUrl";

  private DefaultApolloClientFactory underTest;

  @Test
  public void test_withCredentials() {
    underTest = spy(new DefaultApolloClientFactory(streamRegistryUrl, new Credentials("userName", "password")));

    when(underTest.builder()).thenReturn(builder);
    when(builder.okHttpClient(any())).thenReturn(builder);
    when(builder.serverUrl(streamRegistryUrl)).thenReturn(builder);
    when(builder.addCustomTypeAdapter(any(), any())).thenReturn(builder);

    underTest.create();

    var objectNodeTypeAdapterCaptor = ArgumentCaptor.forClass(ObjectNodeTypeAdapter.class);
    var okHttpClientCaptor = ArgumentCaptor.forClass(OkHttpClient.class);

    verify(builder).okHttpClient(okHttpClientCaptor.capture());
    verify(builder).serverUrl(streamRegistryUrl);
    verify(builder).addCustomTypeAdapter(eq(OBJECTNODE), objectNodeTypeAdapterCaptor.capture());
    verify(builder).build();

    assertThat(okHttpClientCaptor.getValue().interceptors().size(), is(1));
    assertThat(objectNodeTypeAdapterCaptor.getValue(), is(notNullValue()));
  }

  @Test
  public void test_configure_okhttp_builder_with_credentials() {
    underTest = spy(new DefaultApolloClientFactory(streamRegistryUrl, new Credentials("userName", "password"), builder -> builder.hostnameVerifier((hostname, session) -> true)));

    when(underTest.builder()).thenReturn(builder);
    when(builder.okHttpClient(any())).thenReturn(builder);
    when(builder.serverUrl(streamRegistryUrl)).thenReturn(builder);
    when(builder.addCustomTypeAdapter(any(), any())).thenReturn(builder);

    underTest.create();

    var okHttpClientCaptor = ArgumentCaptor.forClass(OkHttpClient.class);
    verify(builder).okHttpClient(okHttpClientCaptor.capture());

    assertThat(okHttpClientCaptor.getValue().interceptors().size(), is(1));
    assertThat(okHttpClientCaptor.getValue().hostnameVerifier().verify(null, null), is(true));
  }
}