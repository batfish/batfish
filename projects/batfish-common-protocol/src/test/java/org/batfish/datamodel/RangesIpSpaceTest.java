package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class RangesIpSpaceTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            RangesIpSpace.create(LongSpace.of(5)), RangesIpSpace.create(LongSpace.of(5)))
        .addEqualityGroup(RangesIpSpace.create(LongSpace.of(6)))
        .testEquals();
  }

  @Test
  public void testIsEmpty() {
    assertTrue(RangesIpSpace.empty().isEmpty());

    RangesIpSpace space =
        RangesIpSpace.builder()
            .including(Prefix.parse("10.0.0.0/8"))
            .excluding(Prefix.parse("10.0.0.0/24"))
            .build();
    assertFalse(space.isEmpty());
  }

  @Test
  public void testComplement() {
    assertThat(RangesIpSpace.empty().complement(), equalTo(UniverseIpSpace.INSTANCE));

    // make a partial space, check one in and on out
    RangesIpSpace space =
        RangesIpSpace.builder()
            .including(Prefix.parse("10.0.0.0/8"))
            .excluding(Prefix.parse("10.0.0.0/24"))
            .build();
    assertFalse(space.containsIp(Ip.parse("10.0.0.1")));
    assertTrue(space.containsIp(Ip.parse("10.0.1.1")));

    // Complement is a RangesIpSpace with opposite containment
    IpSpace complement = space.complement();
    assertThat(complement, instanceOf(RangesIpSpace.class));
    RangesIpSpace complementRange = (RangesIpSpace) complement;
    assertTrue(complementRange.containsIp(Ip.parse("10.0.0.1")));
    assertFalse(complementRange.containsIp(Ip.parse("10.0.1.1")));

    // space union complement is everything.
    assertThat(
        RangesIpSpace.union(space, complementRange),
        equalTo(RangesIpSpace.builder().including(Prefix.ZERO).build()));

    // complement twice is equal to space
    assertThat(complementRange.complement(), equalTo(space));
  }

  @Test
  public void testContainsIp() {
    RangesIpSpace space =
        RangesIpSpace.builder()
            .including(Prefix.parse("1.0.0.0/24"))
            .including(Ip.parse("2.1.2.3"))
            .build();
    assertFalse(space.containsIp(Ip.ZERO));
    assertFalse(space.containsIp(Ip.MAX));

    assertFalse(space.containsIp(Ip.parse("0.255.255.255")));
    assertTrue(space.containsIp(Ip.parse("1.0.0.0")));
    assertTrue(space.containsIp(Ip.parse("1.0.0.128")));
    assertTrue(space.containsIp(Ip.parse("1.0.0.255")));
    assertFalse(space.containsIp(Ip.parse("1.0.1.0")));

    assertFalse(space.containsIp(Ip.parse("2.1.2.2")));
    assertTrue(space.containsIp(Ip.parse("2.1.2.3")));
    assertFalse(space.containsIp(Ip.parse("2.1.2.4")));
  }

  @Test
  public void testBuilding() {
    // Include and exclude same type give empty
    RangesIpSpace ip3 = RangesIpSpace.builder().including(Prefix.parse("3.0.0.0/8")).build();
    assertTrue(
        RangesIpSpace.builder()
            .including(Ip.parse("1.2.3.4"))
            .excluding(Ip.parse("1.2.3.4"))
            .including(Prefix.parse("2.0.0.0/8"))
            .excluding(Prefix.parse("2.0.0.0/8"))
            .including(ip3)
            .excluding(ip3)
            .build()
            .isEmpty());

    RangesIpSpace space =
        RangesIpSpace.builder()
            .including(Prefix.parse("1.2.3.0/24"))
            .excluding(Ip.parse("1.2.3.4"))
            .including(Prefix.parse("2.0.0.0/24"))
            .excluding(Prefix.parse("2.0.0.0/8"))
            .build();
    assertThat(
        space.getSpace(),
        equalTo(
            LongSpace.builder()
                .including(Range.closed(Ip.parse("1.2.3.0").asLong(), Ip.parse("1.2.3.3").asLong()))
                .including(
                    Range.closed(Ip.parse("1.2.3.5").asLong(), Ip.parse("1.2.3.255").asLong()))
                .build()));
  }

  @Test
  public void testSerialization() {
    RangesIpSpace space =
        RangesIpSpace.builder()
            .including(Prefix.parse("10.0.0.0/8"))
            .excluding(Prefix.parse("10.0.0.0/24"))
            .build();
    assertThat(BatfishObjectMapper.clone(space, IpSpace.class), equalTo(space));
    assertThat(SerializationUtils.clone(space), equalTo(space));
  }

  @Test
  public void testSorting() {
    List<RangesIpSpace> space =
        ImmutableList.of(
            RangesIpSpace.empty(),
            RangesIpSpace.builder().including(Ip.parse("1.2.3.4")).build(),
            RangesIpSpace.builder().including(Ip.parse("1.2.3.4")).build(),
            RangesIpSpace.builder().including(Ip.parse("1.2.3.5")).build(),
            RangesIpSpace.builder().including(Prefix.parse("2.0.0.0/24")).build(),
            RangesIpSpace.builder().including(Ip.parse("2.0.0.1")).build(),
            RangesIpSpace.builder().including(Prefix.parse("0.0.0.0/3")).build());
    assertThat(
        space.stream().sorted().collect(Collectors.toList()),
        equalTo(Lists.reverse(space).stream().sorted().collect(Collectors.toList())));
  }
}
