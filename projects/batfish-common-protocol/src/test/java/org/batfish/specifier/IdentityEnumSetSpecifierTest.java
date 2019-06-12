package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests for {@link IdentityEnumSetSpecifier} */
public class IdentityEnumSetSpecifierTest {

  @Test
  public void testEquals() {

    IdentityEnumSetSpecifier group1Elem1 = new IdentityEnumSetSpecifier(ImmutableSet.of());

    IdentityEnumSetSpecifier group2Elem1 = new IdentityEnumSetSpecifier(ImmutableSet.of("a"));

    IdentityEnumSetSpecifier group3Elem1 = new IdentityEnumSetSpecifier(ImmutableSet.of("a", "b"));

    IdentityEnumSetSpecifier group3Elem2 = new IdentityEnumSetSpecifier(ImmutableSet.of("b", "a"));

    new EqualsTester()
        .addEqualityGroup(group1Elem1)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1, group3Elem2)
        .testEquals();
  }

  @Test
  public void testResolve() {
    assertThat(
        new IdentityEnumSetSpecifier(ImmutableSet.of()).resolve(), equalTo(ImmutableSet.of()));
    assertThat(
        new IdentityEnumSetSpecifier(ImmutableSet.of("a")).resolve(),
        equalTo(ImmutableSet.of("a")));
  }
}
