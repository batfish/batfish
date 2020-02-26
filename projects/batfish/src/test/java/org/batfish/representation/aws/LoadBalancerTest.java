package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.batfish.datamodel.NamedPort.EPHEMERAL_HIGHEST;
import static org.batfish.datamodel.NamedPort.EPHEMERAL_LOWEST;
import static org.batfish.representation.aws.LoadBalancer.FINAL_TRANSFORMATION;
import static org.batfish.representation.aws.LoadBalancer.LOAD_BALANCER_INTERFACE_DESCRIPTION_PREFIX;
import static org.batfish.representation.aws.LoadBalancer.chainListenerTransformations;
import static org.batfish.representation.aws.LoadBalancer.computeDefaultFilter;
import static org.batfish.representation.aws.LoadBalancer.computeTargetGroupTransformationStep;
import static org.batfish.representation.aws.LoadBalancer.computeTargetTransformationStep;
import static org.batfish.representation.aws.LoadBalancer.getNodeId;
import static org.batfish.representation.aws.LoadBalancer.isValidTarget;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.ApplyAny;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.aws.LoadBalancer.AvailabilityZone;
import org.batfish.representation.aws.LoadBalancer.Protocol;
import org.batfish.representation.aws.LoadBalancer.Scheme;
import org.batfish.representation.aws.LoadBalancer.Type;
import org.batfish.representation.aws.LoadBalancerListener.ActionType;
import org.batfish.representation.aws.LoadBalancerListener.DefaultAction;
import org.batfish.representation.aws.LoadBalancerListener.Listener;
import org.batfish.representation.aws.LoadBalancerTargetHealth.HealthState;
import org.batfish.representation.aws.LoadBalancerTargetHealth.TargetHealth;
import org.batfish.representation.aws.LoadBalancerTargetHealth.TargetHealthDescription;
import org.junit.Test;

public class LoadBalancerTest {

  /** Default Ip to use in tests for the load balancer */
  private static final Ip _loadBalancerIp = Ip.parse("10.10.10.10");

  private static final String _loadBalancerName = "lbName";
  private static final String _loadBalancerArnSuffix = "net/lbName/987654321";
  private static final String _loadBalancerArn =
      String.format(
          "arn:aws:elasticloadbalancing:region:123456789:loadbalancer/%s", _loadBalancerArnSuffix);

  /** Default load balancer object to use in tests */
  private static final LoadBalancer _loadBalancer =
      new LoadBalancer(
          _loadBalancerArn,
          ImmutableList.of(
              new AvailabilityZone("subnet1", "zone1"), new AvailabilityZone("subnet2", "zone2")),
          "lbDnsName",
          _loadBalancerName,
          Scheme.INTERNET_FACING,
          Type.NETWORK,
          "vpc");

  private static Flow getTcpFlow(Ip dstIp) {
    return Flow.builder()
        .setIpProtocol(IpProtocol.TCP)
        .setSrcPort(49876)
        .setDstIp(dstIp)
        .setDstPort(80)
        .setIngressNode("foo")
        .build();
  }

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/LoadBalancerTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /** inactive load balancer should not show */
    assertThat(
        region.getLoadBalancers(),
        equalTo(
            ImmutableMap.of(
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                new LoadBalancer(
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                    ImmutableList.of(
                        new AvailabilityZone("subnet-01822d50b2db5a4a0", "us-east-2a"),
                        new AvailabilityZone("subnet-09f78a95e9df6b959", "us-east-2b")),
                    "lb-lb-6f57a43b75d8f2c1.elb.us-east-2.amazonaws.com",
                    "lb-lb",
                    Scheme.INTERNET_FACING,
                    Type.NETWORK,
                    "vpc-08afc01f5013ddc43"),
                "arn-application",
                new LoadBalancer(
                    "arn-application",
                    ImmutableList.of(),
                    "lb-lb-6f57a43b75d8f2c1.elb.us-east-2.amazonaws.com",
                    "lb3",
                    Scheme.INTERNAL,
                    Type.APPLICATION,
                    "vpc-08afc01f5013ddc43"))));
  }

  /**
   * Test that we create one configuration per availability zone. The content of the configuration
   * is tested in other tests
   */
  @Test
  public void testToConfigurationNodes() {
    List<Configuration> cfgNodes =
        _loadBalancer.toConfigurationNodes(
            new ConvertedConfiguration(), Region.builder("r1").build(), new Warnings());
    assertThat(cfgNodes.size(), equalTo(2));
  }

  @Test
  public void testToConfigurationNodes_skipApplicationLb() {
    List<Configuration> cfgNodes =
        new LoadBalancer(
                "arn",
                ImmutableList.of(
                    new AvailabilityZone("subnet1", "zone1"),
                    new AvailabilityZone("subnet2", "zone2")),
                "lbDnsName",
                "lbName",
                Scheme.INTERNET_FACING,
                Type.APPLICATION,
                "vpc")
            .toConfigurationNodes(
                new ConvertedConfiguration(), Region.builder("r1").build(), new Warnings());
    assertThat(cfgNodes.size(), equalTo(0));
  }

  /** Tests the basics of the configuration object */
  @Test
  public void testToConfigurationNode() {
    AvailabilityZone availabilityZone = _loadBalancer.getAvailabilityZones().get(0);
    Prefix subnetPrefix = Prefix.create(_loadBalancerIp, 24);
    Region region =
        Region.builder("r1")
            .setNetworkInterfaces(
                ImmutableMap.of(
                    "interface",
                    new NetworkInterface(
                        "interface",
                        availabilityZone.getSubnetId(),
                        _loadBalancer.getVpcId(),
                        ImmutableList.of(),
                        ImmutableList.of(new PrivateIpAddress(true, _loadBalancerIp, null)),
                        LOAD_BALANCER_INTERFACE_DESCRIPTION_PREFIX + _loadBalancerArnSuffix,
                        null)))
            .setSubnets(
                ImmutableMap.of(
                    availabilityZone.getSubnetId(),
                    new Subnet(
                        subnetPrefix,
                        availabilityZone.getSubnetId(),
                        _loadBalancer.getVpcId(),
                        availabilityZone.getZoneName())))
            .build();
    Configuration cfgNode =
        _loadBalancer.toConfigurationNode(
            availabilityZone, new ConvertedConfiguration(), region, new Warnings());

    assertThat(
        cfgNode.getHostname(),
        equalTo(
            getNodeId(_loadBalancer.getDnsName(), availabilityZone.getZoneName()).toLowerCase()));
    assertThat(cfgNode.getDeviceModel(), equalTo(DeviceModel.AWS_ELB_NETWORK));
    assertThat(cfgNode.getHumanName(), equalTo(_loadBalancer.getName()));
    assertThat(cfgNode.getVendorFamily().getAws().getVpcId(), equalTo(_loadBalancer.getVpcId()));
    assertThat(
        cfgNode.getVendorFamily().getAws().getSubnetId(), equalTo(availabilityZone.getSubnetId()));
    assertThat(cfgNode.getVendorFamily().getAws().getRegion(), equalTo(region.getName()));

    Interface viIface = getOnlyElement(cfgNode.getAllInterfaces().values().iterator());
    assertThat(
        viIface.getConcreteAddress(),
        equalTo(ConcreteInterfaceAddress.create(_loadBalancerIp, 24)));

    assertThat(viIface.getIncomingTransformation(), equalTo(FINAL_TRANSFORMATION));
    assertThat(
        viIface.getPostTransformationIncomingFilter(),
        equalTo(
            computeDefaultFilter(
                ImmutableList.of(new PrivateIpAddress(true, _loadBalancerIp, null)))));
  }

  @Test
  public void testInstallTransformation() {
    Interface viIface =
        Interface.builder()
            .setName("interface")
            .setAddress(ConcreteInterfaceAddress.create(_loadBalancerIp, 24))
            .build();

    // We create two listeners that are identically configured and test that transformations for
    // both are created.

    TargetGroup targetGroup =
        new TargetGroup(
            "tgArn", ImmutableList.of(), Protocol.TCP, 80, "tgNameGood", TargetGroup.Type.IP);
    DefaultAction action = new DefaultAction(1, "tgArn", ActionType.FORWARD);
    TargetHealthDescription target =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "1.1.1.1", 80),
            new TargetHealth(HealthState.HEALTHY));

    LoadBalancerListener loadBalancerListener =
        new LoadBalancerListener(
            _loadBalancerArn,
            ImmutableList.of(
                new Listener("listener1Arn", ImmutableList.of(action), Protocol.TCP, 80),
                new Listener("listener2Arn", ImmutableList.of(action), Protocol.TCP, 82)));

    Region region =
        Region.builder("r1")
            .setLoadBalancerListeners(
                ImmutableMap.of(loadBalancerListener.getId(), loadBalancerListener))
            .setTargetGroups(ImmutableMap.of(targetGroup.getId(), targetGroup))
            .setLoadBalancerTargetHealths(
                ImmutableMap.of(
                    "tgArn", new LoadBalancerTargetHealth("tgArn", ImmutableList.of(target))))
            .build();

    _loadBalancer.installTransformations(viIface, "zone1", true, region, new Warnings());
    assertThat(
        viIface.getIncomingTransformation(),
        equalTo(
            chainListenerTransformations(
                ImmutableList.of(
                    Objects.requireNonNull(
                        _loadBalancer.computeListenerTransformation(
                            loadBalancerListener.getListeners().get(0),
                            "zone1",
                            _loadBalancerIp,
                            true,
                            region,
                            new Warnings())),
                    Objects.requireNonNull(
                        _loadBalancer.computeListenerTransformation(
                            loadBalancerListener.getListeners().get(1),
                            "zone1",
                            _loadBalancerIp,
                            true,
                            region,
                            new Warnings()))))));
    assertThat(
        viIface.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                false, ImmutableList.of(viIface.getName()), null, null)));
  }

  @Test
  public void testInstallTransformation_noListener() {
    Interface viIface = Interface.builder().setName("interface").build();
    Region region = Region.builder("r1").build();
    _loadBalancer.installTransformations(viIface, "zone1", true, region, new Warnings());
    assertThat(viIface.getIncomingTransformation(), equalTo(FINAL_TRANSFORMATION));
    assertThat(
        viIface.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                false, ImmutableList.of(viIface.getName()), null, null)));
  }

  /** Test that we skip over bad actions and create the right transformation for the good one */
  @Test
  public void testComputeListenerTransformation() {
    DefaultAction actionBad = new DefaultAction(1, "tgArnBad", ActionType.FORWARD);
    TargetGroup targetGroupBad =
        new TargetGroup(
            "tgArnBad", ImmutableList.of(), Protocol.TCP, 80, "tgNameBad", TargetGroup.Type.IP);

    DefaultAction actionGood = new DefaultAction(2, "tgArnGood", ActionType.FORWARD);
    TargetGroup targetGroupGood =
        new TargetGroup(
            "tgArnGood", ImmutableList.of(), Protocol.TCP, 80, "tgNameGood", TargetGroup.Type.IP);

    TargetHealthDescription badTarget =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "6.6.6.6", 80),
            new TargetHealth(HealthState.UNHEALTHY));
    TargetHealthDescription goodTarget =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "1.1.1.1", 80),
            new TargetHealth(HealthState.HEALTHY));

    Listener listener =
        new Listener("listenerArn", ImmutableList.of(actionBad, actionGood), Protocol.TCP, 80);

    Region region =
        Region.builder("r1")
            .setTargetGroups(
                ImmutableMap.of(
                    targetGroupBad.getId(),
                    targetGroupBad,
                    targetGroupGood.getId(),
                    targetGroupGood))
            .setLoadBalancerTargetHealths(
                ImmutableMap.of(
                    "tgBadArn",
                    new LoadBalancerTargetHealth("tgArnBad", ImmutableList.of(badTarget)),
                    "tgArnGood",
                    new LoadBalancerTargetHealth("tgArnGood", ImmutableList.of(goodTarget))))
            .build();

    LoadBalancerTransformation loadBalancerTransformation =
        _loadBalancer.computeListenerTransformation(
            listener, "zone", _loadBalancerIp, true, region, new Warnings());

    assertThat(
        loadBalancerTransformation.getGuard(),
        equalTo(new MatchHeaderSpace(listener.getMatchingHeaderSpace())));

    assertThat(
        loadBalancerTransformation.getStep(),
        equalTo(
            computeTargetGroupTransformationStep(
                "tgArnGood", "zone", _loadBalancerIp, true, region, new Warnings())));
  }

  /** Tests that we return null when no good action is possible */
  @Test
  public void testComputeListenerTransformation_nullReturn() {
    DefaultAction actionBad = new DefaultAction(1, "tgArnBad", ActionType.FORWARD);
    TargetGroup targetGroupBad =
        new TargetGroup(
            "tgArnBad", ImmutableList.of(), Protocol.TCP, 80, "tgNameBad", TargetGroup.Type.IP);
    TargetHealthDescription badTarget =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "6.6.6.6", 80),
            new TargetHealth(HealthState.UNHEALTHY));

    Listener listener = new Listener("listenerArn", ImmutableList.of(actionBad), Protocol.TCP, 80);

    Region region =
        Region.builder("r1")
            .setTargetGroups(ImmutableMap.of(targetGroupBad.getId(), targetGroupBad))
            .setLoadBalancerTargetHealths(
                ImmutableMap.of(
                    "tgBadArn",
                    new LoadBalancerTargetHealth("tgArnBad", ImmutableList.of(badTarget))))
            .build();

    assertThat(
        _loadBalancer.computeListenerTransformation(
            listener, "zone", _loadBalancerIp, true, region, new Warnings()),
        nullValue());
  }

  @Test
  public void testComputeTargetGroupTransformationStep() {
    TargetHealthDescription unhealthyTarget =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "6.6.6.6", 80),
            new TargetHealth(HealthState.UNHEALTHY));
    TargetHealthDescription healthyTarget1 =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "1.1.1.1", 80),
            new TargetHealth(HealthState.HEALTHY));
    TargetHealthDescription healthyTarget2 =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "2.2.2.2", 80),
            new TargetHealth(HealthState.HEALTHY));

    String targetGroupArn = "tgArn";
    TargetGroup targetGroup =
        new TargetGroup(
            targetGroupArn, ImmutableList.of(), Protocol.TCP, 80, "tgName", TargetGroup.Type.IP);
    Region region =
        Region.builder("r1")
            .setTargetGroups(ImmutableMap.of(targetGroupArn, targetGroup))
            .setLoadBalancerTargetHealths(
                ImmutableMap.of(
                    targetGroupArn,
                    new LoadBalancerTargetHealth(
                        targetGroupArn,
                        ImmutableList.of(unhealthyTarget, healthyTarget1, healthyTarget2))))
            .build();

    Ip loadBalancerIp = Ip.parse("10.10.10.10");

    // unhealthy target is ignored should be ignored
    assertThat(
        computeTargetGroupTransformationStep(
            targetGroupArn, "zone", loadBalancerIp, true, region, new Warnings()),
        equalTo(
            new ApplyAny(
                computeTargetTransformationStep(
                    healthyTarget1.getTarget(), TargetGroup.Type.IP, loadBalancerIp, region),
                computeTargetTransformationStep(
                    healthyTarget2.getTarget(), TargetGroup.Type.IP, loadBalancerIp, region))));
  }

  /** Test that we return null if no valid target is found */
  @Test
  public void testComputeTargetGroupTransformationStep_nullReturn() {
    TargetHealthDescription target1 =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "1.1.1.1", 80),
            new TargetHealth(HealthState.UNHEALTHY));
    TargetHealthDescription target2 =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "2.2.2.2", 80),
            new TargetHealth(HealthState.UNHEALTHY));

    String targetGroupArn = "tgArn";
    TargetGroup targetGroup =
        new TargetGroup(
            targetGroupArn, ImmutableList.of(), Protocol.TCP, 80, "tgName", TargetGroup.Type.IP);
    Region region =
        Region.builder("r1")
            .setTargetGroups(ImmutableMap.of(targetGroupArn, targetGroup))
            .setLoadBalancerTargetHealths(
                ImmutableMap.of(
                    targetGroupArn,
                    new LoadBalancerTargetHealth(
                        targetGroupArn, ImmutableList.of(target1, target2))))
            .build();

    Ip loadBalancerIp = Ip.parse("10.10.10.10");

    // unhealthy target is ignored should be ignored
    assertThat(
        computeTargetGroupTransformationStep(
            targetGroupArn, "zone", loadBalancerIp, true, region, new Warnings()),
        nullValue());
  }

  @Test
  public void testComputeTargetTransformationStep_instanceTarget() {
    Ip targetIp = Ip.parse("1.1.1.1");
    Ip loadBalancerIp = Ip.parse("10.10.10.10");
    LoadBalancerTarget target = new LoadBalancerTarget("zone", targetIp.toString(), 80);

    Region region =
        Region.builder("r1")
            .setInstances(
                ImmutableMap.of(
                    "instance",
                    Instance.builder()
                        .setInstanceId("instance")
                        .setPrimaryPrivateIpAddress(targetIp)
                        .build()))
            .build();
    assertThat(
        computeTargetTransformationStep(target, TargetGroup.Type.IP, loadBalancerIp, region),
        equalTo(
            new ApplyAll(
                TransformationStep.assignSourceIp(loadBalancerIp, loadBalancerIp),
                TransformationStep.assignSourcePort(
                    EPHEMERAL_LOWEST.number(), EPHEMERAL_HIGHEST.number()),
                TransformationStep.assignDestinationIp(targetIp, targetIp),
                TransformationStep.assignDestinationPort(target.getPort(), target.getPort()))));
  }

  @Test
  public void testComputeTargetTransformationStep_ipTarget() {
    Ip targetIp = Ip.parse("1.1.1.1");
    Ip loadBalancerIp = Ip.parse("10.10.10.10");
    LoadBalancerTarget target = new LoadBalancerTarget("zone", targetIp.toString(), 80);

    assertThat(
        computeTargetTransformationStep(
            target, TargetGroup.Type.IP, loadBalancerIp, Region.builder("r1").build()),
        equalTo(
            new ApplyAll(
                TransformationStep.assignSourceIp(loadBalancerIp, loadBalancerIp),
                TransformationStep.assignSourcePort(
                    EPHEMERAL_LOWEST.number(), EPHEMERAL_HIGHEST.number()),
                TransformationStep.assignDestinationIp(targetIp, targetIp),
                TransformationStep.assignDestinationPort(target.getPort(), target.getPort()))));
  }

  @Test
  public void testIsValidTarget_unhealthy() {
    assertFalse(
        isValidTarget(
            new TargetHealthDescription(
                new LoadBalancerTarget(null, "id", 80), new TargetHealth(HealthState.UNHEALTHY)),
            new TargetGroup(
                "tgArg", ImmutableList.of(), Protocol.TCP, 80, "tgName", TargetGroup.Type.IP),
            "availabilityZone",
            true,
            Region.builder("r1").build()));
  }

  @Test
  public void testIsValidTarget_instanceTarget() {
    Subnet subnet = new Subnet(Prefix.parse("1.1.1.1/32"), "subnet", "vpc", "targetZone");
    Instance instance =
        Instance.builder().setInstanceId("instance").setSubnetId(subnet.getId()).build();
    TargetGroup targetGroup =
        new TargetGroup(
            "tgArg", ImmutableList.of(), Protocol.TCP, 80, "tgName", TargetGroup.Type.INSTANCE);
    TargetHealthDescription targetHealthDescription =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", instance.getId(), 80),
            new TargetHealth(HealthState.HEALTHY));
    Region region =
        Region.builder("r1")
            .setInstances(ImmutableMap.of(instance.getId(), instance))
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .build();

    // LB in same zone; cross zone load balancing is off
    assertTrue(isValidTarget(targetHealthDescription, targetGroup, "targetZone", false, region));

    // LB in same zone; cross zone load balancing is on
    assertTrue(isValidTarget(targetHealthDescription, targetGroup, "targetZone", true, region));

    // LB in different zone; cross zone load balancing is off
    assertFalse(isValidTarget(targetHealthDescription, targetGroup, "otherZone", false, region));

    // LB in different zone; cross zone load balancing is on
    assertTrue(isValidTarget(targetHealthDescription, targetGroup, "otherZone", true, region));
  }

  @Test
  public void testIsValidTarget_ipTarget() {
    TargetGroup targetGroup =
        new TargetGroup(
            "tgArg", ImmutableList.of(), Protocol.TCP, 80, "tgName", TargetGroup.Type.IP);
    TargetHealthDescription targetHealthDescription =
        new TargetHealthDescription(
            new LoadBalancerTarget("targetZone", "1.1.1.1", 80),
            new TargetHealth(HealthState.HEALTHY));

    // LB in same zone; cross zone load balancing is off
    assertTrue(
        isValidTarget(
            targetHealthDescription,
            targetGroup,
            "targetZone",
            false,
            Region.builder("r1").build()));

    // LB in same zone; cross zone load balancing is on
    assertTrue(
        isValidTarget(
            targetHealthDescription,
            targetGroup,
            "targetZone",
            true,
            Region.builder("r1").build()));

    // LB in different zone; cross zone load balancing is off
    assertFalse(
        isValidTarget(
            targetHealthDescription,
            targetGroup,
            "otherZone",
            false,
            Region.builder("r1").build()));

    // LB in different zone; cross zone load balancing is on
    assertTrue(
        isValidTarget(
            targetHealthDescription, targetGroup, "otherZone", true, Region.builder("r1").build()));

    // Target in zone "all"; cross zone is off
    assertTrue(
        isValidTarget(
            new TargetHealthDescription(
                new LoadBalancerTarget("all", "1.1.1.1", 80),
                new TargetHealth(HealthState.HEALTHY)),
            targetGroup,
            "otherZone",
            false,
            Region.builder("r1").build()));
  }

  @Test
  public void testChainListenerTransformations() {
    assertThat(chainListenerTransformations(ImmutableList.of()), equalTo(FINAL_TRANSFORMATION));

    AclLineMatchExpr matchExpr =
        new MatchHeaderSpace(
            HeaderSpace.builder().setDstIps(Prefix.parse("1.1.1.1/32").toIpSpace()).build());

    assertThat(
        chainListenerTransformations(
            ImmutableList.of(new LoadBalancerTransformation(matchExpr, Noop.NOOP_SOURCE_NAT))),
        equalTo(
            new Transformation(
                matchExpr, ImmutableList.of(Noop.NOOP_SOURCE_NAT), null, FINAL_TRANSFORMATION)));
  }

  @Test
  public void testFindMyInterface() {
    // right description and subnet
    NetworkInterface networkInterface1 =
        new NetworkInterface(
            "id1",
            "subnet1",
            "vpc",
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("1.1.1.1"), null)),
            LOAD_BALANCER_INTERFACE_DESCRIPTION_PREFIX + _loadBalancerArnSuffix,
            null);
    // right description but wrong subnet
    NetworkInterface networkInterface2 =
        new NetworkInterface(
            "id2",
            "subnet2",
            "vpc",
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("1.1.1.1"), null)),
            LOAD_BALANCER_INTERFACE_DESCRIPTION_PREFIX + _loadBalancerArnSuffix,
            null);
    Region region =
        Region.builder("r1")
            .setNetworkInterfaces(
                ImmutableMap.of(
                    networkInterface1.getId(),
                    networkInterface1,
                    networkInterface2.getId(),
                    networkInterface2))
            .build();

    assertThat(
        LoadBalancer.findMyInterface("subnet1", _loadBalancerArn, region),
        equalTo(Optional.of(networkInterface1)));
    assertThat(
        LoadBalancer.findMyInterface("subnet2", _loadBalancerArn, region),
        equalTo(Optional.of(networkInterface2)));
    assertThat(
        LoadBalancer.findMyInterface("nosubnet", _loadBalancerArn, region),
        equalTo(Optional.empty()));
    assertThat(LoadBalancer.findMyInterface("subnet1", "noarn", region), equalTo(Optional.empty()));
  }

  @Test
  public void testComputeDefaultFilter() {
    Ip loadBalancerIp = Ip.parse("10.10.10.10");
    IpAccessList ipAccessList =
        computeDefaultFilter(ImmutableList.of(new PrivateIpAddress(true, loadBalancerIp, null)));

    // not transformed
    assertThat(
        ipAccessList
            .filter(getTcpFlow(loadBalancerIp), null, ImmutableMap.of(), ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));

    // transformed
    assertThat(
        ipAccessList
            .filter(getTcpFlow(Ip.parse("1.1.1.1")), null, ImmutableMap.of(), ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.PERMIT));
  }
}
