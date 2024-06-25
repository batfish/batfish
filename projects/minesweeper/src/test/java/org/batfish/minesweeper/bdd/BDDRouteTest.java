package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isZero;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.datamodel.OriginType;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;

/** Tests for {@link BDDRoute}. */
public class BDDRouteTest {

  @Test
  public void testCopyConstructorAndEquality() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute r1 = new BDDRoute(factory, 3, 4, 5, 6, 2, ImmutableList.of());
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

  @Test
  public void testWellFormedOriginType() {
    BDDFactory factory = JFactory.init(100, 100);
    BDDRoute route = new BDDRoute(factory, 0, 0, 0, 0, 0, ImmutableList.of());

    BDD anyOriginType =
        factory.orAll(
            route.getOriginType().value(OriginType.EGP),
            route.getOriginType().value(OriginType.IGP),
            route.getOriginType().value(OriginType.INCOMPLETE));
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> route.getOriginType().satAssignmentToValue(anyOriginType.not()));
    assertThat(
        thrown, ThrowableMessageMatcher.hasMessage(containsString("is not valid in this domain")));
    assertThat(route.bgpWellFormednessConstraints().and(anyOriginType.not()), isZero());
  }
}
