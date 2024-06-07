package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDDFactory;
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
        empty.satAssignmentToValue(empty.value(Value.absent()).not()), equalTo(Value.unknown()));
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
}
