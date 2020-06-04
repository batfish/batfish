package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NETWORK_ACLS;
import static org.batfish.representation.aws.NetworkAcl.getAclLine;
import static org.batfish.representation.aws.NetworkAcl.getAclLineName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.junit.Test;

/** Tests for {@link NetworkAcl} */
public class NetworkAclTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/NetworkAclTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NETWORK_ACLS);
    List<NetworkAcl> networkAcls = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      networkAcls.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), NetworkAcl.class));
    }

    assertThat(
        networkAcls,
        equalTo(
            ImmutableList.of(
                new NetworkAcl(
                    "acl-4db39c28",
                    "vpc-f8fad69d",
                    ImmutableList.of(new NetworkAclAssociation("subnet-1f315846")),
                    ImmutableList.of(
                        new NetworkAclEntryV4(
                            Prefix.parse("0.0.0.0/0"), true, true, "-1", 100, null, null),
                        new NetworkAclEntryV4(
                            Prefix.parse("162.243.144.192/32"),
                            false,
                            false,
                            "6",
                            100,
                            null,
                            new PortRange(3306, 3306))),
                    true))));
  }

  @Test
  public void testDeserializationIcmp() throws IOException {
    String text = readResource("org/batfish/representation/aws/NetworkAclIcmpTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NETWORK_ACLS);
    List<NetworkAcl> networkAcls = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      networkAcls.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), NetworkAcl.class));
    }

    assertThat(
        networkAcls,
        equalTo(
            ImmutableList.of(
                new NetworkAcl(
                    "acl-4db39c28",
                    "vpc-f8fad69d",
                    ImmutableList.of(new NetworkAclAssociation("subnet-1f315846")),
                    ImmutableList.of(
                        new NetworkAclEntryV4(
                            Prefix.parse("0.0.0.0/0"),
                            true,
                            true,
                            "1",
                            100,
                            new IcmpTypeCode(-1, -1),
                            null),
                        new NetworkAclEntryV4(
                            Prefix.parse("0.0.0.0/0"),
                            true,
                            true,
                            "1",
                            200,
                            new IcmpTypeCode(0, 1),
                            null)),
                    true))));
  }

  @Test
  public void testDeserializationV6() throws IOException {
    String text = readResource("org/batfish/representation/aws/NetworkAclV6Test.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NETWORK_ACLS);
    List<NetworkAcl> networkAcls = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      networkAcls.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), NetworkAcl.class));
    }

    assertThat(
        networkAcls,
        equalTo(
            ImmutableList.of(
                new NetworkAcl(
                    "acl-4db39c28",
                    "vpc-f8fad69d",
                    ImmutableList.of(new NetworkAclAssociation("subnet-1f315846")),
                    ImmutableList.of(
                        new NetworkAclEntryV6(
                            Prefix6.parse("::/0"), true, true, "-1", 100, null, null)),
                    true))));
  }

  @Test
  public void testGetAclLineAllProtocols() {
    Prefix prefix = Prefix.parse("1.1.1.0/24");
    NetworkAclEntryV4 entry = new NetworkAclEntryV4(prefix, true, true, "-1", 100, null, null);
    assertThat(
        getAclLine(entry),
        equalTo(
            ExprAclLine.builder()
                .setAction(LineAction.PERMIT)
                .setMatchCondition(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(ImmutableSortedSet.of(IpWildcard.create(prefix)))
                            .build()))
                .setName(getAclLineName(100, "ALL", "ALL", prefix, LineAction.PERMIT))
                .build()));

    // has opposite action and egress to those above
    NetworkAclEntryV4 entry2 = new NetworkAclEntryV4(prefix, false, false, "-1", 100, null, null);
    assertThat(
        getAclLine(entry2),
        equalTo(
            ExprAclLine.builder()
                .setAction(LineAction.DENY)
                .setMatchCondition(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(ImmutableSortedSet.of(IpWildcard.create(prefix)))
                            .build()))
                .setName(getAclLineName(100, "ALL", "ALL", prefix, LineAction.DENY))
                .build()));
  }

  @Test
  public void testGetAclLineTcpAll() {
    Prefix prefix = Prefix.parse("1.1.1.0/24");
    NetworkAclEntryV4 entry = new NetworkAclEntryV4(prefix, true, true, "6", 100, null, null);
    assertThat(
        getAclLine(entry),
        equalTo(
            ExprAclLine.builder()
                .setAction(LineAction.PERMIT)
                .setMatchCondition(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(IpProtocol.TCP)
                            .setDstIps(ImmutableSortedSet.of(IpWildcard.create(prefix)))
                            .build()))
                .setName(getAclLineName(100, "TCP", "ALL", prefix, LineAction.PERMIT))
                .build()));
  }

  @Test
  public void testGetAclLineTcpPorts() {
    Prefix prefix = Prefix.parse("1.1.1.0/24");
    NetworkAclEntryV4 entry =
        new NetworkAclEntryV4(prefix, true, true, "6", 100, null, new PortRange(1, 21));
    assertThat(
        getAclLine(entry),
        equalTo(
            ExprAclLine.builder()
                .setAction(LineAction.PERMIT)
                .setMatchCondition(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(IpProtocol.TCP)
                            .setDstIps(ImmutableSortedSet.of(IpWildcard.create(prefix)))
                            .setDstPorts(new SubRange(1, 21))
                            .build()))
                .setName(
                    getAclLineName(
                        100, "TCP", new SubRange(1, 21).toString(), prefix, LineAction.PERMIT))
                .build()));
  }

  @Test
  public void testGetAclLineIcmpAll() {
    Prefix prefix = Prefix.parse("1.1.1.0/24");
    NetworkAclEntryV4 entry =
        new NetworkAclEntryV4(prefix, true, true, "1", 100, new IcmpTypeCode(-1, -1), null);
    assertThat(
        getAclLine(entry),
        equalTo(
            ExprAclLine.builder()
                .setAction(LineAction.PERMIT)
                .setMatchCondition(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(IpProtocol.ICMP)
                            .setDstIps(ImmutableSortedSet.of(IpWildcard.create(prefix)))
                            .build()))
                .setName(getAclLineName(100, "ICMP", "ALL", prefix, LineAction.PERMIT))
                .build()));
  }

  @Test
  public void testGetAclLineIcmpType() {
    Prefix prefix = Prefix.parse("1.1.1.0/24");
    NetworkAclEntryV4 entry =
        new NetworkAclEntryV4(prefix, true, true, "1", 100, new IcmpTypeCode(1, 2), null);
    assertThat(
        getAclLine(entry),
        equalTo(
            ExprAclLine.builder()
                .setAction(LineAction.PERMIT)
                .setMatchCondition(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(IpProtocol.ICMP)
                            .setDstIps(ImmutableSortedSet.of(IpWildcard.create(prefix)))
                            .setIcmpTypes(ImmutableList.of(SubRange.singleton(1)))
                            .setIcmpCodes(ImmutableList.of(SubRange.singleton(2)))
                            .build()))
                .setName(getAclLineName(100, "ICMP", "[type=1, code=2]", prefix, LineAction.PERMIT))
                .build()));
  }
}
