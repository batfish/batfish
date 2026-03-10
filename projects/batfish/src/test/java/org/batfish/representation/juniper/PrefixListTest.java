package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public final class PrefixListTest {
  @Test
  public void testToIpSpace() {
    PrefixList pl = new PrefixList("name");
    // v4 empty
    pl.setHasIpv6(false);
    assertThat(pl.toIpSpace(), equalTo(EmptyIpSpace.INSTANCE));
    // v6 empty
    pl.setHasIpv6(true);
    assertThat(pl.toIpSpace(), equalTo(EmptyIpSpace.INSTANCE));
    // v4 only
    pl.getPrefixes().add(Prefix.parse("1.2.3.4/24"));
    pl.setHasIpv6(false);
    assertThat(pl.toIpSpace(), equalTo(Prefix.parse("1.2.3.4/24").toIpSpace()));
    // v6 mixed
    pl.setHasIpv6(true);
    assertThat(pl.toIpSpace(), equalTo(Prefix.parse("1.2.3.4/24").toIpSpace()));
    // v6 only
    pl.setHasIpv6(true);
    pl.getPrefixes().clear();
    assertThat(pl.toIpSpace(), equalTo(EmptyIpSpace.INSTANCE));
    // v4 empty
    pl.setHasIpv6(true);
    pl.getPrefixes().clear();
    assertThat(pl.toIpSpace(), equalTo(EmptyIpSpace.INSTANCE));
  }
}
