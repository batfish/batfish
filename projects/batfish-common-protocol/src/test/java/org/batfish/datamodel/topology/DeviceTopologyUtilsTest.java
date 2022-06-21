package org.batfish.datamodel.topology;

import static org.batfish.common.topology.bridge_domain.edge.L1ToL3.l1ToL3NonBridged;
import static org.batfish.common.topology.bridge_domain.edge.L3ToL1.l3NonBridgedToL1;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.topology.DeviceTopologyUtils.computeDeviceTopology;
import static org.batfish.datamodel.topology.InterfaceTopology.l13NoEncapsulation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.common.topology.bridge_domain.node.L3NonBridgedInterface;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Test of {@link DeviceTopologyUtils}. */
public final class DeviceTopologyUtilsTest {

  @Test
  public void testComputeDeviceTopology_nonBridgedL3Interface() {
    String hostname = "c";
    String ifaceName = "i1";

    Configuration c =
        Configuration.builder().setHostname(hostname).setConfigurationFormat(CISCO_IOS).build();
    Vrf v = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Interface.builder()
        .setVrf(v)
        .setOwner(c)
        .setName(ifaceName)
        .setTopology(l13NoEncapsulation(ifaceName))
        .build();
    DeviceTopology t = computeDeviceTopology(c);

    assertThat(t.getL1Interfaces(), hasKeys(ifaceName));
    assertThat(t.getL3Interfaces(), hasKeys(ifaceName));

    L1Interface l1 = t.getL1Interfaces().get(ifaceName);
    L3Interface l3 = t.getL3Interfaces().get(ifaceName);

    assertThat(l1.getToL3ForTest(), equalTo(ImmutableMap.of(l3, l1ToL3NonBridged(null))));
    assertThat(l3, instanceOf(L3NonBridgedInterface.class));

    L3NonBridgedInterface l3n = (L3NonBridgedInterface) l3;

    assertThat(l3n.getL1InterfaceForTest(), equalTo(l1));
    assertThat(l3n.getToL1ForTest(), equalTo(l3NonBridgedToL1(null)));
  }
}
