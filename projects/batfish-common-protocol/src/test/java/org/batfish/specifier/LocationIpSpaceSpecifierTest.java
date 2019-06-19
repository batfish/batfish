package org.batfish.specifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LocationIpSpaceSpecifierTest {
  @Rule public ExpectedException _expectedException = ExpectedException.none();

  @Test
  public void testNoLocations() {
    MockSpecifierContext ctxt = MockSpecifierContext.builder().build();
    IpSpaceSpecifier specifier =
        new LocationIpSpaceSpecifier(new MockLocationSpecifier(ImmutableSet.of()));
    assertThat(
        specifier.resolve(ImmutableSet.of(), ctxt),
        equalTo(
            IpSpaceAssignment.of(
                ImmutableList.of(new Entry(EmptyIpSpace.INSTANCE, ImmutableSet.of())))));
  }
}
