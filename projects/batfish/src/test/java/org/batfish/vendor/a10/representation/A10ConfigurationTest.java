package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceEnabledEffective;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceHumanName;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceMtuEffective;
import static org.batfish.vendor.a10.representation.A10Configuration.toDstTransformationSteps;
import static org.batfish.vendor.a10.representation.A10Configuration.toMatchCondition;
import static org.batfish.vendor.a10.representation.A10Configuration.toProtocol;
import static org.batfish.vendor.a10.representation.A10Configuration.toSubRange;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.TransformationStep;
import org.junit.Test;

/** Tests of {@link A10Configuration}. */
public class A10ConfigurationTest {
  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @Test
  public void testGetInterfaceEnabledEffective() {
    Interface ethNullEnabled = new Interface(Interface.Type.ETHERNET, 1);
    Interface loopNullEnabled = new Interface(Interface.Type.LOOPBACK, 1);

    // Defaults
    // Ethernet is disabled by default
    assertFalse(getInterfaceEnabledEffective(ethNullEnabled));
    // Loopback is enabled by default
    assertTrue(getInterfaceEnabledEffective(loopNullEnabled));

    // Explicit enabled value set
    Interface eth = new Interface(Interface.Type.ETHERNET, 1);
    eth.setEnabled(true);
    assertTrue(getInterfaceEnabledEffective(eth));
    eth.setEnabled(false);
    assertFalse(getInterfaceEnabledEffective(eth));
  }

  @Test
  public void testGetInterfaceMtuEffective() {
    Interface eth = new Interface(Interface.Type.ETHERNET, 1);

    assertThat(getInterfaceMtuEffective(eth), equalTo(DEFAULT_MTU));
    eth.setMtu(1234);
    assertThat(getInterfaceMtuEffective(eth), equalTo(1234));
  }

  @Test
  public void testGetInterfaceHumanName() {
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.ETHERNET, 9)), equalTo("Ethernet 9"));
    assertThat(getInterfaceHumanName(new Interface(Interface.Type.TRUNK, 9)), equalTo("Trunk 9"));
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.LOOPBACK, 9)), equalTo("Loopback 9"));
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.VE, 9)), equalTo("VirtualEthernet 9"));
  }

  @Test
  public void testToSubRange() {
    assertThat(
        toSubRange(new VirtualServerPort(80, VirtualServerPort.Type.TCP, null)),
        equalTo(new SubRange(80, 80)));
    assertThat(
        toSubRange(new VirtualServerPort(80, VirtualServerPort.Type.TCP, 0)),
        equalTo(new SubRange(80, 80)));
    assertThat(
        toSubRange(new VirtualServerPort(80, VirtualServerPort.Type.TCP, 10)),
        equalTo(new SubRange(80, 90)));
  }

  @Test
  public void testToProtocol() {
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.TCP, 0)),
        equalTo(IpProtocol.TCP));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.UDP, 0)),
        equalTo(IpProtocol.UDP));
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
}
