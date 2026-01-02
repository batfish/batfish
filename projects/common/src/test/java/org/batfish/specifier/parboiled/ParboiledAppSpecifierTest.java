package org.batfish.specifier.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledAppSpecifierTest {

  @Test
  public void testResolveIcmpAll() {
    assertThat(
        new ParboiledAppSpecifier(new IcmpAllAppAstNode()).resolve(),
        equalTo(ImmutableSet.of(IcmpTypesApplication.ALL)));
  }

  @Test
  public void testResolveIcmpType() {
    assertThat(
        new ParboiledAppSpecifier(new IcmpTypeAppAstNode(8)).resolve(),
        equalTo(ImmutableSet.of(new IcmpTypesApplication(8))));
  }

  @Test
  public void testResolveIcmpTypeCode() {
    assertThat(
        new ParboiledAppSpecifier(new IcmpTypeCodeAppAstNode(8, 0)).resolve(),
        equalTo(ImmutableSet.of(new IcmpTypeCodesApplication(8, 0))));
  }

  @Test
  public void testResolveRegex() {
    assertThat(
        new ParboiledAppSpecifier(new RegexAppAstNode("htt")).resolve(),
        equalTo(
            Arrays.stream(Protocol.values())
                .filter(
                    protocol ->
                        Pattern.compile("htt", Pattern.CASE_INSENSITIVE)
                            .matcher(protocol.toString())
                            .find())
                .map(Protocol::toApplication)
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  public void testResolveRegex_all() {
    assertThat(
        new ParboiledAppSpecifier(new RegexAppAstNode(".*")).resolve(),
        equalTo(
            Arrays.stream(Protocol.values())
                .map(Protocol::toApplication)
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  public void testResolveName() {
    assertThat(
        new ParboiledAppSpecifier(new NameAppAstNode("http")).resolve(),
        equalTo(ImmutableSet.of(NamedApplication.HTTP.getApplication())));
  }

  @Test
  public void testResolveTcp() {
    assertThat(
        new ParboiledAppSpecifier(new TcpAppAstNode()).resolve(),
        equalTo(ImmutableSet.of(TcpApplication.ALL)));

    List<SubRange> ports = ImmutableList.of(new SubRange(0, 9));
    assertThat(
        new ParboiledAppSpecifier(new TcpAppAstNode(ports)).resolve(),
        equalTo(ImmutableSet.of(new TcpApplication(ports))));
  }

  @Test
  public void testResolveUdp() {
    assertThat(
        new ParboiledAppSpecifier(new UdpAppAstNode()).resolve(),
        equalTo(ImmutableSet.of(UdpApplication.ALL)));

    List<SubRange> ports = ImmutableList.of(new SubRange(0, 9));
    assertThat(
        new ParboiledAppSpecifier(new UdpAppAstNode(ports)).resolve(),
        equalTo(ImmutableSet.of(new UdpApplication(ports))));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledAppSpecifier(new UnionAppAstNode(new IcmpAllAppAstNode(), new TcpAppAstNode()))
            .resolve(),
        equalTo(ImmutableSet.of(IcmpTypesApplication.ALL, TcpApplication.ALL)));
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testParseBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    ParboiledAppSpecifier.parse("connected");
  }

  @Test
  public void testParseGoodInput() {
    assertThat(
        ParboiledAppSpecifier.parse("tcp"),
        equalTo(new ParboiledAppSpecifier(new TcpAppAstNode())));
  }
}
