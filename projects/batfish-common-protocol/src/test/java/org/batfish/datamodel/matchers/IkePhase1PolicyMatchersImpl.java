package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IpWildcard;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkePhase1PolicyMatchersImpl {

  static final class HasIkePhase1Key extends FeatureMatcher<IkePhase1Policy, IkePhase1Key> {
    HasIkePhase1Key(@Nonnull Matcher<? super IkePhase1Key> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with IkePhase1Key:", "IkePhase1Key");
    }

    @Override
    protected IkePhase1Key featureValueOf(IkePhase1Policy actual) {
      return actual.getIkePhase1Key();
    }
  }

  static final class HasRemoteIdentity extends FeatureMatcher<IkePhase1Policy, IpWildcard> {
    HasRemoteIdentity(@Nonnull Matcher<? super IpWildcard> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with RemoteIdentity:", "RemoteIdentity");
    }

    @Override
    protected IpWildcard featureValueOf(IkePhase1Policy actual) {
      return actual.getRemoteIdentity();
    }
  }

  static final class HasLocalInterface extends FeatureMatcher<IkePhase1Policy, String> {
    HasLocalInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with LocalInterface:", "LocalInterface");
    }

    @Override
    protected String featureValueOf(IkePhase1Policy actual) {
      return actual.getLocalInterface();
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
