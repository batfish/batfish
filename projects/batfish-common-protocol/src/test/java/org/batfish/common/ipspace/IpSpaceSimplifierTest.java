package org.batfish.common.ipspace;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

public class IpSpaceSimplifierTest {
  private static final Ip IP1234 = new Ip("1.2.3.4");

  private static final IpSpaceSimplifier SIMPLIFIER = new IpSpaceSimplifier(ImmutableMap.of());

  @Test
  public void testVisitAclIpSpace_removeEmptyLines() {
    // simplification should remove EmptyIpSpace lines
    assertThat(
        SIMPLIFIER.simplify(
            AclIpSpace.builder()
                .thenRejecting(EmptyIpSpace.INSTANCE)
                .thenPermitting(IP1234.toIpSpace())
                .thenPermitting(IP1234.toIpSpace())
                .build()),
        equalTo(
            AclIpSpace.builder()
                .thenPermitting(IP1234.toIpSpace())
                .thenPermitting(IP1234.toIpSpace())
                .build()));
  }

  @Test
  public void testVisitAclIpSpace_removeLinesAfterUniverse() {
    // simplification should remove any line after a UniverseIpSpace
    assertThat(
        SIMPLIFIER.simplify(
            AclIpSpace.builder()
                .thenRejecting(IP1234.toIpSpace())
                .thenPermitting(UniverseIpSpace.INSTANCE)
                .thenRejecting(IP1234.toIpSpace())
                .build()),
        equalTo(
            AclIpSpace.builder()
                .thenRejecting(IP1234.toIpSpace())
                .thenPermitting(UniverseIpSpace.INSTANCE)
                .build()));
  }

  @Test
  public void testVisitAclIpSpace_toUniverse() {
    /*
     * If after simplification all lines are permits and the last is universe,
     * then simplify the entire AclIpSpace to UniverseIpSpace.
     */
    assertThat(
        SIMPLIFIER.simplify(
            AclIpSpace.builder()
                .thenPermitting(IP1234.toIpSpace())
                .thenPermitting(IpWildcard.ANY.toIpSpace())
                .thenRejecting(IP1234.toIpSpace())
                .build()),
        equalTo(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testVisitAclIpSpace_toEmpty() {
    assertThat(
        SIMPLIFIER.simplify(AclIpSpace.builder().thenRejecting(IP1234.toIpSpace()).build()),
        equalTo(EmptyIpSpace.INSTANCE));

    assertThat(SIMPLIFIER.simplify(AclIpSpace.builder().build()), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testVisitAclIpSpace_simplifyLines() {
    // simplification should simplify the IpSpaces of each line
    assertThat(
        SIMPLIFIER.simplify(
            AclIpSpace.builder()
                .thenRejecting(IP1234.toIpSpace())
                .thenPermitting(IpWildcard.ANY.toIpSpace())
                .build()),
        equalTo(
            AclIpSpace.builder()
                .thenRejecting(IP1234.toIpSpace())
                .thenPermitting(UniverseIpSpace.INSTANCE)
                .build()));
  }

  @Test
  public void testAclIpSpace_simplifyOneAccept() {
    assertThat(
        SIMPLIFIER.simplify(AclIpSpace.builder().thenPermitting(IP1234.toIpSpace()).build()),
        equalTo(IP1234.toIpSpace()));
  }

  @Test
  public void testVisitEmptyIpSpace() {
    assertThat(SIMPLIFIER.simplify(EmptyIpSpace.INSTANCE), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testVisitIp() {
    assertThat(SIMPLIFIER.simplify(IP1234.toIpSpace()), equalTo(IP1234.toIpSpace()));
  }

  @Test
  public void testVisitIpWildcard() {
    assertThat(SIMPLIFIER.simplify(IpWildcard.ANY.toIpSpace()), equalTo(UniverseIpSpace.INSTANCE));

    IpWildcard ipWildcard = new IpWildcard(new Ip("1.2.0.5"), new Ip(0xFFFF00FFL));
    assertThat(SIMPLIFIER.simplify(ipWildcard.toIpSpace()), equalTo(ipWildcard.toIpSpace()));
  }

  @Test
  public void testVisitIpWildcardSetIpSpace() {
    assertThat(
        SIMPLIFIER.simplify(IpWildcardSetIpSpace.builder().build()),
        equalTo(EmptyIpSpace.INSTANCE));
    assertThat(
        SIMPLIFIER.simplify(
            IpWildcardSetIpSpace.builder().excluding(new IpWildcard("1.2.3.0/24")).build()),
        equalTo(EmptyIpSpace.INSTANCE));

    assertThat(
        SIMPLIFIER.simplify(IpWildcardSetIpSpace.builder().including(IpWildcard.ANY).build()),
        equalTo(UniverseIpSpace.INSTANCE));
    assertThat(
        SIMPLIFIER.simplify(
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
    IpSpace simplifiedIpSpace = new IpWildcard("2.2.2.2").toIpSpace();
    assertThat(SIMPLIFIER.simplify(ipSpace), equalTo(simplifiedIpSpace));

    // blacklisted wildcards that don't overlap whitelisted wildcards are removed
    ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(new IpWildcard("2.2.2.2"))
            .excluding(new IpWildcard("1.0.0.0/8"))
            .build();
    assertThat(SIMPLIFIER.simplify(ipSpace), equalTo(simplifiedIpSpace));
  }

  @Test
  public void testVisitIpWildcardSetIpSpace_whitelistOne() {
    IpWildcard ipWildcard = new IpWildcard("1.2.3.4");
    assertThat(
        SIMPLIFIER.simplify(IpWildcardSetIpSpace.builder().including(ipWildcard).build()),
        equalTo(ipWildcard.toIpSpace()));
  }

  @Test
  public void testVisitPrefix() {
    assertThat(
        SIMPLIFIER.simplify(Prefix.parse("0.0.0.0/0").toIpSpace()),
        equalTo(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testVisitUniverseIpSpace() {
    assertThat(SIMPLIFIER.simplify(UniverseIpSpace.INSTANCE), equalTo(UniverseIpSpace.INSTANCE));
  }
}
