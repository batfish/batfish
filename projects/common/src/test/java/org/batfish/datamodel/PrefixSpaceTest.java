package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.BitSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrefixSpaceTest {

  private PrefixSpace _ps;

  @Before
  public void constructPrefixSpace() {
    _ps = new PrefixSpace();
  }

  @Test
  public void getAddressBits() {
    assertThat(PrefixSpace.getAddressBits(Ip.ZERO), equalTo(new BitSet()));
    assertThat(
        PrefixSpace.getAddressBits(Ip.MAX), equalTo(BitSet.valueOf(new long[] {0xff_ff_ff_ffL})));
    assertThat(
        PrefixSpace.getAddressBits(Ip.parse("128.255.31.3")),
        equalTo(BitSet.valueOf(new long[] {0xc0_f8_ff_01L})));
  }

  @Test
  public void constructEmptyPrefixSpaceTest() {
    assertThat(_ps.isEmpty(), equalTo(true));
  }

  @Test
  public void constructPrefixSpaceTest() {
    _ps = new PrefixSpace(PrefixRange.fromString("100.0.0.0/32"));
    assertThat(_ps.isEmpty(), equalTo(false));
    assertThat(_ps.containsPrefixRange(PrefixRange.fromString("100.0.0.0/32")), equalTo(true));
  }

  @Test
  public void addPrefixTest() {
    Prefix prefix = Prefix.parse("100.0.0.0/32");
    _ps.addPrefix(prefix);
    assertThat(_ps.getPrefixRanges().size(), equalTo(1));
    assertThat(_ps.containsPrefix(prefix), equalTo(true));
  }

  @Test
  public void addPrefixRangeTest() {
    PrefixRange range = PrefixRange.fromString("10.10.10.0/32:16-24");
    _ps = new PrefixSpace(range);
    assertThat(_ps.containsPrefixRange(range), equalTo(true));
    assertThat(
        "Shorter prefixes not included",
        _ps.containsPrefix(Prefix.parse("10.10.10.0/15")),
        equalTo(false));
    assertThat(
        "Shortest prefix is included",
        _ps.containsPrefix(Prefix.parse("10.10.10.0/16")),
        equalTo(true));
    assertThat(
        "Longest prefix is included",
        _ps.containsPrefix(Prefix.parse("10.10.10.0/24")),
        equalTo(true));
    assertThat(
        "Longer prefixes not included",
        _ps.containsPrefix(Prefix.parse("10.10.10.0/25")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in masked bits not included",
        _ps.containsPrefix(Prefix.parse("10.10.11.0/24")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in unmasked bits included",
        _ps.containsPrefix(Prefix.parse("10.10.10.255/24")),
        equalTo(true));
  }

  @Test
  public void addSpaceTest() {
    PrefixRange range = PrefixRange.fromString("100.0.0.0/32");
    _ps.addSpace(new PrefixSpace(range));
    assertThat(_ps.isEmpty(), equalTo(false));
    assertThat(_ps.containsPrefixRange(range), equalTo(true));
  }

  @Test
  public void containsPrefixTest() {
    Prefix prefix = Prefix.parse("10.10.10.0/24");
    _ps.addPrefix(prefix);

    assertThat(
        "Shorter prefixes not included",
        _ps.containsPrefix(Prefix.parse("10.10.10.0/20")),
        equalTo(false));
    assertThat(
        "Exact given prefix is included",
        _ps.containsPrefix(Prefix.parse("10.10.10.0/24")),
        equalTo(true));
    assertThat(
        "Longer prefixes not included",
        _ps.containsPrefix(Prefix.parse("10.10.10.0/26")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in masked bits not included",
        _ps.containsPrefix(Prefix.parse("10.10.11.0/24")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in unmasked bits included",
        _ps.containsPrefix(Prefix.parse("10.10.10.255/24")),
        equalTo(true));
  }

  @Test
  public void containsPrefixRangeTest() {
    PrefixRange range = PrefixRange.fromString("10.10.10.0/20:16-24");
    _ps.addPrefixRange(range);

    assertThat(
        "Ranges earlier than this range not included",
        _ps.containsPrefixRange(PrefixRange.fromString("10.10.10.0/20:15-24")),
        equalTo(false));
    assertThat(
        "Ranges later than this range not included",
        _ps.containsPrefixRange(PrefixRange.fromString("10.10.10.0/20:18-26")),
        equalTo(false));
    assertThat(
        "Ranges contained in this range included",
        _ps.containsPrefixRange(PrefixRange.fromString("10.10.10.0/20:18-20")),
        equalTo(true));
    assertThat(
        "Range not included if prefix is shorter",
        _ps.containsPrefixRange(PrefixRange.fromString("10.10.10.0/8:16-24")),
        equalTo(false));
    assertThat(
        "Range included if prefix is longer",
        _ps.containsPrefixRange(PrefixRange.fromString("10.10.10.0/28:16-24")),
        equalTo(true));
  }

  @Test
  public void getPrefixRangesTest() {
    assertThat("No ranges yet", _ps.getPrefixRanges().isEmpty(), equalTo(true));
    _ps.addPrefixRange(PrefixRange.fromString("10.10.10.0/20:18-21"));
    Set<PrefixRange> ranges = _ps.getPrefixRanges();
    assertThat(ranges.contains(PrefixRange.fromString("10.10.0.0/18")), equalTo(true));
    assertThat(ranges.contains(PrefixRange.fromString("10.10.0.0/19")), equalTo(true));
    assertThat(ranges.contains(PrefixRange.fromString("10.10.10.0/20:18-21")), equalTo(true));
    assertThat(ranges.size(), equalTo(3));
    _ps.addPrefixRange((PrefixRange.fromString("10.10.10.0/20:20-22")));
    ranges = _ps.getPrefixRanges();
    assertThat(ranges.contains(PrefixRange.fromString("10.10.10.0/20:20-22")), equalTo(true));
  }

  @Test
  public void intersectionAndOverlapsTest() {
    PrefixSpace other = new PrefixSpace();
    assertThat("empty spaces don't intersect", _ps.intersection(other).isEmpty(), equalTo(true));
    assertThat("no overlap for empty spaces", _ps.overlaps(other), equalTo(false));
    _ps.addPrefixRange(PrefixRange.fromString("10.10.10.0/20:18-21"));
    assertThat("no intersection for one empty", _ps.intersection(other).isEmpty(), equalTo(true));
    assertThat("no overlap for one empty", _ps.overlaps(other), equalTo(false));
    other.addPrefixRange(PrefixRange.fromString("10.10.10.0/16:14-16"));
    assertThat("no intersection", _ps.intersection(other).isEmpty(), equalTo(true));
    assertThat("no overlap", _ps.overlaps(other), equalTo(false));
    other.addPrefixRange(PrefixRange.fromString("10.10.10.0/20:18-20"));
    PrefixSpace intersection = _ps.intersection(other);
    assertThat(
        "now intersect for length 18",
        intersection.containsPrefix(Prefix.parse("10.10.0.0/18")),
        equalTo(true));
    assertThat(
        "now intersect for length 19",
        intersection.containsPrefix(Prefix.parse("10.10.0.0/19")),
        equalTo(true));
    assertThat(
        "now intersect for range 18-20",
        intersection.containsPrefixRange(PrefixRange.fromString("10.10.0.0/20:18-20")),
        equalTo(true));
    assertThat("don't intersect anywhere else", intersection.getPrefixRanges().size(), equalTo(3));
    assertThat("has overlap", _ps.overlaps(other), equalTo(true));
  }

  @Test
  public void testPruning() {
    PrefixRange ten8to15 = PrefixRange.fromString("10.0.0.0/8:8-15");
    PrefixRange ten8to16 = PrefixRange.fromString("10.0.0.0/8:8-16");
    PrefixRange ten9to17 = PrefixRange.fromString("10.0.0.0/8:9-17");
    PrefixRange one9to9 = PrefixRange.fromString("1.0.0.0/8:9-9");
    PrefixRange eleven9to9 = PrefixRange.fromString("11.0.0.0/8:9-9");
    PrefixRange all = PrefixRange.fromString("0.0.0.0/0:0-32");

    PrefixSpace space = new PrefixSpace();
    space.addPrefixRange(ten8to15);
    assertThat(space.getPrefixRanges(), equalTo(ImmutableSet.of(ten8to15)));

    space.addPrefixRange(ten8to16);
    assertThat(space.getPrefixRanges(), equalTo(ImmutableSet.of(ten8to16)));
    space.addPrefixRange(ten8to16);
    assertThat(space.getPrefixRanges(), equalTo(ImmutableSet.of(ten8to16)));

    space.addPrefixRange(ten9to17);
    assertThat(space.getPrefixRanges(), equalTo(ImmutableSet.of(ten8to16, ten9to17)));

    space.addPrefixRange(one9to9);
    assertThat(space.getPrefixRanges(), equalTo(ImmutableSet.of(one9to9, ten8to16, ten9to17)));

    space.addPrefixRange(eleven9to9);
    assertThat(
        space.getPrefixRanges(), equalTo(ImmutableSet.of(one9to9, ten8to16, ten9to17, eleven9to9)));

    space.addPrefixRange(all);
    assertThat(space.getPrefixRanges(), equalTo(ImmutableSet.of(all)));
  }
}
