package org.batfish.z3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

public class IpAccessListSpecializerTest {
  private static final IpAccessListSpecializer TRIVIAL_SPECIALIZER =
      new IpAccessListSpecializer(new HeaderSpace());

  private static final IpAccessListSpecializer BLACKLIST_ANY_DST_SPECIALIZER =
      new IpAccessListSpecializer(
          HeaderSpace.builder().setNotDstIps(ImmutableSet.of(IpWildcard.ANY)).build());

  private static final IpAccessListSpecializer BLACKLIST_ANY_SRC_SPECIALIZER =
      new IpAccessListSpecializer(
          HeaderSpace.builder().setNotSrcIps(ImmutableSet.of(IpWildcard.ANY)).build());

  private static final IpAccessListSpecializer WHITELIST_ANY_DST_SPECIALIZER =
      new IpAccessListSpecializer(
          HeaderSpace.builder().setDstIps(ImmutableSet.of(IpWildcard.ANY)).build());

  private static final IpAccessListSpecializer WHITELIST_ANY_SRC_SPECIALIZER =
      new IpAccessListSpecializer(
          HeaderSpace.builder().setSrcIps(ImmutableSet.of(IpWildcard.ANY)).build());

  @Test
  public void testSpecializeIpAccessListLine_singleDst() {
    IpAccessListLine ipAccessListLine =
        IpAccessListLine.accepting()
            .setMatchCondition(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(new IpWildcard("1.2.3.0/24").toIpSpace())
                        .build()))
            .build();

    assertThat(
        TRIVIAL_SPECIALIZER.specialize(ipAccessListLine), equalTo(Optional.of(ipAccessListLine)));
    assertThat(
        BLACKLIST_ANY_DST_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(
            Optional.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder().setDstIps(EmptyIpSpace.INSTANCE).build()))
                    .build())));
    assertThat(
        WHITELIST_ANY_DST_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(Optional.of(ipAccessListLine)));
    assertThat(
        BLACKLIST_ANY_SRC_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(Optional.of(ipAccessListLine)));
    assertThat(
        WHITELIST_ANY_SRC_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(Optional.of(ipAccessListLine)));

    // specialize to a headerspace that whitelists part of the dstIp
    IpAccessListSpecializer specializer =
        new IpAccessListSpecializer(
            HeaderSpace.builder().setDstIps(ImmutableSet.of(new IpWildcard("1.2.3.4"))).build());
    assertThat(
        specializer.specialize(ipAccessListLine),
        equalTo(
            Optional.of(
                IpAccessListLine.accepting()
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder().setDstIps(UniverseIpSpace.INSTANCE).build()))
                    .build())));

    // specialize to a headerspace that blacklists part of the dstIp
    specializer =
        new IpAccessListSpecializer(
            HeaderSpace.builder().setNotDstIps(ImmutableSet.of(new IpWildcard("1.2.3.4"))).build());
    assertThat(specializer.specialize(ipAccessListLine), equalTo(Optional.of(ipAccessListLine)));
  }

  @Test
  public void testSpecializeIpAccessListLine_singleSrc() {
    IpAccessListLine ipAccessListLine =
        IpAccessListLine.acceptingHeaderSpace(
            HeaderSpace.builder().setSrcIps(ImmutableSet.of(new IpWildcard("1.2.3.0/24"))).build());

    assertThat(
        TRIVIAL_SPECIALIZER.specialize(ipAccessListLine), equalTo(Optional.of(ipAccessListLine)));
    assertThat(
        BLACKLIST_ANY_DST_SPECIALIZER.specialize(ipAccessListLine), equalTo(Optional.empty()));
    assertThat(
        WHITELIST_ANY_DST_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(Optional.of(ipAccessListLine)));
    assertThat(
        BLACKLIST_ANY_SRC_SPECIALIZER.specialize(ipAccessListLine), equalTo(Optional.empty()));
    assertThat(
        WHITELIST_ANY_SRC_SPECIALIZER.specialize(ipAccessListLine),
        equalTo(Optional.of(ipAccessListLine)));

    // specialize to a headerspace that whitelists part of the srcIp
    IpAccessListSpecializer specializer =
        new IpAccessListSpecializer(
            HeaderSpace.builder().setSrcIps(ImmutableSet.of(new IpWildcard("1.2.3.4"))).build());
    assertThat(
        specializer.specialize(ipAccessListLine),
        equalTo(Optional.of(IpAccessListLine.ACCEPT_ALL)));

    // specialize to a headerspace that blacklists part of the srcIp
    specializer =
        new IpAccessListSpecializer(
            HeaderSpace.builder().setNotSrcIps(ImmutableSet.of(new IpWildcard("1.2.3.4"))).build());
    assertThat(specializer.specialize(ipAccessListLine), equalTo(Optional.of(ipAccessListLine)));
  }
}
