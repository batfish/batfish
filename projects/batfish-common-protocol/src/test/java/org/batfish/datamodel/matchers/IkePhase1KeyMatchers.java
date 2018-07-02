package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.matchers.IkePhase1KeyMatchersImpl.HasKeyValue;

public final class IkePhase1KeyMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code key} matches specified {@code
   * key}
   */
  public static HasKeyValue hasKeyValue(String keyValue) {
    return new HasKeyValue(equalTo(keyValue));
  }

  private IkePhase1KeyMatchers() {}
}
