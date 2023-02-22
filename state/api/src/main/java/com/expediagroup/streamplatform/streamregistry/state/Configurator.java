package com.expediagroup.streamplatform.streamregistry.state;

/**
 * Allows clients to be configured, for example to attach metrics to a client.
 * @param <T> The client type to be configured.
 */
public interface Configurator<T> {
  void configure(T client);
}
