package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.State;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class HeaderSpaceMatchersImpl {

  static class HasDstIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with dstIps:", "dstIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getDstIps();
    }
  }

  static class HasSrcIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with srcIps:", "srcIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getSrcIps();
    }
  }

  static class HasStates extends FeatureMatcher<HeaderSpace, SortedSet<State>> {
    HasStates(@Nonnull Matcher<? super SortedSet<State>> subMatcher) {
      super(subMatcher, "A HeaderSpace with states:", "states");
    }

    @Override
    protected SortedSet<State> featureValueOf(HeaderSpace actual) {
      return actual.getStates();
    }
  }

  private HeaderSpaceMatchersImpl() {}
}
