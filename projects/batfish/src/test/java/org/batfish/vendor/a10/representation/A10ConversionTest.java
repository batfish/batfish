package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.A10Configuration.arePortTypesCompatible;
import static org.batfish.vendor.a10.representation.A10Conversion.toDstTransformationSteps;
import static org.batfish.vendor.a10.representation.A10Conversion.toIntegerSpace;
import static org.batfish.vendor.a10.representation.A10Conversion.toMatchCondition;
import static org.batfish.vendor.a10.representation.A10Conversion.toProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.TransformationStep;
import org.junit.Test;

/** Tests of {@link A10Conversion}. */
public class A10ConversionTest {
  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @Test
  public void testToIntegerSpace() {
    // Real ports
    assertThat(
        toIntegerSpace(new ServerPort(80, ServerPort.Type.TCP, null)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new ServerPort(80, ServerPort.Type.TCP, 0)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new ServerPort(80, ServerPort.Type.TCP, 10)),
        equalTo(IntegerSpace.of(new SubRange(80, 90))));

    // Virtual ports
    assertThat(
        toIntegerSpace(new VirtualServerPort(80, VirtualServerPort.Type.TCP, null)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new VirtualServerPort(80, VirtualServerPort.Type.TCP, 0)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new VirtualServerPort(80, VirtualServerPort.Type.TCP, 10)),
        equalTo(IntegerSpace.of(new SubRange(80, 90))));
  }

  @Test
  public void testToProtocol() {
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.HTTP, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.HTTPS, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.TCP, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.TCP_PROXY, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.UDP, 0)),
        equalTo(Optional.of(IpProtocol.UDP)));

    for (VirtualServerPort.Type type : VirtualServerPort.Type.values()) {
      // Should not throw
      toProtocol(new VirtualServerPort(1, type, 0)).get();
    }
  }

  @Test
  public void testToMatchCondition() {
    Ip addr1 = Ip.parse("10.10.10.10");
    assertThat(
        _tb.toBDD(
            toMatchCondition(
                new VirtualServerTargetAddress(addr1),
                new VirtualServerPort(80, VirtualServerPort.Type.TCP, 10),
                VirtualServerTargetToIpSpace.INSTANCE)),
        equalTo(
            _tb.toBDD(
                AclLineMatchExprs.match(
                    HeaderSpace.builder()
                        .setDstIps(addr1.toIpSpace())
                        .setDstPorts(new SubRange(80, 90))
                        .setIpProtocols(IpProtocol.TCP)
                        .build()))));
  }

  @Test
  public void testToDstTransformationSteps() {
    Ip server1Ip = Ip.parse("10.0.0.1");
    Server server1 = new Server("server1", new ServerTargetAddress(server1Ip));
    server1.getOrCreatePort(80, ServerPort.Type.TCP, null);
    server1.getOrCreatePort(90, ServerPort.Type.TCP, 1);
    ServerPort port = server1.getOrCreatePort(100, ServerPort.Type.TCP, null);
    port.setEnable(false);
    // Different protocol (UDP, not TCP)
    server1.getOrCreatePort(80, ServerPort.Type.UDP, 100);

    Ip server2Ip = Ip.parse("10.0.0.2");
    Server server2 = new Server("server2", new ServerTargetAddress(server2Ip));
    server2.getOrCreatePort(80, ServerPort.Type.TCP, null);
    server2.setEnable(false);

    Map<String, Server> servers = ImmutableMap.of("server1", server1, "server2", server2);
    ServiceGroup serviceGroup = new ServiceGroup("serviceGroup", ServerPort.Type.TCP);
    serviceGroup.getOrCreateMember("server1", 80);
    serviceGroup.getOrCreateMember("server1", 90);
    // Port not enabled
    serviceGroup.getOrCreateMember("server1", 100);
    // Server not enabled
    serviceGroup.getOrCreateMember("server2", 80);

    // Transformation steps include all *enabled* service-group member references
    assertThat(
        toDstTransformationSteps(serviceGroup, servers),
        containsInAnyOrder(
            new ApplyAll(
                TransformationStep.assignDestinationPort(80),
                TransformationStep.assignDestinationIp(server1Ip)),
            new ApplyAll(
                TransformationStep.assignDestinationPort(90, 91),
                TransformationStep.assignDestinationIp(server1Ip))));
  }

  @Test
  public void testArePortTypesCompatible() {
    List<VirtualServerPort.Type> tcpCompatibleVirtualTypes =
        ImmutableList.of(
            VirtualServerPort.Type.HTTP,
            VirtualServerPort.Type.HTTPS,
            VirtualServerPort.Type.TCP,
            VirtualServerPort.Type.TCP_PROXY);
    List<VirtualServerPort.Type> udpCompatibleVirtualTypes =
        ImmutableList.of(VirtualServerPort.Type.UDP);

    for (VirtualServerPort.Type typeToCheck : tcpCompatibleVirtualTypes) {
      assertTrue(arePortTypesCompatible(ServerPort.Type.TCP, typeToCheck));
      assertFalse(arePortTypesCompatible(ServerPort.Type.UDP, typeToCheck));
    }
    for (VirtualServerPort.Type typeToCheck : udpCompatibleVirtualTypes) {
      assertFalse(arePortTypesCompatible(ServerPort.Type.TCP, typeToCheck));
      assertTrue(arePortTypesCompatible(ServerPort.Type.UDP, typeToCheck));
    }
  }
}
