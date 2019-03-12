package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.juniper.Nat.Type;
import org.junit.Test;

public class NatRuleThenPrefixNameTest {

  @Test
  public void testEquals() {
    String prefixName1 = "prefix1";
    String prefixName2 = "prefix2";
    NatRuleThenPrefixName thenPrefixName1 = new NatRuleThenPrefixName(prefixName1);
    NatRuleThenPrefixName thenPrefixName2 = new NatRuleThenPrefixName(prefixName2);

    new EqualsTester()
        .addEqualityGroup(thenPrefixName1, new NatRuleThenPrefixName(prefixName1))
        .addEqualityGroup(thenPrefixName2)
        .testEquals();
  }

  @Test
  public void testToTransformationStepsNoReverse() {
    JuniperConfiguration config = new JuniperConfiguration();
    String prefixName = "prefix";
    Prefix prefix = Prefix.parse("1.1.1.1/24");
    config
        .getMasterLogicalSystem()
        .getAddressBooks()
        .get(LogicalSystem.GLOBAL_ADDRESS_BOOK_NAME)
        .getEntries()
        .put(prefixName, new AddressAddressBookEntry("entry", new IpWildcard(prefix)));
    NatRuleThenPrefixName then = new NatRuleThenPrefixName(prefixName);
    List<TransformationStep> steps =
        then.toTransformationSteps(config, new Nat(Type.STATIC), null, false);
    // should only change dst ip
    assertThat(
        steps,
        contains(
            new ShiftIpAddressIntoSubnet(
                TransformationType.STATIC_NAT, IpField.DESTINATION, prefix)));
  }

  @Test
  public void testToTransformationStepsReverse() {
    JuniperConfiguration config = new JuniperConfiguration();
    String prefixName = "prefix";
    Prefix prefix = Prefix.parse("1.1.1.1/24");
    config
        .getMasterLogicalSystem()
        .getAddressBooks()
        .get(LogicalSystem.GLOBAL_ADDRESS_BOOK_NAME)
        .getEntries()
        .put(prefixName, new AddressAddressBookEntry("entry", new IpWildcard(prefix)));
    NatRuleThenPrefixName then = new NatRuleThenPrefixName(prefixName);
    List<TransformationStep> steps =
        then.toTransformationSteps(config, new Nat(Type.STATIC), null, true);
    // should only change src ip
    assertThat(
        steps,
        contains(
            new ShiftIpAddressIntoSubnet(TransformationType.STATIC_NAT, IpField.SOURCE, prefix)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToTransformationStepsNotStaticNat() {
    NatRuleThenPrefixName thenPrefix1 = new NatRuleThenPrefixName("prefix");
    thenPrefix1.toTransformationSteps(null, new Nat(Type.SOURCE), null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToTransformationStepsNotStaticNat2() {
    NatRuleThenPrefixName thenPrefix1 = new NatRuleThenPrefixName("prefix");
    thenPrefix1.toTransformationSteps(null, new Nat(Type.DESTINATION), null, false);
  }

  @Test(expected = BatfishException.class)
  public void testToTransformationStepsNoAddressEntry() {
    JuniperConfiguration config = new JuniperConfiguration();
    String prefixName = "prefix";

    NatRuleThenPrefixName then = new NatRuleThenPrefixName(prefixName);
    then.toTransformationSteps(config, new Nat(Type.STATIC), null, true);
  }

  @Test(expected = BatfishException.class)
  public void testToTransformationStepsAddressEntryTypeNotMatch() {
    JuniperConfiguration config = new JuniperConfiguration();
    String prefixName = "prefix";
    config
        .getMasterLogicalSystem()
        .getAddressBooks()
        .get(LogicalSystem.GLOBAL_ADDRESS_BOOK_NAME)
        .getEntries()
        .put(prefixName, new AddressSetAddressBookEntry("name"));

    NatRuleThenPrefixName then = new NatRuleThenPrefixName(prefixName);
    then.toTransformationSteps(config, new Nat(Type.STATIC), null, true);
  }
}
