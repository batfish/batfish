package org.batfish.question.ipsecpeers;

import javax.annotation.Nonnull;
import org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus;
import org.batfish.question.ipsecpeers.IpsecPeeringInfoMatchersImpl.HasIpsecPeeringStatus;
import org.hamcrest.Matcher;

public final class IpsecPeeringInfoMatchers {

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peeing
   * info's {@code ipsecPeeringStatus}
   */
  public static @Nonnull HasIpsecPeeringStatus hasIpsecPeeringStatus(
      @Nonnull Matcher<? super IpsecPeeringStatus> subMatcher) {
    return new HasIpsecPeeringStatus(subMatcher);
  }

  private IpsecPeeringInfoMatchers() {}
}
