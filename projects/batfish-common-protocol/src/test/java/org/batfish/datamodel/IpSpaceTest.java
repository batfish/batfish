package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.hamcrest.FeatureMatcher;
import org.junit.Test;

public class IpSpaceTest {

  private static class Contains extends FeatureMatcher<IpSpace, Boolean> {

    private final Ip _ip;

    public Contains(Ip ip) {
      super(equalTo(true), "contains " + ip, "contains " + ip);
      _ip = ip;
    }

    @Override
    protected Boolean featureValueOf(IpSpace actual) {
      return actual.contains(_ip);
    }
  }

  public static Contains contains(Ip ip) {
    return new Contains(ip);
  }

  @Test
  public void testIpSpace() {
    IpSpace any = IpSpace.builder().including(IpWildcard.ANY).build();
    IpSpace justMax = IpSpace.builder().including(new IpWildcard(Ip.MAX)).build();
    IpSpace anyExceptMax =
        IpSpace.builder().including(IpWildcard.ANY).excluding(new IpWildcard(Ip.MAX)).build();
    IpSpace none1 = IpSpace.builder().build();
    IpSpace none2 = IpSpace.builder().including(IpWildcard.ANY).excluding(IpWildcard.ANY).build();
    IpSpace someButNotMax = IpSpace.builder().including(new IpWildcard("1.2.3.4")).build();

    /*
     * Contains every IP, so should contain Ip.MAX
     */
    assertThat(any, contains(Ip.MAX));

    /*
     * Contains just IP.MAX, so should contain Ip.MAX
     */
    assertThat(justMax, contains(Ip.MAX));

    /*
     * Should not contain Ip.MAX because of explicit blacklist
     */
    assertThat(anyExceptMax, not(contains(Ip.MAX)));

    /*
     * Should not contain Ip.MAX because contains nothing
     */
    assertThat(none1, not(contains(Ip.MAX)));

    /*
     * Should not contain Ip.MAX because of general blacklist
     */
    assertThat(none2, not(contains(Ip.MAX)));

    /*
     * Should not contain Ip.MAX because not in whitelist
     */
    assertThat(someButNotMax, not(contains(Ip.MAX)));
  }
}
