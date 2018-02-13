package org.batfish.z3.matchers;

import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledFlowSinks;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledInterfaces;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledNodes;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledVrfs;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasFibConditions;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasIpsByHostname;
import org.hamcrest.Matcher;

public class SynthesizerInputMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled flowSinks.
   */
  public static HasEnabledFlowSinks hasEnabledFlowSinks(
      Matcher<? super Set<NodeInterfacePair>> subMatcher) {
    return new HasEnabledFlowSinks(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled interfaces.
   */
  public static HasEnabledInterfaces hasEnabledInterfaces(
      Matcher<? super Map<String, Map<String, Interface>>> subMatcher) {
    return new HasEnabledInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled nodes.
   */
  public static HasEnabledNodes hasEnabledNodes(
      Matcher<? super Map<String, Configuration>> subMatcher) {
    return new HasEnabledNodes(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled VRFs.
   */
  public static HasEnabledVrfs hasEnabledVrfs(
      Matcher<? super Map<String, Map<String, Vrf>>> subMatcher) {
    return new HasEnabledVrfs(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's fibConditions.
   */
  public static HasFibConditions hasFibConditions(
      Matcher<? super Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>>
          subMatcher) {
    return new HasFibConditions(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's IPs by hostname.
   */
  public static HasIpsByHostname hasIpsByHostname(
      Matcher<? super Map<String, Set<Ip>>> subMatcher) {
    return new HasIpsByHostname(subMatcher);
  }

  private SynthesizerInputMatchers() {}
}
