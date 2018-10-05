package org.batfish.datamodel;

import static org.batfish.datamodel.IntegerSpace.EMPTY;
import static org.batfish.datamodel.IntegerSpace.PORTS;
import static org.batfish.datamodel.IntegerSpace.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.IntegerSpace.Builder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link IntegerSpace}. */
@RunWith(JUnit4.class)
public class IntegerSpaceTest {
  private IntegerSpace.Builder _b;

  @Rule public ExpectedException _expected = ExpectedException.none();

  @Before
  public void setup() {
    _b = IntegerSpace.builder();
  }

  @Test
  public void testClosedRange() {
    IntegerSpace space = _b.including(new SubRange(1, 10)).build();
    assertThat(space.contains(1), equalTo(true));
    assertThat(space.contains(10), equalTo(true));
    assertThat(space.contains(5), equalTo(true));
  }

  @Test
  public void testInfRangesNotAllowedUpper() {
    _expected.expect(IllegalArgumentException.class);
    _b.including(Range.atLeast(1));
  }

  @Test
  public void testInfRangesNotAllowedLower() {
    _expected.expect(IllegalArgumentException.class);
    _b.including(Range.atMost(1));
  }

  @Test
  public void testInfRangesNotAllowedExcludingUpper() {
    _expected.expect(IllegalArgumentException.class);
    _b.excluding(Range.atMost(10));
  }

  @Test
  public void testInfRangesNotAllowedExcludingLower() {
    _expected.expect(IllegalArgumentException.class);
    _b.excluding(Range.atMost(10));
  }

  @Test
  public void testExcludeWithoutInclude() {
    IntegerSpace space = _b.excluding(Range.closed(1, 10)).build();
    assertThat("No inclusions means empty space", space.isEmpty());
    assertThat("No inclusions means contiguous space", space.isContiguous());
  }

  @Test
  public void testIgnoreEmptySubrange() {
    IntegerSpace space = _b.including(new SubRange(10, 0)).build();
    assertThat("Empty subrange means empty space", space.isEmpty());
  }

  @Test
  public void testExclusionAndContains() {
    IntegerSpace space =
        _b.including(new SubRange(0, 10))
            .excluding(new SubRange(1, 2))
            .excluding(new SubRange(5, 6))
            .build();

    assertThat("Space not empty", !space.isEmpty());
    assertThat("Space not contiguous", !space.isContiguous());
    assertThat("Space does not contain excluded values", !space.contains(1));
    assertThat("Space does not contain excluded values", !space.contains(2));
    assertThat("Space does not contain excluded values", !space.contains(5));
    assertThat("Space does not contain excluded values", !space.contains(6));
    assertThat(
        "Space does not contain excluded values",
        !space.contains(IntegerSpace.builder().including(new SubRange(1, 2)).build()));
    assertThat(
        "Space contains child space",
        space.contains(IntegerSpace.builder().including(Range.closed(7, 10)).build()));
    assertThat(
        "Space does not contain partially overlapping spaces",
        !space.contains(IntegerSpace.builder().including(Range.closed(0, 1)).build()));
  }

  @Test
  public void emptyInstanceIsEmpty() {
    assertThat("Empty instance is empty", EMPTY.isEmpty());
  }

  @Test
  public void testEnumerate() {
    IntegerSpace space = _b.including(Range.closedOpen(1, 5)).build();
    assertThat(space.enumerate(), equalTo(ImmutableSortedSet.of(1, 2, 3, 4)));
  }

  @Test
  public void testEnumerateWithExclusions() {
    IntegerSpace space = _b.including(Range.closedOpen(1, 5)).excluding(Range.closed(2, 3)).build();
    assertThat(space.enumerate(), equalTo(ImmutableSortedSet.of(1, 4)));
  }

  @Test
  public void testIntersection() {
    IntegerSpace space = _b.including(Range.closed(0, 90)).excluding(Range.closed(10, 20)).build();
    IntegerSpace space2 =
        IntegerSpace.builder()
            .including(Range.closed(5, 15))
            .excluding(Range.closed(30, 40))
            .build();
    assertThat(
        space.intersection(space2),
        equalTo(IntegerSpace.builder().including(Range.closed(5, 9)).build()));
    assertThat(PORTS.intersection(EMPTY), equalTo(EMPTY));
  }

  @Test
  public void testUnion() {
    IntegerSpace space = _b.including(Range.closed(0, 90)).excluding(Range.closed(10, 20)).build();
    IntegerSpace space2 =
        IntegerSpace.builder()
            .including(Range.closed(10, 20))
            .excluding(Range.closed(30, 40))
            .build();
    assertThat(
        space.union(space2),
        equalTo(IntegerSpace.builder().including(Range.closed(0, 90)).build()));

    assertThat(PORTS.union(EMPTY), equalTo(PORTS));
  }

  @Test
  public void testComplement() {
    IntegerSpace notPorts = PORTS.not(PORTS);
    assertThat("Complement of full space is empty", notPorts.isEmpty());

    IntegerSpace portsWithExclusion = PORTS.toBuilder().excluding(new SubRange(10, 20)).build();
    assertThat(
        portsWithExclusion.not(),
        equalTo(IntegerSpace.builder().including(new SubRange(10, 20)).build()));

    // A bit contrived, but: a complement within a smaller space can produce a valid result
    IntegerSpace small = _b.including(new SubRange(12, 15)).build();
    assertThat(portsWithExclusion.not(small), equalTo(small));

    // Test empty intersections
    assertThat(
        portsWithExclusion.not(IntegerSpace.builder().including(new SubRange(40, 50)).build()),
        equalTo(EMPTY));
    assertThat(portsWithExclusion.not(EMPTY), equalTo(EMPTY));

    assertThat(EMPTY.not(), equalTo(EMPTY));
  }

  @Test
  public void testRoundTripThroughBuilder() {
    IntegerSpace space = _b.including(PORTS).excluding(new SubRange(22, 22)).build();
    assertThat(space.toBuilder().build(), equalTo(space));
  }

  @Test
  public void testConversionToBuilder() {
    IntegerSpace space = _b.including(PORTS).excluding(new SubRange(22, 22)).build();
    Builder newBuilder = space.toBuilder();
    newBuilder.including(Range.closed(-10, -5));
    newBuilder.excluding(Range.closed(53, 53));
    IntegerSpace newSpace = newBuilder.build();
    assertThat("Has newly added value", newSpace.contains(-7));
    assertThat("Does not have newly excluded value", !newSpace.contains(53));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            builder().including(new SubRange(1, 10)).build(),
            builder().including(new SubRange(1, 20)).excluding(new SubRange(11, 20)).build())
        .addEqualityGroup(EMPTY)
        .testEquals();
  }
}
