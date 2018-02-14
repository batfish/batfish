package org.batfish.representation.aws.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.representation.aws.matchers.ElasticsearchDomainMatchersImpl.HasAvailable;
import org.batfish.representation.aws.matchers.ElasticsearchDomainMatchersImpl.HasId;
import org.batfish.representation.aws.matchers.ElasticsearchDomainMatchersImpl.HasSecurityGroups;
import org.batfish.representation.aws.matchers.ElasticsearchDomainMatchersImpl.HasSubnets;
import org.batfish.representation.aws.matchers.ElasticsearchDomainMatchersImpl.HasVpcId;
import org.hamcrest.Matcher;

public final class ElasticsearchDomainMatchers {

  /**
   * Provides a matcher that matches when {@code expectedId} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s id.
   */
  public static HasId hasId(String expectedId) {
    return new HasId(equalTo(expectedId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s id.
   */
  public static HasId hasId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedSecurityGroups} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Security Groups.
   */
  public static HasSecurityGroups hasSecurityGroups(List<String> expectedSecurityGroups) {
    return new HasSecurityGroups(equalTo(expectedSecurityGroups));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Security Groups.
   */
  public static HasSecurityGroups hasSecurityGroups(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasSecurityGroups(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedAvailable} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Available
   */
  public static HasAvailable hasAvailable(Boolean expectedAvailable) {
    return new HasAvailable(equalTo(expectedAvailable));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Available.
   */
  public static HasAvailable hasAvailable(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasAvailable(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedVpcId} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s VPC Id.
   */
  public static HasVpcId hasVpcId(String expectedVpcId) {
    return new HasVpcId(equalTo(expectedVpcId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s VPC Id.
   */
  public static HasVpcId hasVpcId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasVpcId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedSubnets} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Subnets.
   */
  public static HasSubnets hasSubnets(List<String> expectedSubnets) {
    return new HasSubnets(equalTo(expectedSubnets));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Subnets.
   */
  public static HasSubnets hasSubnets(@Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasSubnets(subMatcher);
  }

  private ElasticsearchDomainMatchers() {}
}
