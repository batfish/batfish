package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasDstIps;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasDstPorts;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasNotDstIps;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasNotSrcIps;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasSrcIps;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasSrcOrDstIps;
import org.hamcrest.Matcher;

public class HeaderSpaceMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * dstIps.
   */
  public static HasDstIps hasDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasDstIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * dstPorts.
   */
  public static HasDstPorts hasDstPorts(@Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
    return new HasDstPorts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * notDstIps.
   */
  public static HasNotDstIps hasNotDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasNotDstIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * notSrcIps.
   */
  public static HasNotSrcIps hasNotSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasNotSrcIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * srcIps.
   */
  public static HasSrcIps hasSrcIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasSrcIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * srcOrDstIps.
   */
  public static HasSrcOrDstIps hasSrcOrDstIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasSrcOrDstIps(subMatcher);
  }

  private HeaderSpaceMatchers() {}
}
