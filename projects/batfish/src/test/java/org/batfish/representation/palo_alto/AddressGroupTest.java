package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.AddressGroup.getFilterConjuncts;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
  public void testGetFilterConjuncts() {
    String filter = "'tag1' and 'tag2'";
    assertThat(getFilterConjuncts(filter), contains("tag1", "tag2"));
  }
}
