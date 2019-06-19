package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class InterfaceRangeTest {

  /** Test that members are included */
  @Test
  public void testGetAllMembersIncludeMembers() {
    InterfaceRange irange = new InterfaceRange("test");
    irange.getMembers().add(new InterfaceRangeMember("ge-0/0/[0,1]"));
    assertThat(irange.getAllMembers(), equalTo(ImmutableSet.of("ge-0/0/0", "ge-0/0/1")));
  }

  /** Test that member ranges are included */
  @Test
  public void testGetAllMembersIncludeMemberRanges() {
    InterfaceRange irange = new InterfaceRange("test");
    irange.getMemberRanges().add(new InterfaceRangeMemberRange("ge-0/0/0", "ge-0/0/1"));
    assertThat(irange.getAllMembers(), equalTo(ImmutableSet.of("ge-0/0/0", "ge-0/0/1")));
  }

  /** Test that member AND member ranges are included, and duplicates are resolved */
  @Test
  public void testGetAllMembersIncludeBoth() {
    InterfaceRange irange = new InterfaceRange("test");
    irange.getMemberRanges().add(new InterfaceRangeMemberRange("ge-0/0/0", "ge-0/0/2"));
    irange.getMembers().add(new InterfaceRangeMember("ge-0/0/[1,3]"));
    assertThat(
        irange.getAllMembers(),
        equalTo(ImmutableSet.of("ge-0/0/0", "ge-0/0/1", "ge-0/0/2", "ge-0/0/3")));
  }
}
