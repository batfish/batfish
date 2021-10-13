package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.A10Conversion.DEFAULT_VRRP_A_PREEMPT;
import static org.batfish.vendor.a10.representation.A10Conversion.DEFAULT_VRRP_A_PRIORITY;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIps;
import static org.batfish.vendor.a10.representation.A10Conversion.toDstTransformationSteps;
import static org.batfish.vendor.a10.representation.A10Conversion.toIntegerSpace;
import static org.batfish.vendor.a10.representation.A10Conversion.toMatchCondition;
import static org.batfish.vendor.a10.representation.A10Conversion.toProtocol;
import static org.batfish.vendor.a10.representation.A10Conversion.toVrrpGroupBuilder;
import static org.batfish.vendor.a10.representation.A10Conversion.toVrrpGroups;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.VrrpGroup;
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
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.TCP, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.UDP, 0)),
        equalTo(Optional.of(IpProtocol.UDP)));
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
  public void testGetNatPoolIps() {
    Ip pool0Start = Ip.parse("10.0.0.1");
    Ip pool0End = Ip.parse("10.0.0.3");
    Ip pool1Start = Ip.parse("10.0.1.1");
    Ip pool1End = Ip.parse("10.0.1.2");
    NatPool natPoolVrid0 = new NatPool("pool0", pool0Start, pool0End, 24);
    NatPool natPoolVrid1 = new NatPool("pool1", pool1Start, pool1End, 24);
    natPoolVrid1.setVrid(1);
    List<NatPool> natPools = ImmutableList.of(natPoolVrid0, natPoolVrid1);

    assertThat(
        getNatPoolIps(natPools, 0).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(pool0Start, pool0End, Ip.parse("10.0.0.2")));
    assertThat(
        getNatPoolIps(natPools, 1).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(pool1Start, pool1End));
  }

  @Test
  public void testGetVirtualServerIps() {
    Ip vs0Ip = Ip.parse("10.0.0.1");
    Ip vs1EnabledIp = Ip.parse("10.0.1.1");
    Ip vs1DisabledIp = Ip.parse("10.0.3.1");
    VirtualServer vs0 = new VirtualServer("vs0", new VirtualServerTargetAddress(vs0Ip));
    VirtualServer vs1Enabled =
        new VirtualServer("vs1Enabled", new VirtualServerTargetAddress(vs1EnabledIp));
    vs1Enabled.setVrid(1);
    VirtualServer vs1Disabled =
        new VirtualServer("vs1Disbled", new VirtualServerTargetAddress(vs1DisabledIp));
    vs1Disabled.setVrid(1);
    vs1Disabled.setEnable(false);
    List<VirtualServer> virtualServers = ImmutableList.of(vs0, vs1Enabled, vs1Disabled);

    assertThat(
        getVirtualServerIps(virtualServers, 0).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs0Ip));
    assertThat(
        getVirtualServerIps(virtualServers, 1).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs1EnabledIp));
  }

  @Test
  public void testToVrrpGroupBuilder() {
    Ip ip = Ip.parse("1.1.1.1");
    // null vrid config (must be vrid 0)
    assertThat(
        toVrrpGroupBuilder(null, ImmutableSet.of(ip)).build(),
        equalTo(
            VrrpGroup.builder()
                .setPreempt(DEFAULT_VRRP_A_PREEMPT)
                .setPriority(DEFAULT_VRRP_A_PRIORITY)
                .setVirtualAddresses(ImmutableSet.of(ip))
                .build()));

    // non-null vrid config
    VrrpAVrid vridConfig = new VrrpAVrid();
    vridConfig.setPreemptModeDisable(true);
    vridConfig.getOrCreateBladeParameters().setPriority(5);

    assertThat(
        toVrrpGroupBuilder(vridConfig, ImmutableSet.of(ip)).build(),
        equalTo(
            VrrpGroup.builder()
                .setPreempt(false)
                .setPriority(5)
                .setVirtualAddresses(ImmutableSet.of(ip))
                .build()));
  }

  @Test
  public void testToVrrpGroups() {
    Map<Integer, VrrpGroup.Builder> vrrpGroupBuilders =
        ImmutableMap.of(
            1,
            VrrpGroup.builder()
                .setPriority(5)
                .setPreempt(true)
                .setVirtualAddresses(Ip.parse("1.1.1.1")));
    ConcreteInterfaceAddress sourceAddress =
        ConcreteInterfaceAddress.create(Ip.parse("2.2.2.2"), 24);
    org.batfish.datamodel.Interface iface =
        org.batfish.datamodel.Interface.builder().setName("foo").setAddress(sourceAddress).build();

    assertThat(
        toVrrpGroups(iface, vrrpGroupBuilders),
        equalTo(
            ImmutableMap.of(
                1,
                VrrpGroup.builder()
                    .setPriority(5)
                    .setPreempt(true)
                    .setVirtualAddresses(Ip.parse("1.1.1.1"))
                    .setSourceAddress(sourceAddress)
                    .build())));
  }
}
