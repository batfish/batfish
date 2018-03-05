package org.batfish.datamodel.matchers;

import java.util.Set;
import org.batfish.datamodel.OspfAreaSummary;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasInterfaces;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasSummary;
import org.hamcrest.Matcher;

public class OspfAreaMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area's
   * interfaces.
   */
  public static HasInterfaces hasInterfaces(Matcher<? super Set<String>> subMatcher) {
    return new HasInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area's
   * summary with the specified summaryPrefix.
   */
  public static HasSummary hasSummary(
      Prefix summaryPrefix, Matcher<? super OspfAreaSummary> subMatcher) {
    return new HasSummary(summaryPrefix, subMatcher);
  }
}
