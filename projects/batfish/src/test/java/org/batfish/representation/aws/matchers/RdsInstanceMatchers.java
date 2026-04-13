package org.batfish.representation.aws.matchers;

import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Multimap;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.representation.aws.RdsInstance;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class RdsInstanceMatchers {

  /**
   * Provides a matcher that matches when {@code expectedId} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s id.
   */
  public static Matcher<RdsInstance> hasId(String expectedId) {
    return new HasId(equalTo(expectedId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s id.
   */
  public static Matcher<RdsInstance> hasId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code Db} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s DB Instance Status.
   */
  public static Matcher<RdsInstance> hasDbInstanceStatus(String expectedDbInstanceStatus) {
    return new HasDbInstanceStatus(equalTo(expectedDbInstanceStatus));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s DB Instance Status.
   */
  public static Matcher<RdsInstance> hasDbInstanceStatus(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasDbInstanceStatus(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedAvailabilityZone} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Availability Zone.
   */
  public static Matcher<RdsInstance> hasAvailabilityZone(String expectedAvailabilityZone) {
    return new HasAvailabilityZone(equalTo(expectedAvailabilityZone));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Availability Zone.
   */
  public static Matcher<RdsInstance> hasAvailabilityZone(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasAvailabilityZone(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedSecurityGroups} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Security Groups.
   */
  public static Matcher<RdsInstance> hasSecurityGroups(List<String> expectedSecurityGroups) {
    return new HasSecurityGroups(equalTo(expectedSecurityGroups));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Security Groups.
   */
  public static Matcher<RdsInstance> hasSecurityGroups(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasAvailabilityZone(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedMultiAz} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Multi AZ.
   */
  public static Matcher<RdsInstance> hasMultiAz(Boolean expectedMultiAz) {
    return new HasMultiAz(equalTo(expectedMultiAz));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Multi AZ.
   */
  public static Matcher<RdsInstance> hasMultiAz(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasMultiAz(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedVpcId} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s VPC Id.
   */
  public static Matcher<RdsInstance> hasVpcId(String expectedVpcId) {
    return new HasVpcId(equalTo(expectedVpcId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s VPC Id.
   */
  public static Matcher<RdsInstance> hasVpcId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasVpcId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedAzSubnetIds} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Az Subnet Ids.
   */
  public static Matcher<RdsInstance> hasAzSubnetIds(Multimap<String, String> expectedAzSubnetIds) {
    return new HasAzSubnetIds(equalTo(expectedAzSubnetIds));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Az Subnet Ids.
   */
  public static Matcher<RdsInstance> hasAzSubnetIds(
      @Nonnull Matcher<? super Multimap<String, String>> subMatcher) {
    return new HasAzSubnetIds(subMatcher);
  }

  private RdsInstanceMatchers() {}

  private static final class HasId extends FeatureMatcher<RdsInstance, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "id", "id");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getId();
    }
  }

  private static final class HasAvailabilityZone extends FeatureMatcher<RdsInstance, String> {
    HasAvailabilityZone(Matcher<? super String> subMatcher) {
      super(subMatcher, "availabilityZone", "availabilityZone");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getAvailabilityZone();
    }
  }

  private static final class HasDbInstanceStatus extends FeatureMatcher<RdsInstance, String> {
    HasDbInstanceStatus(Matcher<? super String> subMatcher) {
      super(subMatcher, "dbInstanceStatus", "dbInstanceStatus");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getDbInstanceStatus();
    }
  }

  private static final class HasAzSubnetIds
      extends FeatureMatcher<RdsInstance, Multimap<String, String>> {
    HasAzSubnetIds(Matcher<? super Multimap<String, String>> subMatcher) {
      super(subMatcher, "azSubnetIds", "azSubnetIds");
    }

    @Override
    protected Multimap<String, String> featureValueOf(RdsInstance actual) {
      return actual.getAzSubnetIds();
    }
  }

  private static final class HasSecurityGroups extends FeatureMatcher<RdsInstance, List<String>> {
    HasSecurityGroups(Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "securityGroups", "securityGroups");
    }

    @Override
    protected List<String> featureValueOf(RdsInstance actual) {
      return actual.getSecurityGroups();
    }
  }

  private static final class HasMultiAz extends FeatureMatcher<RdsInstance, Boolean> {
    HasMultiAz(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "multiAz", "multiAz");
    }

    @Override
    protected Boolean featureValueOf(RdsInstance actual) {
      return actual.getMultiAz();
    }
  }

  private static final class HasVpcId extends FeatureMatcher<RdsInstance, String> {
    HasVpcId(Matcher<? super String> subMatcher) {
      super(subMatcher, "vpcId", "vpcId");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getVpcId();
    }
  }
}
