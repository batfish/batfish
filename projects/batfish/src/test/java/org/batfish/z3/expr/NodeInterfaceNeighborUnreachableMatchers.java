package org.batfish.z3.expr;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.z3.expr.NodeInterfaceNeighborUnreachableMatchersImpl.HasHostname;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachableOrExitsNetwork;
import org.hamcrest.Matcher;

public final class NodeInterfaceNeighborUnreachableMatchers {
  private NodeInterfaceNeighborUnreachableMatchers() {}

  public static Matcher<NodeInterfaceNeighborUnreachableOrExitsNetwork> hasHostname(
      Matcher<String> hostnameMatcher) {
    return new HasHostname(hostnameMatcher);
  }

  public static Matcher<NodeInterfaceNeighborUnreachableOrExitsNetwork> hasHostname(
      String hostname) {
    return new HasHostname(equalTo(hostname));
  }
}
