package org.batfish.vendor.check_point_management;

import static org.batfish.datamodel.acl.TraceElements.permittedByNamedIpSpace;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.addressCpmiAnyTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.addressGroupTraceElement;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.trace.TraceTree;
import org.junit.Test;

/** Test of {@link AddressSpaceToMatchExpr}. */
public final class AddressSpaceToMatchExprTest {
  private static final NatSettings TEST_NAT_SETTINGS =
      new NatSettings(true, "gateway", "All", null, "hide");
  private static final AddressSpaceToIpSpace ADDR_SPACE_TO_IP_SPACE =
      new AddressSpaceToIpSpace(ImmutableMap.of());

  private static final Uid ADDR_RANGE_UID = Uid.of("10");
  private static final String ADDR_RANGE_NAME = "addrRangeName";
  private static final AddressRange ADDR_RANGE =
      new AddressRange(
          Ip.parse("10.0.0.0"),
          Ip.parse("10.0.0.255"),
          null,
          null,
          TEST_NAT_SETTINGS,
          ADDR_RANGE_NAME,
          ADDR_RANGE_UID);

  private static final Uid GATEWAY_UID = Uid.of("11");
  private static final String GATEWAY_NAME = "serverName";
  private static final SimpleGateway GATEWAY =
      new SimpleGateway(
          Ip.parse("10.1.1.1"),
          GATEWAY_NAME,
          ImmutableList.of(),
          new GatewayOrServerPolicy("p1", null),
          GATEWAY_UID);

  private static final Uid HOST1_UID = Uid.of("12");
  private static final String HOST1_NAME = "name1";
  private static final Host HOST1 =
      new Host(Ip.parse("10.2.1.1"), TEST_NAT_SETTINGS, HOST1_NAME, HOST1_UID);

  private static final Uid HOST2_UID = Uid.of("13");
  private static final String HOST2_NAME = "name2";
  private static final Host HOST2 =
      new Host(Ip.parse("10.2.2.1"), TEST_NAT_SETTINGS, HOST2_NAME, HOST2_UID);

  private static final Uid HOST3_UID = Uid.of("14");
  private static final String HOST3_NAME = "name3";
  private static final Host HOST3 =
      new Host(Ip.parse("10.2.3.1"), TEST_NAT_SETTINGS, HOST3_NAME, HOST3_UID);

  private static final Uid NETWORK_UID = Uid.of("15");
  private static final String NETWORK_NAME = "network";
  private static final Network NETWORK =
      new Network(
          NETWORK_NAME,
          TEST_NAT_SETTINGS,
          Ip.parse("10.3.0.0"),
          Ip.parse("255.255.0.0"),
          NETWORK_UID);

  private static final ImmutableMap<String, IpSpace> IP_SPACES =
      ImmutableMap.<String, IpSpace>builder()
          .put(ADDR_RANGE_NAME, ADDR_RANGE.accept(ADDR_SPACE_TO_IP_SPACE))
          .put(GATEWAY_NAME, GATEWAY.accept(ADDR_SPACE_TO_IP_SPACE))
          .put(HOST1_NAME, HOST1.accept(ADDR_SPACE_TO_IP_SPACE))
          .put(HOST2_NAME, HOST2.accept(ADDR_SPACE_TO_IP_SPACE))
          .put(HOST3_NAME, HOST3.accept(ADDR_SPACE_TO_IP_SPACE))
          .put(NETWORK_NAME, NETWORK.accept(ADDR_SPACE_TO_IP_SPACE))
          .build();

  private static final ImmutableMap<String, IpSpaceMetadata> IP_SPACE_METADATA =
      ImmutableMap.<String, IpSpaceMetadata>builder()
          .put(ADDR_RANGE_NAME, AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata(ADDR_RANGE))
          .put(GATEWAY_NAME, AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata(GATEWAY))
          .put(HOST1_NAME, AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata(HOST1))
          .put(HOST2_NAME, AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata(HOST2))
          .put(HOST3_NAME, AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata(HOST3))
          .put(NETWORK_NAME, AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata(NETWORK))
          .build();

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), IP_SPACES);

  private static final Flow TEST_FLOW =
      Flow.builder()
          .setIngressNode("node")
          .setSrcPort(12345)
          .setDstPort(23456)
          .setIpProtocol(IpProtocol.TCP)
          .setIngressInterface("eth1")
          .setSrcIp(Ip.parse("10.0.1.2"))
          .setDstIp(Ip.parse("10.0.2.2"))
          .build();

  @Test
  public void testCpmiAnyObject() {
    AddressSpaceToMatchExpr addrSpaceToMatchExpr = new AddressSpaceToMatchExpr(ImmutableMap.of());
    CpmiAnyObject any = new CpmiAnyObject(Uid.of("1"));
    AclLineMatchExpr dstExpr = addrSpaceToMatchExpr.convertDest(any);
    AclLineMatchExpr srcExpr = addrSpaceToMatchExpr.convertSource(any);

    // Destination
    assertBddsEqual(dstExpr, TrueExpr.INSTANCE);
    List<TraceTree> trace =
        AclTracer.trace(
            dstExpr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(addressCpmiAnyTraceElement(false)));

    // Source
    trace =
        AclTracer.trace(
            srcExpr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertBddsEqual(srcExpr, TrueExpr.INSTANCE);
    assertThat(trace.get(0), isTraceTree(addressCpmiAnyTraceElement(true)));
  }

  @Test
  public void testAddressRange() {
    checkAddrSpaceExprAndSimpleTrace(ADDR_RANGE, ADDR_RANGE_NAME, Ip.parse("10.0.0.1"));
  }

  @Test
  public void testGatewayOrServer() {
    checkAddrSpaceExprAndSimpleTrace(GATEWAY, GATEWAY_NAME, Ip.parse("10.1.1.1"));
  }

  @Test
  public void testHost() {
    checkAddrSpaceExprAndSimpleTrace(HOST1, HOST1_NAME, Ip.parse("10.2.1.1"));
  }

  @Test
  public void testNetwork() {
    checkAddrSpaceExprAndSimpleTrace(NETWORK, NETWORK_NAME, Ip.parse("10.3.1.1"));
  }

  @Test
  public void testGroup() {
    Uid group1Uid = Uid.of("1001");
    Uid group2Uid = Uid.of("1002");
    Uid group3Uid = Uid.of("1003");
    String group1Name = "group1";
    // Contains a loop: group1 -> group2 -> group3 -> group1
    Group group1 = new Group(group1Name, ImmutableList.of(group2Uid, HOST1_UID), group1Uid);
    Group group2 = new Group("group2", ImmutableList.of(group3Uid, HOST2_UID), group2Uid);
    Group group3 = new Group("group3", ImmutableList.of(group1Uid, HOST3_UID), group3Uid);
    // Matches group1 -> group2 -> group3 -> host3
    Ip ip = Ip.parse("10.2.3.1");
    AddressSpaceToMatchExpr addrSpaceToMatchExpr =
        new AddressSpaceToMatchExpr(
            ImmutableMap.<Uid, NamedManagementObject>builder()
                .put(group1Uid, group1)
                .put(group2Uid, group2)
                .put(group3Uid, group3)
                .put(HOST1_UID, HOST1)
                .put(HOST2_UID, HOST2)
                .put(HOST3_UID, HOST3)
                .build());

    AclLineMatchExpr expr = addrSpaceToMatchExpr.convertDest(group1);
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setDstIp(ip).build(),
            "eth1",
            ImmutableMap.of(),
            IP_SPACES,
            IP_SPACE_METADATA);

    assertBddsEqual(
        expr,
        AclLineMatchExprs.matchDst(
            AclIpSpace.union(
                IP_SPACES.get(HOST1_NAME), IP_SPACES.get(HOST2_NAME), IP_SPACES.get(HOST3_NAME))));
    assertThat(
        trace.get(0),
        isTraceTree(
            addressGroupTraceElement(group1, false),
            isTraceTree(
                addressGroupTraceElement(group2, false),
                isTraceTree(
                    addressGroupTraceElement(group3, false),
                    isTraceTree(
                        permittedByNamedIpSpace(
                            ip,
                            "destination IP",
                            IP_SPACE_METADATA.get(HOST3_NAME),
                            HOST3_NAME))))));
  }

  @Test
  public void testGroupInvalidMember() {
    Uid unknown1Uid = Uid.of("1");
    Uid group1Uid = Uid.of("11");
    Group group1 = new Group("group1", ImmutableList.of(unknown1Uid), group1Uid);
    AddressSpaceToMatchExpr addrSpaceToMatchExpr = new AddressSpaceToMatchExpr(ImmutableMap.of());

    AclLineMatchExpr expr = addrSpaceToMatchExpr.convertDest(group1);
    // Group containing only invalid members should not match anything
    assertBddsEqual(expr, FalseExpr.INSTANCE);
  }

  /**
   * Helper to check that the specified {@link AddressSpace}'s expression is equivalent to its
   * {@link IpSpace} representation, and that the appropriate simple trace (non-nested) is attached
   * for a flow the specified destination IP.
   */
  private void checkAddrSpaceExprAndSimpleTrace(AddressSpace space, String name, Ip destIp) {
    AddressSpaceToMatchExpr addrSpaceToMatchExpr = new AddressSpaceToMatchExpr(ImmutableMap.of());

    // Destination
    AclLineMatchExpr dstExpr = addrSpaceToMatchExpr.convertDest(space);
    assertBddsEqual(dstExpr, AclLineMatchExprs.matchDst(IP_SPACES.get(name)));
    List<TraceTree> trace =
        AclTracer.trace(
            dstExpr,
            TEST_FLOW.toBuilder().setDstIp(destIp).build(),
            "eth1",
            ImmutableMap.of(),
            IP_SPACES,
            IP_SPACE_METADATA);

    assertThat(
        trace.get(0),
        isTraceTree(
            permittedByNamedIpSpace(destIp, "destination IP", IP_SPACE_METADATA.get(name), name)));

    // Source
    AclLineMatchExpr srcExpr = addrSpaceToMatchExpr.convertSource(space);
    assertBddsEqual(srcExpr, AclLineMatchExprs.matchSrc(IP_SPACES.get(name)));
    trace =
        AclTracer.trace(
            srcExpr,
            TEST_FLOW.toBuilder().setSrcIp(destIp).build(),
            "eth1",
            ImmutableMap.of(),
            IP_SPACES,
            IP_SPACE_METADATA);

    assertThat(
        trace.get(0),
        isTraceTree(
            permittedByNamedIpSpace(destIp, "source IP", IP_SPACE_METADATA.get(name), name)));
  }

  private void assertBddsEqual(AclLineMatchExpr left, AclLineMatchExpr right) {
    assertThat(_tb.toBDD(left), equalTo(_tb.toBDD(right)));
  }
}
