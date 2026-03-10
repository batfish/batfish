package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.IntegerSpace;
import org.junit.Test;

public class CommunityAPDispositionsTest {

  private final CommunityAPDispositions _cAPD1 =
      new CommunityAPDispositions(4, IntegerSpace.of(0), IntegerSpace.of(2));
  private final CommunityAPDispositions _cAPD2 =
      new CommunityAPDispositions(
          4, IntegerSpace.of(1), IntegerSpace.builder().including(0, 2, 3).build());
  private final BDDRoute _bddRoute =
      new BDDRoute(JFactory.init(100, 100), 4, 0, 0, 0, 0, 0, ImmutableList.of());

  @Test
  public void testUnion() {
    CommunityAPDispositions result = _cAPD1.union(_cAPD2);
    assertEquals(
        new CommunityAPDispositions(
            4, IntegerSpace.builder().including(0, 1).build(), IntegerSpace.of(2)),
        result);
  }

  @Test
  public void testDiff() {
    CommunityAPDispositions result = _cAPD1.diff(_cAPD2);
    assertEquals(
        new CommunityAPDispositions(
            4, IntegerSpace.of(0), IntegerSpace.builder().including(1, 2).build()),
        result);
  }

  @Test
  public void testEmpty() {
    CommunityAPDispositions result = CommunityAPDispositions.empty(_bddRoute);
    assertEquals(
        new CommunityAPDispositions(
            4, IntegerSpace.EMPTY, IntegerSpace.builder().including(0, 1, 2, 3).build()),
        result);
  }

  @Test
  public void testExactly() {
    CommunityAPDispositions result =
        CommunityAPDispositions.exactly(ImmutableSet.of(1, 3), _bddRoute);
    assertEquals(
        new CommunityAPDispositions(
            4,
            IntegerSpace.builder().including(1, 3).build(),
            IntegerSpace.builder().including(0, 2).build()),
        result);
  }

  @Test
  public void testIsExact() {
    assertFalse(_cAPD1.isExact());
    assertTrue(_cAPD2.isExact());
  }
}
