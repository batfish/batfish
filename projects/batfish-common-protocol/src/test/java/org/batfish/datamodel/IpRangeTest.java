package org.batfish.datamodel;

import static org.batfish.datamodel.AclIpSpace.union;
import static org.batfish.datamodel.IpRange.range;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@ParametersAreNonnullByDefault
public final class IpRangeTest {

  private static final Map<String, IpSpace> NAMED_IP_SPACES = ImmutableMap.of();

  private static boolean contains(IpSpace space, Ip ip) {
    return space.containsIp(ip, NAMED_IP_SPACES);
  }

  private static boolean equals(IpSpace lhs, IpSpace rhs) {
    IpSpaceToBDD converter = new IpSpaceToBDD(new BDDPacket().getDstIp());
    return lhs.accept(converter).equals(rhs.accept(converter));
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testRange() {
    {
      IpSpace space = range(Ip.ZERO, Ip.MAX);

      assertTrue(contains(space, Ip.ZERO));
      assertTrue(contains(space, Ip.parse("4.23.32.21")));
      assertTrue(contains(space, Ip.parse("5.0.0.0")));
      assertTrue(contains(space, Ip.parse("5.0.0.1")));
      assertTrue(contains(space, Ip.parse("5.0.0.2")));
      assertTrue(contains(space, Ip.MAX));
    }
    {
      IpSpace space = range(Ip.parse("4.23.32.21"), Ip.parse("5.0.0.1"));

      assertFalse(contains(space, Ip.ZERO));
      assertTrue(contains(space, Ip.parse("4.23.32.21")));
      assertTrue(contains(space, Ip.parse("5.0.0.0")));
      assertTrue(contains(space, Ip.parse("5.0.0.1")));
      assertFalse(contains(space, Ip.parse("5.0.0.2")));
      assertFalse(contains(space, Ip.MAX));
    }
    {
      IpSpace space = range(Ip.parse("5.0.0.1"), Ip.parse("5.0.0.1"));

      assertFalse(contains(space, Ip.ZERO));
      assertFalse(contains(space, Ip.parse("4.23.32.21")));
      assertFalse(contains(space, Ip.parse("5.0.0.0")));
      assertTrue(contains(space, Ip.parse("5.0.0.1")));
      assertFalse(contains(space, Ip.parse("5.0.0.2")));
      assertFalse(contains(space, Ip.MAX));
    }
    {
      IpSpace space = range(Ip.parse("0.0.0.1"), Ip.parse("255.255.255.254"));

      assertFalse(contains(space, Ip.ZERO));
      assertTrue(contains(space, Ip.parse("4.23.32.21")));
      assertTrue(contains(space, Ip.parse("5.0.0.0")));
      assertTrue(contains(space, Ip.parse("5.0.0.1")));
      assertTrue(contains(space, Ip.parse("5.0.0.2")));
      assertFalse(contains(space, Ip.MAX));
    }
  }

  @Test
  public void testRangeEquivalence() {
    assertTrue(equals(range(Ip.ZERO, Ip.ZERO), Ip.ZERO.toIpSpace()));
    assertTrue(equals(range(Ip.MAX, Ip.MAX), Ip.MAX.toIpSpace()));
    assertTrue(equals(range(Ip.ZERO, Ip.MAX), UniverseIpSpace.INSTANCE));
    assertTrue(
        equals(
            range(Ip.parse("123.0.0.0"), Ip.parse("123.0.0.255")),
            Prefix.parse("123.0.0.0/24").toIpSpace()));
    assertTrue(
        equals(
            range(Ip.parse("1.255.255.255"), Ip.parse("2.255.255.255")),
            union(Prefix.parse("2.0.0.0/8").toIpSpace(), Ip.parse("1.255.255.255").toIpSpace())));
  }

  @Test
  public void testRangeInvalid() {
    _thrown.expect(IllegalArgumentException.class);
    range(Ip.MAX, Ip.ZERO);
  }
}
