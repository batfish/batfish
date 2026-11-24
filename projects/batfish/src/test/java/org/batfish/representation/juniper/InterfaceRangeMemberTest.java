package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class InterfaceRangeMemberTest {

  @Test
  void testConstructorWildcard() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new InterfaceRangeMember("ge-0/*/0");
            });
    assertThat(
        exception.getMessage(),
        containsString("Use of '*' as fpc or pic or port is not currently supported"));
  }

  @Test
  void testExpandComponent() {
    assertThat(InterfaceRangeMember.expandComponent("0"), equalTo(ImmutableList.of(0)));
    assertThat(InterfaceRangeMember.expandComponent("[0-0]"), equalTo(ImmutableList.of(0)));
    assertThat(InterfaceRangeMember.expandComponent("[0]"), equalTo(ImmutableList.of(0)));
    assertThat(InterfaceRangeMember.expandComponent("[0-2]"), equalTo(ImmutableList.of(0, 1, 2)));
    assertThat(InterfaceRangeMember.expandComponent("[0,2,4]"), equalTo(ImmutableList.of(0, 2, 4)));
  }

  @Test
  void testGetAllMembers() {
    assertThat(
        new InterfaceRangeMember("ge-0/0/0").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0")));
    assertThat(
        new InterfaceRangeMember("ge-[0-1]/0/0").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0", "ge-1/0/0")));
    assertThat(
        new InterfaceRangeMember("ge-0/[0-1]/0").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0", "ge-0/1/0")));
    assertThat(
        new InterfaceRangeMember("ge-0/0/[0-1]").getAllMembers(),
        equalTo(ImmutableList.of("ge-0/0/0", "ge-0/0/1")));
  }
}
