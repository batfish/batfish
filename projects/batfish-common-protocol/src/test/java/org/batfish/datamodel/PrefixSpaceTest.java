package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrefixSpaceTest {

  private PrefixSpace ps;

  @Before
  public void constructPrefixSpace() {
    ps = new PrefixSpace();
  }

  @Test
  public void getAddressBits() {
    assertThat(PrefixSpace.getAddressBits(Ip.ZERO), equalTo(new BitSet()));
    assertThat(
        PrefixSpace.getAddressBits(Ip.MAX), equalTo(BitSet.valueOf(new long[] {0xff_ff_ff_ffL})));
    assertThat(
        PrefixSpace.getAddressBits(new Ip("128.255.31.3")),
        equalTo(BitSet.valueOf(new long[] {0xc0_f8_ff_01L})));
  }

  @Test
  public void constructEmptyPrefixSpaceTest() {
    assertThat(ps.isEmpty(), equalTo(true));
  }

  @Test
  public void constructPrefixSpaceTest() {
    Set<PrefixRange> ranges = new TreeSet<>();
    PrefixRange range = new PrefixRange("100.0.0.0/32");
    ranges.add(range);
    ps = new PrefixSpace(ranges);
    assertThat(ps.isEmpty(), equalTo(false));
    assertThat(ps.containsPrefixRange(range), equalTo(true));
  }

  @Test
  public void addPrefixTest() {
    Prefix prefix = new Prefix("100.0.0.0/32");
    ps.addPrefix(prefix);
    assertThat(ps.getPrefixRanges().size(), equalTo(1));
    assertThat(ps.containsPrefix(prefix), equalTo(true));
  }

  @Test
  public void addPrefixRangeTest() {
    Set<PrefixRange> ranges = new TreeSet<>();
    PrefixRange range = new PrefixRange("10.10.10.0/32:16-24");
    ranges.add(range);
    ps = new PrefixSpace(ranges);
    assertThat(ps.containsPrefixRange(range), equalTo(true));
    assertThat(
        "Shorter prefixes not included",
        ps.containsPrefix(new Prefix("10.10.10.0/15")),
        equalTo(false));
    assertThat(
        "Shortest prefix is included",
        ps.containsPrefix(new Prefix("10.10.10.0/16")),
        equalTo(true));
    assertThat(
        "Longest prefix is included",
        ps.containsPrefix(new Prefix("10.10.10.0/24")),
        equalTo(true));
    assertThat(
        "Longer prefixes not included",
        ps.containsPrefix(new Prefix("10.10.10.0/25")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in masked bits not included",
        ps.containsPrefix(new Prefix("10.10.11.0/24")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in unmasked bits included",
        ps.containsPrefix(new Prefix("10.10.11.0/16")),
        equalTo(true));
  }

  @Test
  public void addSpaceTest() {
    Set<PrefixRange> ranges = new TreeSet<>();
    PrefixRange range = new PrefixRange("100.0.0.0/32");
    ranges.add(range);
    ps.addSpace(new PrefixSpace(ranges));
    assertThat(ps.isEmpty(), equalTo(false));
    assertThat(ps.containsPrefixRange(range), equalTo(true));
  }

  @Test
  public void containsPrefixTest() {
    Prefix prefix = new Prefix("10.10.10.0/24");
    ps.addPrefix(prefix);

    assertThat(
        "Shorter prefixes not included",
        ps.containsPrefix(new Prefix("10.10.10.0/20")),
        equalTo(false));
    assertThat(
        "Exact given prefix is included",
        ps.containsPrefix(new Prefix("10.10.10.0/24")),
        equalTo(true));
    assertThat(
        "Longer prefixes not included",
        ps.containsPrefix(new Prefix("10.10.10.0/26")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in masked bits not included",
        ps.containsPrefix(new Prefix("10.10.11.0/24")),
        equalTo(false));
    assertThat(
        "Prefixes with mismatch in unmasked bits included",
        ps.containsPrefix(new Prefix("10.10.10.255/24")),
        equalTo(true));
  }

  @Test
  public void containsPrefixRangeTest() {
    PrefixRange range = new PrefixRange("10.10.10.0/20:16-24");
    ps.addPrefixRange(range);

    assertThat(
        "Ranges earlier than this range not included",
        ps.containsPrefixRange(new PrefixRange("10.10.10.0/20:15-24")),
        equalTo(false));
    assertThat(
        "Ranges later than this range not included",
        ps.containsPrefixRange(new PrefixRange("10.10.10.0/20:18-26")),
        equalTo(false));
    assertThat(
        "Ranges contained in this range included",
        ps.containsPrefixRange(new PrefixRange("10.10.10.0/20:18-20")),
        equalTo(true));
    assertThat(
        "Range not included if prefix is shorter",
        ps.containsPrefixRange(new PrefixRange("10.10.10.0/8:16-24")),
        equalTo(false));
    assertThat(
        "Range included if prefix is longer",
        ps.containsPrefixRange(new PrefixRange("10.10.10.0/28:16-24")),
        equalTo(true));
  }

  @Test
  public void getPrefixRangesTest() {
    assertThat("No ranges yet", ps.getPrefixRanges().isEmpty(), equalTo(true));
    ps.addPrefixRange(new PrefixRange("10.10.10.0/20:18-21"));
    Set<PrefixRange> ranges = ps.getPrefixRanges();
    assertThat(ranges.contains(new PrefixRange("10.10.0.0/18")), equalTo(true));
    assertThat(ranges.contains(new PrefixRange("10.10.0.0/19")), equalTo(true));
    assertThat(ranges.contains(new PrefixRange("10.10.10.0/20:18-21")), equalTo(true));
    assertThat(ranges.size(), equalTo(3));
    ps.addPrefixRange((new PrefixRange("10.10.10.0/20:20-22")));
    ranges = ps.getPrefixRanges();
    assertThat(ranges.contains(new PrefixRange("10.10.10.0/20:20-22")), equalTo(true));
  }

  @Test
  public void intersectionAndOverlapsTest() {
    PrefixSpace other = new PrefixSpace();
    assertThat(
        "empty spaces don't intersect", this.ps.intersection(other).isEmpty(), equalTo(true));
    assertThat("no overlap for empty spaces", this.ps.overlaps(other), equalTo(false));
    ps.addPrefixRange(new PrefixRange("10.10.10.0/20:18-21"));
    assertThat("no intersection for one empty", this.ps.intersection(other).isEmpty(), equalTo(true));
    assertThat("no overlap for one empty", this.ps.overlaps(other), equalTo(false));
    other.addPrefixRange(new PrefixRange("10.10.10.0/16:14-16"));
    assertThat("no intersection", this.ps.intersection(other).isEmpty(), equalTo(true));
    assertThat("no overlap", this.ps.overlaps(other), equalTo(false));
    other.addPrefixRange(new PrefixRange("10.10.10.0/20:18-20"));
    PrefixSpace intersection = this.ps.intersection(other);
    assertThat(
        "now intersect for length 18",
        intersection.containsPrefix(new Prefix("10.10.0.0/18")),
        equalTo(true));
    assertThat(
        "now intersect for length 19",
        intersection.containsPrefix(new Prefix("10.10.0.0/19")),
        equalTo(true));
    assertThat(
        "now intersect for range 18-20",
        intersection.containsPrefixRange(new PrefixRange("10.10.0.0/20:18-20")),
        equalTo(true));
    assertThat("don't intersect anywhere else", intersection.getPrefixRanges().size(), equalTo(3));
    assertThat("has overlap", this.ps.overlaps(other), equalTo(true));
  }
}
