package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests for {@link IdentityNamedStructureSpecifier} */
public class IdentityNamedStructureSpecifierTest {

  @Test
  public void testEquals() {

    IdentityNamedStructureSpecifier group1Elem1 =
        new IdentityNamedStructureSpecifier(ImmutableSet.of());

    IdentityNamedStructureSpecifier group2Elem1 =
        new IdentityNamedStructureSpecifier(ImmutableSet.of("a"));

    IdentityNamedStructureSpecifier group3Elem1 =
        new IdentityNamedStructureSpecifier(ImmutableSet.of("a", "b"));

    IdentityNamedStructureSpecifier group3Elem2 =
        new IdentityNamedStructureSpecifier(ImmutableSet.of("b", "a"));

    new EqualsTester()
        .addEqualityGroup(group1Elem1)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1, group3Elem2)
        .testEquals();
  }

  @Test
  public void testResolve() {
    assertThat(
        new IdentityNamedStructureSpecifier(ImmutableSet.of()).resolve(),
        equalTo(ImmutableSet.of()));
    assertThat(
        new IdentityNamedStructureSpecifier(ImmutableSet.of("a")).resolve(),
        equalTo(ImmutableSet.of("a")));
  }
}
