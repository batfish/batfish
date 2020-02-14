package org.batfish.datamodel.matchers;

import java.util.List;
import org.batfish.common.ip.AclIpSpace;
import org.batfish.common.ip.AclIpSpaceLine;
import org.batfish.common.ip.IpSpace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AclIpSpaceMatchersImpl {

  static class HasLines extends FeatureMatcher<AclIpSpace, List<AclIpSpaceLine>> {

    public HasLines(Matcher<? super List<AclIpSpaceLine>> subMatcher) {
      super(subMatcher, "an AclIpSpace with lines:", "lines");
    }

    @Override
    protected List<AclIpSpaceLine> featureValueOf(AclIpSpace actual) {
      return actual.getLines();
    }
  }

  static class IsAclIpSpaceThat extends IsInstanceThat<IpSpace, AclIpSpace> {
    IsAclIpSpaceThat(Matcher<? super AclIpSpace> subMatcher) {
      super(AclIpSpace.class, subMatcher);
    }
  }

  private AclIpSpaceMatchersImpl() {}
}
