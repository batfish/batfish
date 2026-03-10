package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class Prefix6Test {
  @Test
  public void testToString() {
    assertThat(Prefix6.ZERO.toString(), equalTo("::/0"));
    assertThat(Prefix6.parse("::/64").toString(), equalTo("::/64"));
    assertThat(Prefix6.parse("0:1:2:3:4:5:6:7/128").toString(), equalTo("0:1:2:3:4:5:6:7/128"));
    // IPv4-compatible IPv6 address (legacy)
    assertThat(Prefix6.parse("::249.10.49.80/124").toString(), equalTo("::249.10.49.80/124"));
    // IPv4-mapped IPv6 address
    assertThat(
        Prefix6.parse("::ffff:249.10.49.80/124").toString(), equalTo("::ffff:249.10.49.80/124"));
  }
}
