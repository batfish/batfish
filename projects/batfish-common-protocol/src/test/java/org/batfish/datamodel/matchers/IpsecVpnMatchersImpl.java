package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.IpsecVpn;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecVpnMatchersImpl {

  static final class HasBindInterface extends FeatureMatcher<IpsecVpn, Interface> {
    HasBindInterface(@Nonnull Matcher<? super Interface> subMatcher) {
      super(subMatcher, "An IPsec VPN with BindInterface:", "BindInterface");
    }

    @Override
    protected Interface featureValueOf(IpsecVpn actual) {
      return actual.getBindInterface();
    }
  }

  static class HasIkeGatewaay extends FeatureMatcher<IpsecVpn, IkeGateway> {

    public HasIkeGatewaay(@Nonnull Matcher<? super IkeGateway> subMatcher) {
      super(subMatcher, "An IPsec VPN with IkeGateway:", "IkeGateway");
    }

    @Override
    protected IkeGateway featureValueOf(IpsecVpn actual) {
      return actual.getIkeGateway();
    }
  }

  static class HasIpsecPolicy extends FeatureMatcher<IpsecVpn, IpsecPolicy> {

    public HasIpsecPolicy(@Nonnull Matcher<? super IpsecPolicy> subMatcher) {
      super(subMatcher, "An IPsec VPN with IpsecPolicy:", "IpsecPolicy");
    }

    @Override
    protected IpsecPolicy featureValueOf(IpsecVpn actual) {
      return actual.getIpsecPolicy();
    }
  }

  static class HasPolicy extends FeatureMatcher<IpsecVpn, IpAccessList> {

    public HasPolicy(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "An IPsec VPN with Policy:", "Policy");
    }

    @Override
    protected IpAccessList featureValueOf(IpsecVpn actual) {
      return actual.getPolicyAccessList();
    }
  }
}
