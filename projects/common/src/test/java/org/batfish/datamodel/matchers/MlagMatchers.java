package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Mlag;
import org.hamcrest.FeatureMatcher;
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

  private static final class HasId extends FeatureMatcher<Mlag, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "An MLAG Configuration with id:", "id");
    }

    @Override
    protected String featureValueOf(Mlag mlag) {
      return mlag.getId();
    }
  }

  private static final class HasLocalInterface extends FeatureMatcher<Mlag, String> {
    HasLocalInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "An MLAG Configuration with localInterface:", "localInterface");
    }

    @Override
    protected @Nullable String featureValueOf(Mlag mlag) {
      return mlag.getLocalInterface();
    }
  }

  private static final class HasPeerAddress extends FeatureMatcher<Mlag, Ip> {
    HasPeerAddress(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An MLAG Configuration with peerAddress:", "peerAddress");
    }

    @Override
    protected @Nullable Ip featureValueOf(Mlag mlag) {
      return mlag.getPeerAddress();
    }
  }

  private static final class HasPeerInterface extends FeatureMatcher<Mlag, String> {
    HasPeerInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "An MLAG Configuration with peerInterface:", "peerInterface");
    }

    @Override
    protected @Nullable String featureValueOf(Mlag mlag) {
      return mlag.getPeerInterface();
    }
  }
}
