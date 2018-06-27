package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.matchers.IkePhase1PolicyMatchersImpl.HasIkePhase1Proposals;
import org.batfish.datamodel.matchers.IkePhase1PolicyMatchersImpl.HasPresharedKey;
import org.hamcrest.Matcher;

public final class IkePhase1PolicyMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Policy's value of {@code preSharedKey}
   * matches specified {@code preSharedKey}
   */
  public static HasPresharedKey hasPresharedKey(String preSharedKey) {
    return new HasPresharedKey(equalTo(preSharedKey));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IKE Phase 1
   * Policy's IKE Phase 1 proposals
   */
  public static HasIkePhase1Proposals hasIkePhase1Proposals(
      @Nonnull Matcher<? super List<IkePhase1Proposal>> subMatcher) {
    return new HasIkePhase1Proposals(subMatcher);
  }

  private IkePhase1PolicyMatchers() {}
}
