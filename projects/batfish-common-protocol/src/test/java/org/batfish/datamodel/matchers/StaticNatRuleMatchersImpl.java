package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.batfish.datamodel.transformation.Transformation;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class StaticNatRuleMatchersImpl {
  private StaticNatRuleMatchersImpl() {}

  static final class HasLocalNetwork extends FeatureMatcher<StaticNatRule, Prefix> {
    HasLocalNetwork(@Nonnull Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "localNetwork", "localNetwork");
    }

    @Override
    protected Prefix featureValueOf(StaticNatRule actual) {
      return actual.getLocalNetwork();
    }
  }

  static final class HasGlobalNetwork extends FeatureMatcher<StaticNatRule, Prefix> {
    HasGlobalNetwork(@Nonnull Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "globalNetwork", "globalNetwork");
    }

    @Override
    protected Prefix featureValueOf(StaticNatRule actual) {
      return actual.getGlobalNetwork();
    }
  }

  static class IsStaticNatRuleThat extends IsInstanceThat<Transformation, StaticNatRule> {
    IsStaticNatRuleThat(Matcher<? super StaticNatRule> subMatcher) {
      super(StaticNatRule.class, subMatcher);
    }
  }
}
