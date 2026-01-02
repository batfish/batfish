package org.batfish.common.matchers;

import javax.annotation.Nonnull;
import org.batfish.common.topology.Layer2Node;
import org.batfish.common.topology.Layer2Topology;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class Layer2TopologyMatchersImpl {
  static final class InSameBroadcastDomain extends TypeSafeDiagnosingMatcher<Layer2Topology> {
    private final Layer2Node _node1;
    private final Layer2Node _node2;

    InSameBroadcastDomain(@Nonnull Layer2Node node1, @Nonnull Layer2Node node2) {
      _node1 = node1;
      _node2 = node2;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A Layer2Topology with a broadcast domain containing nodes %s and %s",
              _node1, _node2));
    }

    @Override
    protected boolean matchesSafely(Layer2Topology item, Description mismatchDescription) {
      boolean matches = item.inSameBroadcastDomain(_node1, _node2);
      if (!matches) {
        if (!item.getBroadcastDomainRepresentative(_node1).isPresent()) {
          mismatchDescription.appendText(String.format("does not contain %s", _node1));
        } else if (!item.getBroadcastDomainRepresentative(_node2).isPresent()) {
          mismatchDescription.appendText(String.format("does not contain %s", _node2));
        } else {
          mismatchDescription.appendText("nodes are in different broadcast domains");
        }
      }
      return matches;
    }
  }

  private Layer2TopologyMatchersImpl() {}
}
