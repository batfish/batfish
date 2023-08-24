package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.datamodel.OriginType;
import org.junit.Test;

/** Tests for {@link BDDRoute}. */
public class BDDRouteTest {

  @Test
  public void testCopyConstructorAndEquality() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute r1 = new BDDRoute(factory, 3, 4, 5, 2);
    BDDRoute r2 = new BDDRoute(r1);
    boolean b = r1.equalsForTesting(r2);
    assertTrue(b);

    // test a few equality violations
    BDDRoute r3 = new BDDRoute(r1);
    BDDDomain<OriginType> newOT = new BDDDomain<>(r3.getOriginType());
    newOT.setValue(OriginType.INCOMPLETE);
    r3.setOriginType(newOT);
    b = r1.equalsForTesting(r3);
    assertFalse(b);

    BDDRoute r4 = new BDDRoute(r1);
    r4.setUnsupported(true);
    b = r1.equalsForTesting(r4);
    assertFalse(b);

    BDDRoute r5 = new BDDRoute(r1);
    r5.setWeight(MutableBDDInteger.makeFromValue(factory, 16, 34));
    b = r1.equalsForTesting(r5);
    assertFalse(b);
  }
}
