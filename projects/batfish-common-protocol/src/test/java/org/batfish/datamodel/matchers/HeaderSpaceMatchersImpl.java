package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.State;
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

  static class HasState extends FeatureMatcher<HeaderSpace, SortedSet<State>> {
    HasState(@Nonnull Matcher<? super SortedSet<State>> subMatcher) {
      super(subMatcher, "A HeaderSpace with state:", "state");
    }

    @Override
    protected SortedSet<State> featureValueOf(HeaderSpace actual) {
      return actual.getStates();
    }
  }

  private HeaderSpaceMatchersImpl() {}
}
