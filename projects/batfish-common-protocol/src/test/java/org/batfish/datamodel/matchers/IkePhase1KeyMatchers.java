package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.matchers.IkePhase1KeyMatchersImpl.HasKey;

public final class IkePhase1KeyMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code key} matches specified {@code
   * key}
   */
  public static HasKey hasKey(String key) {
    return new HasKey(equalTo(key));
  }

  private IkePhase1KeyMatchers() {}
}
