package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link BgpUnnumberedPeerConfig}. */
@ParametersAreNonnullByDefault
public final class BgpUnnumberedPeerConfigMatchers {

  public static @Nonnull Matcher<BgpUnnumberedPeerConfig> hasPeerInterface(
      String expectedPeerInterface) {
    return new HasPeerInterface(equalTo(expectedPeerInterface));
  }

  private BgpUnnumberedPeerConfigMatchers() {}

  private static final class HasPeerInterface
      extends FeatureMatcher<BgpUnnumberedPeerConfig, String> {
    public HasPeerInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "A BgpUnnumberedPeerConfig with peerInterface:", "peerInterface:");
    }

    @Override
    protected @Nonnull String featureValueOf(BgpUnnumberedPeerConfig actual) {
      return actual.getPeerInterface();
    }
  }
}
