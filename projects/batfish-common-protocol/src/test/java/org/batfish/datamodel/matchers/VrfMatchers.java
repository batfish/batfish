package org.batfish.datamodel.matchers;

import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasBgpProcess;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasOspfProcess;
import org.hamcrest.Matcher;

public class VrfMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's BGP
   * process.
   */
  public static HasBgpProcess hasBgpProcess(Matcher<? super BgpProcess> subMatcher) {
    return new HasBgpProcess(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's OSPF
   * process.
   */
  public static HasOspfProcess hasOspfProcess(Matcher<? super OspfProcess> subMatcher) {
    return new HasOspfProcess(subMatcher);
  }
}
