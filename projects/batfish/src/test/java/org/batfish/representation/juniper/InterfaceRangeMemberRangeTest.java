package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InterfaceRangeMemberRangeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testGetAllMembers() {
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
  public void testConstructorBadInterfaceId() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Unexpected interface id pattern");
    new InterfaceRangeMemberRange("ge-0/0/0", "ge-0/*/0");
  }

  @Test
  public void testConstructorDiffTypes() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Cannot have a range with different interface types");
    new InterfaceRangeMemberRange("ge-0/0/0", "xe-0/0/0");
  }
}
