package org.batfish.z3.matchers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasAclActions;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasAclConditions;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasArpTrueEdge;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledEdges;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledInterfaces;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledNodes;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasEnabledVrfs;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasIpsByHostname;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasNeighborUnreachable;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasSourceNats;
import org.batfish.z3.matchers.SynthesizerInputMatchersImpl.HasTopologyInterfaces;
import org.batfish.z3.state.AclPermit;
import org.hamcrest.Matcher;

public class SynthesizerInputMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled ACLs.
   */
  public static HasAclActions hasAclActions(
      Matcher<? super Map<String, Map<String, List<LineAction>>>> subMatcher) {
    return new HasAclActions(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's ACL conditions.
   */
  public static HasAclConditions hasAclConditions(
      Matcher<? super Map<String, Map<String, List<BooleanExpr>>>> subMatcher) {
    return new HasAclConditions(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's arpTrueEdge.
   */
  public static HasArpTrueEdge hasArpTrueEdge(
      Matcher<? super Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>>>
          subMatcher) {
    return new HasArpTrueEdge(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled edges.
   */
  public static HasEnabledEdges hasEnabledEdges(Matcher<? super Set<Edge>> subMatcher) {
    return new HasEnabledEdges(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled interfaces.
   */
  public static HasEnabledInterfaces hasEnabledInterfaces(
      Matcher<? super Map<String, Set<String>>> subMatcher) {
    return new HasEnabledInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled nodes.
   */
  public static HasEnabledNodes hasEnabledNodes(Matcher<? super Set<String>> subMatcher) {
    return new HasEnabledNodes(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's enabled VRFs.
   */
  public static HasEnabledVrfs hasEnabledVrfs(
      Matcher<? super Map<String, Set<String>>> subMatcher) {
    return new HasEnabledVrfs(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's IPs by hostname.
   */
  public static HasIpsByHostname hasIpsByHostname(
      Matcher<? super Map<String, Set<Ip>>> subMatcher) {
    return new HasIpsByHostname(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's neighborUnreachable.
   */
  public static HasNeighborUnreachable hasNeighborUnreachable(
      Matcher<? super Map<String, Map<String, Map<String, BooleanExpr>>>> subMatcher) {
    return new HasNeighborUnreachable(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's source NATs.
   */
  public static HasSourceNats hasSourceNats(
      @Nonnull
          Matcher<? super Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>>>
              subMatcher) {
    return new HasSourceNats(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * SynthesizerInput's topology interfaces.
   */
  public static HasTopologyInterfaces hasTopologyInterfaces(
      Matcher<? super Map<String, Set<String>>> subMatcher) {
    return new HasTopologyInterfaces(subMatcher);
  }

  private SynthesizerInputMatchers() {}
}
