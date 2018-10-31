package org.batfish.z3.expr;

import org.batfish.z3.state.NodeInterfaceNeighborUnreachableOrExitsNetwork;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class NodeInterfaceNeighborUnreachableMatchersImpl {
  private NodeInterfaceNeighborUnreachableMatchersImpl() {}

  public static final class HasHostname
      extends FeatureMatcher<NodeInterfaceNeighborUnreachableOrExitsNetwork, String> {
    public HasHostname(Matcher<? super String> subMatcher) {
      super(
          subMatcher, "a NodeInterfaceNeighborUnreachableOrExitsNetwork with hostname", "hostname");
    }

    @Override
    protected String featureValueOf(
        NodeInterfaceNeighborUnreachableOrExitsNetwork
            nodeInterfaceNeighborUnreachableOrExitsNetwork) {
      return nodeInterfaceNeighborUnreachableOrExitsNetwork.getHostname();
    }
  }
}
