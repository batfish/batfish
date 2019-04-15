package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
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

  static final class HasDescription extends FeatureMatcher<BgpPeerConfig, String> {
    HasDescription(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with description:", "description");
    }

    @Override
    protected String featureValueOf(BgpPeerConfig actual) {
      return actual.getDescription();
    }
  }

  static final class HasExportPolicy extends FeatureMatcher<BgpPeerConfig, String> {
    HasExportPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with exportPolicy:", "exportPolicy");
    }

    @Override
    protected String featureValueOf(BgpPeerConfig actual) {
      return actual.getExportPolicy();
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

  static final class HasLocalIp extends FeatureMatcher<BgpPeerConfig, Ip> {
    HasLocalIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with localIp:", "localIp");
    }

    @Override
    protected Ip featureValueOf(BgpPeerConfig actual) {
      return actual.getLocalIp();
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

  static final class HasRemoteAs extends FeatureMatcher<BgpPeerConfig, LongSpace> {
    HasRemoteAs(@Nonnull Matcher<? super LongSpace> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with remoteAs:", "remoteAs");
    }

    @Override
    protected LongSpace featureValueOf(BgpPeerConfig actual) {
      return actual.getRemoteAsns();
    }
  }
}
