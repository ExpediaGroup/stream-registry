package com.expediagroup.streamplatform.streamregistry.graphql.resolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLDomain;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLSchema;
import com.expediagroup.streamplatform.streamregistry.graphql.model.GraphQLTransformer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.service.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class SchemaDomainResolverTest {

  @Mock
  Service<Domain, Domain.Key> domainKeyService;

  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void resolve() {

    Domain domain = Domain
        .builder()
        .name("streamDomain")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .type("type")
        .configuration(mapper.createObjectNode().put("key", "value"))
        .build();

    when(domainKeyService.get(any())).thenReturn(domain);

    SchemaDomainResolver schemaDomainResolver = new SchemaDomainResolver(
        domainKeyService,
        new GraphQLTransformer()
    );

    GraphQLSchema schema = GraphQLSchema.builder().domain(
        GraphQLDomain.Key.builder().name("theDomain").build()
    ).build();

    GraphQLDomain out = schemaDomainResolver.domain(schema);
  }

}