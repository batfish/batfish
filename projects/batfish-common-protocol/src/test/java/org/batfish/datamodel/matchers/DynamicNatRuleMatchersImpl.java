package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.Transformation;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DynamicNatRuleMatchersImpl {
  private DynamicNatRuleMatchersImpl() {}

  static final class HasAclName extends FeatureMatcher<DynamicNatRule, String> {
    HasAclName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "aclName", "aclName");
    }

    @Nullable
    @Override
    protected String featureValueOf(DynamicNatRule actual) {
      if (actual.getAcl() != null) {
        return actual.getAcl().getName();
      }
      return null;
    }
  }

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
