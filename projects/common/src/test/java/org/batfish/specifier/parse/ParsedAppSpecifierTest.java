package org.batfish.specifier.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.applications.IcmpTypeCodesApplication;
import org.batfish.datamodel.applications.IcmpTypesApplication;
import org.batfish.datamodel.applications.NamedApplication;
import org.batfish.datamodel.applications.TcpApplication;
import org.batfish.datamodel.applications.UdpApplication;
import org.junit.jupiter.api.Test;

class ParsedAppSpecifierTest {

  @Test
  void testResolveIcmpAll() {
    assertThat(
        new ParsedAppSpecifier(new IcmpAllAppAstNode()).resolve(),
        equalTo(ImmutableSet.of(IcmpTypesApplication.ALL)));
  }

  @Test
  void testResolveIcmpType() {
    assertThat(
        new ParsedAppSpecifier(new IcmpTypeAppAstNode(8)).resolve(),
        equalTo(ImmutableSet.of(new IcmpTypesApplication(8))));
  }

  @Test
  void testResolveIcmpTypeCode() {
    assertThat(
        new ParsedAppSpecifier(new IcmpTypeCodeAppAstNode(8, 0)).resolve(),
        equalTo(ImmutableSet.of(new IcmpTypeCodesApplication(8, 0))));
  }

  @Test
  void testResolveRegex() {
    assertThat(
        new ParsedAppSpecifier(new RegexAppAstNode("htt")).resolve(),
        equalTo(
            Arrays.stream(Protocol.values())
                .filter(protocol ->
                    Pattern.compile("htt", Pattern.CASE_INSENSITIVE)
                        .matcher(protocol.toString())
                        .find())
                .map(Protocol::toApplication)
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  void testResolveRegex_all() {
    assertThat(
        new ParsedAppSpecifier(new RegexAppAstNode(".*")).resolve(),
        equalTo(
            Arrays.stream(Protocol.values())
                .map(Protocol::toApplication)
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  void testResolveName() {
    assertThat(
        new ParsedAppSpecifier(new NameAppAstNode("http")).resolve(),
        equalTo(ImmutableSet.of(NamedApplication.HTTP.getApplication())));
  }

  @Test
  void testResolveTcp() {
    assertThat(
        new ParsedAppSpecifier(new TcpAppAstNode()).resolve(),
        equalTo(ImmutableSet.of(TcpApplication.ALL)));

    List<SubRange> ports = ImmutableList.of(new SubRange(0, 9));
    assertThat(
        new ParsedAppSpecifier(new TcpAppAstNode(ports)).resolve(),
        equalTo(ImmutableSet.of(new TcpApplication(ports))));
  }

  @Test
  void testResolveUdp() {
    assertThat(
        new ParsedAppSpecifier(new UdpAppAstNode()).resolve(),
        equalTo(ImmutableSet.of(UdpApplication.ALL)));

    List<SubRange> ports = ImmutableList.of(new SubRange(0, 9));
    assertThat(
        new ParsedAppSpecifier(new UdpAppAstNode(ports)).resolve(),
        equalTo(ImmutableSet.of(new UdpApplication(ports))));
  }

  @Test
  void testResolveUnion() {
    assertThat(
        new ParsedAppSpecifier(new UnionAppAstNode(new IcmpAllAppAstNode(), new TcpAppAstNode()))
            .resolve(),
        equalTo(ImmutableSet.of(IcmpTypesApplication.ALL, TcpApplication.ALL)));
  }

  @Test
  void testParseBadInput() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> ParsedAppSpecifier.parse("connected"));
    assertThat(exception.getMessage(), containsString("Error parsing"));
  }

  @Test
  void testParseGoodInput() {
    assertThat(
        ParsedAppSpecifier.parse("tcp"), equalTo(new ParsedAppSpecifier(new TcpAppAstNode())));
  }
}
