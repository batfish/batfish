package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.NamesTest.FILTER_INVALID_NAMES;
import static org.batfish.datamodel.NamesTest.FILTER_VALID_NAMES;
import static org.batfish.datamodel.NamesTest.INTERFACE_INVALID_NAMES;
import static org.batfish.datamodel.NamesTest.INTERFACE_VALID_NAMES;
import static org.batfish.datamodel.NamesTest.NODE_INVALID_NAMES;
import static org.batfish.datamodel.NamesTest.NODE_VALID_NAMES;
import static org.batfish.datamodel.NamesTest.REFERENCE_OBJECT_INVALID_NAMES;
import static org.batfish.datamodel.NamesTest.REFERENCE_OBJECT_VALID_NAMES;
import static org.batfish.datamodel.NamesTest.VRF_INVALID_NAMES;
import static org.batfish.datamodel.NamesTest.VRF_VALID_NAMES;
import static org.batfish.datamodel.NamesTest.ZONE_INVALID_NAMES;
import static org.batfish.datamodel.NamesTest.ZONE_VALID_NAMES;
import static org.batfish.specifier.parboiled.Parser.initAnchors;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Test;
import org.parboiled.Rule;
import org.parboiled.parserunners.BasicParseRunner;

public class CommonParserTest {

  private static boolean matches(String query, Rule rule) {
    return new BasicParseRunner<AstNode>(CommonParser.INSTANCE.input(rule)).run(query).matched;
  }

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

  @Test
  public void testFilterNameLiteral() {
    Rule rule = CommonParser.INSTANCE.FilterNameLiteral();

    for (String name : FILTER_VALID_NAMES) {
      assertTrue(name, matches(name, rule));
    }

    for (String name : FILTER_INVALID_NAMES) {
      assertFalse(name, matches(name, rule));
    }
  }

  @Test
  public void testInterfaceNameLiteral() {
    Rule rule = CommonParser.INSTANCE.InterfaceNameLiteral();

    for (String name : INTERFACE_VALID_NAMES) {
      assertTrue(name, matches(name, rule));
    }

    for (String name : INTERFACE_INVALID_NAMES) {
      assertFalse(name, matches(name, rule));
    }
  }

  @Test
  public void testNodeNameLiteral() {
    Rule rule = CommonParser.INSTANCE.NodeNameLiteral();

    for (String name : NODE_VALID_NAMES) {
      assertTrue(name, matches(name, rule));
    }

    for (String name : NODE_INVALID_NAMES) {
      assertFalse(name, matches(name, rule));
    }
  }

  @Test
  public void testReferenceObjectNameLiteral() {
    Rule rule = CommonParser.INSTANCE.ReferenceObjectNameLiteral();

    for (String name : REFERENCE_OBJECT_VALID_NAMES) {
      assertTrue(name, matches(name, rule));
    }

    for (String name : REFERENCE_OBJECT_INVALID_NAMES) {
      assertFalse(name, matches(name, rule));
    }
  }

  @Test
  public void testVrfNameLiteral() {
    Rule rule = CommonParser.INSTANCE.VrfNameLiteral();

    for (String name : VRF_VALID_NAMES) {
      assertTrue(name, matches(name, rule));
    }

    for (String name : VRF_INVALID_NAMES) {
      assertFalse(name, matches(name, rule));
    }
  }

  @Test
  public void testZoneNameLiteral() {
    Rule rule = CommonParser.INSTANCE.ZoneNameLiteral();

    for (String name : ZONE_VALID_NAMES) {
      assertTrue(name, matches(name, rule));
    }

    for (String name : ZONE_INVALID_NAMES) {
      assertFalse(name, matches(name, rule));
    }
  }
}
