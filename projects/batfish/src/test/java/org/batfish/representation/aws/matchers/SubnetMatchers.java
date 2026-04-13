package org.batfish.representation.aws.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Subnet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class SubnetMatchers {

  /**
   * Provides a matcher that matches when {@code expectedId} is equal to the {@link
   * org.batfish.representation.aws.Subnet}'s id.
   */
  public static Matcher<Subnet> hasId(String expectedId) {
    return new HasId(equalTo(expectedId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.Subnet}'s id.
   */
  public static Matcher<Subnet> hasId(@Nonnull Matcher<? super String> subMatcher) {
    return new HasId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedVpcId} is equal to the {@link
   * org.batfish.representation.aws.Subnet}'s VPC Id.
   */
  public static Matcher<Subnet> hasVpcId(String expectedVpcId) {
    return new HasVpcId(equalTo(expectedVpcId));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.Subnet}'s VPC Id
   */
  public static Matcher<Subnet> hasVpcID(@Nonnull Matcher<? super String> subMatcher) {
    return new HasVpcId(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedCidrBlock} is equal to the {@link
   * org.batfish.representation.aws.Subnet}'s CIDR Block.
   */
  public static Matcher<Subnet> hasCidrBlock(Prefix expectedCidrBlock) {
    return new HasCidrBlock(equalTo(expectedCidrBlock));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.representation.aws.Subnet}'s CIDR Block
   */
  public static Matcher<Subnet> hasCidrBlock(@Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasCidrBlock(subMatcher);
  }

  private SubnetMatchers() {}

  private static final class HasId extends FeatureMatcher<Subnet, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "id", "id");
    }

    @Override
    protected String featureValueOf(Subnet actual) {
      return actual.getId();
    }
  }

  private static final class HasVpcId extends FeatureMatcher<Subnet, String> {
    HasVpcId(Matcher<? super String> subMatcher) {
      super(subMatcher, "vpcId", "vpcId");
    }

    @Override
    protected String featureValueOf(Subnet actual) {
      return actual.getVpcId();
    }
  }

  private static final class HasCidrBlock extends FeatureMatcher<Subnet, Prefix> {
    HasCidrBlock(Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "CidrBlock", "CidrBlock");
    }

    @Override
    protected Prefix featureValueOf(Subnet actual) {
      return actual.getCidrBlock();
    }
  }
}
