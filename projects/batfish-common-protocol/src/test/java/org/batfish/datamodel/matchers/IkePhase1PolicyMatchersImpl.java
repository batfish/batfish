package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkePhase1PolicyMatchersImpl {

  static final class HasPresharedKey extends FeatureMatcher<IkePhase1Policy, String> {
    HasPresharedKey(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with PreSharedKey:", "PreSharedKey");
    }

    @Override
    protected String featureValueOf(IkePhase1Policy actual) {
      return actual.getPreSharedKey();
    }
  }

  static final class HasIkePhase1Proposals
      extends FeatureMatcher<IkePhase1Policy, List<IkePhase1Proposal>> {
    HasIkePhase1Proposals(@Nonnull Matcher<? super List<IkePhase1Proposal>> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with IkePhase1Proposals:", "IkePhase1Proposals");
    }

    @Override
    protected List<IkePhase1Proposal> featureValueOf(IkePhase1Policy actual) {
      return actual.getIkePhase1Proposals();
    }
  }
}
