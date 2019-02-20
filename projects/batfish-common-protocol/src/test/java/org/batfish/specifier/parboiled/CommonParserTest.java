package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Parser.initAnchors;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Test;

public class CommonParserTest {

  @Test
  public void testInitAnchors() {
    assertThat(
        initAnchors(TestParser.class),
        equalTo(
            ImmutableMap.of(
                "TestSpecifierInput",
                Type.ADDRESS_GROUP_AND_BOOK,
                "EOI",
                Type.EOI,
                "TestIpAddress",
                Type.IP_ADDRESS,
                "TestIpRange",
                Type.IP_RANGE,
                "WhiteSpace",
                Type.WHITESPACE)));
  }
}
