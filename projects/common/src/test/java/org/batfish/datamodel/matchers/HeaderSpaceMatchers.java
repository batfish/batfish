package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class HeaderSpaceMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * dstIps.
   */
  public static Matcher<HeaderSpace> hasDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasDstIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * dstPorts.
   */
  public static Matcher<HeaderSpace> hasDstPorts(
      @Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
    return new HasDstPorts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * notDstIps.
   */
  public static Matcher<HeaderSpace> hasNotDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasNotDstIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * notSrcIps.
   */
  public static Matcher<HeaderSpace> hasNotSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasNotSrcIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * srcIps.
   */
  public static Matcher<HeaderSpace> hasSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasSrcIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * srcOrDstIps.
   */
  public static Matcher<HeaderSpace> hasSrcOrDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasSrcOrDstIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * srcOrDstPorts.
   */
  public static @Nonnull Matcher<HeaderSpace> hasSrcOrDstPorts(
      @Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
    return new HasSrcOrDstPorts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * ipProtocols.
   */
  public static @Nonnull Matcher<HeaderSpace> hasIpProtocols(
      @Nonnull Matcher<? super SortedSet<IpProtocol>> subMatcher) {
    return new HasIpProtocols(subMatcher);
  }

  private HeaderSpaceMatchers() {}

  private static final class HasDstIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with dstIps:", "dstIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getDstIps();
    }
  }

  private static final class HasDstPorts extends FeatureMatcher<HeaderSpace, SortedSet<SubRange>> {
    HasDstPorts(@Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
      super(subMatcher, "A HeaderSpace with dstPorts:", "dstPorts");
    }

    @Override
    protected SortedSet<SubRange> featureValueOf(HeaderSpace headerSpace) {
      return headerSpace.getDstPorts();
    }
  }

  private static final class HasIpProtocols
      extends FeatureMatcher<HeaderSpace, SortedSet<IpProtocol>> {
    HasIpProtocols(@Nonnull Matcher<? super SortedSet<IpProtocol>> subMatcher) {
      super(subMatcher, "A HeaderSpace with ipProtocols:", "ipProtocols");
    }

    @Override
    protected SortedSet<IpProtocol> featureValueOf(HeaderSpace actual) {
      return actual.getIpProtocols();
    }
  }

  private static final class HasNotDstIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasNotDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with notDstIps:", "notDstIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getNotDstIps();
    }
  }

  private static final class HasNotSrcIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasNotSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with notSrcIps:", "notSrcIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getNotSrcIps();
    }
  }

  private static final class HasSrcIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with srcIps:", "srcIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getSrcIps();
    }
  }

  private static final class HasSrcOrDstIps extends FeatureMatcher<HeaderSpace, IpSpace> {
    HasSrcOrDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A HeaderSpace with srcOrDstIps:", "srcOrDstIps");
    }

    @Override
    protected IpSpace featureValueOf(HeaderSpace actual) {
      return actual.getSrcOrDstIps();
    }
  }

  private static final class HasSrcOrDstPorts
      extends FeatureMatcher<HeaderSpace, SortedSet<SubRange>> {
    HasSrcOrDstPorts(@Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
      super(subMatcher, "A HeaderSpace with srcOrDstPorts:", "srcOrDstPorts");
    }

    @Override
    protected SortedSet<SubRange> featureValueOf(HeaderSpace actual) {
      return actual.getSrcOrDstPorts();
    }
  }
}
