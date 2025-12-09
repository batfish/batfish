package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class Ip6Ip6SpaceTest {

  @Test
  public void testContainsIp() {
    Ip6 ip1 = Ip6.parse("2001:db8::1");
    Ip6 ip2 = Ip6.parse("2001:db8::2");
    Ip6Ip6Space ipSpace = Ip6Ip6Space.create(ip1);

    // Should contain only the specific IP it represents
    assertThat(ipSpace, containsIp6(ip1));

    // Should not contain other IPs
    assertThat(ipSpace, not(containsIp6(ip2)));
    assertThat(ipSpace, not(containsIp6(Ip6.ZERO)));
    assertThat(ipSpace, not(containsIp6(Ip6.MAX)));
  }

  @Test
  public void testCaching() {
    Ip6 ip = Ip6.parse("2001:db8::1");
    Ip6Ip6Space ipSpace1 = Ip6Ip6Space.create(ip);
    Ip6Ip6Space ipSpace2 = Ip6Ip6Space.create(ip);

    // Same IP should return the same object (cached)
    assertThat(ipSpace1, sameInstance(ipSpace2));
  }

  @Test
  public void testSerialization() {
    Ip6Ip6Space original = Ip6Ip6Space.create(Ip6.parse("2001:db8::1"));
    assertThat(BatfishObjectMapper.clone(original, Ip6Space.class), equalTo(original));
    assertThat(SerializationUtils.clone(original), equalTo(original));
  }

  @Test
  public void testEquals() {
    Ip6 ip1 = Ip6.parse("2001:db8::1");
    Ip6 ip2 = Ip6.parse("2001:db8::2");

    new EqualsTester()
        .addEqualityGroup(Ip6Ip6Space.create(ip1), Ip6Ip6Space.create(ip1))
        .addEqualityGroup(Ip6Ip6Space.create(ip2))
        .testEquals();
  }

  @Test
  public void testComplement() {
    Ip6 ip = Ip6.parse("2001:db8::1");
    Ip6Ip6Space ipSpace = Ip6Ip6Space.create(ip);
    Ip6Space complement = ipSpace.complement();

    // Complement should not contain the original IP
    assertThat(complement, not(containsIp6(ip)));

    // Complement should contain other IPs
    assertThat(complement, containsIp6(Ip6.parse("2001:db8::2")));
    assertThat(complement, containsIp6(Ip6.ZERO));
    assertThat(complement, containsIp6(Ip6.MAX));
  }
}
