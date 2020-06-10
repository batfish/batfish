package org.batfish.representation.aws.matchers;

import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Multimap;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.representation.aws.matchers.RdsInstanceMatchersImpl.HasAvailabilityZone;
import org.batfish.representation.aws.matchers.RdsInstanceMatchersImpl.HasAzSubnetIds;
import org.batfish.representation.aws.matchers.RdsInstanceMatchersImpl.HasDbInstanceStatus;
import org.batfish.representation.aws.matchers.RdsInstanceMatchersImpl.HasId;
import org.batfish.representation.aws.matchers.RdsInstanceMatchersImpl.HasMultiAz;
import org.batfish.representation.aws.matchers.RdsInstanceMatchersImpl.HasSecurityGroups;
import org.batfish.representation.aws.matchers.RdsInstanceMatchersImpl.HasVpcId;
import org.hamcrest.Matcher;

public final class RdsInstanceMatchers {

  /**
   * Provides a matcher that matches when {@code expectedId} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s id.
   */
  public static HasId hasId(String expectedId) {
    return new HasId(equalTo(expectedId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s id.
   */
  public static HasId hasId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code Db} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s DB Instance Status.
   */
  public static HasDbInstanceStatus hasDbInstanceStatus(String expectedDbInstanceStatus) {
    return new HasDbInstanceStatus(equalTo(expectedDbInstanceStatus));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s DB Instance Status.
   */
  public static HasDbInstanceStatus hasDbInstanceStatus(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasDbInstanceStatus(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedAvailabilityZone} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Availability Zone.
   */
  public static HasAvailabilityZone hasAvailabilityZone(String expectedAvailabilityZone) {
    return new HasAvailabilityZone(equalTo(expectedAvailabilityZone));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Availability Zone.
   */
  public static HasAvailabilityZone hasAvailabilityZone(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasAvailabilityZone(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedSecurityGroups} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Security Groups.
   */
  public static HasSecurityGroups hasSecurityGroups(List<String> expectedSecurityGroups) {
    return new HasSecurityGroups(equalTo(expectedSecurityGroups));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Security Groups.
   */
  public static HasAvailabilityZone hasSecurityGroups(@Nonnull Matcher<? super String> subMatcher) {
    return new HasAvailabilityZone(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedMultiAz} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Multi AZ.
   */
  public static HasMultiAz hasMultiAz(Boolean expectedMultiAz) {
    return new HasMultiAz(equalTo(expectedMultiAz));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Multi AZ.
   */
  public static HasMultiAz hasMultiAz(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasMultiAz(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedVpcId} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s VPC Id.
   */
  public static HasVpcId hasVpcId(String expectedVpcId) {
    return new HasVpcId(equalTo(expectedVpcId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s VPC Id.
   */
  public static HasVpcId hasVpcId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasVpcId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedAzSubnetIds} is equal to the {@link
   * org.batfish.representation.aws.RdsInstance}'s Az Subnet Ids.
   */
  public static HasAzSubnetIds hasAzSubnetIds(Multimap<String, String> expectedAzSubnetIds) {
    return new HasAzSubnetIds(equalTo(expectedAzSubnetIds));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.RdsInstance}'s Az Subnet Ids.
   */
  public static HasAzSubnetIds hasAzSubnetIds(
      @Nonnull Matcher<? super Multimap<String, String>> subMatcher) {
    return new HasAzSubnetIds(subMatcher);
  }

  private RdsInstanceMatchers() {}
}
