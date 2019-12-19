package org.batfish.datamodel.packet_policy;

import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.Map;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.packet_policy.FlowEvaluator.FlowResult;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
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
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            singletonPolicy(new Return(fl)),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());

    assertThat(r.getAction(), equalTo(fl));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void evaluateIfWithMatch() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            singletonPolicy(
                new If(new PacketMatchExpr(TrueExpr.INSTANCE), ImmutableList.of(new Return(fl)))),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());

    assertThat(r.getAction(), equalTo(fl));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void evaluateIfNoMatch() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            singletonPolicy(
                new If(new PacketMatchExpr(FalseExpr.INSTANCE), ImmutableList.of(new Return(fl)))),
            ImmutableMap.of(),
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
        .addEqualityGroup(new FlowResult(_flow, new FibLookup(new LiteralVrfName("avrf"))))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testEvaluateFindsFirstReachableTerminalAction() {
    // Setup policy
    FibLookup action = new FibLookup(new LiteralVrfName("Matched"));
    PacketPolicy policy =
        new PacketPolicy(
            "policyName",
            ImmutableList.of(
                new If(
                    new PacketMatchExpr(FalseExpr.INSTANCE),
                    ImmutableList.of(new Return(new FibLookup(new LiteralVrfName("Unreachable"))))),
                new If(
                    new PacketMatchExpr(TrueExpr.INSTANCE), ImmutableList.of(new Return(action))),
                new Return(new FibLookup(new LiteralVrfName("LastVrf")))),
            _defaultAction);

    // Test:
    FlowResult result =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            policy,
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());

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
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            policy,
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());

    assertThat(result.getAction(), equalTo(_defaultAction.getAction()));
    // No transformations occurred
    assertThat(result.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void testIndirection() {
    String aclName = "acl1";
    String ipSpaceName = "ipSpace1";
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
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
                            ExprAclLine.acceptingHeaderSpace(
                                HeaderSpace.builder()
                                    .setDstIps(new IpSpaceReference(ipSpaceName))
                                    .build())))
                    .build()),
            ImmutableMap.of(ipSpaceName, UniverseIpSpace.INSTANCE),
            ImmutableMap.of());

    assertThat(r.getAction(), equalTo(fl));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void testTrue() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            singletonPolicy(
                new If(
                    org.batfish.datamodel.packet_policy.TrueExpr.instance(),
                    ImmutableList.of(new Return(fl)))),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(r.getAction(), equalTo(fl));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void testFalse() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            singletonPolicy(
                new If(
                    org.batfish.datamodel.packet_policy.FalseExpr.instance(),
                    ImmutableList.of(new Return(fl)))),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(r.getAction(), equalTo(_defaultAction.getAction()));
    assertThat(r.getFinalFlow(), equalTo(_flow));
  }

  @Test
  public void testEvaluateApplyTransformation() {
    Ip natIp = Ip.parse("8.8.8.8");
    ApplyTransformation transformation =
        new ApplyTransformation(
            Transformation.always()
                .apply(TransformationStep.assignDestinationIp(natIp, natIp))
                .build());
    FlowResult r =
        FlowEvaluator.evaluate(
            _flow,
            "Eth0",
            "otherVrf",
            singletonPolicy(transformation),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(r.getAction(), equalTo(_defaultAction.getAction()));
    assertThat(r.getFinalFlow(), equalTo(_flow.toBuilder().setDstIp(natIp).build()));
  }

  @Test
  public void testEvaluateFibLookupOutgoingInterfaceIsOneOf() {
    Prefix dstPrefix = _flow.getDstIp().toPrefix();
    Ip nextVrIp = Ip.parse("5.5.5.5");
    String srcIface = "Eth0";
    String vrfName = "vrf";
    String nextVrfName = "nextVrf";
    String vrfIface = "vrfIface";
    String nextVrfIface = "nextVrfIface";
    ConnectedRoute fakeRoute = new ConnectedRoute(dstPrefix, vrfIface);

    Map<String, Fib> fibs =
        ImmutableMap.of(
            vrfName,
            MockFib.builder()
                .setFibEntries(
                    ImmutableMap.of(
                        _flow.getDstIp(),
                        ImmutableSet.of(
                            new FibEntry(
                                new FibForward(Ip.MAX, vrfIface), ImmutableList.of(fakeRoute))),
                        nextVrIp,
                        ImmutableSet.of(
                            new FibEntry(
                                new FibNextVrf(nextVrfName), ImmutableList.of(fakeRoute)))))
                .build(),
            nextVrfName,
            MockFib.builder()
                .setFibEntries(
                    ImmutableMap.of(
                        _flow.getDstIp(),
                        ImmutableSet.of(
                            new FibEntry(
                                new FibForward(Ip.MAX, nextVrfIface), ImmutableList.of(fakeRoute))),
                        nextVrIp,
                        ImmutableSet.of(
                            new FibEntry(
                                new FibForward(Ip.MAX, nextVrfIface),
                                ImmutableList.of(fakeRoute)))))
                .build());

    Action trueAction = new FibLookup(new LiteralVrfName("finalVrf"));
    Return trueReturn = new Return(trueAction);
    Action defaultAction = Drop.instance();

    {
      // Lookup in interface VRF
      FlowResult r =
          FlowEvaluator.evaluate(
              _flow,
              srcIface,
              vrfName,
              singletonPolicy(
                  new If(
                      new FibLookupOutgoingInterfaceIsOneOf(
                          IngressInterfaceVrf.instance(), ImmutableSet.of(vrfIface)),
                      Collections.singletonList(trueReturn))),
              ImmutableMap.of(),
              ImmutableMap.of(),
              fibs);
      assertThat(r.getAction(), equalTo(trueAction));
    }
    {
      // Lookup in interface VRF, no match
      FlowResult r =
          FlowEvaluator.evaluate(
              _flow,
              srcIface,
              vrfName,
              singletonPolicy(
                  new If(
                      new FibLookupOutgoingInterfaceIsOneOf(
                          IngressInterfaceVrf.instance(), ImmutableSet.of("NoMatchIface")),
                      Collections.singletonList(trueReturn))),
              ImmutableMap.of(),
              ImmutableMap.of(),
              fibs);
      assertThat(r.getAction(), equalTo(defaultAction));
    }

    {
      // Lookup in a different VRF
      FlowResult r =
          FlowEvaluator.evaluate(
              _flow,
              srcIface,
              vrfName,
              singletonPolicy(
                  new If(
                      new FibLookupOutgoingInterfaceIsOneOf(
                          new LiteralVrfName(nextVrfName), ImmutableSet.of(nextVrfIface)),
                      Collections.singletonList(trueReturn))),
              ImmutableMap.of(),
              ImmutableMap.of(),
              fibs);
      assertThat(r.getAction(), equalTo(trueAction));
    }

    {
      // Lookup in original VRF with NEXT VR route -- should match nextVrfIface
      Flow flow = _flow.toBuilder().setDstIp(nextVrIp).build();
      FlowResult r =
          FlowEvaluator.evaluate(
              flow,
              srcIface,
              vrfName,
              singletonPolicy(
                  new If(
                      new FibLookupOutgoingInterfaceIsOneOf(
                          IngressInterfaceVrf.instance(), ImmutableSet.of(nextVrfIface)),
                      Collections.singletonList(trueReturn))),
              ImmutableMap.of(),
              ImmutableMap.of(),
              fibs);
      assertThat(r.getAction(), equalTo(trueAction));
    }
  }

  @Test
  public void testConjunction() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    {
      FlowResult result =
          FlowEvaluator.evaluate(
              _flow,
              "Eth0",
              "otherVrf",
              singletonPolicy(
                  new If(
                      Conjunction.of(org.batfish.datamodel.packet_policy.TrueExpr.instance()),
                      ImmutableList.of(new Return(fl)))),
              ImmutableMap.of(),
              ImmutableMap.of(),
              ImmutableMap.of());

      assertThat(result.getAction(), equalTo(fl));
    }

    {
      FlowResult result =
          FlowEvaluator.evaluate(
              _flow,
              "Eth0",
              "otherVrf",
              singletonPolicy(
                  new If(
                      Conjunction.of(
                          org.batfish.datamodel.packet_policy.TrueExpr.instance(),
                          org.batfish.datamodel.packet_policy.FalseExpr.instance()),
                      ImmutableList.of(new Return(fl)))),
              ImmutableMap.of(),
              ImmutableMap.of(),
              ImmutableMap.of());

      assertThat(result.getAction(), equalTo(_defaultAction.getAction()));
    }
  }
}
