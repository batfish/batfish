package org.batfish.datamodel.visitors;

import static org.batfish.datamodel.visitors.IpSpaceRepresentative.getRepresentative;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

public class IpSpaceRepresentativeTest {
  private static final IpWildcard EVEN_IPS =
      new IpWildcard(new Ip("0.0.0.0"), new Ip("255.255.255.254"));

  @Test
  public void testAclIpSpace() {
    IpSpace ipSpace =
        AclIpSpace.builder()
            .thenRejecting(EVEN_IPS.toIpSpace())
            .thenPermitting(Prefix.parse("1.2.3.0/24").toIpSpace())
            .build();
    assertThat(getRepresentative(ipSpace), equalTo(Optional.of(new Ip("1.2.3.1"))));
  }

  @Test
  public void testAclIpSpace_empty() {
    IpSpace ipSpace =
        AclIpSpace.builder()
            .thenRejecting(Prefix.parse("0.0.0.0/32").toIpSpace())
            .thenRejecting(Prefix.parse("0.0.0.1/32").toIpSpace())
            .thenPermitting(Prefix.parse("0.0.0.0/31").toIpSpace())
            .build();
    assertThat(getRepresentative(ipSpace), equalTo(Optional.empty()));
  }

  /**
   * Test that the representative is chosen to prefer 0s. It's a greedy choice, starting with the
   * high-order bits, so of course we may end up with more than the minimal number of 1s.
   */
  @Test
  public void testAclIpSpace_fourIps() {
    Ip ip1 = new Ip("1.0.0.0");
    Ip ip2 = new Ip("0.1.0.0");
    Ip ip3 = new Ip("0.0.1.0");
    Ip ip4 = new Ip("0.0.0.3");
    IpSpace ipSpace =
        AclIpSpace.builder()
            .thenPermitting(ip1.toIpSpace())
            .thenPermitting(ip2.toIpSpace())
            .thenPermitting(ip3.toIpSpace())
            .thenPermitting(ip4.toIpSpace())
            .build();
    assertThat(getRepresentative(ipSpace), equalTo(Optional.of(ip4)));
  }

  @Test
  public void testEmptyIpSpace() {
    assertThat(getRepresentative(EmptyIpSpace.INSTANCE), equalTo(Optional.empty()));
  }

  @Test
  public void testIpIpSpace() {
    Ip ip = new Ip("1.2.3.4");
    assertThat(getRepresentative(ip.toIpSpace()), equalTo(Optional.of(ip)));
  }

  @Test
  public void testIpWildcardIpSpace() {
    Ip ip = new Ip("1.0.2.0");
    Ip mask = new Ip("0.255.0.255");
    IpWildcard wc = new IpWildcard(ip, mask);
    assertThat(getRepresentative(wc.toIpSpace()), equalTo(Optional.of(ip)));
  }

  @Test
  public void testIpWildcardSetIpSpace() {
    IpSpace ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(new IpWildcard("1.2.3.0/24"))
            .excluding(EVEN_IPS)
            .build();
    assertThat(getRepresentative(ipSpace), equalTo(Optional.of(new Ip("1.2.3.1"))));
  }

  @Test
  public void testPrefixIpSpace() {
    Prefix prefix = Prefix.parse("1.2.0.0/16");
    assertThat(getRepresentative(prefix.toIpSpace()), equalTo(Optional.of(prefix.getStartIp())));
  }

  @Test
  public void testUniverseIpSpace() {
    assertThat(
        getRepresentative(UniverseIpSpace.INSTANCE), equalTo(Optional.of(new Ip("0.0.0.0"))));
  }
}
