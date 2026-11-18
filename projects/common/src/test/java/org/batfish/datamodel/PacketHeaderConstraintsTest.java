package org.batfish.datamodel;

import static org.batfish.datamodel.IntegerSpace.PORTS;
import static org.batfish.datamodel.IpProtocol.ICMP;
import static org.batfish.datamodel.IpProtocol.IP_PROTOCOLS_WITH_PORTS;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.IpProtocol.UDP;
import static org.batfish.datamodel.PacketHeaderConstraints.areProtocolsAndPortsCompatible;
import static org.batfish.datamodel.PacketHeaderConstraints.isValidDscp;
import static org.batfish.datamodel.PacketHeaderConstraints.isValidEcn;
import static org.batfish.datamodel.PacketHeaderConstraints.isValidIcmpTypeOrCode;
import static org.batfish.datamodel.PacketHeaderConstraints.parseApplicationJsonToString;
import static org.batfish.datamodel.PacketHeaderConstraints.parseApplications;
import static org.batfish.datamodel.PacketHeaderConstraints.parseIpProtocols;
import static org.batfish.datamodel.PacketHeaderConstraints.resolveIpProtocols;
import static org.batfish.datamodel.PacketHeaderConstraints.resolvePorts;
import static org.batfish.datamodel.Protocol.DNS;
import static org.batfish.datamodel.Protocol.HTTP;
import static org.batfish.datamodel.Protocol.SSH;
import static org.batfish.datamodel.Protocol.TELNET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.applications.Application;
import org.batfish.datamodel.applications.IcmpTypesApplication;
import org.batfish.datamodel.applications.TcpApplication;
import org.batfish.datamodel.applications.UdpApplication;
import org.junit.jupiter.api.Test;

/** Tests of {@link org.batfish.datamodel.PacketHeaderConstraints} */
class PacketHeaderConstraintsTest {

  /** Test boundary values for ECN */
  @Test
  void testIsValidEcn() {
    assertThat(isValidEcn(IntegerSpace.of(new SubRange(0, 3))), equalTo(true));
    assertThat(isValidEcn(IntegerSpace.of(SubRange.singleton(1))), equalTo(true));
    assertThat(isValidEcn(IntegerSpace.of(new SubRange(-1, 0))), equalTo(false));
    assertThat(isValidEcn(IntegerSpace.of(new SubRange(2, 4))), equalTo(false));
    assertThat(isValidEcn(IntegerSpace.of(new SubRange(2, 1))), equalTo(false));
  }

  /** Test boundary values for DSCP */
  @Test
  void testIsValidDscp() {
    assertThat(isValidDscp(IntegerSpace.of(new SubRange(0, 63))), equalTo(true));
    assertThat(isValidDscp(IntegerSpace.of(SubRange.singleton(1))), equalTo(true));
    assertThat(isValidDscp(IntegerSpace.of(new SubRange(-1, 0))), equalTo(false));
    assertThat(isValidDscp(IntegerSpace.of(new SubRange(2, 64))), equalTo(false));
    assertThat(isValidDscp(IntegerSpace.of(new SubRange(2, 1))), equalTo(false));
  }

  /** Test boundary values for ICMP fields */
  @Test
  void testIsValidIcmp() {
    assertThat(isValidIcmpTypeOrCode(IntegerSpace.of(new SubRange(0, 255))), equalTo(true));
    assertThat(isValidIcmpTypeOrCode(IntegerSpace.of(new SubRange(1, 2))), equalTo(true));
    assertThat(isValidIcmpTypeOrCode(IntegerSpace.of(new SubRange(-1, 0))), equalTo(false));
    assertThat(isValidIcmpTypeOrCode(IntegerSpace.of(new SubRange(2, 256))), equalTo(false));
    assertThat(isValidIcmpTypeOrCode(IntegerSpace.of(new SubRange(2, 1))), equalTo(false));
  }

  @Test
  void testAreProtocolsAndPortsCompatible() {
    // No conflicts with unset values
    assertThat(areProtocolsAndPortsCompatible(null, null, null), equalTo(true));
    assertThat(
        areProtocolsAndPortsCompatible(null, IntegerSpace.of(new SubRange(1, 1024)), null),
        equalTo(true));
    assertThat(
        areProtocolsAndPortsCompatible(null, null, ImmutableSet.of(SSH.toApplication())),
        equalTo(true));

    // Compatible protocols or missing constraints
    assertThat(
        areProtocolsAndPortsCompatible(
            ImmutableSet.of(TCP),
            IntegerSpace.of(new SubRange(22)),
            ImmutableSet.of(SSH.toApplication())),
        equalTo(true));
    assertThat(
        areProtocolsAndPortsCompatible(
            null,
            IntegerSpace.of(new SubRange(0, 1000)),
            ImmutableSet.of(SSH.toApplication(), HTTP.toApplication())),
        equalTo(true));
  }

  @Test
  void testAreProtocolsAndPortsIncompatibleNoTCP() {
    assertThrows(
        IllegalArgumentException.class,
        () -> // Not TCP and SSH
        areProtocolsAndPortsCompatible(
                ImmutableSet.of(UDP),
                IntegerSpace.of(new SubRange(22)),
                ImmutableSet.of(SSH.toApplication())));
  }

  @Test
  void testAreProtocolsAndPortsIncompatibleEmptyPortRange() {
    assertThrows(
        IllegalArgumentException.class,
        () -> // empty port subrange
        areProtocolsAndPortsCompatible(
                ImmutableSet.of(TCP),
                IntegerSpace.of(new SubRange(20, 1)),
                ImmutableSet.of(SSH.toApplication())));
  }

  @Test
  void testAreProtocolsAndPortsIncompatibleWrongPorts() {
    assertThrows(
        IllegalArgumentException.class,
        () -> // wrong port subrange and application protocols
        areProtocolsAndPortsCompatible(
                ImmutableSet.of(TCP),
                IntegerSpace.of(new SubRange(30, 40)),
                ImmutableSet.of(SSH.toApplication(), HTTP.toApplication())));
  }

  @Test
  void testAreProtocolsAndPortsCompatibleDstportOverlap() {
    // No conflicts with unset values
    assertTrue(
        areProtocolsAndPortsCompatible(
            null, IntegerSpace.of(22), ImmutableSet.of(TcpApplication.ALL)));
  }

  @Test
  void testResolveIpProtocols() {
    assertThat(resolveIpProtocols(null, null, null, null, null, null, null), nullValue());
    assertThat(
        resolveIpProtocols(ImmutableSet.of(TCP), null, null, null, null, null, null),
        equalTo(ImmutableSet.of(TCP)));
    assertThat(
        resolveIpProtocols(
            null, IntegerSpace.of(new SubRange(22, 80)), null, null, null, null, null),
        equalTo(IP_PROTOCOLS_WITH_PORTS));
    assertThat(
        resolveIpProtocols(
            null, null, IntegerSpace.of(new SubRange(22, 80)), null, null, null, null),
        equalTo(IP_PROTOCOLS_WITH_PORTS));
    assertThat(
        resolveIpProtocols(
            null, null, null, ImmutableSet.of(SSH.toApplication()), null, null, null),
        equalTo(ImmutableSet.of(TCP)));
    assertThat(
        resolveIpProtocols(
            null,
            null,
            null,
            null,
            ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG),
            null,
            null),
        equalTo(ImmutableSet.of(TCP)));
  }

  @Test
  void testResolveIpProtocolsTcpAndUdp() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            // both tcp and udp at the same time in src/dst
            resolveIpProtocols(
                ImmutableSet.of(TCP),
                null,
                null,
                ImmutableSet.of(DNS.toApplication()),
                null,
                null,
                null));
  }

  @Test
  void testResolveIpProtocolsIcmpAndPorts() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            resolveIpProtocols(
                ImmutableSet.of(ICMP),
                IntegerSpace.of(SubRange.singleton(10)),
                null,
                null,
                null,
                null,
                null));
  }

  @Test
  void testResolveIpProtocolsIcmpAndTcp() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            resolveIpProtocols(
                ImmutableSet.of(ICMP),
                null,
                null,
                ImmutableSet.of(SSH.toApplication()),
                null,
                null,
                null));
  }

  @Test
  void testResolveIpProtocolsTcpFlagsAndUdp() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            resolveIpProtocols(
                ImmutableSet.of(UDP),
                null,
                null,
                null,
                ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG),
                null,
                null));
  }

  @Test
  void testResolveIpProtocolsTcpFlagsAndDns() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            resolveIpProtocols(
                null,
                null,
                null,
                ImmutableSet.of(new UdpApplication(53)),
                ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG),
                null,
                null));
  }

  @Test
  void testResolveIpProtocolsIcmpTypesAndDns() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            resolveIpProtocols(
                null,
                null,
                null,
                ImmutableSet.of(new UdpApplication(53)),
                ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG),
                IntegerSpace.of(1),
                null));
  }

  @Test
  void testResolveIpProtocolsIcmpCodesAndDns() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            resolveIpProtocols(
                null,
                null,
                null,
                ImmutableSet.of(new UdpApplication(53)),
                ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG),
                null,
                IntegerSpace.of(1)));
  }

  @Test
  void testResolvePorts() {
    final IntegerSpace sshSet = IntegerSpace.of(SubRange.singleton(22));

    assertThat(resolvePorts(null, null), nullValue());

    assertThat(resolvePorts(sshSet, ImmutableSet.of(SSH.toApplication())), equalTo(sshSet));
    assertThat(resolvePorts(null, ImmutableSet.of(SSH.toApplication())), equalTo(sshSet));

    assertThat(
        resolvePorts(IntegerSpace.of(new SubRange(22, 25)), null),
        equalTo(IntegerSpace.of(new SubRange(22, 25))));

    assertThat(
        resolvePorts(
            IntegerSpace.builder()
                .including(new SubRange(1, 10))
                .including(new SubRange(20, 30))
                .including(new SubRange(40, 50))
                .build(),
            ImmutableSet.of(SSH.toApplication(), HTTP.toApplication())),
        equalTo(sshSet));

    assertThat(resolvePorts(null, ImmutableSet.of(IcmpTypesApplication.ALL)), equalTo(null));
  }

  @Test
  void testDefaults() {
    PacketHeaderConstraints constraints = PacketHeaderConstraints.unconstrained();
    assertThat(constraints.getSrcPorts(), nullValue());
    assertThat(constraints.getDscps(), nullValue());
    assertThat(constraints.getEcns(), nullValue());
    assertThat(constraints.getPacketLengths(), nullValue());
    assertThat(constraints.getFragmentOffsets(), nullValue());
    assertThat(constraints.getIpProtocols(), nullValue());
    assertThat(constraints.getSrcIps(), nullValue());
    assertThat(constraints.getDstIps(), nullValue());
    assertThat(constraints.getIcmpCodes(), nullValue());
    assertThat(constraints.getIcmpTypes(), nullValue());
    assertThat(constraints.getSrcPorts(), nullValue());
    assertThat(constraints.getDstPorts(), nullValue());
    assertThat(constraints.getApplications(), nullValue());
    assertThat(constraints.resolveIpProtocols(), nullValue());
    assertThat(constraints.resolveDstPorts(), nullValue());
  }

  @Test
  void testBuilderDefaults() {
    PacketHeaderConstraints constraints = PacketHeaderConstraints.builder().build();
    assertThat(constraints.getSrcPorts(), nullValue());
    assertThat(constraints.getDscps(), nullValue());
    assertThat(constraints.getEcns(), nullValue());
    assertThat(constraints.getPacketLengths(), nullValue());
    assertThat(constraints.getFragmentOffsets(), nullValue());
    assertThat(constraints.getIpProtocols(), nullValue());
    assertThat(constraints.getSrcIps(), nullValue());
    assertThat(constraints.getDstIps(), nullValue());
    assertThat(constraints.getIcmpCodes(), nullValue());
    assertThat(constraints.getIcmpTypes(), nullValue());
    assertThat(constraints.getSrcPorts(), nullValue());
    assertThat(constraints.getDstPorts(), nullValue());
    assertThat(constraints.getApplications(), nullValue());
    assertThat(constraints.resolveIpProtocols(), nullValue());
    assertThat(constraints.resolveDstPorts(), nullValue());
  }

  @Test
  void testValidation() {
    PacketHeaderConstraints constraints;
    // concrete resolution
    constraints = PacketHeaderConstraints.builder().setApplications("ssh").build();
    assertThat(constraints.resolveIpProtocols(), equalTo(ImmutableSet.of(TCP)));
    assertThat(constraints.resolveDstPorts(), equalTo(IntegerSpace.of(SubRange.singleton(22))));

    // Headerspace-like resolution
    constraints = PacketHeaderConstraints.builder().setApplications("ssh, http").build();
    assertThat(constraints.resolveIpProtocols(), equalTo(ImmutableSet.of(TCP)));
    assertThat(
        constraints.resolveDstPorts(),
        equalTo(IntegerSpace.unionOf(SubRange.singleton(22), SubRange.singleton(80))));
  }

  @Test
  void testValidationSrcPortICMP() {
    // Src port incompatibility
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setSrcPorts(IntegerSpace.of(SubRange.singleton(22)))
                .setIpProtocols(ImmutableSet.of(ICMP))
                .build());
  }

  @Test
  void testValidationDstPortICMP() {
    // Dst port incompatibility
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setDstPorts(IntegerSpace.of(SubRange.singleton(22)))
                .setIpProtocols(ImmutableSet.of(ICMP))
                .build());
  }

  @Test
  void testValidationTCPICMPIncompatible() {
    // TCP + ICMP codes incompatibility
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setIpProtocols(ImmutableSet.of(TCP))
                .setIcmpCodes(IntegerSpace.of(SubRange.singleton(1)))
                .build());
  }

  @Test
  void testValidationResolveDstPorts() {
    // dst port resolution
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setIpProtocols(ImmutableSet.of(TCP))
                .setDstPorts(IntegerSpace.of(new SubRange(30, 40)))
                .setApplications("ssh")
                .build());
  }

  @Test
  void testValidationInvalidSrcPort() {
    // Port range too large, src or dst
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setIpProtocols(ImmutableSet.of(TCP))
                .setSrcPorts(IntegerSpace.of(new SubRange(0, 1 << 16)))
                .build());
  }

  @Test
  void testValidationInvalidDstPort() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setIpProtocols(ImmutableSet.of(TCP))
                .setDstPorts(IntegerSpace.of(new SubRange(0, 1 << 16)))
                .build());
  }

  @Test
  void testValidationNoProtocols() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PacketHeaderConstraints.builder().setIpProtocols(ImmutableSet.of()).build());
  }

  @Test
  void testValidationNoSrcPorts() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PacketHeaderConstraints.builder().setSrcPorts(IntegerSpace.EMPTY).build());
  }

  @Test
  void testValidationNoDestPorts() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PacketHeaderConstraints.builder().setDstPorts(IntegerSpace.EMPTY).build());
  }

  @Test
  void testValidationNoIcmpCodes() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PacketHeaderConstraints.builder().setIcmpCodes(IntegerSpace.EMPTY).build());
  }

  @Test
  void testValidationNoIcmpTypes() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PacketHeaderConstraints.builder().setIcmpTypes(IntegerSpace.EMPTY).build());
  }

  @Test
  void testValidationDstProtocolsIncompatible() {
    // Reject empty IP protocol intersections
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setIpProtocols(ImmutableSet.of(ICMP, TCP))
                .setApplications("dns")
                .build());
  }

  @Test
  void testValidationSrcProtocolsIncompatible() {
    // Reject empty port intersections
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PacketHeaderConstraints.builder()
                .setApplications("dns")
                .setDstPorts(IntegerSpace.of(new SubRange(1, 2)))
                .build());
  }

  @Test
  void testCreationExcludedPortsOnly() throws IOException {
    PacketHeaderConstraints phc =
        BatfishObjectMapper.mapper()
            .readValue("{\"dstPorts\": \"!22,!40-50\"}", PacketHeaderConstraints.class);
    assertThat(
        phc.resolveDstPorts(),
        equalTo(
            IntegerSpace.builder()
                .including(PORTS)
                .excluding(Range.singleton(22))
                .excluding(Range.closed(40, 50))
                .build()));
  }

  @Test
  void testParseApplications() throws IOException {
    Set<Application> expected = ImmutableSet.of(SSH.toApplication(), TELNET.toApplication());
    // this is what we expect via pybatfish
    assertThat(
        parseApplications(
            parseApplicationJsonToString(
                BatfishObjectMapper.mapper().readValue("[\"SSH\", \"TELNET\"]", JsonNode.class))),
        equalTo(expected));
    // some lazy clients may send a string like this
    assertThat(
        parseApplications(
            parseApplicationJsonToString(
                BatfishObjectMapper.mapper().readValue("\"SSH, TELNET\"", JsonNode.class))),
        equalTo(expected));
  }

  @Test
  void testParseApplicationsQuotesInString() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            // quoted values in a non-json list
            parseApplications(
                parseApplicationJsonToString(
                    BatfishObjectMapper.mapper().readValue("\"\\\"SSH\\\"\"", JsonNode.class))));
  }

  @Test
  void testParseIpProtocols() throws IOException {
    Set<IpProtocol> expected = ImmutableSet.of(TCP, UDP);
    // this is what we expect via pybatfish
    assertThat(
        parseIpProtocols(
            BatfishObjectMapper.mapper().readValue("[\"TCP\", \"17\"]", JsonNode.class)),
        equalTo(expected));
    // some lazy clients may send a string like this
    assertThat(
        parseIpProtocols(BatfishObjectMapper.mapper().readValue("\"TCP, 17\"", JsonNode.class)),
        equalTo(expected));
  }

  @Test
  void testParseIpProtocolsQuotesInString() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            // quoted values in a non-json list
            parseIpProtocols(
                BatfishObjectMapper.mapper().readValue("\"\\\"TCP\\\"\"", JsonNode.class)));
  }

  @Test
  void testParseIpProtocolsNegation() throws IOException {
    Set<IpProtocol> ipProtocols =
        parseIpProtocols(BatfishObjectMapper.mapper().readValue("\"!ICMP\"", JsonNode.class));
    assertThat(
        ipProtocols,
        equalTo(Sets.difference(ImmutableSet.copyOf(IpProtocol.values()), ImmutableSet.of(ICMP))));
  }

  @Test
  void testStringCreationPorts() throws IOException {
    PacketHeaderConstraints phc =
        BatfishObjectMapper.mapper()
            .readValue("{\"dstPorts\": \"11,!22,!40-50\"}", PacketHeaderConstraints.class);
    assertThat(
        phc.resolveDstPorts(),
        equalTo(IntegerSpace.builder().including(Range.singleton(11)).build()));
  }

  @Test
  void testSerialization() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder()
            .setIpProtocols(ImmutableSet.of(TCP, UDP))
            .setDstIp("1.1.1.1")
            .setDstPorts(IntegerSpace.PORTS)
            .build();

    // test (de)serialization
    PacketHeaderConstraints phcDup = BatfishObjectMapper.clone(phc, PacketHeaderConstraints.class);
    assertThat(phc, equalTo(phcDup));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            PacketHeaderConstraints.unconstrained(), PacketHeaderConstraints.builder().build())
        .addEqualityGroup(PacketHeaderConstraints.builder().setDstIp("1.1.1.1"))
        .addEqualityGroup(
            PacketHeaderConstraints.builder().setSrcIp("2.2.2.2").setDstIp("1.1.1.1").build())
        .addEqualityGroup(
            PacketHeaderConstraints.builder()
                .setTcpFlags(Collections.singleton(TcpFlagsMatchConditions.ACK_TCP_FLAG))
                .build())
        .addEqualityGroup(
            PacketHeaderConstraints.builder()
                .setTcpFlags(Collections.singleton(TcpFlagsMatchConditions.SYN_ACK_TCP_FLAG))
                .build())
        .testEquals();
  }
}
