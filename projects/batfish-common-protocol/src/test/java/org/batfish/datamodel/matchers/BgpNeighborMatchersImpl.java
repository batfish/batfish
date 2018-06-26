package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class BgpNeighborMatchersImpl {

  static final class HasClusterId extends FeatureMatcher<BgpPeerConfig, Long> {
    HasClusterId(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with clusterId:", "clusterId");
    }

    @Override
    protected Long featureValueOf(BgpPeerConfig actual) {
      return actual.getClusterId();
    }
  }

  static final class HasLocalAs extends FeatureMatcher<BgpPeerConfig, Long> {
    HasLocalAs(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with localAs:", "localAs");
    }

    @Override
    protected Long featureValueOf(BgpPeerConfig actual) {
      return actual.getLocalAs();
    }
  }

  static final class HasEnforceFirstAs extends FeatureMatcher<BgpPeerConfig, Boolean> {
    HasEnforceFirstAs(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with enforce-first-as:", "enforce-first-as");
    }

    @Override
    protected Boolean featureValueOf(BgpPeerConfig actual) {
      return actual.getEnforceFirstAs();
    }
  }

  static final class HasRemoteAs extends FeatureMatcher<BgpPeerConfig, Long> {
    HasRemoteAs(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with remoteAs:", "remoteAs");
    }

    @Override
    protected Long featureValueOf(BgpPeerConfig actual) {
      return actual.getRemoteAs();
    }
  }

  static final class IsDynamic extends FeatureMatcher<BgpPeerConfig, Boolean> {
    IsDynamic(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with dynamic set to:", "dynamic");
    }

    @Override
    protected Boolean featureValueOf(BgpPeerConfig actual) {
      return actual.getDynamic();
    }
  }
}
