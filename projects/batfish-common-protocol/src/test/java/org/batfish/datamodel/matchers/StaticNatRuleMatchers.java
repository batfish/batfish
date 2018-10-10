package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.StaticNatRuleMatchersImpl.HasGlobalNetwork;
import org.batfish.datamodel.matchers.StaticNatRuleMatchersImpl.HasLocalNetwork;
import org.batfish.datamodel.matchers.StaticNatRuleMatchersImpl.IsStaticNatRuleThat;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.hamcrest.Matcher;

public final class StaticNatRuleMatchers {
  private StaticNatRuleMatchers() {}

  /**
   * Provides a matcher that matches when {@code expectedLocalNetwork} is equal to the {@link
   * StaticNatRule}'s local network.
   */
  public static HasLocalNetwork hasLocalNetwork(Prefix expectedLocalNetwork) {
    return new HasLocalNetwork(equalTo(expectedLocalNetwork));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * StaticNatRule}'s local network.
   */
  public static HasLocalNetwork hasLocalNetwork(@Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasLocalNetwork(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedPoolIpLast} is equal to the {@link
   * StaticNatRule}'s global network.
   */
  public static HasGlobalNetwork hasGlobalNetwork(Prefix expectedGlobalNetwork) {
    return new HasGlobalNetwork(equalTo(expectedGlobalNetwork));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * StaticNatRule}'s global network.
   */
  public static HasGlobalNetwork hasGlobalNetwork(@Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasGlobalNetwork(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is an {@link StaticNatRule} matched by the
   * provided {@code subMatcher}.
   */
  public static IsStaticNatRuleThat isStaticNatRuleThat(
      @Nonnull Matcher<? super StaticNatRule> subMatcher) {
    return new IsStaticNatRuleThat(subMatcher);
  }
}
