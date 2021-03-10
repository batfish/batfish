package org.batfish.representation.fortios;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.util.stream.Stream;
import org.batfish.common.Warnings;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class AddressTest {
  @Test
  public void testToIpSpace_ipmask() {
    Address address = new Address("name", new BatfishUUID(1));
    Prefix prefix = Prefix.parse("1.1.1.0/24");
    address.getTypeSpecificFields().setSubnet(prefix);
    assertConvertsWithoutWarnings(address, prefix.toIpSpace());
  }

  @Test
  public void testToIpSpace_iprange() {
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.IPRANGE);
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.255");
    address.getTypeSpecificFields().setStartIp(startIp);
    address.getTypeSpecificFields().setEndIp(endIp);
    assertConvertsWithoutWarnings(address, IpRange.range(startIp, endIp));
  }

  @Test
  public void testToIpSpace_wildcard() {
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.WILDCARD);
    IpWildcard wildcard =
        IpWildcard.ipWithWildcardMask(Ip.parse("1.1.1.1"), Ip.parse("255.0.255.128"));
    address.getTypeSpecificFields().setWildcard(wildcard);
    assertConvertsWithoutWarnings(address, wildcard.toIpSpace());
  }

  @Test
  public void testToIpSpace_unsupportedTypes() {
    Stream.of(
            Address.Type.INTERFACE_SUBNET,
            Address.Type.DYNAMIC,
            Address.Type.FQDN,
            Address.Type.GEOGRAPHY,
            Address.Type.MAC)
        .forEach(
            unsupportedType -> {
              Address address = new Address("name", new BatfishUUID(1));
              address.setType(unsupportedType);
              Warnings w = new Warnings(true, true, true);
              assertThat(address.toIpSpace(w), equalTo(EmptyIpSpace.INSTANCE));
              assertThat(
                  w.getRedFlagWarnings(),
                  contains(
                      hasText(
                          String.format(
                              "Addresses of type %s are unsupported and will be considered"
                                  + " unmatchable.",
                              unsupportedType))));
            });
  }

  private static void assertConvertsWithoutWarnings(Address address, IpSpace expected) {
    Warnings w = new Warnings(true, true, true);
    assertThat(address.toIpSpace(w), equalTo(expected));
    assertThat(w.getRedFlagWarnings(), empty());
  }
}
