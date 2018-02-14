package org.batfish.datamodel.matchers;

import java.util.Map;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.matchers.OspfAreaMatchersImpl.HasInterfaces;
import org.hamcrest.Matcher;

public class OspfAreaMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area's
   * interfaces.
   */
  public static HasInterfaces hasInterfaces(Matcher<? super Map<String, Interface>> subMatcher) {
    return new HasInterfaces(subMatcher);
  }
}
