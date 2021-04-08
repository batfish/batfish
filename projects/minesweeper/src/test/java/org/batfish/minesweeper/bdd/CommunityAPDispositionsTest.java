package org.batfish.minesweeper.bdd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class CommunityAPDispositionsTest {

  private final CommunityAPDispositions _cAPD1 =
      new CommunityAPDispositions(4, ImmutableSet.of(0), ImmutableSet.of(2));
  private final CommunityAPDispositions _cAPD2 =
      new CommunityAPDispositions(4, ImmutableSet.of(1), ImmutableSet.of(0, 2, 3));
  private final BDDRoute _bddRoute = new BDDRoute(4, 0);

  @Test
  public void testUnion() {
    CommunityAPDispositions result = _cAPD1.union(_cAPD2);
    assertEquals(new CommunityAPDispositions(4, ImmutableSet.of(0, 1), ImmutableSet.of(2)), result);
  }

  @Test
  public void testDiff() {
    CommunityAPDispositions result = _cAPD1.diff(_cAPD2);
    assertEquals(new CommunityAPDispositions(4, ImmutableSet.of(0), ImmutableSet.of(1, 2)), result);
  }

  @Test
  public void testEmpty() {
    CommunityAPDispositions result = CommunityAPDispositions.empty(_bddRoute);
    assertEquals(
        new CommunityAPDispositions(4, ImmutableSet.of(), ImmutableSet.of(0, 1, 2, 3)), result);
  }

  @Test
  public void testExactly() {
    CommunityAPDispositions result =
        CommunityAPDispositions.exactly(ImmutableSet.of(1, 3), _bddRoute);
    assertEquals(
        new CommunityAPDispositions(4, ImmutableSet.of(1, 3), ImmutableSet.of(0, 2)), result);
  }

  @Test
  public void testIsExact() {
    assertFalse(_cAPD1.isExact());
    assertTrue(_cAPD2.isExact());
  }
}
