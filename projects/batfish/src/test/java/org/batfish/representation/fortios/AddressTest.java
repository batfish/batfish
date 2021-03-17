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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AddressTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static Address createAddressWithIps(Ip ip1, Ip ip2) {
    Address address = new Address("name", new BatfishUUID(1));
    address.getTypeSpecificFields().setIp1(ip1);
    address.getTypeSpecificFields().setIp2(ip2);
    return address;
  }

  @Test
  public void testToIpSpace_ipmaskDefault() {
    Address address = new Address("name", new BatfishUUID(1));
    assertConvertsWithoutWarnings(address, Prefix.ZERO.toIpSpace());
  }

  @Test
  public void testToIpSpace_ipmask() {
    Ip ip = Ip.parse("1.1.1.1");
    Ip mask = Ip.parse("255.255.255.0");
    Address address = createAddressWithIps(ip, mask);
    assertConvertsWithoutWarnings(address, Prefix.create(ip, mask).toIpSpace());
  }

  @Test
  public void testToIpSpace_ipmaskInvalidMask() {
    // 1.1.1.0 isn't a valid subnet mask
    Address address = createAddressWithIps(Ip.parse("1.1.1.0"), Ip.parse("1.1.1.0"));
    _thrown.expect(IllegalStateException.class);
    address.toIpSpace(new Warnings());
  }

  @Test
  public void testToIpSpace_iprangeDefault() {
    // Must set end IP
    Ip endIp = Ip.parse("1.1.1.255");
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.IPRANGE);
    address.getTypeSpecificFields().setIp2(endIp);
    assertConvertsWithoutWarnings(address, IpRange.range(Ip.ZERO, endIp));
  }

  @Test
  public void testToIpSpace_iprange() {
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.255");
    Address address = createAddressWithIps(startIp, endIp);
    address.setType(Address.Type.IPRANGE);
    assertConvertsWithoutWarnings(address, IpRange.range(startIp, endIp));
  }

  @Test
  public void testToIpSpace_iprangeNoEndIp() {
    // 1.1.1.0 isn't a valid subnet mask
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.IPRANGE);
    _thrown.expect(IllegalStateException.class);
    address.toIpSpace(new Warnings());
  }

  @Test
  public void testToIpSpace_iprangeEndIpTooLow() {
    Address address = createAddressWithIps(Ip.parse("2.2.2.2"), Ip.parse("1.1.1.1"));
    address.setType(Address.Type.IPRANGE);
    _thrown.expect(IllegalArgumentException.class);
    address.toIpSpace(new Warnings());
  }

  @Test
  public void testToIpSpace_wildcardDefault() {
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.WILDCARD);
    assertConvertsWithoutWarnings(address, IpWildcard.ANY.toIpSpace());
  }

  @Test
  public void testToIpSpace_wildcard() {
    Ip mask = Ip.parse("255.0.255.128"); // FortiOS format (bits that are set matter)
    IpWildcard wildcard = IpWildcard.ipWithWildcardMask(Ip.parse("1.1.1.1"), mask.inverted());
    Address address = createAddressWithIps(wildcard.getIp(), mask);
    address.setType(Address.Type.WILDCARD);
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
