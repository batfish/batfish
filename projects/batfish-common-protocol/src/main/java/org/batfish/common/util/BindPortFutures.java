package org.batfish.common.util;

import java.util.concurrent.CompletableFuture;

public class BindPortFutures {

  private final CompletableFuture<Integer> _poolPort;
  private final CompletableFuture<Integer> _workPort;
  private final CompletableFuture<Integer> _servicePort;

  public BindPortFutures() {
    _poolPort = new CompletableFuture<>();
    _workPort = new CompletableFuture<>();
    _servicePort = new CompletableFuture<>();
  }

  public CompletableFuture<Integer> getPoolPort() {
    return _poolPort;
  }

  public CompletableFuture<Integer> getWorkPort() {
    return _workPort;
  }

  public CompletableFuture<Integer> getServicePort() {
    return _servicePort;
  }
}
