package org.batfish.datamodel.matchers;

import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasOspfProcess;
import org.hamcrest.Matcher;

public class VrfMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's OSPF
   * process.
   */
  public static Matcher<? super Vrf> hasOspfProcess(Matcher<? super OspfProcess> subMatcher) {
    return new HasOspfProcess(subMatcher);
  }
}
