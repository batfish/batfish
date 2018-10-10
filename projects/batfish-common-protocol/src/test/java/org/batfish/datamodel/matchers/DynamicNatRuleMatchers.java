package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.DynamicNatRuleMatchersImpl.HasAclName;
import org.batfish.datamodel.matchers.DynamicNatRuleMatchersImpl.HasPoolIpFirst;
import org.batfish.datamodel.matchers.DynamicNatRuleMatchersImpl.HasPoolIpLast;
import org.batfish.datamodel.matchers.DynamicNatRuleMatchersImpl.IsDynamicNatRuleThat;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.hamcrest.Matcher;

public final class DynamicNatRuleMatchers {

  private DynamicNatRuleMatchers() {}

  /**
   * Provides a matcher that matches when {@code expectedPoolIpFirst} is equal to the {@link
   * DynamicNatRule}'s ACL name.
   */
  public static HasAclName hasAclName(String expectedAclName) {
    return new HasAclName(equalTo(expectedAclName));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * DynamicNatRule}'s ACL name.
   */
  public static HasAclName hasAclName(@Nonnull Matcher<? super String> subMatcher) {
    return new HasAclName(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedPoolIpFirst} is equal to the {@link
   * DynamicNatRule}'s first pool-ip.
   */
  public static HasPoolIpFirst hasPoolIpFirst(Ip expectedPoolIpFirst) {
    return new HasPoolIpFirst(equalTo(expectedPoolIpFirst));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * DynamicNatRule}'s first pool-ip.
   */
  public static HasPoolIpFirst hasPoolIpFirst(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasPoolIpFirst(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedPoolIpLast} is equal to the {@link
   * DynamicNatRule}'s last pool-ip.
   */
  public static HasPoolIpLast hasPoolIpLast(Ip expectedPoolIpLast) {
    return new HasPoolIpLast(equalTo(expectedPoolIpLast));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * DynamicNatRule}'s last pool-ip.
   */
  public static HasPoolIpLast hasPoolIpLast(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasPoolIpLast(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is an {@link DynamicNatRule} matched by the
   * provided {@code subMatcher}.
   */
  public static IsDynamicNatRuleThat isDynamicNatRuleThat(
      @Nonnull Matcher<? super DynamicNatRule> subMatcher) {
    return new IsDynamicNatRuleThat(subMatcher);
  }
}
