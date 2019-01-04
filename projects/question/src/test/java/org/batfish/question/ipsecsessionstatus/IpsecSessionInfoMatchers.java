package org.batfish.question.ipsecsessionstatus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.IpsecSessionStatus;
import org.batfish.question.ipsecsessionstatus.IpsecSessionInfoMatchersImpl.HasIpsecSessionStatus;
import org.hamcrest.Matcher;

/** {@link Matcher Hamcrest matchers} for {@link IpsecSessionInfo}. */
public final class IpsecSessionInfoMatchers {

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peeing
   * info's {@code ipsecPeeringStatus}
   */
  public static @Nonnull HasIpsecSessionStatus hasIpsecSessionStatus(
      @Nonnull Matcher<? super IpsecSessionStatus> subMatcher) {
    return new HasIpsecSessionStatus(subMatcher);
  }

  private IpsecSessionInfoMatchers() {}
}
