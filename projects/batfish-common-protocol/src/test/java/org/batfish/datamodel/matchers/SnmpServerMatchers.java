package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SnmpServer;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class SnmpServerMatchers {

  static final class HasCommunities extends FeatureMatcher<SnmpServer, Map<String, SnmpCommunity>> {

    public HasCommunities(@Nonnull Matcher<? super Map<String, SnmpCommunity>> subMatcher) {
      super(subMatcher, "An SnmpServer with communities:", "communities");
    }

    @Override
    protected Map<String, SnmpCommunity> featureValueOf(SnmpServer actual) {
      return actual.getCommunities();
    }
  }

  public static @Nonnull Matcher<SnmpServer> hasCommunities(
      @Nonnull Matcher<? super Map<String, SnmpCommunity>> subMatcher) {
    return new HasCommunities(subMatcher);
  }

  private SnmpServerMatchers() {}
}
