package org.batfish.datamodel;

import static org.batfish.datamodel.IntegerSpace.EMPTY;
import static org.batfish.datamodel.IntegerSpace.PORTS;
import static org.batfish.datamodel.IntegerSpace.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IntegerSpace.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link IntegerSpace}. */
final class IntegerSpaceTest {
  private IntegerSpace.Builder _b;

  @BeforeEach
  void setup() {
    _b = IntegerSpace.builder();
  }

  @Test
  void testClosedRangeCreation() {
    IntegerSpace space = _b.including(new SubRange(1, 10)).build();
    for (int i = 1; i <= 10; i++) {
      assertTrue(space.contains(i), "Closed ranges are inclusive");
    }
    assertFalse(space.contains(11), "No members outside of subrange");
    assertFalse(space.contains(0), "No members outside of subrange");
  }

  @Test
  void testInfRangesNotAllowedUpper() {
    assertThrows(IllegalArgumentException.class, () -> _b.including(Range.atLeast(1)));
  }

  @Test
  void testInfRangesNotAllowedLower() {
    assertThrows(IllegalArgumentException.class, () -> _b.including(Range.atMost(1)));
  }

  @Test
  void testInfRangesNotAllowedExcludingUpper() {
    assertThrows(IllegalArgumentException.class, () -> _b.excluding(Range.atMost(10)));
  }

  @Test
  void testInfRangesNotAllowedExcludingLower() {
    assertThrows(IllegalArgumentException.class, () -> _b.excluding(Range.atMost(10)));
  }

  @Test
  void testExcludeWithoutInclude() {
    IntegerSpace space = _b.excluding(Range.closed(1, 10)).build();
    assertTrue(space.isEmpty(), "No inclusions means empty space");
    assertTrue(space.isContiguous(), "No inclusions means contiguous space");
  }

  @Test
  void testIgnoreEmptySubrange() {
    IntegerSpace space = _b.including(new SubRange(10, 0)).build();
    assertTrue(space.isEmpty(), "Empty subrange means empty space");
  }

  @Test
  void testExclusionAndContains() {
    IntegerSpace space =
        _b.including(new SubRange(0, 10))
            .excluding(new SubRange(1, 2))
            .excluding(new SubRange(5, 6))
            .build();

    assertFalse(space.isEmpty(), "Space not empty");
    assertFalse(space.isContiguous(), "Space not contiguous");
    assertFalse(space.contains(1), "Space does not contain excluded values");
    assertFalse(space.contains(2), "Space does not contain excluded values");
    assertFalse(space.contains(5), "Space does not contain excluded values");
    assertFalse(space.contains(6), "Space does not contain excluded values");
    assertFalse(
        space.contains(builder().including(new SubRange(1, 2)).build()),
        "Space does not contain excluded values");
    assertTrue(
        space.contains(IntegerSpace.builder().including(Range.closed(7, 10)).build()),
        "Space contains child space");
    assertFalse(
        space.contains(builder().including(Range.closed(0, 1)).build()),
        "Space does not contain partially overlapping spaces");
  }

  @Test
  void emptyInstanceIsEmpty() {
    assertTrue(EMPTY.isEmpty(), "Empty instance is empty");
  }

  @Test
  void testEnumerate() {
    IntegerSpace space = _b.including(Range.closedOpen(1, 5)).build();
    assertThat(space.enumerate(), equalTo(ImmutableSortedSet.of(1, 2, 3, 4)));
  }

  @Test
  void testEnumerateWithExclusions() {
    IntegerSpace space = _b.including(Range.closedOpen(1, 5)).excluding(Range.closed(2, 3)).build();
    assertThat(space.enumerate(), equalTo(ImmutableSortedSet.of(1, 4)));
  }

  @Test
  void testIntersection() {
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
  void testUnion() {
    IntegerSpace space = _b.including(Range.closed(0, 90)).excluding(Range.closed(10, 20)).build();
    IntegerSpace space2 =
        IntegerSpace.builder()
            .including(Range.closed(10, 20))
            .excluding(Range.closed(30, 40))
            .build();
    assertThat(
        space.union(space2),
        equalTo(IntegerSpace.builder().including(Range.closed(0, 90)).build()));

    IntegerSpace space3 = IntegerSpace.of(Range.closed(40, 200));
    assertThat(
        space.union(space3),
        equalTo(
            IntegerSpace.builder()
                .including(Range.closed(0, 9))
                .including(Range.closed(21, 200))
                .build()));

    assertThat(PORTS.union(EMPTY), equalTo(PORTS));
    assertThat(EMPTY.union(PORTS), equalTo(PORTS));
  }

  @Test
  void testComplement() {
    Range<Integer> u16 = Range.closed(0, 65535);
    IntegerSpace ports = IntegerSpace.of(u16);
    IntegerSpace notPorts = ports.complement(u16);
    assertTrue(notPorts.isEmpty(), "Complement of full space is empty");
    assertThat("Complement of empty should be all", EMPTY.complement(u16), equalTo(ports));

    // Non-empty complement
    IntegerSpace u8 = IntegerSpace.of(Range.closed(0, 255));
    assertThat(u8.complement(u16), equalTo(IntegerSpace.of(Range.closed(256, 65535))));
    IntegerSpace middle = IntegerSpace.of(Range.closed(10, 20));
    assertThat(
        middle.complement(u16),
        equalTo(
            IntegerSpace.builder()
                .including(Range.closed(0, 9))
                .including(Range.closed(21, 65535))
                .build()));
  }

  @Test
  void testComplementRejectsSmaller() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> IntegerSpace.of(Range.closed(10, 100)).complement(Range.singleton(5)));
    assertThat(
        exception.getMessage(),
        containsString(
            "Cannot take the complement of space 10-100 within a smaller bounds [5..5]."));
  }

  @Test
  void testRoundTripThroughBuilder() {
    IntegerSpace space = _b.including(PORTS).excluding(SubRange.singleton(22)).build();
    assertThat(space.toBuilder().build(), equalTo(space));
  }

  @Test
  void testConversionToBuilder() {
    IntegerSpace space = _b.including(PORTS).excluding(SubRange.singleton(22)).build();
    Builder newBuilder = space.toBuilder();
    newBuilder.including(Range.closed(-10, -5));
    newBuilder.excluding(Range.closed(53, 53));
    IntegerSpace newSpace = newBuilder.build();
    assertTrue(newSpace.contains(-7), "Has newly added value");
    assertFalse(newSpace.contains(53), "Does not have newly excluded value");
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            builder().including(new SubRange(1, 10)).build(),
            builder().including(new SubRange(1, 20)).excluding(new SubRange(11, 20)).build())
        .addEqualityGroup(EMPTY)
        .testEquals();
  }

  @Test
  void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(IntegerSpace.PORTS, IntegerSpace.class),
        equalTo(IntegerSpace.PORTS));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(IntegerSpace.PORTS), equalTo(IntegerSpace.PORTS));
  }

  @Test
  void testCreationFromInts() {
    assertThat(
        IntegerSpace.builder().including(3).including(4).build(),
        equalTo(IntegerSpace.of(Range.closed(3, 4))));

    assertThat(
        IntegerSpace.builder().including(3, 4).build(),
        equalTo(IntegerSpace.of(Range.closed(3, 4))));

    assertThat(
        IntegerSpace.builder().including(Range.closed(3, 5)).excluding(5).build(),
        equalTo(IntegerSpace.of(Range.closed(3, 4))));
  }

  @Test
  void testCreationFromString() {
    assertThat(
        IntegerSpace.create("10"),
        equalTo(IntegerSpace.builder().including(Range.closed(10, 10)).build()));
    assertThat(
        IntegerSpace.create("10-20"),
        equalTo(IntegerSpace.builder().including(Range.closed(10, 20)).build()));
    assertThat(
        IntegerSpace.create("10-20,   30 -40"),
        equalTo(
            IntegerSpace.builder()
                .including(Range.closed(10, 20))
                .including(Range.closed(30, 40))
                .build()));
    assertThat(
        IntegerSpace.create("10-20,30-  40,!15"),
        equalTo(
            IntegerSpace.builder()
                .including(Range.closed(10, 20))
                .including(Range.closed(30, 40))
                .excluding(Range.closed(15, 15))
                .build()));
    assertThat(
        IntegerSpace.create("!   35 -36,10-20,30-40,!15"),
        equalTo(
            IntegerSpace.builder()
                .including(Range.closed(10, 20))
                .including(Range.closed(30, 40))
                .excluding(Range.closed(15, 15))
                .excluding(Range.closed(35, 36))
                .build()));
  }

  @Test
  void testCreationFromStringNull() {
    assertThat(IntegerSpace.create(null), equalTo(EMPTY));
    assertThat(IntegerSpace.Builder.create(null).build(), equalTo(EMPTY));
  }

  @Test
  void testCreationFromStringInvalidEmpty() {
    assertTrue(IntegerSpace.create("").isEmpty(), "empty string, empty space");
  }

  @Test
  void testCreationFromStringInvalidEmptyList() {
    assertThrows(IllegalArgumentException.class, () -> IntegerSpace.create(","));
  }

  @Test
  void testCreationFromStringInvalidWord() {
    assertThrows(IllegalArgumentException.class, () -> IntegerSpace.create("a"));
  }

  @Test
  void testCreationFromStringInvalidNotWord() {
    assertThrows(IllegalArgumentException.class, () -> IntegerSpace.create("!a"));
  }

  @Test
  void testCreationFromStringInvalidRange() {
    assertThrows(IllegalArgumentException.class, () -> IntegerSpace.create("54-!54"));
  }

  @Test
  void testCreationFromStringInvalidNegative() {
    assertThrows(IllegalArgumentException.class, () -> IntegerSpace.create("-1"));
  }

  @Test
  void testCreationFromStringInvalidListWithNegativeValues() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            // read: [-2..-1]
            IntegerSpace.create("-2--1"));
  }

  @Test
  void testCreationFromSubRange() {
    assertThat(IntegerSpace.of(new SubRange(3, 4)), equalTo(IntegerSpace.create("3-4")));
  }

  @Test
  void testDifference() {
    IntegerSpace s1 = IntegerSpace.builder().including(Range.closed(1, 10)).build();
    IntegerSpace s2 = IntegerSpace.builder().including(Range.closed(5, 15)).build();
    IntegerSpace expected = IntegerSpace.builder().including(Range.closed(1, 4)).build();
    IntegerSpace expectedFlipped = IntegerSpace.builder().including(Range.closed(11, 15)).build();
    assertThat(s1.difference(s2), equalTo(expected));
    assertThat(s2.difference(s1), equalTo(expectedFlipped));

    IntegerSpace unconnected = IntegerSpace.builder().including(Range.closed(100, 200)).build();
    assertThat(s1.difference(unconnected), equalTo(s1));
    assertThat(unconnected.difference(s1), equalTo(unconnected));

    assertThat(s1.difference(s1), equalTo(EMPTY));
  }

  @Test
  void testSymmetricDifference() {
    IntegerSpace s1 = IntegerSpace.builder().including(Range.closed(1, 10)).build();
    IntegerSpace s2 = IntegerSpace.builder().including(Range.closed(5, 15)).build();
    IntegerSpace expected =
        IntegerSpace.builder()
            .including(Range.closed(1, 4))
            .including(Range.closed(11, 15))
            .build();

    assertThat(s1.symmetricDifference(s2), equalTo(expected));
    assertThat(s2.symmetricDifference(s1), equalTo(expected));

    IntegerSpace unconnected = IntegerSpace.builder().including(Range.closed(100, 200)).build();
    assertThat(s1.symmetricDifference(unconnected), equalTo(s1.union(unconnected)));
    assertThat(unconnected.symmetricDifference(s1), equalTo(s1.union(unconnected)));

    assertThat(s1.symmetricDifference(s1), equalTo(EMPTY));
  }

  @Test
  void testIsSingleton() {
    IntegerSpace s1 = IntegerSpace.builder().including(Range.closed(1, 1)).build();
    IntegerSpace s2 =
        IntegerSpace.builder().including(Range.closed(1, 3)).excluding(Range.closed(2, 3)).build();
    IntegerSpace twoValues = IntegerSpace.builder().including(Range.closed(1, 2)).build();
    assertTrue(s1.isSingleton(), "Must be singleton");
    assertTrue(s2.isSingleton(), "Must be singleton");
    assertFalse(twoValues.isSingleton(), "Must not be singleton");
  }

  @Test
  void testSingletonValue() {
    IntegerSpace s1 = IntegerSpace.builder().including(Range.closed(1, 1)).build();
    IntegerSpace s2 =
        IntegerSpace.builder().including(Range.closed(1, 3)).excluding(Range.closed(2, 3)).build();
    IntegerSpace twoValues = IntegerSpace.builder().including(Range.closed(1, 2)).build();
    assertThat(s1.singletonValue(), equalTo(1));
    assertThat(s2.singletonValue(), equalTo(1));
    assertThrows(NoSuchElementException.class, () -> twoValues.singletonValue());
  }

  @Test
  void testConversionToSubrange() {
    assertThat(IntegerSpace.of(1).getSubRanges(), equalTo(ImmutableSet.of(SubRange.singleton(1))));
    assertThat(PORTS.getSubRanges(), equalTo(ImmutableSet.of(new SubRange(0, 65535))));
    assertThat(EMPTY.getSubRanges(), equalTo(ImmutableSet.of()));
  }

  @Test
  void testStaticCreators() {
    SubRange r1 = SubRange.singleton(1);
    SubRange r2 = new SubRange(4, 5);
    assertThat(
        IntegerSpace.unionOf(r1, r2),
        equalTo(IntegerSpace.builder().including(r1).including(r2).build()));
  }

  @Test
  void testStaticBuilderCreators() {
    Builder b = Builder.create("10-20");
    assertThat(b, not(nullValue()));
    assertThat(b.build(), equalTo(IntegerSpace.of(Range.closed(10, 20))));
  }

  @Test
  void testStream() {
    IntegerSpace space = IntegerSpace.unionOf(new SubRange(1, 5), new SubRange(-3, -1));
    List<Integer> streamed = space.stream().collect(ImmutableList.toImmutableList());
    assertThat(streamed, equalTo(ImmutableList.of(-3, -2, -1, 1, 2, 3, 4, 5)));
  }

  @Test
  void testToString() {
    assertThat(
        IntegerSpace.builder().including(new SubRange(5, 10)).excluding(9).build().toString(),
        equalTo("5-8,10"));

    assertThat(IntegerSpace.builder().build().toString(), equalTo(""));
  }

  @Test
  void testExcludingAllSubranges() {
    // excluding 2 from 1 leaves 1
    assertThat(
        IntegerSpace.of(1).toBuilder()
            .excludingAllSubRanges(ImmutableList.of(SubRange.singleton(2)))
            .build(),
        equalTo(IntegerSpace.of(1)));
    // excluding 2 from 1-2 leaves 1
    assertThat(
        IntegerSpace.of(new SubRange(1, 2)).toBuilder()
            .excludingAllSubRanges(ImmutableList.of(SubRange.singleton(2)))
            .build(),
        equalTo(IntegerSpace.of(1)));
    // excluding 2,3 from 1-3 leaves 1
    assertThat(
        IntegerSpace.of(new SubRange(1, 3)).toBuilder()
            .excludingAllSubRanges(ImmutableList.of(SubRange.singleton(2), SubRange.singleton(3)))
            .build(),
        equalTo(IntegerSpace.of(1)));
  }
}
