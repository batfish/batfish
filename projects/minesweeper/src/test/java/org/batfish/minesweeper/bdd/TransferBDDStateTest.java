package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.junit.Test;

public class TransferBDDStateTest {
  @Test
  public void testEquals() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute route = new BDDRoute(factory, 0, 0, 0, 0, 0, 0, ImmutableList.of());
    TransferParam p = new TransferParam(false);
    TransferReturn ret = new TransferReturn(route, factory.one(), false);
    TransferResult r1 = new TransferResult(ret, route, false, false, false, false);
    TransferResult r2 = new TransferResult(ret, route, true, false, false, false);

    new EqualsTester()
        .addEqualityGroup(new TransferBDDState(p, r1), new TransferBDDState(p, r1))
        .addEqualityGroup(new TransferBDDState(p, r2))
        .testEquals();
  }
}
