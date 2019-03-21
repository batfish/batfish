package org.batfish.question.testroutepolicies;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableSortedMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SchemaUtils;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.table.TableAnswerElement;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TestRoutePoliciesAnswerer}. */
public class TestPoliciesAnswererTestRouting {
  private Configuration _c;
  private RoutingPolicy.Builder _policyBuilder;
  private IBatfish _batfish;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    _c = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setOwner(_c).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = nf.routingPolicyBuilder().setOwner(_c);
    _batfish = new MockBatfish(ImmutableSortedMap.of(_c.getHostname(), _c));
  }

  @Test
  public void testPermit() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(Direction.IN, inputRoute, _c.getHostname(), policy.getName());
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer();

    Object routeJson =
        SchemaUtils.convertType(
            BatfishObjectMapper.mapper().valueToTree(inputRoute), Schema.OBJECT);
    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_NODE,
                    equalTo(new Node(_c.getHostname())),
                    Schema.NODE),
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_POLICY_NAME,
                    equalTo(policy.getName()),
                    Schema.STRING),
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_INPUT_ROUTE, equalTo(routeJson), Schema.OBJECT),
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_ACTION,
                    equalTo(LineAction.PERMIT.toString()),
                    Schema.STRING),
                // outputRoute == inputRoute
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_OUTPUT_ROUTE,
                    equalTo(routeJson),
                    Schema.OBJECT))));
  }

  @Test
  public void testDeny() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();

    BgpRoute inputRoute =
        BgpRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    TestRoutePoliciesQuestion question =
        new TestRoutePoliciesQuestion(Direction.IN, inputRoute, _c.getHostname(), policy.getName());
    TestRoutePoliciesAnswerer answerer = new TestRoutePoliciesAnswerer(question, _batfish);

    TableAnswerElement answer = (TableAnswerElement) answerer.answer();

    Object routeJson =
        SchemaUtils.convertType(
            BatfishObjectMapper.mapper().valueToTree(inputRoute), Schema.OBJECT);
    assertThat(
        answer.getRows().getData(),
        Matchers.contains(
            allOf(
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_NODE,
                    equalTo(new Node(_c.getHostname())),
                    Schema.NODE),
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_POLICY_NAME,
                    equalTo(policy.getName()),
                    Schema.STRING),
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_INPUT_ROUTE, equalTo(routeJson), Schema.OBJECT),
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_ACTION,
                    equalTo(LineAction.DENY.toString()),
                    Schema.STRING),
                // outputRoute == inputRoute
                hasColumn(
                    TestRoutePoliciesAnswerer.COL_OUTPUT_ROUTE, nullValue(), Schema.OBJECT))));
  }
}
