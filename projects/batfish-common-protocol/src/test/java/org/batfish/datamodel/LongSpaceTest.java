package org.batfish.datamodel;

import static org.batfish.datamodel.LongSpace.EMPTY;
import static org.batfish.datamodel.LongSpace.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.LongSpace.Builder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link LongSpace}. */
@RunWith(JUnit4.class)
public final class LongSpaceTest {

  private static final LongSpace LONGSPACE1 = LongSpace.of(Range.closed(1L, 100000L));

  private LongSpace.Builder _b;

  @Rule public ExpectedException _expected = ExpectedException.none();

  @Test
  public void emptyInstanceIsEmpty() {
    assertTrue("Empty instance is empty", EMPTY.isEmpty());
  }

  @Before
  public void setup() {
    _b = LongSpace.builder();
  }

  @Test
  public void testClosedRangeCreation() {
    LongSpace space = _b.including(Range.closed(1L, 10L)).build();
    for (long i = 1L; i <= 10L; i++) {
      assertTrue("Closed ranges are inclusive", space.contains(i));
    }
    assertFalse("No members outside of range", space.contains(11L));
    assertFalse("No members outside of range", space.contains(0L));
  }

  @Test
  public void testComplement() {
    Range<Long> numbers = Range.closed(0L, 10000L);
    LongSpace allNumbers = LongSpace.of(numbers);
    LongSpace notAllNumbers = allNumbers.complement(numbers);
    assertTrue("Complement of full space is empty", notAllNumbers.isEmpty());
    assertThat("Complement of empty is everything", EMPTY.complement(numbers), equalTo(allNumbers));

    // Non-empty complement
    LongSpace u8 = LongSpace.of(Range.closed(0L, 255L));
    assertThat(u8.complement(numbers), equalTo(LongSpace.of(Range.closed(256L, 10000L))));
    LongSpace middle = LongSpace.of(Range.closed(10L, 20L));
    assertThat(
        middle.complement(numbers),
        equalTo(
            LongSpace.builder()
                .including(Range.closed(0L, 9L))
                .including(Range.closed(21L, 10000L))
                .build()));
  }

  @Test
  public void testComplementRejectsSmaller() {
    _expected.expect(IllegalArgumentException.class);
    _expected.expectMessage(
        "Cannot take the complement of space 10-100 within a smaller bounds [5..5].");
    LongSpace.of(Range.closed(10L, 100L)).complement(Range.singleton(5L));
  }

  @Test
  public void testConversionToBuilder() {
    LongSpace space = _b.including(LONGSPACE1).excluding(Range.closed(22L, 22L)).build();
    Builder newBuilder = space.toBuilder();
    newBuilder.including(Range.closed(-10L, -5L));
    newBuilder.excluding(Range.closed(53L, 53L));
    LongSpace newSpace = newBuilder.build();
    assertTrue("Has newly added value", newSpace.contains(-7L));
    assertFalse("Does not have newly excluded value", newSpace.contains(53L));
  }

  @Test
  public void testConversionToRanges() {
    assertThat(LongSpace.of(1L).getRanges(), equalTo(ImmutableSet.of(Range.closedOpen(1L, 2L))));
    assertThat(
        LONGSPACE1.getRanges(),
        equalTo(ImmutableSet.of(Range.closedOpen(1L, LONGSPACE1.greatest() + 1L))));
    assertThat(EMPTY.getRanges(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCreationFromLongs() {
    assertThat(
        LongSpace.builder().including(3L).including(4L).build(),
        equalTo(LongSpace.of(Range.closed(3L, 4L))));

    assertThat(
        LongSpace.builder().including(Range.closed(3L, 5L)).excluding(5L).build(),
        equalTo(LongSpace.of(Range.closed(3L, 4L))));
  }

  @Test
  public void testCreationFromString() {
    assertThat(
        LongSpace.create("10"),
        equalTo(LongSpace.builder().including(Range.closed(10L, 10L)).build()));
    assertThat(
        LongSpace.create("10-20"),
        equalTo(LongSpace.builder().including(Range.closed(10L, 20L)).build()));
    assertThat(
        LongSpace.create("10-20,   30 -40"),
        equalTo(
            LongSpace.builder()
                .including(Range.closed(10L, 20L))
                .including(Range.closed(30L, 40L))
                .build()));
    assertThat(
        LongSpace.create("10-20,30-  40,!15"),
        equalTo(
            LongSpace.builder()
                .including(Range.closed(10L, 20L))
                .including(Range.closed(30L, 40L))
                .excluding(Range.closed(15L, 15L))
                .build()));
    assertThat(
        LongSpace.create("!   35 -36,10-20,30-40,!15"),
        equalTo(
            LongSpace.builder()
                .including(Range.closed(10L, 20L))
                .including(Range.closed(30L, 40L))
                .excluding(Range.closed(15L, 15L))
                .excluding(Range.closed(35L, 36L))
                .build()));
  }

  @Test
  public void testCreationFromStringInvalidEmpty() {
    assertTrue("empty string, empty space", LongSpace.create("").isEmpty());
  }

  @Test
  public void testCreationFromStringInvalidEmptyList() {
    _expected.expect(IllegalArgumentException.class);
    LongSpace.create(",");
  }

  @Test
  public void testCreationFromStringInvalidListWithNegativeValues() {
    _expected.expect(IllegalArgumentException.class);
    // read: [-2..-1]
    LongSpace.create("-2--1");
  }

  @Test
  public void testCreationFromStringInvalidNegative() {
    _expected.expect(IllegalArgumentException.class);
    LongSpace.create("-1");
  }

  @Test
  public void testCreationFromStringInvalidNotWord() {
    _expected.expect(IllegalArgumentException.class);
    LongSpace.create("!a");
  }

  @Test
  public void testCreationFromStringInvalidRange() {
    _expected.expect(IllegalArgumentException.class);
    LongSpace.create("54-!54");
  }

  @Test
  public void testCreationFromStringInvalidWord() {
    _expected.expect(IllegalArgumentException.class);
    LongSpace.create("a");
  }

  @Test
  public void testCreationFromStringNull() {
    assertThat(LongSpace.create(null), equalTo(EMPTY));
    assertThat(LongSpace.Builder.create(null).build(), equalTo(EMPTY));
  }

  @Test
  public void testCreationFromSubRange() {
    assertThat(LongSpace.of(Range.closed(3L, 4L)), equalTo(LongSpace.create("3-4")));
  }

  @Test
  public void testDifference() {
    LongSpace s1 = LongSpace.builder().including(Range.closed(1L, 10L)).build();
    LongSpace s2 = LongSpace.builder().including(Range.closed(5L, 15L)).build();
    LongSpace expected = LongSpace.builder().including(Range.closed(1L, 4L)).build();
    LongSpace expectedFlipped = LongSpace.builder().including(Range.closed(11L, 15L)).build();
    assertThat(s1.difference(s2), equalTo(expected));
    assertThat(s2.difference(s1), equalTo(expectedFlipped));

    LongSpace unconnected = LongSpace.builder().including(Range.closed(100L, 200L)).build();
    assertThat(s1.difference(unconnected), equalTo(s1));
    assertThat(unconnected.difference(s1), equalTo(unconnected));

    assertThat(s1.difference(s1), equalTo(EMPTY));
  }

  @Test
  public void testEnumerate() {
    LongSpace space = _b.including(Range.closedOpen(1L, 5L)).build();
    assertThat(space.enumerate(), equalTo(ImmutableSortedSet.of(1L, 2L, 3L, 4L)));
  }

  @Test
  public void testEnumerateWithExclusions() {
    LongSpace space =
        _b.including(Range.closedOpen(1L, 5L)).excluding(Range.closed(2L, 3L)).build();
    assertThat(space.enumerate(), equalTo(ImmutableSortedSet.of(1L, 4L)));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            builder().including(Range.closed(1L, 10L)).build(),
            builder().including(Range.closed(1L, 20L)).excluding(Range.closed(11L, 20L)).build())
        .addEqualityGroup(EMPTY)
        .testEquals();
  }

  @Test
  public void testExcludeWithoutInclude() {
    LongSpace space = _b.excluding(Range.closed(1L, 10L)).build();
    assertTrue("No inclusions means empty space", space.isEmpty());
    assertTrue("No inclusions means contiguous space", space.isContiguous());
  }

  @Test
  public void testExclusionAndContains() {
    LongSpace space =
        _b.including(Range.closed(0L, 10L))
            .excluding(Range.closed(1L, 2L))
            .excluding(Range.closed(5L, 6L))
            .build();

    assertFalse("Space not empty", space.isEmpty());
    assertFalse("Space not contiguous", space.isContiguous());
    assertFalse("Space does not contain excluded values", space.contains(1L));
    assertFalse("Space does not contain excluded values", space.contains(2L));
    assertFalse("Space does not contain excluded values", space.contains(5L));
    assertFalse("Space does not contain excluded values", space.contains(6L));
    assertFalse(
        "Space does not contain excluded values",
        space.contains(builder().including(Range.closed(1L, 2L)).build()));
    assertTrue(
        "Space contains child space",
        space.contains(LongSpace.builder().including(Range.closed(7L, 10L)).build()));
    assertFalse(
        "Space does not contain partially overlapping spaces",
        space.contains(builder().including(Range.closed(0L, 1L)).build()));
  }

  @Test
  public void testIgnoreEmptySubrange() {
    LongSpace space = _b.including(Range.open(0L, 1L)).build();
    assertTrue("Empty space means empty space", space.isEmpty());
  }

  @Test
  public void testInfRangesNotAllowedExcludingLower() {
    _expected.expect(IllegalArgumentException.class);
    _b.excluding(Range.atMost(10L));
  }

  @Test
  public void testInfRangesNotAllowedExcludingUpper() {
    _expected.expect(IllegalArgumentException.class);
    _b.excluding(Range.atMost(10L));
  }

  @Test
  public void testInfRangesNotAllowedLower() {
    _expected.expect(IllegalArgumentException.class);
    _b.including(Range.atMost(1L));
  }

  @Test
  public void testInfRangesNotAllowedUpper() {
    _expected.expect(IllegalArgumentException.class);
    _b.including(Range.atLeast(1L));
  }

  @Test
  public void testIsSingleton() {
    LongSpace s1 = LongSpace.builder().including(Range.closed(1L, 1L)).build();
    LongSpace s2 =
        LongSpace.builder().including(Range.closed(1L, 3L)).excluding(Range.closed(2L, 3L)).build();
    LongSpace twoValues = LongSpace.builder().including(Range.closed(1L, 2L)).build();
    assertTrue("Must be singleton", s1.isSingleton());
    assertTrue("Must be singleton", s2.isSingleton());
    assertFalse("Must not be singleton", twoValues.isSingleton());
  }

  @Test
  public void testIntersection() {
    LongSpace space = _b.including(Range.closed(0L, 90L)).excluding(Range.closed(10L, 20L)).build();
    LongSpace space2 =
        LongSpace.builder()
            .including(Range.closed(5L, 15L))
            .excluding(Range.closed(30L, 40L))
            .build();
    assertThat(
        space.intersection(space2),
        equalTo(LongSpace.builder().including(Range.closed(5L, 9L)).build()));
    assertThat(LONGSPACE1.intersection(EMPTY), equalTo(EMPTY));
  }

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(LONGSPACE1, LongSpace.class), equalTo(LONGSPACE1));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(LONGSPACE1), equalTo(LONGSPACE1));
  }

  @Test
  public void testRoundTripThroughBuilder() {
    LongSpace space = _b.including(LONGSPACE1).excluding(Range.closed(22L, 22L)).build();
    assertThat(space.toBuilder().build(), equalTo(space));
  }

  @Test
  public void testSerialization() throws IOException {
    String serialized = BatfishObjectMapper.writeString(LONGSPACE1);
    assertThat(
        BatfishObjectMapper.mapper().readValue(serialized, LongSpace.class), equalTo(LONGSPACE1));
  }

  @Test
  public void testSingletonValue() {
    LongSpace s1 = LongSpace.builder().including(Range.closed(1L, 1L)).build();
    LongSpace s2 =
        LongSpace.builder().including(Range.closed(1L, 3L)).excluding(Range.closed(2L, 3L)).build();
    LongSpace twoValues = LongSpace.builder().including(Range.closed(1L, 2L)).build();
    assertThat(s1.singletonValue(), equalTo(1L));
    assertThat(s2.singletonValue(), equalTo(1L));
    _expected.expect(NoSuchElementException.class);
    twoValues.singletonValue();
  }

  @Test
  public void testStaticBuilderCreators() {
    Builder b = Builder.create("10-20");
    assertThat(b, not(nullValue()));
    assertThat(b.build(), equalTo(LongSpace.of(Range.closed(10L, 20L))));
  }

  @Test
  public void testStaticCreators() {
    Range<Long> r1 = Range.closed(1L, 1L);
    Range<Long> r2 = Range.closed(4L, 5L);
    assertThat(
        LongSpace.unionOf(r1, r2),
        equalTo(LongSpace.builder().including(r1).including(r2).build()));
  }

  @Test
  public void testStream() {
    LongSpace space = LongSpace.unionOf(Range.closed(1L, 5L), Range.closed(-3L, -1L));
    List<Long> streamed = space.stream().collect(ImmutableList.toImmutableList());
    assertThat(streamed, equalTo(ImmutableList.of(-3L, -2L, -1L, 1L, 2L, 3L, 4L, 5L)));
  }

  @Test
  public void testSymmetricDifference() {
    LongSpace s1 = LongSpace.builder().including(Range.closed(1L, 10L)).build();
    LongSpace s2 = LongSpace.builder().including(Range.closed(5L, 15L)).build();
    LongSpace expected =
        LongSpace.builder()
            .including(Range.closed(1L, 4L))
            .including(Range.closed(11L, 15L))
            .build();

    assertThat(s1.symmetricDifference(s2), equalTo(expected));
    assertThat(s2.symmetricDifference(s1), equalTo(expected));

    LongSpace unconnected = LongSpace.builder().including(Range.closed(100L, 200L)).build();
    assertThat(s1.symmetricDifference(unconnected), equalTo(s1.union(unconnected)));
    assertThat(unconnected.symmetricDifference(s1), equalTo(s1.union(unconnected)));

    assertThat(s1.symmetricDifference(s1), equalTo(EMPTY));
  }

  @Test
  public void testToString() {
    assertThat(
        LongSpace.builder().including(Range.closed(5L, 10L)).excluding(9L).build().toString(),
        equalTo("5-8,10"));

    assertThat(LongSpace.builder().build().toString(), equalTo(""));
  }

  @Test
  public void testUnion() {
    LongSpace space = _b.including(Range.closed(0L, 90L)).excluding(Range.closed(10L, 20L)).build();
    LongSpace space2 =
        LongSpace.builder()
            .including(Range.closed(10L, 20L))
            .excluding(Range.closed(30L, 40L))
            .build();
    assertThat(
        space.union(space2), equalTo(LongSpace.builder().including(Range.closed(0L, 90L)).build()));

    assertThat(LONGSPACE1.union(EMPTY), equalTo(LONGSPACE1));
  }
}
