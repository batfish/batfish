package org.batfish.representation.palo_alto;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Map;
import org.junit.Test;

/** Tests of {@link ApplicationGroup}. */
public class ApplicationGroupTest {
  @Test
  public void testGetDescendantObjectsBase() {
    ApplicationGroup ag = new ApplicationGroup("group");
    Map<String, Application> applications =
        ImmutableMap.of(
            "a1", Application.builder("a1").build(), "a2", Application.builder("a2").build());

    // only one of the address objects is a member
    ag.getMembers().add("a1");
    assertThat(
        ag.getDescendantObjects(applications, ImmutableMap.of(), new HashSet<>()),
        equalTo(ImmutableSet.of("a1")));
  }

  @Test
  public void testGetDescendantObjectsCircular() {
    Map<String, ApplicationGroup> ApplicationGroups =
        ImmutableMap.of(
            "parentGroup",
            new ApplicationGroup("parentGroup"),
            "childGroup",
            new ApplicationGroup("childGroup"),
            "grandchildGroup",
            new ApplicationGroup("grandchildGroup"));
    Map<String, Application> Applications =
        ImmutableMap.of(
            "a1", Application.builder("a1").build(), "a2", Application.builder("a2").build());

    // parent -> child -> {parent, grandChild}
    // grandChild -> {child, ad1}
    ApplicationGroups.get("parentGroup").getMembers().add("childGroup");
    ApplicationGroups.get("childGroup").getMembers().add("grandchildGroup");
    ApplicationGroups.get("grandchildGroup")
        .getMembers()
        .addAll(ImmutableSet.of("childGroup", "a1"));

    assertThat(
        ApplicationGroups.get("parentGroup")
            .getDescendantObjects(Applications, ApplicationGroups, new HashSet<>()),
        equalTo(ImmutableSet.of("a1")));
  }

  @Test
  public void testGetDescendantObjectsEmpty() {
    ApplicationGroup ag = new ApplicationGroup("group");
    assertThat(
        ag.getDescendantObjects(ImmutableMap.of(), ImmutableMap.of(), new HashSet<>()),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testGetDescendantObjectsIndirect() {
    Map<String, ApplicationGroup> ApplicationGroups =
        ImmutableMap.of(
            "parentGroup",
            new ApplicationGroup("parentGroup"),
            "childGroup",
            new ApplicationGroup("childGroup"));
    Map<String, Application> Applications =
        ImmutableMap.of(
            "a1", Application.builder("a1").build(), "a2", Application.builder("a2").build());

    ApplicationGroups.get("parentGroup").getMembers().add("childGroup");
    ApplicationGroups.get("childGroup").getMembers().add("a1");

    assertThat(
        ApplicationGroups.get("parentGroup")
            .getDescendantObjects(Applications, ApplicationGroups, new HashSet<>()),
        equalTo(ImmutableSet.of("a1")));
  }
}
