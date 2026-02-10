package org.batfish.representation.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.EmptyIp6Space;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ip6Space;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.representation.palo_alto.AddressObject.Type;
import org.junit.Test;

/** Tests of {@link AddressObject} */
public class AddressObjectTest {

  @Test
  public void testGetConcreteInterfaceAddress() {
    AddressObject a = new AddressObject("name");
    Warnings w = new Warnings();

    a.setPrefix(IpPrefix.parse("1.2.3.4/24"));
    assertThat(
        a.toConcreteInterfaceAddress(w), equalTo(ConcreteInterfaceAddress.parse("1.2.3.4/24")));

    // Implied /32
    a.setIp(Ip.parse("1.2.3.4"));
    assertThat(
        a.toConcreteInterfaceAddress(w), equalTo(ConcreteInterfaceAddress.parse("1.2.3.4/32")));

    // Cannot convert ip range to concrete interface address
    Range<Ip> range = Range.closed(Ip.ZERO, Ip.parse("1.1.1.1"));
    a.setIpRange(range);
    assertThat(a.toConcreteInterfaceAddress(w), nullValue());
  }

  @Test
  public void testSetClearsTypeAndMembers() {
    AddressObject a = new AddressObject("name");
    assertNull(a.getIp());
    assertNull(a.getType());

    a.setIp(Ip.ZERO);
    assertThat(a.getIp(), equalTo(Ip.ZERO));
    assertThat(a.getType(), equalTo(Type.IP));

    // Setting prefix clears members and updates type
    a.setPrefix(IpPrefix.ZERO);
    assertNull(a.getIp());
    assertThat(a.getIpPrefix(), equalTo(IpPrefix.ZERO));
    assertThat(a.getType(), equalTo(Type.PREFIX));

    // Setting range clears members and updates type
    Range<Ip> range = Range.closed(Ip.ZERO, Ip.parse("1.1.1.1"));
    a.setIpRange(range);
    assertNull(a.getIpPrefix());
    assertThat(a.getIpRange(), equalTo(range));
    assertThat(a.getType(), equalTo(Type.IP_RANGE));

    // Setting IP clears members and updates type
    a.setIp(Ip.ZERO);
    assertNull(a.getIpRange());
    assertThat(a.getIp(), equalTo(Ip.ZERO));
    assertThat(a.getType(), equalTo(Type.IP));

    // Setting IP to null clears type and members
    a.setIp((Ip) null);
    assertNull(a.getIp());
    assertNull(a.getType());
  }

  @Test
  public void testGetIpSpacePrefixAndRange() {
    AddressObject a = new AddressObject("name");

    a.setPrefix(IpPrefix.parse("1.2.3.4/24"));
    assertThat(a.getIpSpace(), equalTo(IpPrefix.parse("1.2.3.4/24").getPrefix().toIpSpace()));

    Range<Ip> range = Range.closed(Ip.parse("1.2.3.4"), Ip.parse("1.2.3.10"));
    a.setIpRange(range);
    assertThat(a.getIpSpace(), equalTo(IpRange.range(Ip.parse("1.2.3.4"), Ip.parse("1.2.3.10"))));
  }

  @Test
  public void testGetIpSpaceFqdnReturnsEmpty() {
    AddressObject a = new AddressObject("name");
    a.setFqdn("example.com");

    IpSpace ipSpace = a.getIpSpace();
    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
    assertThat(a.getType(), equalTo(Type.FQDN));
  }

  @Test
  public void testGetIpSpaceIpLocationReturnsEmpty() {
    AddressObject a = new AddressObject("name");
    a.setIpLocation("us-west-1");

    IpSpace ipSpace = a.getIpSpace();
    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
    assertThat(a.getType(), equalTo(Type.IP_LOCATION));
  }

  @Test
  public void testGetIpSpaceNoAddressSetReturnsEmpty() {
    AddressObject a = new AddressObject("name");
    IpSpace ipSpace = a.getIpSpace();
    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testGetIpSpaceWithSingleIp() {
    AddressObject a = new AddressObject("name");
    a.setIp(Ip.parse("192.168.1.1"));

    IpSpace ipSpace = a.getIpSpace();
    assertThat(ipSpace, equalTo(Ip.parse("192.168.1.1").toIpSpace()));
    assertThat(a.getType(), equalTo(Type.IP));
  }

  @Test
  public void testGetIp6SpaceWithIp6() {
    AddressObject a = new AddressObject("name");
    Ip6 ip6 = Ip6.parse("2001:db8::1");
    a.setIp(ip6);

    Ip6Space ip6Space = a.getIp6Space();
    // IPv6 address is converted to /128 prefix
    assertThat(ip6Space.toString(), containsString("/128"));
    assertThat(a.getType(), equalTo(Type.IP));
  }

  @Test
  public void testGetIp6SpaceWithPrefix6() {
    AddressObject a = new AddressObject("name");
    Ip6Prefix prefix6 = new Ip6Prefix(Ip6.parse("2001:db8::1"), 32);
    a.setPrefix(prefix6);

    Ip6Space ip6Space = a.getIp6Space();
    assertThat(ip6Space.toString(), containsString("/32"));
    assertThat(a.getType(), equalTo(Type.PREFIX));
  }

  @Test
  public void testGetIp6SpaceNoAddressSetReturnsEmpty() {
    AddressObject a = new AddressObject("name");
    Ip6Space ip6Space = a.getIp6Space();
    assertThat(ip6Space, equalTo(EmptyIp6Space.INSTANCE));
  }

  @Test
  public void testGetIp6SpaceWithIpv4ReturnsEmpty() {
    AddressObject a = new AddressObject("name");
    a.setIp(Ip.parse("192.168.1.1"));

    Ip6Space ip6Space = a.getIp6Space();
    assertThat(ip6Space, equalTo(EmptyIp6Space.INSTANCE));
  }

  @Test
  public void testGetAddressAsRangeSetWithIp() {
    AddressObject a = new AddressObject("name");
    Ip ip = Ip.parse("10.0.0.1");
    a.setIp(ip);

    RangeSet<Ip> rangeSet = a.getAddressAsRangeSet();
    assertThat(rangeSet, equalTo(ImmutableRangeSet.of(Range.singleton(ip))));
  }

  @Test
  public void testGetAddressAsRangeSetWithPrefix() {
    AddressObject a = new AddressObject("name");
    IpPrefix prefix = IpPrefix.parse("10.0.0.0/24");
    a.setPrefix(prefix);

    RangeSet<Ip> rangeSet = a.getAddressAsRangeSet();
    assertThat(
        rangeSet,
        equalTo(
            ImmutableRangeSet.of(
                Range.closed(prefix.getPrefix().getStartIp(), prefix.getPrefix().getEndIp()))));
  }

  @Test
  public void testGetAddressAsRangeSetWithIpRange() {
    AddressObject a = new AddressObject("name");
    Range<Ip> ipRange = Range.closed(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.100"));
    a.setIpRange(ipRange);

    RangeSet<Ip> rangeSet = a.getAddressAsRangeSet();
    assertThat(rangeSet, equalTo(ImmutableRangeSet.of(ipRange)));
  }

  @Test
  public void testGetAddressAsRangeSetNoAddressReturnsEmpty() {
    AddressObject a = new AddressObject("name");
    RangeSet<Ip> rangeSet = a.getAddressAsRangeSet();
    assertThat(rangeSet, equalTo(ImmutableRangeSet.of()));
  }

  @Test
  public void testSetFqdn() {
    AddressObject a = new AddressObject("name");
    a.setFqdn("example.com");

    assertThat(a.getFqdn(), equalTo("example.com"));
    assertThat(a.getType(), equalTo(Type.FQDN));
    assertNull(a.getIp());
  }

  @Test
  public void testSetFqdnNull() {
    AddressObject a = new AddressObject("name");
    a.setFqdn("example.com");
    assertThat(a.getFqdn(), equalTo("example.com"));

    a.setFqdn(null);
    assertNull(a.getFqdn());
    assertNull(a.getType());
  }

  @Test
  public void testSetIpLocation() {
    AddressObject a = new AddressObject("name");
    a.setIpLocation("us-west-1");

    assertThat(a.getIpLocation(), equalTo("us-west-1"));
    assertThat(a.getType(), equalTo(Type.IP_LOCATION));
    assertNull(a.getIp());
  }

  @Test
  public void testSetIpLocationNull() {
    AddressObject a = new AddressObject("name");
    a.setIpLocation("us-west-1");
    assertThat(a.getIpLocation(), equalTo("us-west-1"));

    a.setIpLocation(null);
    assertNull(a.getIpLocation());
    assertNull(a.getType());
  }

  @Test
  public void testSetIp6() {
    AddressObject a = new AddressObject("name");
    Ip6 ip6 = Ip6.parse("2001:db8::1");
    a.setIp(ip6);

    assertThat(a.getIp6(), equalTo(ip6));
    assertThat(a.getType(), equalTo(Type.IP));
    assertNull(a.getIp());
  }

  @Test
  public void testSetIp6Null() {
    AddressObject a = new AddressObject("name");
    Ip6 ip6 = Ip6.parse("2001:db8::1");
    a.setIp(ip6);

    a.setIp((Ip6) null);
    assertNull(a.getIp6());
    assertNull(a.getType());
  }

  @Test
  public void testSetPrefix6() {
    AddressObject a = new AddressObject("name");
    Ip6Prefix prefix6 = new Ip6Prefix(Ip6.parse("2001:db8::1"), 32);
    a.setPrefix(prefix6);

    assertThat(a.getPrefix6(), equalTo(prefix6));
    assertThat(a.getType(), equalTo(Type.PREFIX));
    assertNull(a.getIpPrefix());
  }

  @Test
  public void testSetPrefix6Null() {
    AddressObject a = new AddressObject("name");
    Ip6Prefix prefix6 = new Ip6Prefix(Ip6.parse("2001:db8::1"), 32);
    a.setPrefix(prefix6);

    a.setPrefix((Ip6Prefix) null);
    assertNull(a.getPrefix6());
    assertNull(a.getType());
  }

  @Test
  public void testSetIpRange6() {
    AddressObject a = new AddressObject("name");
    Range<Ip6> ipRange6 = Range.closed(Ip6.parse("2001:db8::1"), Ip6.parse("2001:db8::100"));
    a.setIpRange6(ipRange6);

    assertThat(a.getIpRange6(), equalTo(ipRange6));
    assertThat(a.getType(), equalTo(Type.IP_RANGE));
    assertNull(a.getIpRange());
  }

  @Test
  public void testSetIpRange6Null() {
    AddressObject a = new AddressObject("name");
    Range<Ip6> ipRange6 = Range.closed(Ip6.parse("2001:db8::1"), Ip6.parse("2001:db8::100"));
    a.setIpRange6(ipRange6);

    a.setIpRange6(null);
    assertNull(a.getIpRange6());
    assertNull(a.getType());
  }

  @Test
  public void testSetDescription() {
    AddressObject a = new AddressObject("name");
    a.setDescription("Test description");

    assertThat(a.getDescription(), equalTo("Test description"));
  }

  @Test
  public void testGetName() {
    AddressObject a = new AddressObject("test-name");
    assertThat(a.getName(), equalTo("test-name"));
  }

  @Test
  public void testGetTagsReturnsEmptySetInitially() {
    AddressObject a = new AddressObject("name");
    assertThat(a.getTags(), empty());
  }

  @Test
  public void testGetTagsIsModifiable() {
    AddressObject a = new AddressObject("name");
    a.getTags().add("tag1");
    a.getTags().add("tag2");

    assertThat(a.getTags(), hasSize(2));
    assertTrue(a.getTags().contains("tag1"));
    assertTrue(a.getTags().contains("tag2"));
  }

  @Test
  public void testTransitionFromFqdnToIp() {
    AddressObject a = new AddressObject("name");
    a.setFqdn("example.com");
    assertThat(a.getType(), equalTo(Type.FQDN));

    a.setIp(Ip.parse("10.0.0.1"));
    assertThat(a.getType(), equalTo(Type.IP));
    assertNull(a.getFqdn());
    assertThat(a.getIp(), equalTo(Ip.parse("10.0.0.1")));
  }

  @Test
  public void testTransitionFromIpLocationToPrefix() {
    AddressObject a = new AddressObject("name");
    a.setIpLocation("us-west-1");
    assertThat(a.getType(), equalTo(Type.IP_LOCATION));

    IpPrefix expectedPrefix = IpPrefix.parse("10.0.0.0/24");
    a.setPrefix(expectedPrefix);
    assertThat(a.getType(), equalTo(Type.PREFIX));
    assertNull(a.getIpLocation());
    assertThat(a.getIpPrefix().getPrefix(), equalTo(expectedPrefix.getPrefix()));
  }

  @Test
  public void testToConcreteInterfaceAddressWithFqdnReturnsNullAndWarns() {
    AddressObject a = new AddressObject("name");
    a.setFqdn("example.com");
    Warnings w = new Warnings(true, true, true);

    ConcreteInterfaceAddress result = a.toConcreteInterfaceAddress(w);

    assertNull(result);
    assertThat(w.getRedFlagWarnings(), hasSize(1));
  }

  @Test
  public void testToConcreteInterfaceAddressWithIpLocationReturnsNullAndWarns() {
    AddressObject a = new AddressObject("name");
    a.setIpLocation("us-west-1");
    Warnings w = new Warnings(true, true, true);

    ConcreteInterfaceAddress result = a.toConcreteInterfaceAddress(w);

    assertNull(result);
    assertThat(w.getRedFlagWarnings(), hasSize(1));
  }

  @Test
  public void testToConcreteInterfaceAddressWithIp6RangeReturnsNullAndWarns() {
    AddressObject a = new AddressObject("name");
    Range<Ip6> ipRange6 = Range.closed(Ip6.parse("2001:db8::1"), Ip6.parse("2001:db8::100"));
    a.setIpRange6(ipRange6);
    Warnings w = new Warnings(true, true, true);

    ConcreteInterfaceAddress result = a.toConcreteInterfaceAddress(w);

    assertNull(result);
    assertThat(w.getRedFlagWarnings(), hasSize(1));
  }

  @Test
  public void testToConcreteInterfaceAddressNoAddressSetReturnsNullAndWarns() {
    AddressObject a = new AddressObject("name");
    Warnings w = new Warnings(true, true, true);

    ConcreteInterfaceAddress result = a.toConcreteInterfaceAddress(w);

    assertNull(result);
    assertThat(w.getRedFlagWarnings(), hasSize(1));
  }
}
