package org.batfish.datamodel.matchers;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Mlag;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class MlagMatchersImpl {

  static final class HasId extends FeatureMatcher<Mlag, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "An MLAG Configuration with id:", "id");
    }

    @Override
    protected String featureValueOf(Mlag mlag) {
      return mlag.getId();
    }
  }

  static final class HasLocalInterface extends FeatureMatcher<Mlag, String> {
    HasLocalInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "An MLAG Configuration with localInterface:", "localInterface");
    }

    @Override
    protected @Nullable String featureValueOf(Mlag mlag) {
      return mlag.getLocalInterface();
    }
  }

  static final class HasPeerAddress extends FeatureMatcher<Mlag, Ip> {
    HasPeerAddress(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An MLAG Configuration with peerAddress:", "peerAddress");
    }

    @Override
    protected @Nullable Ip featureValueOf(Mlag mlag) {
      return mlag.getPeerAddress();
    }
  }

  static final class HasPeerInterface extends FeatureMatcher<Mlag, String> {
    HasPeerInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "An MLAG Configuration with peerInterface:", "peerInterface");
    }

    @Override
    protected @Nullable String featureValueOf(Mlag mlag) {
      return mlag.getPeerInterface();
    }
  }

  // Prevent initialization
  private MlagMatchersImpl() {}
}
