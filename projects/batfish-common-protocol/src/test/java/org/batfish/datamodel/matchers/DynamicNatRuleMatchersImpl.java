package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.Transformation;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DynamicNatRuleMatchersImpl {
  private DynamicNatRuleMatchersImpl() {}

  static final class HasPoolIpFirst extends FeatureMatcher<DynamicNatRule, Ip> {
    HasPoolIpFirst(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "poolIpFirst", "poolIpFirst");
    }

    @Override
    protected Ip featureValueOf(DynamicNatRule actual) {
      return actual.getPoolIpFirst();
    }
  }

  static final class HasPoolIpLast extends FeatureMatcher<DynamicNatRule, Ip> {
    HasPoolIpLast(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "poolIpLast", "poolIpLast");
    }

    @Override
    protected Ip featureValueOf(DynamicNatRule actual) {
      return actual.getPoolIpLast();
    }
  }

  static class IsDynamicNatRuleThat extends IsInstanceThat<Transformation, DynamicNatRule> {
    IsDynamicNatRuleThat(Matcher<? super DynamicNatRule> subMatcher) {
      super(DynamicNatRule.class, subMatcher);
    }
  }
}

