package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class RouteMatchersImpl {
  static final class HasAdministrativeCost extends FeatureMatcher<Route, Integer> {
    HasAdministrativeCost(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Route with administrativeCost:", "administrativeCost");
    }

    @Override
    protected Integer featureValueOf(Route actual) {
      return actual.getAdministrativeCost();
    }
  }

  static final class HasMetric extends FeatureMatcher<Route, Long> {
    HasMetric(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A Route with metric:", "metric");
    }

    @Override
    protected Long featureValueOf(Route actual) {
      return actual.getMetric();
    }
  }

  static final class HasNextHop extends FeatureMatcher<Route, String> {
    HasNextHop(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A Route with nextHopInterface:", "nextHopInterface");
    }

    @Override
    protected String featureValueOf(Route actual) {
      return actual.getNextHop();
    }
  }

  static final class HasNextHopInterface extends FeatureMatcher<Route, String> {
    HasNextHopInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A Route with nextHopInterface:", "nextHopInterface");
    }

    @Override
    protected String featureValueOf(Route actual) {
      return actual.getNextHopInterface();
    }
  }

  static final class HasNextHopIp extends FeatureMatcher<Route, Ip> {
    HasNextHopIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A Route with nextHopIp:", "nextHopIp");
    }

    @Override
    protected Ip featureValueOf(Route actual) {
      return actual.getNextHopIp();
    }
  }

  static final class HasPrefix extends FeatureMatcher<Route, Prefix> {
    HasPrefix(@Nonnull Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "A Route with network:", "network");
    }

    @Override
    protected Prefix featureValueOf(Route actual) {
      return actual.getNetwork();
    }
  }

  static final class HasProtocol extends FeatureMatcher<Route, RoutingProtocol> {
    HasProtocol(@Nonnull Matcher<? super RoutingProtocol> subMatcher) {
      super(subMatcher, "A Route with protocol:", "protocol");
    }

    @Override
    protected RoutingProtocol featureValueOf(Route actual) {
      return actual.getProtocol();
    }
  }

  private RouteMatchersImpl() {}
}
