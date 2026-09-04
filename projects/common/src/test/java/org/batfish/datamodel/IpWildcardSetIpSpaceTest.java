package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class IpWildcardSetIpSpaceTest {
  private final IpSpace _ipSpace =
      IpWildcardSetIpSpace.builder()
          .including(IpWildcard.parse("1.1.1.0/24"), IpWildcard.parse("1.1.2.0/24"))
          .excluding(IpWildcard.parse("1.1.1.1/32"))
          .build();

  @Test
  public void testContainsIp() {
    assertThat(_ipSpace, containsIp(Ip.parse("1.1.1.0")));
    assertThat(_ipSpace, not(containsIp(Ip.parse("1.1.1.1"))));
    assertThat(_ipSpace, containsIp(Ip.parse("1.1.2.0")));
    assertThat(_ipSpace, not(containsIp(Ip.parse("1.1.3.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace notIpSpace = _ipSpace.complement();
    assertThat(notIpSpace, not(containsIp(Ip.parse("1.1.1.0"))));
    assertThat(notIpSpace, containsIp(Ip.parse("1.1.1.1")));
    assertThat(notIpSpace, not(containsIp(Ip.parse("1.1.2.0"))));
    assertThat(notIpSpace, containsIp(Ip.parse("1.1.3.0")));

    IpWildcardSetIpSpace prefix =
        IpWildcardSetIpSpace.builder().including(IpWildcard.parse("1.2.3.0/24")).build();
    IpSpace notPrefix = prefix.complement();
    assertThat(notPrefix, not(containsIp(Ip.parse("1.2.3.4"))));
    assertThat(notPrefix, containsIp(Ip.parse("1.1.1.1")));
    // structural equality
    assertThat(prefix.complement().complement(), equalTo(prefix));

    IpWildcardSetIpSpace actuallyEmpty =
        IpWildcardSetIpSpace.builder().excluding(IpWildcard.parse("1.2.3.0/24")).build();
    assertThat(actuallyEmpty.complement(), is(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testJavaSerializationCompact() {
    IpWildcardSetIpSpace space =
        IpWildcardSetIpSpace.builder()
            .including(
                IpWildcard.parse("1.2.3.4:0.0.0.255"), IpWildcard.create(Ip.parse("0.0.0.0")))
            .excluding(
                IpWildcard.create(Prefix.parse("1.2.3.0/30")),
                IpWildcard.parse("255.255.255.255:255.255.255.255"))
            .build();
    IpWildcardSetIpSpace clone = org.apache.commons.lang3.SerializationUtils.clone(space);
    assertThat(clone, equalTo(space));
    assertThat(clone.getBlacklist(), equalTo(space.getBlacklist()));
    assertThat(clone.getWhitelist(), equalTo(space.getWhitelist()));
    assertThat(
        org.apache.commons.lang3.SerializationUtils.clone(IpWildcardSetIpSpace.builder().build()),
        equalTo(IpWildcardSetIpSpace.builder().build()));
  }
}
