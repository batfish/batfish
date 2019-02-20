package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InterfaceRangeMemberTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorWildcard() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Use of '*' as fpc or pic or port is not currently supported");
    new InterfaceRangeMember("ge-0/*/0");
  }

  @Test
  public void testExpandComponent() {
    assertThat(InterfaceRangeMember.expandComponent("0"), equalTo(ImmutableList.of(0)));
    assertThat(InterfaceRangeMember.expandComponent("[0-0]"), equalTo(ImmutableList.of(0)));
    assertThat(InterfaceRangeMember.expandComponent("[0]"), equalTo(ImmutableList.of(0)));
    assertThat(InterfaceRangeMember.expandComponent("[0-2]"), equalTo(ImmutableList.of(0, 1, 2)));
    assertThat(InterfaceRangeMember.expandComponent("[0,2,4]"), equalTo(ImmutableList.of(0, 2, 4)));
  }

  @Test
  public void testGetAllMembers() {
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
