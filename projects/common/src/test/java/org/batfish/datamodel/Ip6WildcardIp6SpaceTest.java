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

public class Ip6WildcardIp6SpaceTest {

  @Test
  public void testContainsIp() {
    Ip6 ip1 = Ip6.parse("2001:db8::1");
    Ip6 ip2 = Ip6.parse("2001:db8::2");
    Ip6Wildcard wildcard = Ip6Wildcard.parse("2001:db8::1/128");
    Ip6WildcardIp6Space ipSpace = Ip6WildcardIp6Space.create(wildcard);

    // Should contain only the specific IP in the wildcard
    assertThat(ipSpace, containsIp6(ip1));

    // Should not contain other IPs
    assertThat(ipSpace, not(containsIp6(ip2)));
    assertThat(ipSpace, not(containsIp6(Ip6.ZERO)));
    assertThat(ipSpace, not(containsIp6(Ip6.MAX)));
  }

  @Test
  public void testContainsIpRange() {
    Ip6 ip1 = Ip6.parse("2001:db8::0");
    Ip6 ip2 = Ip6.parse("2001:db8::1");
    Ip6 ip3 = Ip6.parse("2001:db8::2");
    Ip6 ip4 = Ip6.parse("2001:db8::ffff");
    Ip6Wildcard wildcard = Ip6Wildcard.parse("2001:db8::/127");
    Ip6WildcardIp6Space ipSpace = Ip6WildcardIp6Space.create(wildcard);

    // Should contain IPs in the /127 range (2001:db8::0 and 2001:db8::1)
    assertThat(ipSpace, containsIp6(ip1));
    assertThat(ipSpace, containsIp6(ip2));

    // Should not contain IPs outside the range
    assertThat(ipSpace, not(containsIp6(ip3)));
    assertThat(ipSpace, not(containsIp6(ip4)));
    assertThat(ipSpace, not(containsIp6(Ip6.ZERO)));
    assertThat(ipSpace, not(containsIp6(Ip6.MAX)));
  }

  @Test
  public void testCaching() {
    Ip6Wildcard wildcard = Ip6Wildcard.parse("2001:db8::1/128");
    Ip6WildcardIp6Space ipSpace1 = Ip6WildcardIp6Space.create(wildcard);
    Ip6WildcardIp6Space ipSpace2 = Ip6WildcardIp6Space.create(wildcard);

    // Same wildcard should return the same object (cached)
    assertThat(ipSpace1, sameInstance(ipSpace2));
  }

  @Test
  public void testSerialization() {
    Ip6WildcardIp6Space original = Ip6WildcardIp6Space.create(Ip6Wildcard.parse("2001:db8::1/128"));
    assertThat(BatfishObjectMapper.clone(original, Ip6Space.class), equalTo(original));
    assertThat(SerializationUtils.clone(original), equalTo(original));
  }

  @Test
  public void testEquals() {
    Ip6Wildcard wildcard1 = Ip6Wildcard.parse("2001:db8::1/128");
    Ip6Wildcard wildcard2 = Ip6Wildcard.parse("2001:db8::2/128");

    new EqualsTester()
        .addEqualityGroup(
            Ip6WildcardIp6Space.create(wildcard1), Ip6WildcardIp6Space.create(wildcard1))
        .addEqualityGroup(Ip6WildcardIp6Space.create(wildcard2))
        .testEquals();
  }

  @Test
  public void testComplement() {
    Ip6Wildcard wildcard = Ip6Wildcard.parse("2001:db8::1/128");
    Ip6WildcardIp6Space ipSpace = Ip6WildcardIp6Space.create(wildcard);
    Ip6Space complement = ipSpace.complement();

    // Complement should not contain the original IP
    assertThat(complement, not(containsIp6(Ip6.parse("2001:db8::1"))));

    // Complement should contain other IPs
    assertThat(complement, containsIp6(Ip6.parse("2001:db8::2")));
    assertThat(complement, containsIp6(Ip6.ZERO));
    assertThat(complement, containsIp6(Ip6.MAX));
  }
}
