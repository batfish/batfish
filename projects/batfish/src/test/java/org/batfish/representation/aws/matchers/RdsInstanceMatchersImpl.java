package org.batfish.representation.aws.matchers;

import com.google.common.collect.Multimap;
import java.util.List;
import org.batfish.representation.aws.RdsInstance;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class RdsInstanceMatchersImpl {
  static final class HasId extends FeatureMatcher<RdsInstance, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "id", "id");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getId();
    }
  }

  static final class HasAvailabilityZone extends FeatureMatcher<RdsInstance, String> {
    HasAvailabilityZone(Matcher<? super String> subMatcher) {
      super(subMatcher, "availabilityZone", "availabilityZone");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getAvailabilityZone();
    }
  }

  static final class HasDbInstanceStatus extends FeatureMatcher<RdsInstance, String> {
    HasDbInstanceStatus(Matcher<? super String> subMatcher) {
      super(subMatcher, "dbInstanceStatus", "dbInstanceStatus");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getDbInstanceStatus();
    }
  }

  static final class HasAzSubnetIds extends FeatureMatcher<RdsInstance, Multimap<String, String>> {
    HasAzSubnetIds(Matcher<? super Multimap<String, String>> subMatcher) {
      super(subMatcher, "azSubnetIds", "azSubnetIds");
    }

    @Override
    protected Multimap<String, String> featureValueOf(RdsInstance actual) {
      return actual.getAzSubnetIds();
    }
  }

  static final class HasSecurityGroups extends FeatureMatcher<RdsInstance, List<String>> {
    HasSecurityGroups(Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "securityGroups", "securityGroups");
    }

    @Override
    protected List<String> featureValueOf(RdsInstance actual) {
      return actual.getSecurityGroups();
    }
  }

  static final class HasMultiAz extends FeatureMatcher<RdsInstance, Boolean> {
    HasMultiAz(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "multiAz", "multiAz");
    }

    @Override
    protected Boolean featureValueOf(RdsInstance actual) {
      return actual.getMultiAz();
    }
  }

  static final class HasVpcId extends FeatureMatcher<RdsInstance, String> {
    HasVpcId(Matcher<? super String> subMatcher) {
      super(subMatcher, "vpcId", "vpcId");
    }

    @Override
    protected String featureValueOf(RdsInstance actual) {
      return actual.getVpcId();
    }
  }

  private RdsInstanceMatchersImpl() {}
}
