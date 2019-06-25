package com.expediagroup.streamplatform.streamregistry.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Configuration
public class DefaultHandlerConfiguration {
  @Bean
  Handler<Schema> defaultSchemaHandler() {
    return new DefaultHandler<>();
  }

  @Bean
  Handler<Stream> defaultStreamHandler() {
    return new DefaultHandler<>();
  }
}
