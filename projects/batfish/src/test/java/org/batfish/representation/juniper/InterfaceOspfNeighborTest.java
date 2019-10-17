package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class InterfaceOspfNeighborTest {

  @Test
  public void testEquals() {
    InterfaceOspfNeighbor neighbor1 = new InterfaceOspfNeighbor(Ip.parse("1.1.1.1"));
    neighbor1.setDesignated(true);
    InterfaceOspfNeighbor neighbor2 = new InterfaceOspfNeighbor(Ip.parse("1.1.1.1"));
    neighbor2.setDesignated(true);
    InterfaceOspfNeighbor neighbor3 = new InterfaceOspfNeighbor(Ip.parse("1.1.1.1"));
    neighbor3.setDesignated(false);
    new EqualsTester()
        .addEqualityGroup(neighbor1, neighbor2)
        .addEqualityGroup(neighbor3)
        .addEqualityGroup(new InterfaceOspfNeighbor(Ip.parse("1.1.1.2")))
        .testEquals();
  }
}
