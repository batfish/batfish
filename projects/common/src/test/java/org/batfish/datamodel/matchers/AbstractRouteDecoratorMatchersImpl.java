package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHop;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class AbstractRouteDecoratorMatchersImpl {
  static final class HasAdministrativeCost extends FeatureMatcher<AbstractRouteDecorator, Integer> {
    HasAdministrativeCost(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with administrativeCost:", "administrativeCost");
    }

    @Override
    protected Integer featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getAdministrativeCost();
    }
  }

  static final class HasMetric extends FeatureMatcher<AbstractRouteDecorator, Long> {
    HasMetric(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with metric:", "metric");
    }

    @Override
    protected Long featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getMetric();
    }
  }

  static final class HasNextHop extends FeatureMatcher<AbstractRouteDecorator, NextHop> {
    HasNextHop(@Nonnull Matcher<? super NextHop> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nextHop:", "nextHop");
    }

    @Override
    protected NextHop featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNextHop();
    }
  }

  static final class HasNextHopInterface extends FeatureMatcher<AbstractRouteDecorator, String> {
    HasNextHopInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nextHopInterface:", "nextHopInterface");
    }

    @Override
    protected String featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNextHopInterface();
    }
  }

  static final class HasNextHopIp extends FeatureMatcher<AbstractRouteDecorator, Ip> {
    HasNextHopIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nextHopIp:", "nextHopIp");
    }

    @Override
    protected Ip featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNextHopIp();
    }
  }

  static final class HasPrefix extends FeatureMatcher<AbstractRouteDecorator, Prefix> {
    HasPrefix(@Nonnull Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with network:", "network");
    }

    @Override
    protected Prefix featureValueOf(AbstractRouteDecorator actual) {
      return actual.getNetwork();
    }
  }

  static final class HasProtocol extends FeatureMatcher<AbstractRouteDecorator, RoutingProtocol> {
    HasProtocol(@Nonnull Matcher<? super RoutingProtocol> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with protocol:", "protocol");
    }

    @Override
    protected RoutingProtocol featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getProtocol();
    }
  }

  static final class HasTag extends FeatureMatcher<AbstractRouteDecorator, Long> {
    HasTag(Matcher<? super Long> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with tag", "tag");
    }

    @Override
    protected Long featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getTag();
    }
  }

  static final class IsNonForwarding extends FeatureMatcher<AbstractRouteDecorator, Boolean> {
    IsNonForwarding(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nonRouting:", "nonRouting");
    }

    @Override
    protected Boolean featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNonForwarding();
    }
  }

  static final class IsNonRouting extends FeatureMatcher<AbstractRouteDecorator, Boolean> {
    IsNonRouting(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nonRouting:", "nonRouting");
    }

    @Override
    protected Boolean featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNonRouting();
    }
  }

  private AbstractRouteDecoratorMatchersImpl() {}
}
