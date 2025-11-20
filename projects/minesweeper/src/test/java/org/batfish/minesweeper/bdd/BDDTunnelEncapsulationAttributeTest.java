package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value;
import org.junit.Test;

public class BDDTunnelEncapsulationAttributeTest {
  @Test
  public void testEquals() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    TunnelEncapsulationAttribute a = new TunnelEncapsulationAttribute(Ip.create(1));
    TunnelEncapsulationAttribute b = new TunnelEncapsulationAttribute(Ip.create(2));

    BDDTunnelEncapsulationAttribute base =
        BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a));
    new EqualsTester()
        .addEqualityGroup(
            base,
            BDDTunnelEncapsulationAttribute.copyOf(base),
            BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a)),
            // Document that values are not considered.
            BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(b)),
            // Document that no values are *not* considered: different size but same numBits.
            BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a, b)))
        .addEqualityGroup(BDDTunnelEncapsulationAttribute.create(factory, 1, ImmutableList.of(a)))
        .addEqualityGroup(base.and(factory.ithVar(4)))
        .testEquals();
  }

  /**
   * Test an empty {@link BDDTunnelEncapsulationAttribute}. This is interesting as there are still
   * two domain values: not present, or present with unknown value.
   */
  @Test
  public void testEmpty() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    BDDTunnelEncapsulationAttribute empty =
        BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of());

    assertThat(empty.getNumBits(), equalTo(1));
    assertThat(empty.getIsValidConstraint(), isOne());
    assertThat(empty.satAssignmentToValue(factory.one()), equalTo(Value.absent()));
    assertThat(
        empty.satAssignmentToValue(empty.value(Value.absent()).not()), equalTo(Value.other()));
  }

  /** Test basic functionality when there is a single value. */
  @Test
  public void testSingleton() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    TunnelEncapsulationAttribute a = new TunnelEncapsulationAttribute(Ip.create(1));
    BDDTunnelEncapsulationAttribute singleton =
        BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a));

    assertThat(singleton.getNumBits(), equalTo(2));
    assertThat(
        singleton.getIsValidConstraint(),
        equalTo(
            singleton
                .value(Value.absent())
                .or(singleton.value(Value.literal(a)))
                .or(singleton.value(Value.other()))));
    // ABSENT preferred over VALUE over UNKNOWN
    assertThat(singleton.satAssignmentToValue(factory.one()), equalTo(Value.absent()));
    assertThat(
        singleton.satAssignmentToValue(singleton.value(Value.absent()).not().satOne()),
        equalTo(Value.literal(a)));
    assertThat(
        singleton.satAssignmentToValue(
            singleton
                .value(Value.absent())
                .not()
                .and(singleton.value(Value.literal(a)).not())
                .satOne()),
        equalTo(Value.other()));
  }

  /** Test basic functionality. */
  @Test
  public void testSimple() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    TunnelEncapsulationAttribute a = new TunnelEncapsulationAttribute(Ip.create(1));
    TunnelEncapsulationAttribute b = new TunnelEncapsulationAttribute(Ip.create(2));
    BDDTunnelEncapsulationAttribute simple =
        BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a, b));

    assertThat(simple.getNumBits(), equalTo(2));
    assertThat(simple.getIsValidConstraint(), isOne());
    assertThat(simple.satAssignmentToValue(factory.one()), equalTo(Value.absent()));
    assertThat(
        simple.satAssignmentToValue(simple.value(Value.absent()).not().satOne()),
        equalTo(Value.literal(a)));
    assertThat(
        simple.satAssignmentToValue(
            simple.value(Value.absent()).not().and(simple.value(Value.literal(a)).not()).satOne()),
        equalTo(Value.literal(b)));
  }

  @Test
  public void testAllDifferences() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    TunnelEncapsulationAttribute a = new TunnelEncapsulationAttribute(Ip.create(1));
    BDDTunnelEncapsulationAttribute base =
        BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a));

    BDDTunnelEncapsulationAttribute alwaysA = BDDTunnelEncapsulationAttribute.copyOf(base);
    alwaysA.setValue(Value.literal(a));
    BDDTunnelEncapsulationAttribute aConditional = alwaysA.and(factory.ithVar(4));

    assertThat(aConditional.allDifferences(alwaysA), equalTo(factory.nithVar(4)));
    assertThat(alwaysA.allDifferences(aConditional), equalTo(factory.nithVar(4)));
  }

  @Test
  public void testSupport() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);

    TunnelEncapsulationAttribute a = new TunnelEncapsulationAttribute(Ip.create(1));
    BDDTunnelEncapsulationAttribute base =
        BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a));

    BDDTunnelEncapsulationAttribute alwaysA = BDDTunnelEncapsulationAttribute.copyOf(base);
    alwaysA.setValue(Value.literal(a));
    BDDTunnelEncapsulationAttribute aConditional = alwaysA.and(factory.ithVar(4));

    assertThat(alwaysA.support(), equalTo(factory.one()));
    assertThat(aConditional.support(), equalTo(factory.ithVar(4)));
  }

  @Test
  public void testAugmentPairing() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(10);
    TunnelEncapsulationAttribute a = new TunnelEncapsulationAttribute(Ip.create(1));
    TunnelEncapsulationAttribute b = new TunnelEncapsulationAttribute(Ip.create(2));

    // create two symbolic tunnel encapsulation attributes, each of which has four possible values
    // (the attributes a and b above, plus two defaults that are always included).
    // they are created respectively at index 0 and 4, so they use different underlying BDD
    // variables.
    // because each has four possible values, each uses (log 4) = 2 BDD variables
    BDDTunnelEncapsulationAttribute x =
        BDDTunnelEncapsulationAttribute.create(factory, 0, ImmutableList.of(a, b));
    BDDTunnelEncapsulationAttribute y =
        BDDTunnelEncapsulationAttribute.create(factory, 4, ImmutableList.of(a, b));

    // create a pairing from x to y
    BDDPairing pairing1 = factory.makePair();
    y.augmentPairing(x, pairing1);

    // create a pairing from BDD vars 0 to 4 and 1 to 5
    BDDPairing pairing2 = factory.makePair();
    pairing2.set(new int[] {0, 1}, new int[] {4, 5});

    // check that these two pairings are equivalent by comparing the results of applying them to
    // (var0 and var1) (which is the support of x)
    assertThat(x.support().veccompose(pairing1), equalTo(x.support().veccompose(pairing2)));
  }
}
