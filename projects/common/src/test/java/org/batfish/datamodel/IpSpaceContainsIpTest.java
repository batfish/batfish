package org.batfish.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;

/** Test for {@link IpSpaceContainsIp}. */
public class IpSpaceContainsIpTest {
  private static final String IP_SPACE_NAME = "ipSpace";
  private static final Map<String, IpSpace> NAMED_IP_SPACES =
      ImmutableMap.of(IP_SPACE_NAME, Prefix.parse("1.0.0.0/8").toIpSpace());

  private static IpSpaceContainsIp containsIp(Ip ip) {
    return new IpSpaceContainsIp(ip, NAMED_IP_SPACES);
  }

  @Test
  public void testVisitIpSpaceReference_true() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    assertTrue(containsIp(ip1).visitIpSpaceReference(new IpSpaceReference(IP_SPACE_NAME)));
    assertFalse(containsIp(ip2).visitIpSpaceReference(new IpSpaceReference(IP_SPACE_NAME)));
    assertFalse(containsIp(ip1).visitIpSpaceReference(new IpSpaceReference("missing ipSpace")));
  }
}
