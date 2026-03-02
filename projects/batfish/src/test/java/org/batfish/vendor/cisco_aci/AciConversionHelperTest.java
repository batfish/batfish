package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.junit.Test;

/**
 * Tests for {@link AciConversion} helper methods.
 *
 * <p>This test class verifies the behavior of utility methods marked with
 * {@code @VisibleForTesting} in the AciConversion class.
 */
public class AciConversionHelperTest {

  /** Test getContractAclName with various contract names. */
  @Test
  public void testGetContractAclName() {
    assertThat(AciConversion.getContractAclName("web_contract"), equalTo("~CONTRACT~web_contract"));
    assertThat(AciConversion.getContractAclName("allow_http"), equalTo("~CONTRACT~allow_http"));
    assertThat(
        AciConversion.getContractAclName("tenant1:contract1"),
        equalTo("~CONTRACT~tenant1:contract1"));
    assertThat(AciConversion.getContractAclName("default"), equalTo("~CONTRACT~default"));
    assertThat(AciConversion.getContractAclName(""), equalTo("~CONTRACT~"));
  }

  /** Test toInterfaceAddress with IPv4 addresses. */
  @Test
  public void testToInterfaceAddress_ipv4() {
    Ip ip = Ip.parse("10.1.1.1");

    ConcreteInterfaceAddress addr = AciConversion.toInterfaceAddress(ip, 24);
    assertThat(addr.getIp(), equalTo(ip));
    assertThat(addr.getNetworkBits(), equalTo(24));
    assertThat(addr.toString(), equalTo("10.1.1.1/24"));
  }

  /** Note: IPv6 tests removed as Ip.parse() doesn't support IPv6 directly */

  /** Test toInterfaceAddress with maximum prefix length. */
  @Test
  public void testToInterfaceAddress_maxPrefixLength() {
    Ip ip = Ip.parse("192.168.1.100");

    ConcreteInterfaceAddress addr = AciConversion.toInterfaceAddress(ip, 32);
    assertThat(addr.getIp(), equalTo(ip));
    assertThat(addr.getNetworkBits(), equalTo(32));
  }

  /** Note: Default route test removed - /0 prefix not supported by ConcreteInterfaceAddress */

  /** Test toIpWildcard with standard IP and wildcard mask. */
  @Test
  public void testToIpWildcard_standard() {
    Ip prefix = Ip.parse("192.168.1.0");
    Ip wildcard = Ip.parse("0.0.0.255");

    IpWildcard ipWildcard = AciConversion.toIpWildcard(prefix, wildcard);
    assertThat(ipWildcard.getIp(), equalTo(prefix));
    assertThat(ipWildcard.getWildcardMaskAsIp(), equalTo(wildcard));
  }

  /** Test toIpWildcard with /32 (single host). */
  @Test
  public void testToIpWildcard_singleHost() {
    Ip prefix = Ip.parse("10.1.1.100");
    Ip wildcard = Ip.parse("0.0.0.0");

    IpWildcard ipWildcard = AciConversion.toIpWildcard(prefix, wildcard);
    assertThat(ipWildcard.getIp(), equalTo(prefix));
    assertThat(ipWildcard.getWildcardMaskAsIp(), equalTo(wildcard));
  }

  /** Test toIpWildcard with /24 (subnet). */
  @Test
  public void testToIpWildcard_subnet() {
    Ip prefix = Ip.parse("172.16.0.0");
    Ip wildcard = Ip.parse("0.0.255.255");

    IpWildcard ipWildcard = AciConversion.toIpWildcard(prefix, wildcard);
    assertThat(ipWildcard.getIp(), equalTo(prefix));
    assertThat(ipWildcard.getWildcardMaskAsIp(), equalTo(wildcard));
  }

  /** Note: IPv6 wildcard test removed - Ip.parse() doesn't support IPv6 directly */

  /** Test toInterfaceAddress with loopback address. */
  @Test
  public void testToInterfaceAddress_loopback() {
    Ip ip = Ip.parse("127.0.0.1");

    ConcreteInterfaceAddress addr = AciConversion.toInterfaceAddress(ip, 8);
    assertThat(addr.getIp(), equalTo(ip));
    assertThat(addr.getNetworkBits(), equalTo(8));
  }

  /** Test toIpWildcard with any/any (0.0.0.0/0). */
  @Test
  public void testToIpWildcard_anyAny() {
    Ip prefix = Ip.parse("0.0.0.0");
    Ip wildcard = Ip.parse("255.255.255.255");

    IpWildcard ipWildcard = AciConversion.toIpWildcard(prefix, wildcard);
    assertThat(ipWildcard.getIp(), equalTo(prefix));
    assertThat(ipWildcard.getWildcardMaskAsIp(), equalTo(wildcard));
  }

  /** Test getContractAclName with special characters. */
  @Test
  public void testGetContractAclName_specialCharacters() {
    assertThat(
        AciConversion.getContractAclName("web-db_contract"), equalTo("~CONTRACT~web-db_contract"));
    assertThat(
        AciConversion.getContractAclName("tenant.app.contract"),
        equalTo("~CONTRACT~tenant.app.contract"));
    assertThat(
        AciConversion.getContractAclName("contract_with_underscore"),
        equalTo("~CONTRACT~contract_with_underscore"));
  }

  /** Note: Link-local IPv6 test removed - Ip.parse() doesn't support IPv6 directly */

  /** Test toIpWildcard with all zeros wildcard (exact match). */
  @Test
  public void testToIpWildcard_exactMatch() {
    Ip prefix = Ip.parse("10.0.0.1");
    Ip wildcard = Ip.parse("0.0.0.0");

    IpWildcard ipWildcard = AciConversion.toIpWildcard(prefix, wildcard);
    assertThat(ipWildcard.getIp(), equalTo(prefix));
    assertThat(ipWildcard.getWildcardMaskAsIp(), equalTo(wildcard));
  }
}
