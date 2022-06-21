package org.batfish.common.topology.bridge_domain.node;

import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.vxlan.VxlanNode;
import org.junit.Test;

/** Test of {@link L2Vni}. */
public final class L2VniTest {

  @Test
  public void testEquals() {
    L2Vni obj = L2Vni.of(new VxlanNode("a", 1, LAYER_2));
    new EqualsTester()
        .addEqualityGroup(obj, L2Vni.of(new VxlanNode("a", 1, LAYER_2)))
        .addEqualityGroup(L2Vni.of(new VxlanNode("b", 1, LAYER_2)))
        .testEquals();
  }
}
