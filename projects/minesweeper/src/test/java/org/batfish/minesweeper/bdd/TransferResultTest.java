package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.junit.Test;

public class TransferResultTest {
  @Test
  public void testEquals() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute route = new BDDRoute(factory, 0, 0, 0, 0, 0, 0, ImmutableList.of());
    BDDRoute route2 = new BDDRoute(route);
    route2.setNextHopSet(true);
    TransferReturn r1 = new TransferReturn(route, factory.one(), false);
    TransferReturn r2 = new TransferReturn(route, factory.nithVar(0), false);

    new EqualsTester()
        .addEqualityGroup(
            new TransferResult(r1, route, false, false, false, false),
            new TransferResult(r1, route, false, false, false, false))
        .addEqualityGroup(new TransferResult(r1, route2, false, false, false, false))
        .addEqualityGroup(new TransferResult(r2, route, false, false, false, false))
        .addEqualityGroup(new TransferResult(r2, route, true, false, false, false))
        .addEqualityGroup(new TransferResult(r2, route, true, true, false, false))
        .addEqualityGroup(new TransferResult(r2, route, true, true, true, false))
        .addEqualityGroup(new TransferResult(r2, route, true, true, true, true))
        .testEquals();
  }
}
