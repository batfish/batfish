package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Layer2Topology}. */
public final class Layer2TopologyTest {
  @Rule public ExpectedException _exception = ExpectedException.none();

  private static final NodeInterfacePair I1 = new NodeInterfacePair("H1", "I1");
  private static final NodeInterfacePair I2 = new NodeInterfacePair("H2", "I2");
  private static final NodeInterfacePair I3 = new NodeInterfacePair("H3", "I3");

  @Test
  public void testLayer2Topology() {
    Layer2Topology topology =
        new Layer2Topology(ImmutableList.of(ImmutableSet.of(I1, I2), ImmutableSet.of(I3)));
    assertThat("I1 I2", topology.inSameBroadcastDomain(I1, I2));
    assertThat("I1 I3", !topology.inSameBroadcastDomain(I1, I3));
    assertThat("I3", topology.inSameBroadcastDomain(I3, I3));
  }

  @Test
  public void testLayer2Topology_overlapping() {
    // overlapping broadcast domains are not allowed
    _exception.expect(IllegalArgumentException.class);
    new Layer2Topology(ImmutableList.of(ImmutableSet.of(I1, I2), ImmutableSet.of(I1, I3)));
  }
}
