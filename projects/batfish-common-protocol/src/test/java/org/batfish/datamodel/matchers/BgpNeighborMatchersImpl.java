package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class BgpNeighborMatchersImpl {
  static final class HasAllowLocalAsIn extends FeatureMatcher<BgpPeerConfig, Boolean> {
    HasAllowLocalAsIn(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with allowLocalAsIn:", "allowLocalAsIn");
    }

    @Override
    protected Boolean featureValueOf(BgpPeerConfig actual) {
      return actual.getAllowLocalAsIn();
    }
  }

  static final class HasAllowRemoteAsOut extends FeatureMatcher<BgpPeerConfig, Boolean> {
    HasAllowRemoteAsOut(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with allowRemoteAsOut:", "allowRemoteAsOut");
    }

    @Override
    protected Boolean featureValueOf(BgpPeerConfig actual) {
      return actual.getAllowRemoteAsOut();
    }
  }

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

  static final class HasRemoteAs extends FeatureMatcher<BgpActivePeerConfig, Long> {
    HasRemoteAs(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with remoteAs:", "remoteAs");
    }

    @Override
    protected Long featureValueOf(BgpActivePeerConfig actual) {
      return actual.getRemoteAs();
    }
  }

  static final class HasRemoteAses extends FeatureMatcher<BgpPassivePeerConfig, List<Long>> {
    HasRemoteAses(@Nonnull Matcher<? super List<Long>> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with remoteAs:", "remoteAs");
    }

    @Override
    protected List<Long> featureValueOf(BgpPassivePeerConfig actual) {
      return actual.getRemoteAs();
    }
  }
}
