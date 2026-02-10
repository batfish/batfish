package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.AddressGroup.getFilterConjuncts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link AddressGroup} */
public class AddressGroupTest {

  @Test
  public void testGetDescendantObjectsBase() {
    AddressGroup ag = new AddressGroup("group");
    Map<String, AddressObject> addressObjects =
        ImmutableMap.of("ad1", new AddressObject("ad1"), "ad2", new AddressObject("ad2"));

    // only one of the address objects is a member
    ag.addMember("ad1");
    assertThat(
        ag.getDescendantObjects(addressObjects, ImmutableMap.of(), new HashSet<>()),
        equalTo(ImmutableSet.of("ad1")));
  }

  @Test
  public void testGetDescendantObjectsCircular() {
    Map<String, AddressGroup> addressGroups =
        ImmutableMap.of(
            "parentGroup",
            new AddressGroup("parentGroup"),
            "childGroup",
            new AddressGroup("childGroup"),
            "grandchildGroup",
            new AddressGroup("grandchildGroup"));
    Map<String, AddressObject> addressObjects =
        ImmutableMap.of("ad1", new AddressObject("ad1"), "ad2", new AddressObject("ad2"));

    // parent -> child -> {parent, grandChild}
    // grandChild -> {child, ad1}
    addressGroups.get("parentGroup").addMember("childGroup");
    addressGroups.get("childGroup").addMember("grandchildGroup");
    addressGroups.get("grandchildGroup").addMember("childGroup");
    addressGroups.get("grandchildGroup").addMember("ad1");

    assertThat(
        addressGroups
            .get("parentGroup")
            .getDescendantObjects(addressObjects, addressGroups, new HashSet<>()),
        equalTo(ImmutableSet.of("ad1")));
  }

  @Test
  public void testGetDescendantObjectsEmpty() {
    AddressGroup ag = new AddressGroup("group");
    assertThat(
        ag.getDescendantObjects(ImmutableMap.of(), ImmutableMap.of(), new HashSet<>()),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testGetDescendantObjectsIndirect() {
    Map<String, AddressGroup> addressGroups =
        ImmutableMap.of(
            "parentGroup",
            new AddressGroup("parentGroup"),
            "childGroup",
            new AddressGroup("childGroup"));
    Map<String, AddressObject> addressObjects =
        ImmutableMap.of("ad1", new AddressObject("ad1"), "ad2", new AddressObject("ad2"));

    addressGroups.get("parentGroup").addMember("childGroup");
    addressGroups.get("childGroup").addMember("ad1");

    assertThat(
        addressGroups
            .get("parentGroup")
            .getDescendantObjects(addressObjects, addressGroups, new HashSet<>()),
        equalTo(ImmutableSet.of("ad1")));
  }

  @Test
  public void testGetDynamicDescendantObjectsConjunction() {
    String filter = "'tag1' and 'tag2'";
    AddressGroup ag = new AddressGroup("name");
    ag.setFilter(filter);

    AddressGroup agMatch = new AddressGroup("agMatch");
    agMatch.getTags().add("tag1");
    agMatch.getTags().add("tag2");
    AddressObject aoMatch = new AddressObject("aoMatch");

    AddressGroup agOther = new AddressGroup("agOther");
    agOther.getTags().add("tag1");
    AddressObject aoOther = new AddressObject("aoOther");
    aoOther.getTags().add("tag1");

    AddressObject aoMatchDirect = new AddressObject("aoMatchDirect");
    aoMatchDirect.getTags().add("tag1");
    aoMatchDirect.getTags().add("tag2");

    Map<String, AddressGroup> ags =
        ImmutableMap.of(
            "agMatch", agMatch,
            "agOther", agOther);
    Map<String, AddressObject> aos =
        ImmutableMap.of(
            "aoMatch", aoMatch,
            "aoMatchDirect", aoMatchDirect,
            "aoOther", aoOther);
    agMatch.addMember("aoMatch");
    agOther.addMember("aoOther");

    // Tag conjunction should evaluate to IpSpace containing the addresses with all tags
    Set<String> matches = ag.getDescendantObjects(aos, ags, new HashSet<>());
    assertThat(matches, contains("aoMatch", "aoMatchDirect"));
  }

  @Test
  public void testGetFilterConjunctsNoQuotes() {
    String filter = "tag";
    assertThat(getFilterConjuncts(filter), contains("tag"));
  }

  @Test
  public void testGetFilterConjunctsShortString() {
    String filter = "'";
    assertThat(getFilterConjuncts(filter), contains("'"));
  }

  @Test
  public void testGetFilterConjuncts() {
    String filter = "'tag1' and 'tag2'";
    assertThat(getFilterConjuncts(filter), contains("tag1", "tag2"));
  }

  @Test
  public void testGetIpRangeSet() {
    // For an empty address group
    AddressGroup group = new AddressGroup("group");
    Map<String, AddressGroup> groupMap = ImmutableMap.of(group.getName(), group);
    assertThat(group.getIpRangeSet(ImmutableMap.of(), groupMap), equalTo(ImmutableRangeSet.of()));

    // For an address group containing an undefined address object
    String addrObjName = "addrObj";
    group.addMember(addrObjName);
    assertThat(group.getIpRangeSet(ImmutableMap.of(), groupMap), equalTo(ImmutableRangeSet.of()));

    // For an address group containing an empty address object
    AddressObject addressObj = new AddressObject(addrObjName);
    Map<String, AddressObject> addrObjects = ImmutableMap.of(addressObj.getName(), addressObj);
    assertThat(group.getIpRangeSet(addrObjects, groupMap), equalTo(ImmutableRangeSet.of()));

    // For address object containing an IP
    addressObj.setIp(Ip.ZERO);
    assertThat(
        group.getIpRangeSet(addrObjects, groupMap),
        equalTo(ImmutableRangeSet.of(Range.singleton(Ip.ZERO))));

    // For address object containing a prefix
    addressObj.setPrefix(IpPrefix.ZERO);
    assertThat(
        group.getIpRangeSet(addrObjects, groupMap),
        equalTo(ImmutableRangeSet.of(Range.closed(Ip.ZERO, Ip.MAX))));

    // For address object containing an IP range
    Range<Ip> range = Range.closed(Ip.ZERO, Ip.parse("1.1.1.1"));
    addressObj.setIpRange(range);
    assertThat(group.getIpRangeSet(addrObjects, groupMap), equalTo(ImmutableRangeSet.of(range)));

    // For two address objects with overlapping IP ranges
    Range<Ip> range2 = Range.closed(Ip.parse("1.0.0.0"), Ip.parse("2.0.0.0"));
    AddressObject addressObj2 = new AddressObject("addressObj2");
    addressObj2.setIpRange(range2);
    group.addMember(addressObj2.getName());
    Map<String, AddressObject> bothObjectsMap =
        ImmutableMap.of(addressObj.getName(), addressObj, addressObj2.getName(), addressObj2);
    assertThat(
        group.getIpRangeSet(bothObjectsMap, groupMap),
        equalTo(ImmutableRangeSet.of(Range.closed(Ip.ZERO, Ip.parse("2.0.0.0")))));
  }
}
