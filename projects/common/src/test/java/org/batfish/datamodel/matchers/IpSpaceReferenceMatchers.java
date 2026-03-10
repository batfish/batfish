package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpSpaceReferenceMatchers {
  static final class HasName extends FeatureMatcher<IpSpaceReference, String> {
    HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "An IpSpaceReference with name:", "name");
    }

    @Override
    protected String featureValueOf(IpSpaceReference actual) {
      return actual.getName();
    }
  }

  static class IsIpSpaceReferenceThat extends IsInstanceThat<IpSpace, IpSpaceReference> {
    IsIpSpaceReferenceThat(@Nonnull Matcher<? super IpSpaceReference> subMatcher) {
      super(IpSpaceReference.class, subMatcher);
    }
  }

  private IpSpaceReferenceMatchers() {}
}
