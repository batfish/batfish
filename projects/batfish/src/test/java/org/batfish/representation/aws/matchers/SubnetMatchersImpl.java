package org.batfish.representation.aws.matchers;

import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Subnet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class SubnetMatchersImpl {
  static final class HasId extends FeatureMatcher<Subnet, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "id", "id");
    }

    @Override
    protected String featureValueOf(Subnet actual) {
      return actual.getId();
    }
  }

  static final class HasVpcId extends FeatureMatcher<Subnet, String> {
    HasVpcId(Matcher<? super String> subMatcher) {
      super(subMatcher, "vpcId", "vpcId");
    }

    @Override
    protected String featureValueOf(Subnet actual) {
      return actual.getVpcId();
    }
  }

  static final class HasCidrBlock extends FeatureMatcher<Subnet, Prefix> {
    HasCidrBlock(Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "CidrBlock", "CidrBlock");
    }

    @Override
    protected Prefix featureValueOf(Subnet actual) {
      return actual.getCidrBlock();
    }
  }

  private SubnetMatchersImpl() {}
}
