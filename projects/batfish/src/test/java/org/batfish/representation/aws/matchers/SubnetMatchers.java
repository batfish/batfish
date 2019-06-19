package org.batfish.representation.aws.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.matchers.SubnetMatchersImpl.HasCidrBlock;
import org.batfish.representation.aws.matchers.SubnetMatchersImpl.HasId;
import org.batfish.representation.aws.matchers.SubnetMatchersImpl.HasVpcId;
import org.hamcrest.Matcher;

public final class SubnetMatchers {

  /**
   * Provides a matcher that matches when {@code expectedId} is equal to the {@link
   * org.batfish.representation.aws.Subnet}'s id.
   */
  public static HasId hasId(String expectedId) {
    return new HasId(equalTo(expectedId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.Subnet}'s id.
   */
  public static HasId hasId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedVpcId} is equal to the {@link
   * org.batfish.representation.aws.Subnet}'s VPC Id.
   */
  public static HasVpcId hasVpcId(String expectedVpcId) {
    return new HasVpcId(equalTo(expectedVpcId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.Subnet}'s VPC Id
   */
  public static HasVpcId hasVpcID(@Nonnull Matcher<? super String> subMatcher) {
    return new HasVpcId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedCidrBlock} is equal to the {@link
   * org.batfish.representation.aws.Subnet}'s CIDR Block.
   */
  public static HasCidrBlock hasCidrBlock(Prefix expectedCidrBlock) {
    return new HasCidrBlock(equalTo(expectedCidrBlock));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.Subnet}'s CIDR Block
   */
  public static HasCidrBlock hasCidrBlock(@Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasCidrBlock(subMatcher);
  }

  private SubnetMatchers() {}
}
