package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.STATIC_NAT;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.representation.juniper.Nat.Type.STATIC;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.juniper.Nat.Type;
import org.junit.Test;

public class NatRuleThenPrefixNameTest {

  @Test
  public void testEquals() {
    String prefixName1 = "prefix1";
    String prefixName2 = "prefix2";
    NatRuleThenPrefixName thenPrefixName1 = new NatRuleThenPrefixName(prefixName1, DESTINATION);
    NatRuleThenPrefixName thenPrefixName2 = new NatRuleThenPrefixName(prefixName2, DESTINATION);

    new EqualsTester()
        .addEqualityGroup(thenPrefixName1, new NatRuleThenPrefixName(prefixName1, DESTINATION))
        .addEqualityGroup(thenPrefixName2)
        .addEqualityGroup(new NatRuleThenPrefixName(prefixName1, SOURCE))
        .testEquals();
  }

  @Test
  public void testToTransformationSteps() {
    String prefixName = "entry";
    Prefix prefix = Prefix.parse("1.1.1.1/24");
    Map<String, AddressBookEntry> entryMap =
        ImmutableMap.of("entry", new AddressAddressBookEntry("entry", IpWildcard.create(prefix)));

    NatRuleThenPrefixName then = new NatRuleThenPrefixName(prefixName, DESTINATION);
    List<TransformationStep> steps =
        then.toTransformationSteps(new Nat(STATIC), entryMap, null, null);
    // should only change dst ip
    assertThat(steps, contains(new ShiftIpAddressIntoSubnet(STATIC_NAT, DESTINATION, prefix)));

    then = new NatRuleThenPrefixName(prefixName, SOURCE);
    steps = then.toTransformationSteps(new Nat(STATIC), entryMap, null, null);
    // should only change dst ip
    assertThat(steps, contains(new ShiftIpAddressIntoSubnet(STATIC_NAT, SOURCE, prefix)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToTransformationStepsNotStaticNat() {
    NatRuleThenPrefixName thenPrefix1 = new NatRuleThenPrefixName("prefix", DESTINATION);
    thenPrefix1.toTransformationSteps(new Nat(Type.SOURCE), null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToTransformationStepsNotStaticNat2() {
    NatRuleThenPrefixName thenPrefix1 = new NatRuleThenPrefixName("prefix", DESTINATION);
    thenPrefix1.toTransformationSteps(new Nat(Type.DESTINATION), null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToTransformationStepsNullAddressMap() {
    String prefixName = "prefix";
    NatRuleThenPrefixName then = new NatRuleThenPrefixName(prefixName, DESTINATION);
    then.toTransformationSteps(new Nat(STATIC), null, null, null);
  }

  @Test(expected = BatfishException.class)
  public void testToTransformationStepsAddressEntryTypeNotMatch() {
    String prefixName = "prefix";
    Map<String, AddressBookEntry> entryMap =
        ImmutableMap.of(prefixName, new AddressSetAddressBookEntry("name"));

    NatRuleThenPrefixName then = new NatRuleThenPrefixName(prefixName, DESTINATION);
    then.toTransformationSteps(new Nat(STATIC), entryMap, null, null);
  }
}
