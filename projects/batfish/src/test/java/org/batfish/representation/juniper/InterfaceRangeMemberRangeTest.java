package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class InterfaceRangeMemberRangeTest {

  @Test
  void testGetAllMembers() {
    assertThat(
        new InterfaceRangeMemberRange("ge-0/0/0", "ge-0/0/0").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0")));
    assertThat(
        new InterfaceRangeMemberRange("ge-0/0/0", "ge-0/0/2").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0", "ge-0/0/1", "ge-0/0/2")));
    assertThat(
        new InterfaceRangeMemberRange("ge-0/0/0", "ge-0/2/0").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0", "ge-0/1/0", "ge-0/2/0")));
    assertThat(
        new InterfaceRangeMemberRange("ge-0/0/0", "ge-2/0/0").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0", "ge-1/0/0", "ge-2/0/0")));
  }

  @Test
  void testConstructorBadInterfaceId() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new InterfaceRangeMemberRange("ge-0/0/0", "ge-0/*/0");
            });
    assertThat(exception.getMessage(), containsString("Unexpected interface id pattern"));
  }

  @Test
  void testConstructorDiffTypes() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new InterfaceRangeMemberRange("ge-0/0/0", "xe-0/0/0");
            });
    assertThat(
        exception.getMessage(),
        containsString("Cannot have a range with different interface types"));
  }
}
