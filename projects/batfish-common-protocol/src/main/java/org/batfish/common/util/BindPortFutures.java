package org.batfish.common.util;

import java.util.concurrent.CompletableFuture;

public class BindPortFutures {

  private final CompletableFuture<Integer> _workV2Port;

  public BindPortFutures() {
    _workV2Port = new CompletableFuture<>();
  }

  public CompletableFuture<Integer> getWorkV2Port() {
    return _workV2Port;
  }
}
