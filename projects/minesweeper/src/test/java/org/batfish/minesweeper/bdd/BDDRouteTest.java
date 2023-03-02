package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertTrue;

import net.sf.javabdd.JFactory;
import org.junit.Test;

/** Tests for {@link BDDRoute}. */
public class BDDRouteTest {

  @Test
  public void testCopyConstructorAndEquality() {
    BDDRoute r1 = new BDDRoute(JFactory.init(100, 100), 3, 4);
    BDDRoute r2 = new BDDRoute(r1);
    boolean b = r1.equalsForTesting(r2);
    assertTrue(b);
  }
}
