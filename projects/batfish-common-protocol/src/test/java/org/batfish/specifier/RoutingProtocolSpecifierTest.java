package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.junit.Test;

/** Tests of {@link RoutingProtocolSpecifier} */
public final class RoutingProtocolSpecifierTest {
  @Test
  public void testCreationDefault() {
    assertThat(
        RoutingProtocolSpecifier.create(null).getProtocols(),
        equalTo(Stream.of(RoutingProtocol.values()).collect(Collectors.toSet())));
  }

  @Test
  public void testCaseInsensitive() {
    assertThat(
        RoutingProtocolSpecifier.create("IGP").getProtocols(),
        equalTo(RoutingProtocolSpecifier.create("iGp").getProtocols()));
  }

  @Test
  public void testTrimming() {
    assertThat(
        RoutingProtocolSpecifier.create("bgp  ,           isis  ").getProtocols(),
        equalTo(
            ImmutableSet.of(
                RoutingProtocol.BGP,
                RoutingProtocol.IBGP,
                RoutingProtocol.ISIS_L1,
                RoutingProtocol.ISIS_L2)));
  }

  @Test
  public void testAggregate() {
    assertThat(
        RoutingProtocolSpecifier.create("aggregate").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.AGGREGATE)));
  }

  @Test
  public void testAll() {
    assertThat(
        RoutingProtocolSpecifier.create("all").getProtocols(),
        equalTo(Stream.of(RoutingProtocol.values()).collect(Collectors.toSet())));
  }

  @Test
  public void testBgp() {
    assertThat(
        RoutingProtocolSpecifier.create("bgp").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.BGP, RoutingProtocol.IBGP)));
  }

  @Test
  public void testConnected() {
    assertThat(
        RoutingProtocolSpecifier.create("connected").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.CONNECTED)));
  }

  @Test
  public void testEbgp() {
    assertThat(
        RoutingProtocolSpecifier.create("ebgp").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.BGP)));
  }

  @Test
  public void testEigrp() {
    assertThat(
        RoutingProtocolSpecifier.create("eigrp").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX)));
  }

  @Test
  public void testEigrpExt() {
    assertThat(
        RoutingProtocolSpecifier.create("eigrp-ext").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.EIGRP_EX)));
  }

  @Test
  public void testEigrpInt() {
    assertThat(
        RoutingProtocolSpecifier.create("eigrp-int").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.EIGRP)));
  }

  @Test
  public void testIbgp() {
    assertThat(
        RoutingProtocolSpecifier.create("ibgp").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.IBGP)));
  }

  @Test
  public void testIgp() {
    assertThat(
        RoutingProtocolSpecifier.create("igp").getProtocols(),
        equalTo(
            ImmutableSet.of(
                RoutingProtocol.OSPF,
                RoutingProtocol.OSPF_IA,
                RoutingProtocol.OSPF_E1,
                RoutingProtocol.OSPF_E2,
                RoutingProtocol.ISIS_L1,
                RoutingProtocol.ISIS_L2,
                RoutingProtocol.EIGRP,
                RoutingProtocol.EIGRP_EX,
                RoutingProtocol.RIP)));
  }

  @Test
  public void testIsis() {
    assertThat(
        RoutingProtocolSpecifier.create("isis").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.ISIS_L1, RoutingProtocol.ISIS_L2)));
  }

  @Test
  public void testIsisL1() {
    assertThat(
        RoutingProtocolSpecifier.create("isis-l1").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.ISIS_L1)));
  }

  @Test
  public void testIsisL2() {
    assertThat(
        RoutingProtocolSpecifier.create("isis-l2").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.ISIS_L2)));
  }

  @Test
  public void testLocal() {
    assertThat(
        RoutingProtocolSpecifier.create("local").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.LOCAL)));
  }

  @Test
  public void testOspf() {
    assertThat(
        RoutingProtocolSpecifier.create("ospf").getProtocols(),
        equalTo(
            ImmutableSet.of(
                RoutingProtocol.OSPF,
                RoutingProtocol.OSPF_IA,
                RoutingProtocol.OSPF_E1,
                RoutingProtocol.OSPF_E2)));
  }

  @Test
  public void testOspfExt() {
    assertThat(
        RoutingProtocolSpecifier.create("ospf-ext").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.OSPF_E1, RoutingProtocol.OSPF_E2)));
  }

  @Test
  public void testOspfExt1() {
    assertThat(
        RoutingProtocolSpecifier.create("ospf-ext1").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.OSPF_E1)));
  }

  @Test
  public void testOspfExt2() {
    assertThat(
        RoutingProtocolSpecifier.create("ospf-ext2").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.OSPF_E2)));
  }

  @Test
  public void testOspfInt() {
    assertThat(
        RoutingProtocolSpecifier.create("ospf-int").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.OSPF, RoutingProtocol.OSPF_IA)));
  }

  @Test
  public void testOspfInter() {
    assertThat(
        RoutingProtocolSpecifier.create("ospf-inter").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.OSPF_IA)));
  }

  @Test
  public void testOspfIntra() {
    assertThat(
        RoutingProtocolSpecifier.create("ospf-intra").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.OSPF)));
  }

  @Test
  public void testRip() {
    assertThat(
        RoutingProtocolSpecifier.create("rip").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.RIP)));
  }

  @Test
  public void testStatic() {
    assertThat(
        RoutingProtocolSpecifier.create("static").getProtocols(),
        equalTo(ImmutableSet.of(RoutingProtocol.STATIC)));
  }

  @Test
  public void testSerialization() throws IOException {
    String serialized =
        BatfishObjectMapper.writePrettyString(RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER);
    assertThat(
        BatfishObjectMapper.mapper().readValue(serialized, RoutingProtocolSpecifier.class),
        equalTo(RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER));
  }

  @Test
  public void testAutocomplete() {
    assertThat(
        RoutingProtocolSpecifier.autoComplete("b")
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(
                RoutingProtocolSpecifier.BGP,
                RoutingProtocolSpecifier.EBGP,
                RoutingProtocolSpecifier.IBGP)));
  }
}
