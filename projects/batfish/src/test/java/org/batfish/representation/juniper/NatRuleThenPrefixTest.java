package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.juniper.Nat.Type;
import org.junit.Test;

public class NatRuleThenPrefixTest {

  @Test
  public void testEquals() {
    Prefix prefix1 = Prefix.parse("1.1.1.1/24");
    Prefix prefix2 = Prefix.parse("2.1.1.1/24");
    NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1);
    NatRuleThenPrefix thenPrefix2 = new NatRuleThenPrefix(prefix2);

    new EqualsTester()
        .addEqualityGroup(thenPrefix1, new NatRuleThenPrefix(prefix1))
        .addEqualityGroup(thenPrefix2)
        .testEquals();
  }

  @Test
  public void testToTransformationStepsReverse() {
    Prefix prefix1 = Prefix.parse("1.1.1.1/24");
    NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1);
    List<TransformationStep> steps =
        thenPrefix1.toTransformationSteps(null, new Nat(Type.STATIC), null, true, null);
    // should only change src ip
    assertThat(
        steps,
        contains(
            new ShiftIpAddressIntoSubnet(TransformationType.STATIC_NAT, IpField.SOURCE, prefix1)));
  }

  @Test
  public void testToTransformationStepsNoReverse() {
    Prefix prefix1 = Prefix.parse("1.1.1.1/24");
    NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1);
    List<TransformationStep> steps =
        thenPrefix1.toTransformationSteps(null, new Nat(Type.STATIC), null, false, null);
    // should only change dst ip
    assertThat(
        steps,
        contains(
            new ShiftIpAddressIntoSubnet(
                TransformationType.STATIC_NAT, IpField.DESTINATION, prefix1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToTransformationStepsNotStaticNat() {
    Prefix prefix1 = Prefix.parse("1.1.1.1/24");
    NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1);
    thenPrefix1.toTransformationSteps(null, new Nat(Type.SOURCE), null, false, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToTransformationStepsNotStaticNat2() {
    Prefix prefix1 = Prefix.parse("1.1.1.1/24");
    NatRuleThenPrefix thenPrefix1 = new NatRuleThenPrefix(prefix1);
    thenPrefix1.toTransformationSteps(null, new Nat(Type.DESTINATION), null, false, null);
  }
}
