package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.matchers.IkePolicyMatchersImpl.HasPresharedKeyHash;

public final class IkePolicyMatchers {

  /**
   * Provides a matcher that matches if the IKE Policy's value of {@code preSharedKeyHash} matches
   * specified {@code preSharedKeyHash}
   */
  public static HasPresharedKeyHash hasPresharedKeyHash(String preSharedKeyHash) {
    return new HasPresharedKeyHash(equalTo(preSharedKeyHash));
  }

  private IkePolicyMatchers() {}
}
