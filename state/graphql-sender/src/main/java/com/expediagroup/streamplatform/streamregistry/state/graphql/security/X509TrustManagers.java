package com.expediagroup.streamplatform.streamregistry.state.graphql.security;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public enum X509TrustManagers implements X509TrustManager {
  TRUST_ALL {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[] {};
    }
  }
}
