package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests for {@link ConstantEnumSetSpecifier} */
public class ConstantEnumSetSpecifierTest {

  @Test
  public void testEquals() {

    ConstantEnumSetSpecifier<String> group1Elem1 =
        new ConstantEnumSetSpecifier<>(ImmutableSet.of());

    ConstantEnumSetSpecifier<String> group2Elem1 =
        new ConstantEnumSetSpecifier<>(ImmutableSet.of("a"));

    ConstantEnumSetSpecifier<String> group3Elem1 =
        new ConstantEnumSetSpecifier<>(ImmutableSet.of("a", "b"));

    ConstantEnumSetSpecifier<String> group3Elem2 =
        new ConstantEnumSetSpecifier<>(ImmutableSet.of("b", "a"));

    new EqualsTester()
        .addEqualityGroup(group1Elem1)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1, group3Elem2)
        .testEquals();
  }

  @Test
  public void testResolve() {
    assertThat(
        new ConstantEnumSetSpecifier<>(ImmutableSet.of()).resolve(), equalTo(ImmutableSet.of()));
    assertThat(
        new ConstantEnumSetSpecifier<>(ImmutableSet.of("a")).resolve(),
        equalTo(ImmutableSet.of("a")));
  }
}
