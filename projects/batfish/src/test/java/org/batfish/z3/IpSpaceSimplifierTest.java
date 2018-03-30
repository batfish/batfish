package org.batfish.z3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

public class IpSpaceSimplifierTest {
  @Test
  public void testVisitAclIpSpace() {}

  @Test
  public void testVisitEmptyIpSpace() {
    assertThat(IpSpaceSimplifier.simplify(EmptyIpSpace.INSTANCE), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testVisitIp() {
    Ip ip = new Ip("1.2.3.4");
    assertThat(IpSpaceSimplifier.simplify(ip), equalTo(ip));
  }

  @Test
  public void testVisitIpWildcard() {
    assertThat(IpSpaceSimplifier.simplify(IpWildcard.ANY), equalTo(UniverseIpSpace.INSTANCE));

    IpWildcard ipWildcard = new IpWildcard(new Ip("1.2.0.5"), new Ip(0xFFFF00FFL));
    assertThat(IpSpaceSimplifier.simplify(ipWildcard), equalTo(ipWildcard));
  }

  @Test
  public void testVisitIpWildcardSetIpSpace() {
    assertThat(
        IpSpaceSimplifier.simplify(IpWildcardSetIpSpace.builder().build()),
        equalTo(EmptyIpSpace.INSTANCE));
    assertThat(
        IpSpaceSimplifier.simplify(
            IpWildcardSetIpSpace.builder().excluding(new IpWildcard("1.2.3.0/24")).build()),
        equalTo(EmptyIpSpace.INSTANCE));

    assertThat(
        IpSpaceSimplifier.simplify(
            IpWildcardSetIpSpace.builder().including(IpWildcard.ANY).build()),
        equalTo(UniverseIpSpace.INSTANCE));
    assertThat(
        IpSpaceSimplifier.simplify(
            IpWildcardSetIpSpace.builder()
                .including(IpWildcard.ANY)
                .excluding(IpWildcard.ANY)
                .build()),
        equalTo(EmptyIpSpace.INSTANCE));

    // whitelisted wildcards that are covered by a blacklisted wildcard are removed
    IpWildcardSetIpSpace ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(new IpWildcard("1.2.1.0/24"), new IpWildcard("2.2.2.2"))
            .excluding(new IpWildcard("1.2.0.0/16"))
            .build();
    IpWildcardSetIpSpace simplifiedIpSpace =
        IpWildcardSetIpSpace.builder().including(new IpWildcard("2.2.2.2")).build();
    assertThat(IpSpaceSimplifier.simplify(ipSpace), equalTo(simplifiedIpSpace));

    // blacklisted wildcards that don't overlap whitelisted wildcards are removed
    ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(new IpWildcard("2.2.2.2"))
            .excluding(new IpWildcard("1.0.0.0/8"))
            .build();
    assertThat(IpSpaceSimplifier.simplify(ipSpace), equalTo(simplifiedIpSpace));
  }

  @Test
  public void testVisitPrefix() {
    assertThat(
        IpSpaceSimplifier.simplify(Prefix.parse("0.0.0.0/0")), equalTo(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testVisitUniverseIpSpace() {
    assertThat(
        IpSpaceSimplifier.simplify(UniverseIpSpace.INSTANCE), equalTo(UniverseIpSpace.INSTANCE));
  }
}
