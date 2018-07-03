package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.matchers.IkePhase1KeyMatchersImpl.HasKeyHash;

public final class IkePhase1KeyMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code keyHash} matches specified
   * {@code keyHash}
   */
  public static HasKeyHash hasKeyHash(String keyHash) {
    return new HasKeyHash(equalTo(keyHash));
  }

  private IkePhase1KeyMatchers() {}
}
