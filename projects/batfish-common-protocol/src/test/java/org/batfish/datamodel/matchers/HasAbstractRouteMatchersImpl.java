package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.HasAbstractRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class HasAbstractRouteMatchersImpl {
  static final class HasAdministrativeCost extends FeatureMatcher<HasAbstractRoute, Integer> {
    HasAdministrativeCost(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A HasAbstractRoute with administrativeCost:", "administrativeCost");
    }

    @Override
    protected Integer featureValueOf(HasAbstractRoute actual) {
      return actual.getAbstractRoute().getAdministrativeCost();
    }
  }

  static final class HasMetric extends FeatureMatcher<HasAbstractRoute, Long> {
    HasMetric(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A HasAbstractRoute with metric:", "metric");
    }

    @Override
    protected Long featureValueOf(HasAbstractRoute actual) {
      return actual.getAbstractRoute().getMetric();
    }
  }

  static final class HasNextHopInterface extends FeatureMatcher<HasAbstractRoute, String> {
    HasNextHopInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A HasAbstractRoute with nextHopInterface:", "nextHopInterface");
    }

    @Override
    protected String featureValueOf(HasAbstractRoute actual) {
      return actual.getAbstractRoute().getNextHopInterface();
    }
  }

  static final class HasNextHopIp extends FeatureMatcher<HasAbstractRoute, Ip> {
    HasNextHopIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A HasAbstractRoute with nextHopIp:", "nextHopIp");
    }

    @Override
    protected Ip featureValueOf(HasAbstractRoute actual) {
      return actual.getAbstractRoute().getNextHopIp();
    }
  }

  static final class HasPrefix extends FeatureMatcher<HasAbstractRoute, Prefix> {
    HasPrefix(@Nonnull Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "A HasAbstractRoute with network:", "network");
    }

    @Override
    protected Prefix featureValueOf(HasAbstractRoute actual) {
      return actual.getNetwork();
    }
  }

  static final class HasProtocol extends FeatureMatcher<HasAbstractRoute, RoutingProtocol> {
    HasProtocol(@Nonnull Matcher<? super RoutingProtocol> subMatcher) {
      super(subMatcher, "A HasAbstractRoute with protocol:", "protocol");
    }

    @Override
    protected RoutingProtocol featureValueOf(HasAbstractRoute actual) {
      return actual.getAbstractRoute().getProtocol();
    }
  }

  static final class IsNonForwarding extends FeatureMatcher<HasAbstractRoute, Boolean> {
    IsNonForwarding(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A HasAbstractRoute with nonRouting:", "nonRouting");
    }

    @Override
    protected Boolean featureValueOf(HasAbstractRoute actual) {
      return actual.getAbstractRoute().getNonForwarding();
    }
  }

  private HasAbstractRouteMatchersImpl() {}
}
