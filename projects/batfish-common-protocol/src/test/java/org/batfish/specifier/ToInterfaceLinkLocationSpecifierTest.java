package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class ToInterfaceLinkLocationSpecifierTest {

  MockSpecifierContext _mockContext = MockSpecifierContext.builder().build();

  /** Do we resolve empty sets properly? */
  @Test
  public void resolveEmptySet() {
    LocationSpecifier inner = new MockLocationSpecifier(ImmutableSet.of());
    assertThat(
        new ToInterfaceLinkLocationSpecifier(inner).resolve(_mockContext),
        equalTo(ImmutableSet.of()));
  }

  /** Do we resolve non empty sets properly? */
  @Test
  public void resolveNonEmptySet() {
    LocationSpecifier inner =
        new MockLocationSpecifier(
            ImmutableSet.of(
                new InterfaceLocation("foo1", "bar1"), new InterfaceLinkLocation("foo2", "bar2")));
    assertThat(
        new ToInterfaceLinkLocationSpecifier(inner).resolve(_mockContext),
        equalTo(
            ImmutableSet.of(
                new InterfaceLinkLocation("foo1", "bar1"),
                new InterfaceLinkLocation("foo2", "bar2"))));
  }

  /** Do we convert locations properly? */
  @Test
  public void toInterfaceLinkLocation() {
    InterfaceLocation ifaceLocation = new InterfaceLocation("foo", "bar");
    InterfaceLinkLocation ifaceLinkLocation = new InterfaceLinkLocation("foo", "bar");

    assertThat(
        ToInterfaceLinkLocationSpecifier.toInterfaceLinkLocation(ifaceLocation),
        equalTo(ifaceLinkLocation));
    assertThat(
        ToInterfaceLinkLocationSpecifier.toInterfaceLinkLocation(ifaceLinkLocation),
        equalTo(ifaceLinkLocation));
  }
}
