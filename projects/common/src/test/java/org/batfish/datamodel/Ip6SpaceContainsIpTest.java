package org.batfish.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;

public class Ip6SpaceContainsIpTest {
  private static final String IP_SPACE_NAME = "ip6Space";
  private static final String DESCRIPTION = "description";
  private static final Map<String, Ip6Space> NAMED_IP_SPACES =
      ImmutableMap.of(IP_SPACE_NAME, Prefix6.parse("1::1/16").toIp6Space());

  private static Ip6SpaceContainsIp containsIp(Ip6 ip6) {
    return new Ip6SpaceContainsIp(ip6, NAMED_IP_SPACES);
  }

  @Test
  public void testVisitIpSpaceReference_true() {
    Ip6 ip1 = Ip6.parse("1:1:1:1::1");
    Ip6 ip2 = Ip6.parse("2:2:2:2::2");
    assertTrue(
        containsIp(ip1).visitIp6SpaceReference(new Ip6SpaceReference(IP_SPACE_NAME, DESCRIPTION)));
    assertFalse(
        containsIp(ip2).visitIp6SpaceReference(new Ip6SpaceReference(IP_SPACE_NAME, DESCRIPTION)));
    assertFalse(
        containsIp(ip1)
            .visitIp6SpaceReference(new Ip6SpaceReference("missing ip6Space", DESCRIPTION)));
  }
}
