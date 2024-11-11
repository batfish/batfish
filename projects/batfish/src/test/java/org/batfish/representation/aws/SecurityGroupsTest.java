package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.IpProtocol.ICMP;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.matchers.AclLineMatchers.isExprAclLineThat;
import static org.batfish.datamodel.matchers.ExprAclLineMatchers.hasMatchCondition;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SECURITY_GROUPS;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForIcmpCode;
import static org.batfish.representation.aws.Utils.traceElementForIcmpType;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.aws.IpPermissions.AddressType;
import org.batfish.representation.aws.IpPermissions.IpRange;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link SecurityGroup}. */
public class SecurityGroupsTest {

  private List<SecurityGroup> _securityGroups;
  private Flow.Builder _flowBuilder;
  private Region _region;
  private Warnings _warnings;

  public static String TEST_ACL = "test_acl";
  private static final AclLineMatchExpr matchIp =
      matchSrc(
          Ip.parse("1.2.3.4").toIpSpace(),
          traceElementForAddress("source", "1.2.3.4/32", AddressType.CIDR_IP));

  private static final AclLineMatchExpr matchUniverse =
      matchSrc(
          UniverseIpSpace.INSTANCE,
          traceElementForAddress("source", "0.0.0.0/0", AddressType.CIDR_IP));

  private static final MatchHeaderSpace matchTcp =
      new MatchHeaderSpace(
          HeaderSpace.builder().setIpProtocols(TCP).build(), traceElementForProtocol(TCP));

  private static final MatchHeaderSpace matchIcmp =
      new MatchHeaderSpace(
          HeaderSpace.builder().setIpProtocols(ICMP).build(), traceElementForProtocol(ICMP));

  private static MatchHeaderSpace matchPorts(int fromPort, int toPort) {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setDstPorts(new SubRange(fromPort, toPort)).build(),
        traceElementForDstPorts(fromPort, toPort));
  }

  @Before
  public void setup() throws IOException {
    JsonNode json =
        BatfishObjectMapper.mapper()
            .readTree(readResource("org/batfish/representation/aws/SecurityGroupTest.json", UTF_8));
    _securityGroups =
        BatfishObjectMapper.mapper()
            .convertValue(
                json.get(JSON_KEY_SECURITY_GROUPS), new TypeReference<List<SecurityGroup>>() {});

    _region = new Region("test");
    _flowBuilder = Flow.builder().setIngressNode("foo").setIpProtocol(IpProtocol.TCP);
    _warnings = new Warnings(true, true, true);
  }

  @Test
  public void testDeser() throws IOException {
    String text = readResource("org/batfish/representation/aws/SecurityGroupTestDeser.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_SECURITY_GROUPS);
    List<SecurityGroup> securityGroups = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      securityGroups.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), SecurityGroup.class));
    }

    assertThat(
        securityGroups,
        equalTo(
            ImmutableList.of(
                new SecurityGroup(
                    "For test",
                    "sg-01a88a2ecd621d9ba",
                    "Single port ",
                    ImmutableList.of(
                        new IpPermissions(
                            "-1",
                            null,
                            null,
                            ImmutableList.of(new IpRange(Prefix.parse("0.0.0.0/0"))),
                            ImmutableList.of(),
                            ImmutableList.of()),
                        new IpPermissions(
                            "-1",
                            null,
                            null,
                            ImmutableList.of(),
                            ImmutableList.of("pl-7ba54012"),
                            ImmutableList.of())),
                    ImmutableList.of(
                        new IpPermissions(
                            "tcp",
                            22,
                            22,
                            ImmutableList.of(
                                new IpRange("Allowing single port", Prefix.parse("1.2.3.4/32"))),
                            ImmutableList.of(),
                            ImmutableList.of())),
                    ImmutableMap.of("testKey", "testValue"),
                    "vpc-6f6f8316"))));
  }

  @Test
  public void testSinglePort() {
    SecurityGroup sg = _securityGroups.get(0);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchPorts(22, 22), matchIp),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testBeginningHalfOpenInterval() {
    SecurityGroup sg = _securityGroups.get(1);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchPorts(0, 22), matchIp),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testEndHalfOpenInterval() {
    SecurityGroup sg = _securityGroups.get(2);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchPorts(65530, 65535), matchIp),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testFullInterval() {
    SecurityGroup sg = _securityGroups.get(3);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchIp), getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testIcmpType() {
    SecurityGroup sg = _securityGroups.get(9);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(
                        matchIcmp,
                        new MatchHeaderSpace(
                            HeaderSpace.builder().setIcmpTypes(8).build(),
                            traceElementForIcmpType(8)),
                        matchUniverse),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testIcmpTypeAndCode() {
    SecurityGroup sg = _securityGroups.get(10);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(
                        matchIcmp,
                        new MatchHeaderSpace(
                            HeaderSpace.builder().setIcmpTypes(8).build(),
                            traceElementForIcmpType(8)),
                        new MatchHeaderSpace(
                            HeaderSpace.builder().setIcmpCodes(9).build(),
                            traceElementForIcmpCode(9)),
                        matchUniverse),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testIcmpInvalidCodeOnly() {
    SecurityGroup sg = _securityGroups.get(11);
    List<AclLine> inboundRules = sg.toAclLines(_region, true, _warnings);

    // Should still see a VI ACL line for this rule because we want a 1-to-1 mapping of rules:lines
    assertThat(inboundRules, contains(isExprAclLineThat(hasMatchCondition(FALSE))));
    assertThat(_warnings.getRedFlagWarnings(), hasSize(1));
    Warning w = Iterables.getOnlyElement(_warnings.getRedFlagWarnings());
    assertThat(
        w.getText(),
        allOf(
            containsString("ICMP types invalid with code only [ingress]"),
            containsString("unexpected for ICMP to have FromPort=-1 and ToPort=9")));
  }

  @Test
  public void testAllTrafficAllowed() {
    SecurityGroup sg = _securityGroups.get(4);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchUniverse), getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testClosedInterval() {
    SecurityGroup sg = _securityGroups.get(5);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchPorts(45, 50), matchIp),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testInvalidStartInterval() {
    SecurityGroup sg = _securityGroups.get(6);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchPorts(0, 50), matchIp),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testInvalidEndInterval() {
    SecurityGroup sg = _securityGroups.get(7);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchPorts(30, 65535), matchIp),
                    getTraceElementForRule(rangeDesc)))));
  }

  @Test
  public void testStatefulTcpRules() {
    SecurityGroup sg = _securityGroups.get(8);
    String rangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsIngress()).getIpRanges())
            .getDescription();
    AclLine line = Iterables.getOnlyElement(sg.toAclLines(_region, true, _warnings));
    assertThat(
        line,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(matchTcp, matchPorts(22, 22), matchIp),
                    getTraceElementForRule(rangeDesc)))));
    String outRangeDesc =
        Iterables.getOnlyElement(Iterables.getOnlyElement(sg.getIpPermsEgress()).getIpRanges())
            .getDescription();
    AclLine outline = Iterables.getOnlyElement(sg.toAclLines(_region, false, _warnings));
    assertThat(
        outline,
        isExprAclLineThat(
            hasMatchCondition(
                new AndMatchExpr(
                    ImmutableList.of(
                        matchTcp,
                        matchPorts(80, 80),
                        matchDst(
                            Ip.parse("5.6.7.8").toIpSpace(),
                            traceElementForAddress(
                                "destination", "5.6.7.8/32", AddressType.CIDR_IP))),
                    getTraceElementForRule(outRangeDesc)))));
  }

  @Test
  public void testDeniedSynOnlyResponse() {
    SecurityGroup sg = _securityGroups.get(8);

    IpAccessList outFilter =
        IpAccessList.builder()
            .setName(TEST_ACL)
            .setLines(sg.toAclLines(_region, false, _warnings))
            .build();

    // flow containing SYN and ~ACK should be rejected
    _flowBuilder
        .setDstIp(Ip.parse("1.2.3.4"))
        .setSrcPort(22)
        .setDstPort(NamedPort.EPHEMERAL_LOWEST.number())
        .setTcpFlagsAck(false)
        .setTcpFlagsSyn(true);

    assertThat(
        outFilter
            .filter(_flowBuilder.build(), null, ImmutableMap.of(), ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testDeniedWrongIpResponse() {
    SecurityGroup sg = _securityGroups.get(8);

    IpAccessList outFilter =
        IpAccessList.builder()
            .setName(TEST_ACL)
            .setLines(sg.toAclLines(_region, false, _warnings))
            .build();

    // flow containing wrong destination IP should be rejected
    _flowBuilder
        .setDstIp(Ip.parse("1.2.3.5"))
        .setSrcPort(22)
        .setDstPort(NamedPort.EPHEMERAL_LOWEST.number())
        .setTcpFlagsAck(true)
        .setTcpFlagsSyn(true);

    assertThat(
        outFilter
            .filter(_flowBuilder.build(), null, ImmutableMap.of(), ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testPrefixList() {
    String prefixListId = "pl-7ba54012";
    Prefix prefix = Prefix.parse("10.10.10.0/24");

    IpPermissions perms =
        new IpPermissions(
            "-1",
            null,
            null,
            ImmutableList.of(),
            ImmutableList.of(prefixListId),
            ImmutableList.of());

    SecurityGroup sg =
        new SecurityGroup("test", "test", ImmutableList.of(perms), ImmutableList.of(), "vpc");

    List<AclLine> outboundRules =
        sg.toAclLines(
            Region.builder("r1")
                .setPrefixLists(
                    ImmutableMap.of(
                        prefixListId,
                        new PrefixList(prefixListId, ImmutableList.of(prefix), "test")))
                .build(),
            false,
            _warnings);

    IpAccessList outFilter =
        IpAccessList.builder().setName(TEST_ACL).setLines(outboundRules).build();

    // flow in the prefix list range should be accepted
    assertThat(
        outFilter
            .filter(
                _flowBuilder.setDstIp(Ip.parse("10.10.10.1")).setSrcPort(10).setDstPort(10).build(),
                null,
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.PERMIT));

    // flow outside the prefix list range should be denied
    assertThat(
        outFilter
            .filter(
                _flowBuilder.setDstIp(Ip.parse("1.1.1.1")).setSrcPort(10).setDstPort(10).build(),
                null,
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testToAclLines() {
    String rangeDesc = "IP range description";
    SecurityGroup sg =
        new SecurityGroup(
            "sg-001",
            "sg-1",
            ImmutableList.of(),
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    22,
                    22,
                    ImmutableList.of(new IpRange(rangeDesc, Prefix.parse("2.2.2.0/24"))),
                    ImmutableList.of(),
                    ImmutableList.of())),
            "vpc");
    List<AclLine> lines = sg.toAclLines(Region.builder("r").build(), true, new Warnings());
    assertThat(
        lines,
        equalTo(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setName("sg-001 - sg-1 [ingress]")
                    .setTraceElement(null)
                    .setMatchCondition(
                        new AndMatchExpr(
                            ImmutableList.of(
                                matchTcp,
                                matchPorts(22, 22),
                                matchSrc(
                                    Prefix.parse("2.2.2.0/24").toIpSpace(),
                                    traceElementForAddress(
                                        "source", "2.2.2.0/24", AddressType.CIDR_IP))),
                            getTraceElementForRule(rangeDesc)))
                    .build())));
  }
}
