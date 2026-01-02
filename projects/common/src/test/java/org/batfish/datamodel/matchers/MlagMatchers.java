package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Mlag;
import org.batfish.datamodel.matchers.MlagMatchersImpl.HasId;
import org.batfish.datamodel.matchers.MlagMatchersImpl.HasLocalInterface;
import org.batfish.datamodel.matchers.MlagMatchersImpl.HasPeerAddress;
import org.batfish.datamodel.matchers.MlagMatchersImpl.HasPeerInterface;
import org.hamcrest.Matcher;

/** Matchers for {@link Mlag} */
@ParametersAreNonnullByDefault
public final class MlagMatchers {

  /** Provides a matcher that matches if given {@link Mlag}'s id is {@code expectedId}. */
  public static @Nonnull Matcher<Mlag> hasId(String expectedId) {
    return new HasId(equalTo(expectedId));
  }

  /**
   * Provides a matcher that matches if given {@link Mlag}'s localInterface is {@code
   * expectedLocalInterface}.
   */
  public static @Nonnull Matcher<Mlag> hasLocalInterface(String expectedLocalInterface) {
    return new HasLocalInterface(equalTo(expectedLocalInterface));
  }

  /**
   * Provides a matcher that matches if given {@link Mlag}'s peerAddress is {@code
   * expectedPeerAddress}.
   */
  public static @Nonnull Matcher<Mlag> hasPeerAddress(Ip expectedPeerAddress) {
    return new HasPeerAddress(equalTo(expectedPeerAddress));
  }

  /**
   * Provides a matcher that matches if given {@link Mlag}'s peerInterface is {@code
   * expectedPeerInterface}.
   */
  public static @Nonnull Matcher<Mlag> hasPeerInterface(String expectedPeerInterface) {
    return new HasPeerInterface(equalTo(expectedPeerInterface));
  }

  // Prevent initialization
  private MlagMatchers() {}
}
