package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class HeaderSpaceMatchersImpl {

  static class HasDstIps extends FeatureMatcher<HeaderSpace, SortedSet<IpWildcard>> {
    HasDstIps(@Nonnull Matcher<? super SortedSet<IpWildcard>> subMatcher) {
      super(subMatcher, "A HeaderSpace with dstIps:", "dstIps");
    }

    @Override
    protected SortedSet<IpWildcard> featureValueOf(HeaderSpace actual) {
      return actual.getDstIps();
    }
  }

  static class HasSrcIps extends FeatureMatcher<HeaderSpace, SortedSet<IpWildcard>> {
    HasSrcIps(@Nonnull Matcher<? super SortedSet<IpWildcard>> subMatcher) {
      super(subMatcher, "A HeaderSpace with srcIps:", "srcIps");
    }

    @Override
    protected SortedSet<IpWildcard> featureValueOf(HeaderSpace actual) {
      return actual.getSrcIps();
    }
  }

  private HeaderSpaceMatchersImpl() {}
}
