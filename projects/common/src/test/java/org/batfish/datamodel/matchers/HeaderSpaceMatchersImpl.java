package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
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

  static class HasDstPorts extends FeatureMatcher<HeaderSpace, SortedSet<SubRange>> {
    HasDstPorts(@Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
      super(subMatcher, "A HeaderSpace with dstPorts:", "dstPorts");
    }

    @Override
    protected SortedSet<SubRange> featureValueOf(HeaderSpace headerSpace) {
      return headerSpace.getDstPorts();
    }
  }

  static class HasIpProtocols extends FeatureMatcher<HeaderSpace, SortedSet<IpProtocol>> {
    HasIpProtocols(@Nonnull Matcher<? super SortedSet<IpProtocol>> subMatcher) {
      super(subMatcher, "A HeaderSpace with ipProtocols:", "ipProtocols");
    }

    @Override
    protected SortedSet<IpProtocol> featureValueOf(HeaderSpace actual) {
      return actual.getIpProtocols();
    }
  }

  static class HasNotDstIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasNotDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with notDstIps:", "notDstIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getNotDstIps();
    }
  }

  static class HasNotSrcIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasNotSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with notSrcIps:", "notSrcIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getNotSrcIps();
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

  static class HasSrcOrDstIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasSrcOrDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with srcOrDstIps:", "srcOrDstIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getSrcOrDstIps();
    }
  }

  static class HasSrcOrDstPorts extends FeatureMatcher<HeaderSpace, SortedSet<SubRange>> {
    HasSrcOrDstPorts(@Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
      super(subMatcher, "A HeaderSpace with srcOrDstPorts:", "srcOrDstPorts");
    }

    @Override
    protected SortedSet<SubRange> featureValueOf(HeaderSpace actual) {
      return actual.getSrcOrDstPorts();
    }
  }

  private HeaderSpaceMatchersImpl() {}
}
