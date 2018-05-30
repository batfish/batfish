package org.batfish.z3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.matchers.IpSpaceMatchers;
import org.junit.Test;

public class IpSpaceIpSpaceSpecializerTest {

  private static final IpSpaceIpSpaceSpecializer universeSpecializer =
      new IpSpaceIpSpaceSpecializer(UniverseIpSpace.INSTANCE, ImmutableMap.of());

  private static final IpSpaceIpSpaceSpecializer emptySpecializer =
      new IpSpaceIpSpaceSpecializer(EmptyIpSpace.INSTANCE, ImmutableMap.of());

  private static final IpSpaceIpSpaceSpecializer whitelistAnySpecializer =
      new IpSpaceIpSpaceSpecializer(
          IpWildcardSetIpSpace.builder().including(ImmutableSortedSet.of(IpWildcard.ANY)).build(),
          ImmutableMap.of());

  private static final IpSpaceIpSpaceSpecializer blacklistAnySpecializer =
      new IpSpaceIpSpaceSpecializer(
          IpWildcardSetIpSpace.builder().excluding(ImmutableSortedSet.of(IpWildcard.ANY)).build(),
          ImmutableMap.of());

  @Test
  public void testSpecializeAclIpSpace() {
    AclIpSpace ipSpace =
        AclIpSpace.builder()
            .thenPermitting(Prefix.parse("0.0.1.0/24").toIpSpace())
            .thenRejecting(Prefix.parse("0.0.1.4/30").toIpSpace())
            .thenPermitting(Prefix.parse("0.0.1.6/31").toIpSpace())
            .build();

    // without simplification
    assertThat(universeSpecializer.visitAclIpSpace(ipSpace), equalTo(ipSpace));
    assertThat(emptySpecializer.visitAclIpSpace(ipSpace), equalTo(EmptyIpSpace.INSTANCE));
    assertThat(whitelistAnySpecializer.visitAclIpSpace(ipSpace), equalTo(ipSpace));
    assertThat(blacklistAnySpecializer.visitAclIpSpace(ipSpace), equalTo(EmptyIpSpace.INSTANCE));

    // with simplification
    assertThat(universeSpecializer.specialize(ipSpace), equalTo(ipSpace));
    assertThat(emptySpecializer.specialize(ipSpace), equalTo(EmptyIpSpace.INSTANCE));
    assertThat(whitelistAnySpecializer.specialize(ipSpace), equalTo(ipSpace));
    assertThat(blacklistAnySpecializer.specialize(ipSpace), equalTo(EmptyIpSpace.INSTANCE));

    // headerspace is contained in all lines
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(new IpWildcard("0.0.1.6/32").toIpSpace(), ImmutableMap.of());
    assertThat(
        specializer.visitAclIpSpace(ipSpace),
        equalTo(
            AclIpSpace.builder()
                .thenPermitting(UniverseIpSpace.INSTANCE)
                .thenRejecting(UniverseIpSpace.INSTANCE)
                .thenPermitting(UniverseIpSpace.INSTANCE)
                .build()));

    // headerspace is outside of all lines
    specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.1.1.1/32")).build(),
            ImmutableMap.of());
    assertThat(specializer.visitAclIpSpace(ipSpace), equalTo(EmptyIpSpace.INSTANCE));

    // not contained in any line, and mayIntersect the first only
    IpWildcard specializerWildcard = new IpWildcard(new Ip(0x00000100L), new Ip(0xFF000000L));
    assertThat(specializerWildcard, not(IpSpaceMatchers.subsetOf(new IpWildcard("0.0.1.0/24"))));
    assertThat(specializerWildcard, not(IpSpaceMatchers.supersetOf(new IpWildcard("0.0.1.0/24"))));
    assertThat(specializerWildcard, IpSpaceMatchers.intersects(new IpWildcard("0.0.1.0/24")));

    specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(specializerWildcard).build(),
            ImmutableMap.of());
    assertThat(
        specializer.visitAclIpSpace(ipSpace),
        equalTo(
            AclIpSpace.builder().thenPermitting(Prefix.parse("0.0.1.0/24").toIpSpace()).build()));
  }

  @Test
  public void testSpecializeIp() {
    IpSpace ipSpace = new Ip("1.1.1.1").toIpSpace();
    assertThat(ipSpace.accept(universeSpecializer), equalTo(ipSpace));
    assertThat(ipSpace.accept(whitelistAnySpecializer), equalTo(ipSpace));
    assertThat(ipSpace.accept(blacklistAnySpecializer), equalTo(EmptyIpSpace.INSTANCE));
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().excluding(new IpWildcard("1.1.1.0/24")).build(),
            ImmutableMap.of());
    assertThat(ipSpace.accept(specializer), equalTo(EmptyIpSpace.INSTANCE));

    // blacklist takes priority
    specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder()
                .including(new IpWildcard("1.1.1.0/24"))
                .excluding(new IpWildcard("1.1.1.0/30"))
                .build(),
            ImmutableMap.of());
    assertThat(ipSpace.accept(specializer), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testSpecializeIpWildcard() {
    IpSpace ipSpace = new IpWildcard(new Ip(0x01010001L), new Ip(0x0000FF00L)).toIpSpace();
    assertThat(ipSpace.accept(universeSpecializer), equalTo(ipSpace));
    assertThat(ipSpace.accept(whitelistAnySpecializer), equalTo(ipSpace));
    assertThat(ipSpace.accept(blacklistAnySpecializer), equalTo(EmptyIpSpace.INSTANCE));

    IpSpace anyIpSpace = IpWildcard.ANY.toIpSpace();
    assertThat(anyIpSpace.accept(universeSpecializer), equalTo(UniverseIpSpace.INSTANCE));
    assertThat(anyIpSpace.accept(whitelistAnySpecializer), equalTo(UniverseIpSpace.INSTANCE));
    assertThat(anyIpSpace.accept(blacklistAnySpecializer), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testSpecializeIpWildcard_supersetOfWhitelisted() {
    IpWildcard ip = new IpWildcard("1.2.0.0/16");
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.2.3.0/24")).build(),
            ImmutableMap.of());
    assertThat(specializer.specialize(ip), equalTo(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testSpecializeIpWildcard_subsetOfWhitelisted() {
    IpSpace ipSpace = new IpWildcard("1.2.3.0/24").toIpSpace();
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.2.0.0/16")).build(),
            ImmutableMap.of());
    assertThat(specializer.specialize(ipSpace), equalTo(ipSpace));
  }

  @Test
  public void testSpecializeIpWildcard_subsetOfWhitelisted_intersectBlacklist() {
    IpSpace ipSpace = new IpWildcard("1.2.3.0/24").toIpSpace();
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder()
                .including(new IpWildcard("1.2.0.0/16"))
                .excluding(new IpWildcard("1.2.3.4"))
                .build(),
            ImmutableMap.of());
    assertThat(specializer.specialize(ipSpace), equalTo(ipSpace));
  }

  @Test
  public void testSpecializeIpWildcardSetIpSpace() {
    IpWildcardSetIpSpace ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(new IpWildcard("1.1.1.0/24"))
            .including(new IpWildcard("1.2.0.0/24"))
            .excluding(new IpWildcard("1.1.1.4/30"))
            .build();
    assertThat(universeSpecializer.visitIpWildcardSetIpSpace(ipSpace), equalTo(ipSpace));
    assertThat(whitelistAnySpecializer.visitIpWildcardSetIpSpace(ipSpace), equalTo(ipSpace));
    assertThat(
        blacklistAnySpecializer.visitIpWildcardSetIpSpace(ipSpace), equalTo(EmptyIpSpace.INSTANCE));

    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.1.1.0/24")).build(),
            ImmutableMap.of());
    assertThat(
        specializer.visitIpWildcardSetIpSpace(ipSpace),
        equalTo(
            IpWildcardSetIpSpace.builder()
                .including(IpWildcard.ANY)
                .excluding(new IpWildcard("1.1.1.4/30"))
                .build()));

    /*
     * Entire headerspace is contained in ipSpace, to specialize to UniverseIpSpace
     */
    specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.2.0.0/30")).build(),
            ImmutableMap.of());
    assertThat(specializer.visitIpWildcardSetIpSpace(ipSpace), equalTo(UniverseIpSpace.INSTANCE));

    /*
     * ipSpace containsIp only a portion of the headerspace, but we can remove parts of ipSpace
     * that are irrelevant.
     */
    specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.2.0.0/16")).build(),
            ImmutableMap.of());
    assertThat(
        specializer.visitIpWildcardSetIpSpace(ipSpace),
        equalTo(IpWildcardSetIpSpace.builder().including(new IpWildcard("1.2.0.0/24")).build()));
  }

  /**
   * If specialize ipWildcard is a subset of IpSpace blacklist is a subset of IpSpace whitelist,
   * return EmptyIpSpace
   */
  @Test
  public void testSpecializeIpWildcardSetIpSpace_specializerWhitelistInBlackAndWhiteLists() {
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.1.1.0/24")).build(),
            ImmutableMap.of());

    assertThat(
        specializer.specialize(
            IpWildcardSetIpSpace.builder()
                .including(new IpWildcard("1.0.0.0/8"))
                .excluding(new IpWildcard("1.1.0.0/16"))
                .build()),
        equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testSpecializeIpWildcardSetIpSpace_blacklistAll() {
    /*
     * Entire headerspace is blacklisted
     */
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().excluding(IpWildcard.ANY).build(), ImmutableMap.of());
    assertThat(
        specializer.specialize(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.2.3.0/24")).build()),
        equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testSpecializePrefix() {
    IpSpace ipSpace = Prefix.parse("1.1.1.0/24").toIpSpace();
    assertThat(ipSpace.accept(universeSpecializer), equalTo(ipSpace));
    assertThat(ipSpace.accept(whitelistAnySpecializer), equalTo(ipSpace));
    assertThat(ipSpace.accept(blacklistAnySpecializer), equalTo(EmptyIpSpace.INSTANCE));

    /*
     * All headerspace Ips are matched by the IpSpace, so it specializes to the universe.
     */
    IpSpaceIpSpaceSpecializer specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder().including(new IpWildcard("1.1.1.4/30")).build(),
            ImmutableMap.of());
    assertThat(ipSpace.accept(specializer), equalTo(UniverseIpSpace.INSTANCE));

    specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder()
                .including(new IpWildcard("1.0.0.0/8"))
                .excluding(new IpWildcard("1.1.0.0/16"))
                .build(),
            ImmutableMap.of());
    assertThat(ipSpace.accept(specializer), equalTo(EmptyIpSpace.INSTANCE));

    specializer =
        new IpSpaceIpSpaceSpecializer(
            IpWildcardSetIpSpace.builder()
                .including(new IpWildcard("1.0.0.0/8"))
                .excluding(new IpWildcard("1.1.1.1/32"))
                .build(),
            ImmutableMap.of());
    assertThat(ipSpace.accept(specializer), equalTo(ipSpace));
  }
}
