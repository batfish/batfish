package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrefixTest {
  @Test
  public void testCanonicalization() {
    Prefix p = Prefix.parse("255.255.255.255/15");
    assertThat(p.getStartIp(), equalTo(new Ip("255.254.0.0")));
    assertThat(p.getPrefixLength(), equalTo(15));
  }

  @Test
  public void testContains() {
    PrefixIpSpace p = Prefix.parse("1.2.3.4/31").toIpSpace();
    assertThat(p, containsIp(new Ip("1.2.3.4")));
    assertThat(p, containsIp(new Ip("1.2.3.5")));
    assertThat(p, not(containsIp(new Ip("1.2.3.6"))));
    assertThat(p, not(containsIp(new Ip("1.2.3.3"))));

    // Edge cases - 32 bit prefix
    p = Prefix.parse("1.2.3.4/32").toIpSpace();
    assertThat(p, containsIp(new Ip("1.2.3.4")));
    assertThat(p, not(containsIp(new Ip("1.2.3.5"))));
    assertThat(p, not(containsIp(new Ip("1.2.3.3"))));

    // Edge cases - 0 bit prefix
    p = Prefix.parse("0.0.0.0/0").toIpSpace();
    assertThat(p, containsIp(new Ip("0.0.0.0")));
    assertThat(p, containsIp(new Ip("128.128.128.128")));
    assertThat(p, containsIp(new Ip("255.255.255.255")));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace = Prefix.parse("1.2.3.4/31").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("1.2.3.4"))));
    assertThat(ipSpace, not(containsIp(new Ip("1.2.3.5"))));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.6")));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.3")));

    // Edge cases - 32 bit prefix
    ipSpace = Prefix.parse("1.2.3.4/32").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("1.2.3.4"))));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.5")));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.3")));

    // Edge cases - 0 bit prefix
    ipSpace = Prefix.parse("0.0.0.0/0").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("0.0.0.0"))));
    assertThat(ipSpace, not(containsIp(new Ip("128.128.128.128"))));
    assertThat(ipSpace, not(containsIp(new Ip("255.255.255.255"))));
  }
}
