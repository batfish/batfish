package org.batfish.representation.aws.matchers;

import java.util.List;
import org.batfish.representation.aws.ElasticsearchDomain;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class ElasticsearchDomainMatchersImpl {
  static final class HasId extends FeatureMatcher<ElasticsearchDomain, String> {
    HasId(Matcher<? super String> subMatcher) {
      super(subMatcher, "id", "id");
    }

    @Override
    protected String featureValueOf(ElasticsearchDomain actual) {
      return actual.getId();
    }
  }

  static final class HasSecurityGroups extends FeatureMatcher<ElasticsearchDomain, List<String>> {
    HasSecurityGroups(Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "securityGroups", "securityGroups");
    }

    @Override
    protected List<String> featureValueOf(ElasticsearchDomain actual) {
      return actual.getSecurityGroups();
    }
  }

  static final class HasSubnets extends FeatureMatcher<ElasticsearchDomain, List<String>> {
    HasSubnets(Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "subnets ", "subnets");
    }

    @Override
    protected List<String> featureValueOf(ElasticsearchDomain actual) {
      return actual.getSubnets();
    }
  }

  static final class HasAvailable extends FeatureMatcher<ElasticsearchDomain, Boolean> {
    HasAvailable(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "available", "available");
    }

    @Override
    protected Boolean featureValueOf(ElasticsearchDomain actual) {
      return actual.getAvailable();
    }
  }

  static final class HasVpcId extends FeatureMatcher<ElasticsearchDomain, String> {
    HasVpcId(Matcher<? super String> subMatcher) {
      super(subMatcher, "vpcId", "vpcId");
    }

    @Override
    protected String featureValueOf(ElasticsearchDomain actual) {
      return actual.getVpcId();
    }
  }

  private ElasticsearchDomainMatchersImpl() {}
}
