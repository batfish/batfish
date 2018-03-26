package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.matchers.IpSpaceMatchers;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrefixTest {
  public static class PrefixContainsIp extends TypeSafeDiagnosingMatcher<Prefix> {
    private final Ip _ip;

    public PrefixContainsIp(Ip ip) {
      this._ip = ip;
    }

    @Override
    protected boolean matchesSafely(Prefix prefix, Description mismatchDescription) {
      if (prefix.containsIp(_ip)) {
        return true;
      }
      mismatchDescription
          .appendText("prefix ")
          .appendValue(prefix)
          .appendText(" does not contain ")
          .appendValue(_ip);
      return false;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("contains IP address ").appendValue(_ip);
    }
  }

  public static PrefixContainsIp containsIp(Ip ip) {
    return new PrefixContainsIp(ip);
  }

  @Test
  public void testCanonicalization() {
    Prefix p = Prefix.parse("255.255.255.255/15");
    assertThat(p.getStartIp(), equalTo(new Ip("255.254.0.0")));
    assertThat(p.getPrefixLength(), equalTo(15));
  }

  @Test
  public void testContains() {
    Prefix p = Prefix.parse("1.2.3.4/31");
    assertThat(p, containsIp(new Ip("1.2.3.4")));
    assertThat(p, containsIp(new Ip("1.2.3.5")));
    assertThat(p, not(containsIp(new Ip("1.2.3.6"))));
    assertThat(p, not(containsIp(new Ip("1.2.3.3"))));

    // Edge cases - 32 bit prefix
    p = Prefix.parse("1.2.3.4/32");
    assertThat(p, containsIp(new Ip("1.2.3.4")));
    assertThat(p, not(containsIp(new Ip("1.2.3.5"))));
    assertThat(p, not(containsIp(new Ip("1.2.3.3"))));

    // Edge cases - 0 bit prefix
    p = Prefix.parse("0.0.0.0/0");
    assertThat(p, containsIp(new Ip("0.0.0.0")));
    assertThat(p, containsIp(new Ip("128.128.128.128")));
    assertThat(p, containsIp(new Ip("255.255.255.255")));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace = Prefix.parse("1.2.3.4/31").complement();
    assertThat(ipSpace, not(IpSpaceMatchers.containsIp(new Ip("1.2.3.4"))));
    assertThat(ipSpace, not(IpSpaceMatchers.containsIp(new Ip("1.2.3.5"))));
    assertThat(ipSpace, IpSpaceMatchers.containsIp(new Ip("1.2.3.6")));
    assertThat(ipSpace, IpSpaceMatchers.containsIp(new Ip("1.2.3.3")));

    // Edge cases - 32 bit prefix
    ipSpace = Prefix.parse("1.2.3.4/32").complement();
    assertThat(ipSpace, not(IpSpaceMatchers.containsIp(new Ip("1.2.3.4"))));
    assertThat(ipSpace, IpSpaceMatchers.containsIp(new Ip("1.2.3.5")));
    assertThat(ipSpace, IpSpaceMatchers.containsIp(new Ip("1.2.3.3")));

    // Edge cases - 0 bit prefix
    ipSpace = Prefix.parse("0.0.0.0/0").complement();
    assertThat(ipSpace, not(IpSpaceMatchers.containsIp(new Ip("0.0.0.0"))));
    assertThat(ipSpace, not(IpSpaceMatchers.containsIp(new Ip("128.128.128.128"))));
    assertThat(ipSpace, not(IpSpaceMatchers.containsIp(new Ip("255.255.255.255"))));
  }
}
