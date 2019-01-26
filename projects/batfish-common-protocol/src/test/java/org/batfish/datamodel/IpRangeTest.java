package org.batfish.datamodel;

import static org.batfish.datamodel.IpRange.greaterThanOrEqualTo;
import static org.batfish.datamodel.IpRange.range;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@ParametersAreNonnullByDefault
public final class IpRangeTest {

  private static final Map<String, IpSpace> NAMED_IP_SPACES = ImmutableMap.of();

  private static boolean contains(IpSpace space, Ip ip) {
    return space.containsIp(ip, NAMED_IP_SPACES);
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testGreaterThanOrEqualTo() {
    {
      IpSpace space = greaterThanOrEqualTo(Ip.ZERO);

      assertTrue(contains(space, Ip.ZERO));
      assertTrue(contains(space, Ip.parse("4.23.32.21")));
      assertTrue(contains(space, Ip.parse("5.0.0.0")));
      assertTrue(contains(space, Ip.parse("5.0.0.1")));
      assertTrue(contains(space, Ip.parse("5.0.0.2")));
      assertTrue(contains(space, Ip.MAX));
    }
    {
      IpSpace space = greaterThanOrEqualTo(Ip.parse("5.0.0.1"));

      assertFalse(contains(space, Ip.ZERO));
      assertFalse(contains(space, Ip.parse("4.23.32.21")));
      assertFalse(contains(space, Ip.parse("5.0.0.0")));
      assertTrue(contains(space, Ip.parse("5.0.0.1")));
      assertTrue(contains(space, Ip.parse("5.0.0.2")));
      assertTrue(contains(space, Ip.MAX));
    }
    {
      IpSpace space = greaterThanOrEqualTo(Ip.MAX);

      assertFalse(contains(space, Ip.ZERO));
      assertFalse(contains(space, Ip.parse("4.23.32.21")));
      assertFalse(contains(space, Ip.parse("5.0.0.0")));
      assertFalse(contains(space, Ip.parse("5.0.0.1")));
      assertFalse(contains(space, Ip.parse("5.0.0.2")));
      assertTrue(contains(space, Ip.MAX));
    }
  }

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
  }

  @Test
  public void testRangeInvalid() {
    _thrown.expect(IllegalArgumentException.class);
    range(Ip.MAX, Ip.ZERO);
  }
}
