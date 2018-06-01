package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class InterfaceAddressMatchersImpl {

  static final class HasIp extends FeatureMatcher<InterfaceAddress, Ip> {
    HasIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An interface with IP:", "Ip");
    }

    @Override
    protected Ip featureValueOf(InterfaceAddress actual) {
      return actual.getIp();
    }
  }
}
