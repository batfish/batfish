package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.BDDPacket;
import org.junit.Test;

/** Tests for {@link BDDLongAtom}. */
public class BDDLongAtomTest {

  private static BDDFactory factory(int numVars) {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(numVars);
    return factory;
  }

  /** An atom at index 0 of a fresh 5-variable factory, tracking the given known values. */
  private static BDDLongAtom atom(Long... knownValues) {
    return BDDLongAtom.create(factory(5), 0, ImmutableList.copyOf(knownValues));
  }

  /** The BDD for the OTHER value: neither 0 nor any of the known values. */
  private static BDD other(BDDLongAtom atom, List<Long> knownValues) {
    BDD result = atom.value(0).not().and(atom.getIsValidConstraint());
    for (long value : knownValues) {
      result = result.diff(atom.value(value));
    }
    return result;
  }

  @Test
  public void testEquals() {
    BDDFactory factory = factory(5);
    BDDLongAtom base = BDDLongAtom.create(factory, 0, ImmutableList.of(5L));
    new EqualsTester()
        .addEqualityGroup(
            base,
            BDDLongAtom.copyOf(base),
            BDDLongAtom.create(factory, 0, ImmutableList.of(5L)),
            // Document that values are not considered.
            BDDLongAtom.create(factory, 0, ImmutableList.of(6L)),
            // Document that the number of values is not considered: same numBits.
            BDDLongAtom.create(factory, 0, ImmutableList.of(5L, 6L)))
        .addEqualityGroup(BDDLongAtom.create(factory, 1, ImmutableList.of(5L)))
        .addEqualityGroup(base.and(factory.ithVar(4)))
        .testEquals();
  }

  /** With no known values there are still two domain values: unset (0) and OTHER. */
  @Test
  public void testEmpty() {
    BDDLongAtom empty = atom();

    assertThat(empty.getNumBits(), equalTo(1));
    assertThat(empty.getIsValidConstraint(), isOne());
    assertThat(empty.satAssignmentToLong(empty.value(0)), equalTo(0L));
    assertThat(empty.satAssignmentToLong(empty.value(0).not()), equalTo(1L));
  }

  @Test
  public void testMinimumIsZero() {
    BDDLongAtom atom = atom(5L, 7L);

    assertThat(atom.satAssignmentToLong(atom.getIsValidConstraint().satOne()), equalTo(0L));
  }

  @Test
  public void testKnownValues() {
    BDDLongAtom atom = atom(5L, 7L);

    assertThat(atom.satAssignmentToLong(atom.value(5).satOne()), equalTo(5L));
    assertThat(atom.satAssignmentToLong(atom.value(7).satOne()), equalTo(7L));
  }

  @Test
  public void testOtherIsSmallestUnknownPositiveValue() {
    List<Long> known = ImmutableList.of(1L, 2L, 4L);
    BDDLongAtom atom = atom(1L, 2L, 4L);

    assertThat(atom.satAssignmentToLong(other(atom, known).satOne()), equalTo(3L));
  }

  @Test
  public void testOtherIsOneWhenOneIsNotKnown() {
    List<Long> known = ImmutableList.of(5L);
    BDDLongAtom atom = atom(5L);

    assertThat(atom.satAssignmentToLong(other(atom, known).satOne()), equalTo(1L));
  }

  @Test
  public void testValidConstraintExcludesUnusedBitPatterns() {
    // {0, 5, OTHER} in two bits leaves one bit pattern unused.
    BDDLongAtom atom = atom(5L);

    assertThat(atom.getNumBits(), equalTo(2));
    assertThat(
        atom.getIsValidConstraint(),
        equalTo(atom.value(0).or(atom.value(5)).or(other(atom, ImmutableList.of(5L)))));
    assertThat(atom.getIsValidConstraint().isOne(), equalTo(false));
  }

  @Test
  public void testNumBitsFor() {
    assertThat(BDDLongAtom.numBitsFor(ImmutableList.of()), equalTo(1));
    assertThat(BDDLongAtom.numBitsFor(ImmutableList.of(1L, 2L)), equalTo(2));
    assertThat(BDDLongAtom.numBitsFor(ImmutableList.of(1L, 2L, 3L)), equalTo(3));
    assertThat(atom(1L, 2L, 3L).getNumBits(), equalTo(3));
  }

  @Test
  public void testSetValue() {
    BDDLongAtom atom = atom(5L);
    atom.setValue(5);

    assertThat(atom.value(5), isOne());
  }

  @Test
  public void testSetUnknownValueRejected() {
    BDDLongAtom atom = atom(5L);

    assertThrows(IllegalArgumentException.class, () -> atom.setValue(6));
  }

  @Test
  public void testValueOfUnknownValueRejected() {
    BDDLongAtom atom = atom(5L);

    assertThrows(IllegalArgumentException.class, () -> atom.value(6));
  }

  @Test
  public void testNegativeValueRejected() {
    BDDLongAtom atom = atom(5L);

    assertThrows(IllegalArgumentException.class, () -> atom.value(-1));
  }

  @Test
  public void testCreateRejectsZero() {
    assertThrows(IllegalArgumentException.class, () -> atom(0L, 5L));
  }

  @Test
  public void testCreateRejectsUnsortedValues() {
    assertThrows(IllegalArgumentException.class, () -> atom(7L, 5L));
  }

  @Test
  public void testCreateRejectsDuplicateValues() {
    assertThrows(IllegalArgumentException.class, () -> atom(5L, 5L));
  }

  @Test
  public void testCopyIsIndependent() {
    BDDLongAtom atom = atom(5L);
    BDDLongAtom copy = BDDLongAtom.copyOf(atom);
    copy.setValue(5);

    assertThat(atom.value(5).isOne(), equalTo(false));
  }

  @Test
  public void testAllDifferences() {
    BDDFactory factory = factory(5);
    BDDLongAtom alwaysFive = BDDLongAtom.create(factory, 0, ImmutableList.of(5L));
    alwaysFive.setValue(5);
    BDDLongAtom fiveConditional = alwaysFive.and(factory.ithVar(4));

    assertThat(fiveConditional.allDifferences(alwaysFive), equalTo(factory.nithVar(4)));
    assertThat(alwaysFive.allDifferences(fiveConditional), equalTo(factory.nithVar(4)));
  }

  @Test
  public void testSupport() {
    BDDFactory factory = factory(5);
    BDDLongAtom alwaysFive = BDDLongAtom.create(factory, 0, ImmutableList.of(5L));
    alwaysFive.setValue(5);
    BDDLongAtom fiveConditional = alwaysFive.and(factory.ithVar(4));

    assertThat(alwaysFive.support(), equalTo(factory.one()));
    assertThat(fiveConditional.support(), equalTo(factory.ithVar(4)));
  }

  @Test
  public void testAugmentPairing() {
    BDDFactory factory = factory(10);
    // each has four values {0, 5, 7, OTHER}, so uses 2 BDD variables: 0-1 and 4-5 respectively
    BDDLongAtom x = BDDLongAtom.create(factory, 0, ImmutableList.of(5L, 7L));
    BDDLongAtom y = BDDLongAtom.create(factory, 4, ImmutableList.of(5L, 7L));

    BDDPairing pairing1 = factory.makePair();
    y.augmentPairing(x, pairing1);
    BDDPairing pairing2 = factory.makePair();
    pairing2.set(new int[] {0, 1}, new int[] {4, 5});

    assertThat(x.support().veccompose(pairing1), equalTo(x.support().veccompose(pairing2)));
  }

  @Test
  public void testVeccompose() {
    BDDFactory factory = factory(10);
    BDDLongAtom x = BDDLongAtom.create(factory, 0, ImmutableList.of(5L, 7L));
    BDDLongAtom y = BDDLongAtom.create(factory, 4, ImmutableList.of(5L, 7L));
    BDDPairing xToY = factory.makePair();
    y.augmentPairing(x, xToY);

    assertThat(x.veccompose(xToY), equalTo(y));
  }
}
