package org.batfish.representation.aws.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.representation.aws.ElasticsearchDomain;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class ElasticsearchDomainMatchers {

  /**
   * Provides a matcher that matches when {@code expectedId} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s id.
   */
  public static Matcher<ElasticsearchDomain> hasId(String expectedId) {
    return new HasId(equalTo(expectedId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s id.
   */
  public static Matcher<ElasticsearchDomain> hasId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedSecurityGroups} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Security Groups.
   */
  public static Matcher<ElasticsearchDomain> hasSecurityGroups(
      List<String> expectedSecurityGroups) {
    return new HasSecurityGroups(equalTo(expectedSecurityGroups));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Security Groups.
   */
  public static Matcher<ElasticsearchDomain> hasSecurityGroups(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasSecurityGroups(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedAvailable} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Available
   */
  public static Matcher<ElasticsearchDomain> hasAvailable(Boolean expectedAvailable) {
    return new HasAvailable(equalTo(expectedAvailable));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Available.
   */
  public static Matcher<ElasticsearchDomain> hasAvailable(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasAvailable(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedVpcId} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s VPC Id.
   */
  public static Matcher<ElasticsearchDomain> hasVpcId(String expectedVpcId) {
    return new HasVpcId(equalTo(expectedVpcId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s VPC Id.
   */
  public static Matcher<ElasticsearchDomain> hasVpcId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasVpcId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedSubnets} is equal to the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Subnets.
   */
  public static Matcher<ElasticsearchDomain> hasSubnets(List<String> expectedSubnets) {
    return new HasSubnets(equalTo(expectedSubnets));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.ElasticsearchDomain}'s Subnets.
   */
  public static Matcher<ElasticsearchDomain> hasSubnets(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasSubnets(subMatcher);
  }

  private ElasticsearchDomainMatchers() {}

  private static final class HasId extends FeatureMatcher<ElasticsearchDomain, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "id", "id");
    }

    @Override
    protected String featureValueOf(ElasticsearchDomain actual) {
      return actual.getId();
    }
  }

  private static final class HasSecurityGroups
      extends FeatureMatcher<ElasticsearchDomain, List<String>> {
    HasSecurityGroups(Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "securityGroups", "securityGroups");
    }

    @Override
    protected List<String> featureValueOf(ElasticsearchDomain actual) {
      return actual.getSecurityGroups();
    }
  }

  private static final class HasSubnets extends FeatureMatcher<ElasticsearchDomain, List<String>> {
    HasSubnets(Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "subnets ", "subnets");
    }

    @Override
    protected List<String> featureValueOf(ElasticsearchDomain actual) {
      return actual.getSubnets();
    }
  }

  private static final class HasAvailable extends FeatureMatcher<ElasticsearchDomain, Boolean> {
    HasAvailable(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "available", "available");
    }

    @Override
    protected Boolean featureValueOf(ElasticsearchDomain actual) {
      return actual.getAvailable();
    }
  }

  private static final class HasVpcId extends FeatureMatcher<ElasticsearchDomain, String> {
    HasVpcId(Matcher<? super String> subMatcher) {
      super(subMatcher, "vpcId", "vpcId");
    }

    @Override
    protected String featureValueOf(ElasticsearchDomain actual) {
      return actual.getVpcId();
    }
  }
}
