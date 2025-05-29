package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.junit.Test;

public class TransferReturnTest {
  @Test
  public void testEquals() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute route1 = new BDDRoute(factory, 0, 0, 0, 0, 0, 0, ImmutableList.of());
    BDDRoute route2 = new BDDRoute(route1);
    route2.getAdminDist().setValue(1);

    new EqualsTester()
        .addEqualityGroup(
            new TransferReturn(route1, factory.one(), false),
            new TransferReturn(route1, factory.one(), false))
        .addEqualityGroup(new TransferReturn(route2, factory.one(), false))
        .addEqualityGroup(new TransferReturn(route2, factory.nithVar(1), false))
        .addEqualityGroup(new TransferReturn(route2, factory.nithVar(1), true))
        .testEquals();
  }
}
