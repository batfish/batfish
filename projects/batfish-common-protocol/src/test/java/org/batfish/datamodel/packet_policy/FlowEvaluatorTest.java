package org.batfish.datamodel.packet_policy;

import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.packet_policy.FlowEvaluator.FlowResult;
import org.junit.Before;
import org.junit.Test;
import org.parboiled.common.ImmutableList;

/** Tests of {@link FlowEvaluator} */
public final class FlowEvaluatorTest {

  private Flow _flow;
  private Return _defaultAction;

  @Before
  public void setup() {
    _flow =
        Flow.builder()
            .setIngressNode("someNode")
            .setIngressInterface("Eth0")
            .setTag("noTag")
            .setSrcIp(Ip.parse("1.1.1.1"))
            .setDstIp(Ip.parse("2.2.2.2"))
            .build();
    _defaultAction = new Return(Drop.instance());
  }

  private PacketPolicy singletonPolicy(Statement t) {
    return new PacketPolicy("policyName", ImmutableList.of(t), _defaultAction);
  }

  @Test
  public void evaluateReturn() {
    FibLookup fl = new FibLookup("vrf");
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow, "Eth0", singletonPolicy(new Return(fl)), ImmutableMap.of(), ImmutableMap.of());

    assertThat(r.getAction(), equalTo(fl));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void evaluateIfWithMatch() {
    FibLookup fl = new FibLookup("vrf");
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            singletonPolicy(
                new If(new PacketMatchExpr(TrueExpr.INSTANCE), ImmutableList.of(new Return(fl)))),
            ImmutableMap.of(),
            ImmutableMap.of());

    assertThat(r.getAction(), equalTo(fl));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void evaluateIfNoMatch() {
    FibLookup fl = new FibLookup("vrf");
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            singletonPolicy(
                new If(new PacketMatchExpr(FalseExpr.INSTANCE), ImmutableList.of(new Return(fl)))),
            ImmutableMap.of(),
            ImmutableMap.of());

    // Implicit deny all was hit
    assertThat(r.getAction(), equalTo(Drop.instance()));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void testFlowResultEquality() {
    new EqualsTester()
        .addEqualityGroup(
            new FlowResult(_flow, Drop.instance()), new FlowResult(_flow, Drop.instance()))
        .addEqualityGroup(
            new FlowResult(
                _flow.toBuilder().setIngressNode("differentNode").build(), Drop.instance()))
        .addEqualityGroup(new FlowResult(_flow, new FibLookup("aVRF")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testEvaluateFindsFirstReachableTerminalAction() {
    // Setup policy
    FibLookup action = new FibLookup("Matched");
    PacketPolicy policy =
        new PacketPolicy(
            "policyName",
            ImmutableList.of(
                new If(
                    new PacketMatchExpr(FalseExpr.INSTANCE),
                    ImmutableList.of(new Return(new FibLookup("Unreachable")))),
                new If(
                    new PacketMatchExpr(TrueExpr.INSTANCE), ImmutableList.of(new Return(action))),
                new Return(new FibLookup("lastVRF"))),
            _defaultAction);

    // Test:
    FlowResult result =
        FlowEvaluator.evaluate(_flow, "Eth0", policy, ImmutableMap.of(), ImmutableMap.of());

    assertThat(result.getAction(), equalTo(action));
    // No transformations occurred
    assertThat(result.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void testReturnsDefaultActionWhenNoStatements() {
    // Setup policy
    PacketPolicy policy = new PacketPolicy("policyName", ImmutableList.of(), _defaultAction);
    // Test:
    FlowResult result =
        FlowEvaluator.evaluate(_flow, "Eth0", policy, ImmutableMap.of(), ImmutableMap.of());

    assertThat(result.getAction(), equalTo(_defaultAction.getAction()));
    // No transformations occurred
    assertThat(result.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void testIndirection() {
    String aclName = "acl1";
    String ipSpaceName = "ipSpace1";
    FibLookup fl = new FibLookup("vrf");
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            singletonPolicy(
                new If(
                    new PacketMatchExpr(permittedByAcl(aclName)),
                    ImmutableList.of(new Return(fl)))),
            ImmutableMap.of(
                aclName,
                IpAccessList.builder()
                    .setName(aclName)
                    .setLines(
                        ImmutableList.of(
                            IpAccessListLine.acceptingHeaderSpace(
                                HeaderSpace.builder()
                                    .setDstIps(new IpSpaceReference(ipSpaceName))
                                    .build())))
                    .build()),
            ImmutableMap.of(ipSpaceName, UniverseIpSpace.INSTANCE));

    assertThat(r.getAction(), equalTo(fl));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }
}
