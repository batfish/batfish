package org.batfish.representation.palo_alto;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/** Tests for {@link AddressGroup} */
public class AddressGroupTest {

  @Test
  public void testGetDescendantObjectsBase() {
    AddressGroup ag = new AddressGroup("group");
    Map<String, AddressObject> addressObjects =
        ImmutableMap.of("ad1", new AddressObject("ad1"), "ad2", new AddressObject("ad2"));

    // only one of the address objects is a member
    ag.getMembers().add("ad1");
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
    addressGroups.get("parentGroup").getMembers().add("childGroup");
    addressGroups.get("childGroup").getMembers().add("grandchildGroup");
    addressGroups.get("grandchildGroup").getMembers().addAll(ImmutableSet.of("childGroup", "ad1"));

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

    addressGroups.get("parentGroup").getMembers().add("childGroup");
    addressGroups.get("childGroup").getMembers().add("ad1");

    assertThat(
        addressGroups
            .get("parentGroup")
            .getDescendantObjects(addressObjects, addressGroups, new HashSet<>()),
        equalTo(ImmutableSet.of("ad1")));
  }

  @Test
  public void testJavaSerialization() throws IOException {
    AddressGroup group = new AddressGroup("group");
    assertThat(SerializationUtils.clone(group), CoreMatchers.equalTo(group));
  }
}
