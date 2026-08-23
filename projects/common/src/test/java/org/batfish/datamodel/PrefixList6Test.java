package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link PrefixList6}. */
public final class PrefixList6Test {

  @Test
  public void testFirstMatchAndImplicitDeny() {
    PrefixList6 prefixList =
        new PrefixList6(
            List.of(
                new PrefixList6.Line(
                    LineAction.DENY,
                    Prefix6.parse(
                        "2001:db8:100::/48"),
                    new SubRange(
                        64, 128)),
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.ZERO,
                    new SubRange(
                        0, 128))));

    assertThat(
        prefixList.permits(
            Prefix6.parse(
                "2001:db8:100:1::/64")),
        equalTo(false));

    assertThat(
        prefixList.permits(
            Prefix6.parse(
                "2001:db8:200:1::/64")),
        equalTo(true));

    PrefixList6 denyAll =
        PrefixList6.denyAll();

    assertThat(
        denyAll.permits(
            Prefix6.parse(
                "2001:db8::/32")),
        equalTo(false));
  }

  @Test
  public void testSerialization() {
    PrefixList6 prefixList =
        new PrefixList6(
            List.of(
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.parse(
                        "2001:db8::/32"),
                    new SubRange(
                        48, 64))));

    PrefixList6 clone =
        BatfishObjectMapper.clone(
            prefixList,
            PrefixList6.class);

    assertThat(
        clone,
        equalTo(prefixList));
  }
}
