package org.batfish.representation.juniper;

import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.juniper.Nat.Type;
import org.junit.jupiter.api.Test;

class NatRuleThenPrefixTest {

  @Test
  void testEquals() {
    Prefix prefix1 = Prefix.parse("1.1.1.1/24");
    Prefix prefix2 = Prefix.parse("2.1.1.1/24");
    NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1, DESTINATION);

    new EqualsTester()
        .addEqualityGroup(thenPrefix1, new NatRuleThenPrefix(prefix1, DESTINATION))
        .addEqualityGroup(new NatRuleThenPrefix(prefix2, DESTINATION))
        .addEqualityGroup(new NatRuleThenPrefix(prefix1, SOURCE))
        .testEquals();
  }

  @Test
  void testToTransformationSteps() {
    Prefix prefix1 = Prefix.parse("1.1.1.1/24");
    NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1, DESTINATION);
    List<TransformationStep> steps =
        thenPrefix1.toTransformationSteps(new Nat(Type.STATIC), null, null, null);
    // should only change dst ip
    assertThat(
        steps,
        contains(
            new ShiftIpAddressIntoSubnet(TransformationType.STATIC_NAT, DESTINATION, prefix1)));

    thenPrefix1 = new NatRuleThenPrefix(prefix1, SOURCE);
    steps = thenPrefix1.toTransformationSteps(new Nat(Type.STATIC), null, null, null);
    // should only change src ip
    assertThat(
        steps,
        contains(new ShiftIpAddressIntoSubnet(TransformationType.STATIC_NAT, SOURCE, prefix1)));
  }

  @Test
  void testToTransformationStepsNotStaticNat() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Prefix prefix1 = Prefix.parse("1.1.1.1/24");
          NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1, DESTINATION);
          thenPrefix1.toTransformationSteps(new Nat(Type.SOURCE), null, null, null);
        });
  }

  @Test
  void testToTransformationStepsNotStaticNat2() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Prefix prefix1 = Prefix.parse("1.1.1.1/24");
          NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1, DESTINATION);
          thenPrefix1.toTransformationSteps(new Nat(Type.DESTINATION), null, null, null);
        });
  }
}
