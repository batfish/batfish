package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class BgpUnnumberedPeerConfigMatchersImpl {

  static final class HasPeerInterface extends FeatureMatcher<BgpUnnumberedPeerConfig, String> {
    public HasPeerInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "A BgpUnnumberedPeerConfig with peerInterface:", "peerInterface:");
    }

    @Override
    protected @Nonnull String featureValueOf(BgpUnnumberedPeerConfig actual) {
      return actual.getPeerInterface();
    }
  }

  private BgpUnnumberedPeerConfigMatchersImpl() {}
}
