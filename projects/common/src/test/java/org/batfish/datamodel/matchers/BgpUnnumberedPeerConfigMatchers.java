package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.matchers.BgpUnnumberedPeerConfigMatchersImpl.HasPeerInterface;
import org.hamcrest.Matcher;

/** Matchers for {@link BgpUnnumberedPeerConfig}. */
@ParametersAreNonnullByDefault
public final class BgpUnnumberedPeerConfigMatchers {

  public static @Nonnull Matcher<BgpUnnumberedPeerConfig> hasPeerInterface(
      String expectedPeerInterface) {
    return new HasPeerInterface(equalTo(expectedPeerInterface));
  }

  private BgpUnnumberedPeerConfigMatchers() {}
}
