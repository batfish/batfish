package org.batfish.datamodel.visitors;

import static org.batfish.datamodel.visitors.IpSpaceToRangeSet.toRangeSet;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

/** Test for {@link IpSpaceToRangeSet}. */
public final class IpSpaceToRangeSetTest {
  @Test
  public void testEmpty() {
    assertEquals(ImmutableRangeSet.of(), toRangeSet(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testIp() {
    Ip ip = Ip.parse("1.2.3.4");
    assertEquals(ImmutableRangeSet.of(Range.closed(ip, ip)), toRangeSet(ip.toIpSpace()));
  }

  @Test
  public void testIpWildcard() {
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    assertEquals(
        ImmutableRangeSet.of(Range.closed(prefix.getStartIp(), prefix.getEndIp())),
        toRangeSet(IpWildcard.create(prefix).toIpSpace()));
  }

  @Test
  public void testIpWildcardSet() {
    Prefix p10 = Prefix.parse("10.0.0.0/8");
    Prefix p20 = Prefix.parse("20.0.0.0/8");
    Ip ip10 = Ip.parse("10.10.10.10");
    Ip ip20 = Ip.parse("20.20.20.20");
    assertEquals(
        ImmutableRangeSet.builder()
            .add(Range.closedOpen(p10.getStartIp(), ip10))
            .add(Range.openClosed(ip10, p10.getEndIp()))
            .add(Range.closedOpen(p20.getStartIp(), ip20))
            .add(Range.openClosed(ip20, p20.getEndIp()))
            .build(),
        toRangeSet(
            IpWildcardSetIpSpace.builder()
                .including(IpWildcard.create(p10), IpWildcard.create(p20))
                .excluding(IpWildcard.create(ip10), IpWildcard.create(ip20))
                .build()));
  }

  @Test
  public void testPrefix() {
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    assertEquals(
        ImmutableRangeSet.of(Range.closed(prefix.getStartIp(), prefix.getEndIp())),
        toRangeSet(prefix.toIpSpace()));
  }

  @Test
  public void testUniverse() {
    assertEquals(
        ImmutableRangeSet.of(Range.closed(Prefix.ZERO.getStartIp(), Prefix.ZERO.getEndIp())),
        toRangeSet(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testAclIpSpace() {
    // no lines -- default deny
    {
      assertEquals(ImmutableRangeSet.of(), toRangeSet(AclIpSpace.of()));
    }

    // permit 1 IP
    {
      Ip ip = Ip.parse("1.2.3.4");
      assertEquals(
          toRangeSet(ip.toIpSpace()), toRangeSet(AclIpSpace.permitting(ip.toIpSpace()).build()));
    }

    // permit 2 IPs
    {
      IpSpace ip1 = Ip.parse("1.2.3.4").toIpSpace();
      IpSpace ip2 = Ip.parse("5.6.7.8").toIpSpace();
      assertEquals(
          union(toRangeSet(ip1), toRangeSet(ip2)),
          toRangeSet(AclIpSpace.permitting(ip1).thenPermitting(ip2).build()));
    }

    // 1.2.3.0/24 - 1.2.3.4
    {
      IpSpace prefix = Prefix.parse("1.2.3.0/24").toIpSpace();
      IpSpace ip = Ip.parse("1.2.3.4").toIpSpace();
      assertEquals(
          difference(toRangeSet(prefix), toRangeSet(ip)),
          toRangeSet(AclIpSpace.rejecting(ip).thenPermitting(prefix).build()));
    }

    // 1.2.3.4 - 1.2.3.0/24 (shadowing)
    {
      IpSpace prefix = Prefix.parse("1.2.3.0/24").toIpSpace();
      IpSpace ip = Ip.parse("1.2.3.4").toIpSpace();
      assertEquals(
          ImmutableRangeSet.of(), // empty
          toRangeSet(AclIpSpace.rejecting(prefix).thenPermitting(ip).build()));
    }
  }

  private static RangeSet<Ip> difference(RangeSet<Ip> ips1, RangeSet<Ip> ips2) {
    TreeRangeSet<Ip> result = TreeRangeSet.create();
    result.addAll(ips1);
    result.removeAll(ips2);
    return ImmutableRangeSet.copyOf(result);
  }

  private static RangeSet<Ip> union(RangeSet<Ip> ips1, RangeSet<Ip> ips2) {
    return ImmutableRangeSet.<Ip>builder().addAll(ips1).addAll(ips2).build();
  }
}
