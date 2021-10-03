package org.batfish.common.util.isp;

import static org.batfish.common.util.isp.IspModelingUtils.LINK_LOCAL_ADDRESS;

import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.Layer1Node;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

public class IspInterfaceTest {

  @Test
  public void testEquals() {
    Layer1Node layer1Node = new Layer1Node("host", "interface");
    new EqualsTester()
        .addEqualityGroup(
            new IspInterface("name", LINK_LOCAL_ADDRESS, layer1Node, null),
            new IspInterface("name", LINK_LOCAL_ADDRESS, new Layer1Node("host", "interface"), null))
        .addEqualityGroup(new IspInterface("other", LINK_LOCAL_ADDRESS, layer1Node, null))
        .addEqualityGroup(
            new IspInterface(
                "name", ConcreteInterfaceAddress.parse("1.1.1.1/24"), layer1Node, null))
        .addEqualityGroup(
            new IspInterface(
                "name", LINK_LOCAL_ADDRESS, new Layer1Node("other", "interface"), null))
        .addEqualityGroup(new IspInterface("name", LINK_LOCAL_ADDRESS, layer1Node, 23))
        .testEquals();
  }
}
