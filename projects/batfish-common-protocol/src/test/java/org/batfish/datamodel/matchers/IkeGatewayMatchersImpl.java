package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IkePolicy;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkeGatewayMatchersImpl {

  static final class HasAddress extends FeatureMatcher<IkeGateway, Ip> {
    HasAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IKE gateway with address:", "Address");
    }

    @Override
    protected Ip featureValueOf(IkeGateway actual) {
      return actual.getAddress();
    }
  }

  static class HasExternalInterface extends FeatureMatcher<IkeGateway, Interface> {

    public HasExternalInterface(@Nonnull Matcher<? super Interface> subMatcher) {
      super(subMatcher, "An IKE gateway with external interface:", "externalInterface");
    }

    @Override
    protected Interface featureValueOf(IkeGateway actual) {
      return actual.getExternalInterface();
    }
  }

  static class HasIkePolicy extends FeatureMatcher<IkeGateway, IkePolicy> {

    public HasIkePolicy(@Nonnull Matcher<? super IkePolicy> subMatcher) {
      super(subMatcher, "An IKE gateway with IKE policy:", "ikePolicy");
    }

    @Override
    protected IkePolicy featureValueOf(IkeGateway actual) {
      return actual.getIkePolicy();
    }
  }

  static final class HasLocalIp extends FeatureMatcher<IkeGateway, Ip> {
    HasLocalIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IKE gateway with local IP:", "localIp");
    }

    @Override
    protected Ip featureValueOf(IkeGateway actual) {
      return actual.getLocalIp();
    }
  }

  static final class HasName extends FeatureMatcher<IkeGateway, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE gateway with name:", "name");
    }

    @Override
    protected String featureValueOf(IkeGateway actual) {
      return actual.getName();
    }
  }
}
