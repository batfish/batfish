package org.batfish.representation.palo_alto;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.getBuiltInApps;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
    Application a1 = Application.builder("a1").build();
    Application a2 = Application.builder("a2").build();
    Map<String, Application> applications = ImmutableMap.of("a1", a1, "a2", a2);

    // only one of the address objects is a member
    ag.getMembers().add("a1");
    assertThat(
        ag.getDescendantObjects(applications, ImmutableMap.of(), new HashSet<>()),
        equalTo(ImmutableSet.of(a1)));
  }

  @Test
  public void testGetDescendantObjectsBuiltIn() {
    ApplicationGroup ag = new ApplicationGroup("group");
    // Built-in application
    ag.getMembers().add("ftp");

    assertThat(
        ag.getDescendantObjects(ImmutableMap.of(), ImmutableMap.of(), new HashSet<>()),
        equalTo(ImmutableSet.of(getOnlyElement(getBuiltInApps("ftp")))));
  }

  @Test
  public void testGetDescendantObjectsCircular() {
    Application a1 = Application.builder("a1").build();
    Application a2 = Application.builder("a2").build();
    Map<String, ApplicationGroup> applicationGroups =
        ImmutableMap.of(
            "parentGroup",
            new ApplicationGroup("parentGroup"),
            "childGroup",
            new ApplicationGroup("childGroup"),
            "grandchildGroup",
            new ApplicationGroup("grandchildGroup"));
    Map<String, Application> applications = ImmutableMap.of("a1", a1, "a2", a2);

    // parent -> child -> {parent, grandChild}
    // grandChild -> {child, ad1}
    applicationGroups.get("parentGroup").getMembers().add("childGroup");
    applicationGroups.get("childGroup").getMembers().add("grandchildGroup");
    applicationGroups
        .get("grandchildGroup")
        .getMembers()
        .addAll(ImmutableSet.of("childGroup", "a1"));

    assertThat(
        applicationGroups
            .get("parentGroup")
            .getDescendantObjects(applications, applicationGroups, new HashSet<>()),
        equalTo(ImmutableSet.of(a1)));
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
    Application a1 = Application.builder("a1").build();
    Application a2 = Application.builder("a2").build();
    Map<String, ApplicationGroup> applicationGroups =
        ImmutableMap.of(
            "parentGroup",
            new ApplicationGroup("parentGroup"),
            "childGroup",
            new ApplicationGroup("childGroup"));
    Map<String, Application> applications = ImmutableMap.of("a1", a1, "a2", a2);

    applicationGroups.get("parentGroup").getMembers().add("childGroup");
    applicationGroups.get("childGroup").getMembers().add("a1");

    assertThat(
        applicationGroups
            .get("parentGroup")
            .getDescendantObjects(applications, applicationGroups, new HashSet<>()),
        equalTo(ImmutableSet.of(a1)));
  }
}
