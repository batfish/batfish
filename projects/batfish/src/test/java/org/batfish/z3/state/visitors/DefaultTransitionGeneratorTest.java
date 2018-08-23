package org.batfish.z3.state.visitors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.Field;
import org.batfish.z3.MockSynthesizerInput;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.MockBooleanAtom;
import org.batfish.z3.expr.MockIntAtom;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.AclLineNoMatch;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.DropAcl;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.NeighborUnreachable;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDrop;
import org.batfish.z3.state.NodeDropAcl;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachable;
import org.batfish.z3.state.NodeNeighborUnreachable;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutEdge;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutVrf;
import org.junit.Test;

public class DefaultTransitionGeneratorTest {

  private static final String ACL1 = "acl1";

  private static final String ACL2 = "acl2";

  private static final String ACL3 = "acl3";

  private static final BooleanExpr B1 = b(1);

  private static final BooleanExpr B2 = b(2);

  private static final String INTERFACE1 = "interface1";

  private static final String INTERFACE2 = "interface2";

  private static final String INTERFACE3 = "interface3";

  private static final String INTERFACE4 = "interface4";

  private static final Ip IP1 = new Ip("1.1.1.1");

  private static final Ip IP2 = new Ip("2.2.2.2");

  private static final Ip IP3 = new Ip("3.3.3.3");

  private static final Ip IP4 = new Ip("4.4.4.4");

  private static final String NAT_ACL1 = "natacl1";

  private static final String NAT_ACL2 = "natacl2";

  private static final String NODE1 = "node1";

  private static final String NODE2 = "node2";

  private static final String NODE3 = "node3";

  private static final String NODE4 = "node4";

  private static final String VRF1 = "vrf1";

  private static final String VRF2 = "vrf2";

  private static final Field SRC_INTERFACE_FIELD = new Field("SRC_INTERFACE", 1);

  private static final BooleanExpr NO_SRC_INTERFACE_FIELD =
      new EqExpr(
          new VarIntExpr(SRC_INTERFACE_FIELD),
          new LitIntExpr(
              DefaultTransitionGenerator.NO_SOURCE_INTERFACE, SRC_INTERFACE_FIELD.getSize()));

  private static final BooleanExpr TRANSIT_NODES_NOT_TRANSITED =
      new EqExpr(
          new VarIntExpr(DefaultTransitionGenerator.TRANSITED_TRANSIT_NODES_FIELD),
          DefaultTransitionGenerator.NOT_TRANSITED);

  private static BooleanExpr b(int num) {
    return new MockBooleanAtom(num);
  }

  private static IntExpr i(int num) {
    return new MockIntAtom(num);
  }

  private static Map<String, Map<String, List<LineAction>>> aclActions() {
    List<LineAction> acl1ActionsByLine =
        ImmutableList.of(LineAction.PERMIT, LineAction.DENY, LineAction.PERMIT, LineAction.DENY);
    List<LineAction> acl2ActionsByLine =
        ImmutableList.of(LineAction.DENY, LineAction.PERMIT, LineAction.DENY, LineAction.PERMIT);
    ImmutableMap<String, List<LineAction>> aclActions =
        ImmutableMap.of(ACL1, acl1ActionsByLine, ACL2, acl2ActionsByLine, ACL3, ImmutableList.of());
    Map<String, Map<String, List<LineAction>>> aclActionss =
        ImmutableMap.of(NODE1, aclActions, NODE2, aclActions);
    return aclActionss;
  }

  private static Map<String, Map<String, List<BooleanExpr>>> aclConditions() {
    List<BooleanExpr> acl1Conditions =
        ImmutableList.of(
            TrueExpr.INSTANCE, FalseExpr.INSTANCE, TrueExpr.INSTANCE, FalseExpr.INSTANCE);
    List<BooleanExpr> acl2Conditions =
        ImmutableList.of(
            FalseExpr.INSTANCE, TrueExpr.INSTANCE, FalseExpr.INSTANCE, TrueExpr.INSTANCE);
    ImmutableMap<String, List<BooleanExpr>> aclConditions =
        ImmutableMap.of(ACL1, acl1Conditions, ACL2, acl2Conditions, ACL3, ImmutableList.of());
    Map<String, Map<String, List<BooleanExpr>>> acl =
        ImmutableMap.of(NODE1, aclConditions, NODE2, aclConditions);
    return acl;
  }

  @Test
  public void testPreOutEdgePostNat() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledEdges(
                ImmutableSet.of(
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE2),
                    new Edge(NODE1, INTERFACE2, NODE2, INTERFACE1),
                    new Edge(NODE1, INTERFACE2, NODE2, INTERFACE2),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE1),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE2),
                    new Edge(NODE2, INTERFACE2, NODE1, INTERFACE1),
                    new Edge(NODE2, INTERFACE2, NODE1, INTERFACE2)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PreOutEdgePostNat.State.INSTANCE)));

    // PreOutEdgePostNatForTopologyEdges
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                new PreOutEdgePostNat(NODE1, INTERFACE1, NODE2, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE2),
                new PreOutEdgePostNat(NODE1, INTERFACE1, NODE2, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE1, INTERFACE2, NODE2, INTERFACE1),
                new PreOutEdgePostNat(NODE1, INTERFACE2, NODE2, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE1, INTERFACE2, NODE2, INTERFACE2),
                new PreOutEdgePostNat(NODE1, INTERFACE2, NODE2, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE1),
                new PreOutEdgePostNat(NODE2, INTERFACE1, NODE1, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE2),
                new PreOutEdgePostNat(NODE2, INTERFACE1, NODE1, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE2, INTERFACE2, NODE1, INTERFACE1),
                new PreOutEdgePostNat(NODE2, INTERFACE2, NODE1, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdge(NODE2, INTERFACE2, NODE1, INTERFACE2),
                new PreOutEdgePostNat(NODE2, INTERFACE2, NODE1, INTERFACE2))));
  }

  @Test
  public void testVisitAccept() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(Accept.State.INSTANCE)));

    assertThat(rules, hasItem(new BasicRuleStatement(new NodeAccept(NODE1), Accept.INSTANCE)));
    assertThat(rules, hasItem(new BasicRuleStatement(new NodeAccept(NODE2), Accept.INSTANCE)));
  }

  @Test
  public void testVisitAclDeny() {
    SynthesizerInput input = MockSynthesizerInput.builder().setAclActions(aclActions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclDeny.State.INSTANCE)));

    // MatchDenyLine
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL1, 1), new AclDeny(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL1, 3), new AclDeny(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL2, 0), new AclDeny(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL2, 2), new AclDeny(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL1, 1), new AclDeny(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL1, 3), new AclDeny(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL2, 0), new AclDeny(NODE2, ACL2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL2, 2), new AclDeny(NODE2, ACL2))));

    // MatchNoLines
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineNoMatch(NODE1, ACL1, 3), new AclDeny(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineNoMatch(NODE1, ACL2, 3), new AclDeny(NODE1, ACL2))));
    assertThat(rules, hasItem(new BasicRuleStatement(new AclDeny(NODE1, ACL3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineNoMatch(NODE2, ACL1, 3), new AclDeny(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineNoMatch(NODE2, ACL2, 3), new AclDeny(NODE2, ACL2))));
    assertThat(rules, hasItem(new BasicRuleStatement(new AclDeny(NODE1, ACL3))));
  }

  @Test
  public void testVisitAclLineMatch() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setAclConditions(aclConditions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclLineMatch.State.INSTANCE)));

    // MatchCurrentAndDontMatchPrevious

    assertThat(
        rules,
        hasItem(new BasicRuleStatement(TrueExpr.INSTANCE, new AclLineMatch(NODE1, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                FalseExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL1, 0)),
                new AclLineMatch(NODE1, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL1, 1)),
                new AclLineMatch(NODE1, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                FalseExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL1, 2)),
                new AclLineMatch(NODE1, ACL1, 3))));
    assertThat(
        rules,
        hasItem(new BasicRuleStatement(FalseExpr.INSTANCE, new AclLineMatch(NODE1, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL2, 0)),
                new AclLineMatch(NODE1, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                FalseExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL2, 1)),
                new AclLineMatch(NODE1, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL2, 2)),
                new AclLineMatch(NODE1, ACL2, 3))));
    assertThat(
        rules,
        hasItem(new BasicRuleStatement(TrueExpr.INSTANCE, new AclLineMatch(NODE2, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                FalseExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL1, 0)),
                new AclLineMatch(NODE2, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL1, 1)),
                new AclLineMatch(NODE2, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                FalseExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL1, 2)),
                new AclLineMatch(NODE2, ACL1, 3))));
    assertThat(
        rules,
        hasItem(new BasicRuleStatement(FalseExpr.INSTANCE, new AclLineMatch(NODE2, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL2, 0)),
                new AclLineMatch(NODE2, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                FalseExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL2, 1)),
                new AclLineMatch(NODE2, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL2, 2)),
                new AclLineMatch(NODE2, ACL2, 3))));
  }

  @Test
  public void testVisitAclLineNoMatch() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setAclConditions(aclConditions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclLineNoMatch.State.INSTANCE)));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL1, 0)),
                new AclLineNoMatch(NODE1, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL1, 1)),
                new AclLineNoMatch(NODE1, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL1, 2)),
                new AclLineNoMatch(NODE1, ACL1, 3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL2, 0)),
                new AclLineNoMatch(NODE1, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL2, 1)),
                new AclLineNoMatch(NODE1, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE1, ACL2, 2)),
                new AclLineNoMatch(NODE1, ACL2, 3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL1, 0)),
                new AclLineNoMatch(NODE2, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL1, 1)),
                new AclLineNoMatch(NODE2, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL1, 2)),
                new AclLineNoMatch(NODE2, ACL1, 3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL2, 0)),
                new AclLineNoMatch(NODE2, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(FalseExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL2, 1)),
                new AclLineNoMatch(NODE2, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(TrueExpr.INSTANCE),
                ImmutableSet.of(new AclLineNoMatch(NODE2, ACL2, 2)),
                new AclLineNoMatch(NODE2, ACL2, 3))));
  }

  @Test
  public void testVisitAclPermit() {
    SynthesizerInput input = MockSynthesizerInput.builder().setAclActions(aclActions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclPermit.State.INSTANCE)));

    // MatchPermitLine
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL1, 0), new AclPermit(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL1, 2), new AclPermit(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL2, 1), new AclPermit(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE1, ACL2, 3), new AclPermit(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL1, 0), new AclPermit(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL1, 2), new AclPermit(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL2, 1), new AclPermit(NODE2, ACL2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(new AclLineMatch(NODE2, ACL2, 3), new AclPermit(NODE2, ACL2))));
  }

  @Test
  public void testVisitDrop() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(Drop.State.INSTANCE)));

    assertThat(rules, hasItem(new BasicRuleStatement(new NodeDrop(NODE1), Drop.INSTANCE)));
    assertThat(rules, hasItem(new BasicRuleStatement(new NodeDrop(NODE2), Drop.INSTANCE)));
  }

  @Test
  public void testVisitDropAcl() {
    SynthesizerInput input = MockSynthesizerInput.builder().build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropAcl.State.INSTANCE)));

    Set<RuleStatement> expectedCopyDropAclIn =
        ImmutableSet.of(new BasicRuleStatement(DropAclIn.INSTANCE, DropAcl.INSTANCE));

    Set<RuleStatement> expectedCopyDropAclOut =
        ImmutableSet.of(new BasicRuleStatement(DropAclOut.INSTANCE, DropAcl.INSTANCE));

    assertThat(rules, equalTo(Sets.union(expectedCopyDropAclIn, expectedCopyDropAclOut)));
  }

  @Test
  public void testVisitDropAclIn() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropAclIn.State.INSTANCE)));

    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropAclIn(NODE1), DropAclIn.INSTANCE)));
    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropAclIn(NODE2), DropAclIn.INSTANCE)));
  }

  @Test
  public void testVisitDropAclOut() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropAclOut.State.INSTANCE)));

    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropAclOut(NODE1), DropAclOut.INSTANCE)));
    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropAclOut(NODE2), DropAclOut.INSTANCE)));
  }

  @Test
  public void testVisitDropNoRoute() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropNoRoute.State.INSTANCE)));

    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropNoRoute(NODE1), DropNoRoute.INSTANCE)));
    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropNoRoute(NODE2), DropNoRoute.INSTANCE)));
  }

  @Test
  public void testVisitDropNullRoute() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropNullRoute.State.INSTANCE)));

    assertThat(
        rules,
        hasItem(new BasicRuleStatement(new NodeDropNullRoute(NODE1), DropNullRoute.INSTANCE)));
    assertThat(
        rules,
        hasItem(new BasicRuleStatement(new NodeDropNullRoute(NODE2), DropNullRoute.INSTANCE)));
  }

  @Test
  public void testVisitNeighborUnreachable() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setNeighborUnreachable(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(VRF1, ImmutableMap.of(INTERFACE1, b(1))),
                    NODE2,
                    ImmutableMap.of(VRF1, ImmutableMap.of(INTERFACE1, b(2)))))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NeighborUnreachable.State.INSTANCE)));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NodeNeighborUnreachable(NODE1), NeighborUnreachable.INSTANCE)));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NodeNeighborUnreachable(NODE2), NeighborUnreachable.INSTANCE)));
  }

  @Test
  public void testVisitNodeAccept() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledNodes(ImmutableSet.of(NODE1, NODE2))
            .setEnabledVrfs(
                ImmutableMap.of(NODE1, ImmutableSet.of(VRF1), NODE2, ImmutableSet.of(VRF2)))
            .setIpsByNodeVrf(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(VRF1, ImmutableSet.of(IP1, IP2)),
                    NODE2,
                    ImmutableMap.of(VRF2, ImmutableSet.of(IP3, IP4))))
            .setNamedIpSpaces(ImmutableMap.of(NODE1, ImmutableMap.of(), NODE2, ImmutableMap.of()))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeAccept.State.INSTANCE)));

    // PostInForMe
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                HeaderSpaceMatchExpr.matchDstIp(
                    IpWildcardSetIpSpace.builder()
                        .including(ImmutableSet.of(new IpWildcard(IP1), new IpWildcard(IP2)))
                        .build(),
                    ImmutableMap.of()),
                new PostInVrf(NODE1, VRF1),
                new NodeAccept(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                HeaderSpaceMatchExpr.matchDstIp(
                    IpWildcardSetIpSpace.builder()
                        .including(ImmutableSet.of(new IpWildcard(IP3), new IpWildcard(IP4)))
                        .build(),
                    ImmutableMap.of()),
                new PostInVrf(NODE2, VRF2),
                new NodeAccept(NODE2))));
  }

  @Test
  public void testVisitNodeDrop() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDrop.State.INSTANCE)));

    // CopyNodeDropAcl
    assertThat(rules, hasItem(new BasicRuleStatement(new NodeDropAcl(NODE1), new NodeDrop(NODE1))));
    assertThat(rules, hasItem(new BasicRuleStatement(new NodeDropAcl(NODE2), new NodeDrop(NODE2))));

    // CopyNodeDropNoRoute
    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropNoRoute(NODE1), new NodeDrop(NODE1))));
    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropNoRoute(NODE2), new NodeDrop(NODE2))));

    // CopyNodeDropNullRoute
    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropNullRoute(NODE1), new NodeDrop(NODE1))));
    assertThat(
        rules, hasItem(new BasicRuleStatement(new NodeDropNullRoute(NODE2), new NodeDrop(NODE2))));
  }

  @Test
  public void testVisitNodeDropAcl() {
    SynthesizerInput input =
        MockSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropAcl.State.INSTANCE)));

    Set<RuleStatement> expectedCopyNodeDropAclIn =
        ImmutableSet.of(
            new BasicRuleStatement(new NodeDropAclIn(NODE1), new NodeDropAcl(NODE1)),
            new BasicRuleStatement(new NodeDropAclIn(NODE2), new NodeDropAcl(NODE2)));

    Set<RuleStatement> expectedCopyNodeDropAclOut =
        ImmutableSet.of(
            new BasicRuleStatement(new NodeDropAclOut(NODE1), new NodeDropAcl(NODE1)),
            new BasicRuleStatement(new NodeDropAclOut(NODE2), new NodeDropAcl(NODE2)));

    assertThat(rules, equalTo(Sets.union(expectedCopyNodeDropAclIn, expectedCopyNodeDropAclOut)));
  }

  @Test
  public void testVisitNodeDropAclIn() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setIncomingAcls(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(INTERFACE1, ACL1, INTERFACE2, ACL2),
                    NODE2,
                    ImmutableMap.of(INTERFACE1, ACL1, INTERFACE2, ACL2)))
            .setTopologyInterfaces(
                ImmutableMap.of(
                    NODE1,
                    ImmutableSet.of(INTERFACE1, INTERFACE2),
                    NODE2,
                    ImmutableSet.of(INTERFACE1, INTERFACE2)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropAclIn.State.INSTANCE)));

    // FailIncomingAcl
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclDeny(NODE1, ACL1), new PreInInterface(NODE1, INTERFACE1)),
                new NodeDropAclIn(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclDeny(NODE1, ACL2), new PreInInterface(NODE1, INTERFACE2)),
                new NodeDropAclIn(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclDeny(NODE2, ACL1), new PreInInterface(NODE2, INTERFACE1)),
                new NodeDropAclIn(NODE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclDeny(NODE2, ACL2), new PreInInterface(NODE2, INTERFACE2)),
                new NodeDropAclIn(NODE2))));
  }

  @Test
  public void testVisitNodeDropAclOut() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledEdges(
                ImmutableSet.of(
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new Edge(NODE1, INTERFACE2, NODE2, INTERFACE2),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE1),
                    new Edge(NODE2, INTERFACE2, NODE1, INTERFACE2)))
            .setOutgoingAcls(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(INTERFACE1, ACL1),
                    NODE2,
                    ImmutableMap.of(INTERFACE1, ACL1, INTERFACE2, ACL2)))
            .setSourceNats(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        INTERFACE1,
                        ImmutableList.of(
                            Maps.immutableEntry(new AclPermit(NODE1, NAT_ACL1), TrueExpr.INSTANCE),
                            Maps.immutableEntry(
                                new AclPermit(NODE1, NAT_ACL2), FalseExpr.INSTANCE)),
                        INTERFACE2,
                        ImmutableList.of(
                            Maps.immutableEntry(new AclPermit(NODE1, NAT_ACL1), TrueExpr.INSTANCE),
                            Maps.immutableEntry(
                                new AclPermit(NODE1, NAT_ACL2), FalseExpr.INSTANCE))),
                    NODE2,
                    ImmutableMap.of(
                        INTERFACE1,
                        ImmutableList.of(
                            Maps.immutableEntry(new AclPermit(NODE2, NAT_ACL1), TrueExpr.INSTANCE),
                            Maps.immutableEntry(
                                new AclPermit(NODE2, NAT_ACL1), FalseExpr.INSTANCE)),
                        INTERFACE2,
                        ImmutableList.of())))
            .setTopologyInterfaces(
                ImmutableMap.of(
                    NODE1,
                    ImmutableSet.of(INTERFACE1, INTERFACE2),
                    NODE2,
                    ImmutableSet.of(INTERFACE1, INTERFACE2)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropAclOut.State.INSTANCE)));

    // Just test the DropAclOut rules for Node2
    Set<RuleStatement> node2DropAclOutRules =
        rules
            .stream()
            .map(BasicRuleStatement.class::cast)
            .filter(rule -> rule.getPostconditionState().equals(new NodeDropAclOut(NODE2)))
            .collect(Collectors.toSet());

    // FailOutgoingAclNoMatchSrcNat
    assertThat(
        node2DropAclOutRules,
        containsInAnyOrder(
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(
                    new AclDeny(NODE2, ACL1),
                    new PreOutEdgePostNat(NODE2, INTERFACE1, NODE1, INTERFACE1)),
                new NodeDropAclOut(NODE2)),
            new BasicRuleStatement(
                TrueExpr.INSTANCE,
                ImmutableSet.of(
                    new AclDeny(NODE2, ACL2),
                    new PreOutEdgePostNat(NODE2, INTERFACE2, NODE1, INTERFACE2)),
                new NodeDropAclOut(NODE2))));
  }

  @Test
  public void testVisitNodeDropAclOut_nonEdgeInterfaces() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setNeighborUnreachable(
                ImmutableMap.of(NODE1, ImmutableMap.of(VRF1, ImmutableMap.of(INTERFACE1, b(1)))))
            .setOutgoingAcls(ImmutableMap.of(NODE1, ImmutableMap.of(INTERFACE1, ACL1)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropAclOut.State.INSTANCE)));

    // NeighborUnreachable fail OutAcl
    assertThat(
        rules,
        contains(
            new BasicRuleStatement(
                b(1),
                ImmutableSet.of(new AclDeny(NODE1, ACL1), new PreOutVrf(NODE1, VRF1)),
                new NodeDropAclOut(NODE1))));
  }

  @Test
  public void testVisitNodeDropNoRoute() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setRoutableIps(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(VRF1, B1, VRF2, B2),
                    NODE2,
                    ImmutableMap.of(VRF1, B1, VRF2, B2)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropNoRoute.State.INSTANCE)));

    // DestinationRouting
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(B1), new PreOutVrf(NODE1, VRF1), new NodeDropNoRoute(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(B2), new PreOutVrf(NODE1, VRF2), new NodeDropNoRoute(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(B1), new PreOutVrf(NODE2, VRF1), new NodeDropNoRoute(NODE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(B2), new PreOutVrf(NODE2, VRF2), new NodeDropNoRoute(NODE2))));
  }

  @Test
  public void testVisitNodeDropNullRoute() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setNullRoutedIps(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(VRF1, B1, VRF2, B2),
                    NODE2,
                    ImmutableMap.of(VRF1, B1, VRF2, B2)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropNullRoute.State.INSTANCE)));

    // DestinationRouting
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(B1, new PreOutVrf(NODE1, VRF1), new NodeDropNullRoute(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(B2, new PreOutVrf(NODE1, VRF2), new NodeDropNullRoute(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(B1, new PreOutVrf(NODE2, VRF1), new NodeDropNullRoute(NODE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(B2, new PreOutVrf(NODE2, VRF2), new NodeDropNullRoute(NODE2))));
  }

  @Test
  public void testVisitNodeInterfaceNeighborUnreachable() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setNeighborUnreachable(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        VRF1,
                        ImmutableMap.of(INTERFACE1, b(1), INTERFACE2, b(2)),
                        VRF2,
                        ImmutableMap.of(INTERFACE3, b(3))),
                    NODE2,
                    ImmutableMap.of(VRF1, ImmutableMap.of(INTERFACE1, b(4)))))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeInterfaceNeighborUnreachable.State.INSTANCE)));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(1),
                new PreOutVrf(NODE1, VRF1),
                new NodeInterfaceNeighborUnreachable(NODE1, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(2),
                new PreOutVrf(NODE1, VRF1),
                new NodeInterfaceNeighborUnreachable(NODE1, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(3),
                new PreOutVrf(NODE1, VRF2),
                new NodeInterfaceNeighborUnreachable(NODE1, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(4),
                new PreOutVrf(NODE2, VRF1),
                new NodeInterfaceNeighborUnreachable(NODE2, INTERFACE1))));
  }

  @Test
  public void testVisitNodeNeighborUnreachable() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setNeighborUnreachable(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        VRF1,
                        ImmutableMap.of(INTERFACE1, b(1), INTERFACE2, b(2)),
                        VRF2,
                        ImmutableMap.of(INTERFACE3, b(3))),
                    NODE2,
                    ImmutableMap.of(VRF1, ImmutableMap.of(INTERFACE1, b(4)))))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeNeighborUnreachable.State.INSTANCE)));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NodeInterfaceNeighborUnreachable(NODE1, INTERFACE1),
                new NodeNeighborUnreachable(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NodeInterfaceNeighborUnreachable(NODE1, INTERFACE2),
                new NodeNeighborUnreachable(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NodeInterfaceNeighborUnreachable(NODE1, INTERFACE3),
                new NodeNeighborUnreachable(NODE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NodeInterfaceNeighborUnreachable(NODE2, INTERFACE1),
                new NodeNeighborUnreachable(NODE2))));
  }

  @Test
  public void testVisitPostInInterface() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setIncomingAcls(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(INTERFACE1, ACL1, INTERFACE2, ACL2),
                    NODE2,
                    ImmutableMap.of(INTERFACE1, ACL1, INTERFACE2, ACL2)))
            .setEnabledInterfaces(
                ImmutableMap.of(
                    NODE1,
                    ImmutableSet.of(INTERFACE1, INTERFACE2, INTERFACE3),
                    NODE2,
                    ImmutableSet.of(INTERFACE1, INTERFACE2, INTERFACE3)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PostInInterface.State.INSTANCE)));

    // PassIncomingAcl
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclPermit(NODE1, ACL1), new PreInInterface(NODE1, INTERFACE1)),
                new PostInInterface(NODE1, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclPermit(NODE1, ACL2), new PreInInterface(NODE1, INTERFACE2)),
                new PostInInterface(NODE1, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreInInterface(NODE1, INTERFACE3), new PostInInterface(NODE1, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclPermit(NODE2, ACL1), new PreInInterface(NODE2, INTERFACE1)),
                new PostInInterface(NODE2, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(new AclPermit(NODE2, ACL2), new PreInInterface(NODE2, INTERFACE2)),
                new PostInInterface(NODE2, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreInInterface(NODE2, INTERFACE3), new PostInInterface(NODE2, INTERFACE3))));
  }

  @Test
  public void testVisitPostInVrf_OriginateVrf() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setSrcInterfaceField(SRC_INTERFACE_FIELD)
            .setEnabledVrfs(
                ImmutableMap.of(
                    NODE1, ImmutableSet.of(VRF1, VRF2), NODE2, ImmutableSet.of(VRF1, VRF2)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PostInVrf.State.INSTANCE)));

    BooleanExpr preconditions =
        new AndExpr(
            ImmutableList.of(
                NO_SRC_INTERFACE_FIELD,
                // transit nodes constraint
                TrueExpr.INSTANCE));

    // CopyOriginateVrf
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                preconditions, new OriginateVrf(NODE1, VRF1), new PostInVrf(NODE1, VRF1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                preconditions, new OriginateVrf(NODE1, VRF2), new PostInVrf(NODE1, VRF2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                preconditions, new OriginateVrf(NODE2, VRF1), new PostInVrf(NODE2, VRF1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                preconditions, new OriginateVrf(NODE2, VRF2), new PostInVrf(NODE2, VRF2))));
  }

  @Test
  public void testVisitPostInVrf_OriginateVrf_transitNodes() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledVrfs(
                ImmutableMap.of(
                    NODE1, ImmutableSet.of(VRF1, VRF2), NODE2, ImmutableSet.of(VRF1, VRF2)))
            .setEnabledEdges(
                ImmutableSet.of(
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE1)))
            .setOutgoingAcls(
                ImmutableMap.of(
                    NODE1, ImmutableMap.of(),
                    NODE2, ImmutableMap.of()))
            .setSrcInterfaceField(SRC_INTERFACE_FIELD)
            .setTransitNodes(ImmutableSet.of(NODE1))
            .setEnabledInterfaces(
                ImmutableMap.of(
                    NODE1, ImmutableSet.of(INTERFACE1), NODE2, ImmutableSet.of(INTERFACE1)))
            .build();

    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PostInVrf.State.INSTANCE)));

    BooleanExpr preconditions =
        new AndExpr(ImmutableList.of(NO_SRC_INTERFACE_FIELD, TRANSIT_NODES_NOT_TRANSITED));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                preconditions, new OriginateVrf(NODE1, VRF1), new PostInVrf(NODE1, VRF1))));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                preconditions, new OriginateVrf(NODE2, VRF1), new PostInVrf(NODE2, VRF1))));
  }

  @Test
  public void testVisitPostInVrf_PostInInterface() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledInterfacesByNodeVrf(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        VRF1,
                        ImmutableSet.of(INTERFACE1, INTERFACE2),
                        VRF2,
                        ImmutableSet.of(INTERFACE3, INTERFACE4)),
                    NODE2,
                    ImmutableMap.of(
                        VRF1,
                        ImmutableSet.of(INTERFACE1, INTERFACE2),
                        VRF2,
                        ImmutableSet.of(INTERFACE3, INTERFACE4))))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PostInVrf.State.INSTANCE)));

    // PostInInterfaceCorrespondingVrf
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE1, INTERFACE1), new PostInVrf(NODE1, VRF1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE1, INTERFACE2), new PostInVrf(NODE1, VRF1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE1, INTERFACE3), new PostInVrf(NODE1, VRF2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE1, INTERFACE4), new PostInVrf(NODE1, VRF2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE2, INTERFACE1), new PostInVrf(NODE2, VRF1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE2, INTERFACE2), new PostInVrf(NODE2, VRF1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE2, INTERFACE3), new PostInVrf(NODE2, VRF2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostInInterface(NODE2, INTERFACE4), new PostInVrf(NODE2, VRF2))));
  }

  @Test
  public void testVisitPostOutEdge() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledEdges(
                ImmutableSet.of(
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new Edge(NODE1, INTERFACE2, NODE2, INTERFACE2),
                    new Edge(NODE1, INTERFACE3, NODE2, INTERFACE3),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE1)))
            .setOutgoingAcls(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(INTERFACE1, ACL1, INTERFACE2, ACL2),
                    NODE2,
                    ImmutableMap.of(INTERFACE1, ACL1, INTERFACE2, ACL2)))
            .setSourceNats(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        INTERFACE1,
                        ImmutableList.of(
                            Maps.immutableEntry(new AclPermit(NODE1, NAT_ACL1), TrueExpr.INSTANCE),
                            Maps.immutableEntry(
                                new AclPermit(NODE1, NAT_ACL2), FalseExpr.INSTANCE)),
                        INTERFACE2,
                        ImmutableList.of(),
                        INTERFACE3,
                        ImmutableList.of()),
                    NODE2,
                    ImmutableMap.of(
                        INTERFACE1,
                        ImmutableList.of(
                            Maps.immutableEntry(new AclPermit(NODE2, NAT_ACL1), FalseExpr.INSTANCE),
                            Maps.immutableEntry(new AclPermit(NODE2, NAT_ACL2), TrueExpr.INSTANCE)),
                        INTERFACE2,
                        ImmutableList.of(),
                        INTERFACE3,
                        ImmutableList.of())))
            .setNodesWithSrcInterfaceConstraints(ImmutableSet.of(NODE1))
            .setSrcInterfaceField(SRC_INTERFACE_FIELD)
            .setSrcInterfaceFieldValues(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        INTERFACE1, i(1),
                        INTERFACE2, i(2),
                        INTERFACE3, i(3))))
            .setTopologyInterfaces(
                ImmutableMap.of(
                    NODE1,
                    ImmutableSet.of(INTERFACE1, INTERFACE2, INTERFACE3),
                    NODE2,
                    ImmutableSet.of(INTERFACE1, INTERFACE2, INTERFACE3)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PostOutEdge.State.INSTANCE)));
    // PassOutgoingAcl
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(
                    new TransformedVarIntExpr(SRC_INTERFACE_FIELD),
                    new LitIntExpr(0, SRC_INTERFACE_FIELD.getSize())),
                ImmutableSet.of(
                    new AclPermit(NODE1, ACL1),
                    new PreOutEdgePostNat(NODE1, INTERFACE1, NODE2, INTERFACE1)),
                new PostOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(
                    new TransformedVarIntExpr(SRC_INTERFACE_FIELD),
                    new LitIntExpr(0, SRC_INTERFACE_FIELD.getSize())),
                ImmutableSet.of(
                    new AclPermit(NODE1, ACL2),
                    new PreOutEdgePostNat(NODE1, INTERFACE2, NODE2, INTERFACE2)),
                new PostOutEdge(NODE1, INTERFACE2, NODE2, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(
                    new TransformedVarIntExpr(SRC_INTERFACE_FIELD),
                    new LitIntExpr(0, SRC_INTERFACE_FIELD.getSize())),
                ImmutableSet.of(new PreOutEdgePostNat(NODE1, INTERFACE3, NODE2, INTERFACE3)),
                new PostOutEdge(NODE1, INTERFACE3, NODE2, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                ImmutableSet.of(
                    new AclPermit(NODE2, ACL1),
                    new PreOutEdgePostNat(NODE2, INTERFACE1, NODE1, INTERFACE1)),
                new PostOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE1))));
  }

  @Test
  public void testVisitPostOutEdge_nonTransitNodes() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledEdges(
                ImmutableSet.of(
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE1)))
            .setOutgoingAcls(
                ImmutableMap.of(
                    NODE1, ImmutableMap.of(),
                    NODE2, ImmutableMap.of()))
            .setNonTransitNodes(ImmutableSet.of(NODE1))
            .setTopologyInterfaces(
                ImmutableMap.of(
                    NODE1, ImmutableSet.of(INTERFACE1), NODE2, ImmutableSet.of(INTERFACE1)))
            .build();

    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PostOutEdge.State.INSTANCE)));

    // node1 is a non-transit node, so set its flag in the transitedNonTransitNodesField
    assertThat(
        rules,
        not(
            hasItem(
                new BasicRuleStatement(
                    new PreOutEdgePostNat(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new PostOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE1)))));

    // node2 is not a non-transit node, so don't update transitedNodesField
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdgePostNat(NODE2, INTERFACE1, NODE1, INTERFACE1),
                new PostOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE1))));
  }

  @Test
  public void testVisitPostOutEdge_transitNodes() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledInterfacesByNodeVrf(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(VRF1, ImmutableSet.of(INTERFACE1)),
                    NODE2,
                    ImmutableMap.of(VRF1, ImmutableSet.of(INTERFACE1))))
            .setEnabledEdges(
                ImmutableSet.of(
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE1)))
            .setOutgoingAcls(
                ImmutableMap.of(
                    NODE1, ImmutableMap.of(),
                    NODE2, ImmutableMap.of()))
            .setTransitNodes(ImmutableSet.of(NODE1))
            .setTopologyInterfaces(
                ImmutableMap.of(
                    NODE1, ImmutableSet.of(INTERFACE1), NODE2, ImmutableSet.of(INTERFACE1)))
            .build();

    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PostInVrf.State.INSTANCE, PostOutEdge.State.INSTANCE)));

    // node1 is a transit node, so set its flag in the transitedTransitNodesField
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(
                    new TransformedVarIntExpr(
                        DefaultTransitionGenerator.TRANSITED_TRANSIT_NODES_FIELD),
                    new LitIntExpr(1, 1)),
                new PreOutEdgePostNat(NODE1, INTERFACE1, NODE2, INTERFACE1),
                new PostOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE1))));

    // node2 is not a transit node, so don't update transitedNodesField
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PreOutEdgePostNat(NODE2, INTERFACE1, NODE1, INTERFACE1),
                new PostOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE1))));
  }

  @Test
  public void testVisitPreInInterface() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            // enabledInterface affects which OriginateInterfaceLink rules are generated
            .setEnabledInterfaces(
                ImmutableMap.of(
                    NODE1, ImmutableSet.of(INTERFACE1, INTERFACE2, INTERFACE3),
                    NODE2, ImmutableSet.of(INTERFACE1, INTERFACE2, INTERFACE3)))
            .setEnabledEdges(
                ImmutableSet.of(
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE2),
                    new Edge(NODE1, INTERFACE1, NODE2, INTERFACE3),
                    new Edge(NODE1, INTERFACE2, NODE2, INTERFACE1),
                    new Edge(NODE1, INTERFACE2, NODE2, INTERFACE2),
                    new Edge(NODE1, INTERFACE2, NODE2, INTERFACE3),
                    new Edge(NODE1, INTERFACE3, NODE2, INTERFACE1),
                    new Edge(NODE1, INTERFACE3, NODE2, INTERFACE2),
                    new Edge(NODE1, INTERFACE3, NODE2, INTERFACE3),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE1),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE2),
                    new Edge(NODE2, INTERFACE1, NODE1, INTERFACE3),
                    new Edge(NODE2, INTERFACE2, NODE1, INTERFACE1),
                    new Edge(NODE2, INTERFACE2, NODE1, INTERFACE2),
                    new Edge(NODE2, INTERFACE2, NODE1, INTERFACE3),
                    new Edge(NODE2, INTERFACE3, NODE1, INTERFACE1),
                    new Edge(NODE2, INTERFACE3, NODE1, INTERFACE2),
                    new Edge(NODE2, INTERFACE3, NODE1, INTERFACE3)))
            .setNodeInterfaces(
                ImmutableMap.of(
                    NODE1, ImmutableList.of(INTERFACE1, INTERFACE2, INTERFACE3),
                    NODE2, ImmutableList.of(INTERFACE1, INTERFACE2, INTERFACE3)))
            .setNodesWithSrcInterfaceConstraints(ImmutableSet.of(NODE1))
            .setSrcInterfaceField(SRC_INTERFACE_FIELD)
            .setSrcInterfaceFieldValues(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        INTERFACE1, i(1),
                        INTERFACE2, i(2),
                        INTERFACE3, i(3))))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PreInInterface.State.INSTANCE)));

    // PreInInterface
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new EqExpr(new VarIntExpr(SRC_INTERFACE_FIELD), i(1)),
                        // transit nodes constraint
                        TrueExpr.INSTANCE)),
                new OriginateInterfaceLink(NODE1, INTERFACE1),
                new PreInInterface(NODE1, INTERFACE1))));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new EqExpr(new VarIntExpr(SRC_INTERFACE_FIELD), i(2)),
                        // transit nodes constraint
                        TrueExpr.INSTANCE)),
                new OriginateInterfaceLink(NODE1, INTERFACE2),
                new PreInInterface(NODE1, INTERFACE2))));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new EqExpr(new VarIntExpr(SRC_INTERFACE_FIELD), i(3)),
                        // transit nodes constraint
                        TrueExpr.INSTANCE)),
                new OriginateInterfaceLink(NODE1, INTERFACE3),
                new PreInInterface(NODE1, INTERFACE3))));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        NO_SRC_INTERFACE_FIELD,
                        // transit nodes constraint
                        TrueExpr.INSTANCE)),
                new OriginateInterfaceLink(NODE2, INTERFACE1),
                new PreInInterface(NODE2, INTERFACE1))));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        NO_SRC_INTERFACE_FIELD,
                        // transit nodes constraint
                        TrueExpr.INSTANCE)),
                new OriginateInterfaceLink(NODE2, INTERFACE2),
                new PreInInterface(NODE2, INTERFACE2))));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        NO_SRC_INTERFACE_FIELD,
                        // transit nodes constraint
                        TrueExpr.INSTANCE)),
                new OriginateInterfaceLink(NODE2, INTERFACE2),
                new PreInInterface(NODE2, INTERFACE2))));

    // PostOutNeighbor
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE1),
                new PreInInterface(NODE2, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE2),
                new PreInInterface(NODE2, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE3),
                new PreInInterface(NODE2, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE2, NODE2, INTERFACE1),
                new PreInInterface(NODE2, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE2, NODE2, INTERFACE2),
                new PreInInterface(NODE2, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE2, NODE2, INTERFACE3),
                new PreInInterface(NODE2, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE3, NODE2, INTERFACE1),
                new PreInInterface(NODE2, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE3, NODE2, INTERFACE2),
                new PreInInterface(NODE2, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new PostOutEdge(NODE1, INTERFACE3, NODE2, INTERFACE3),
                new PreInInterface(NODE2, INTERFACE3))));

    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(1)),
                new PostOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE1),
                new PreInInterface(NODE1, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(2)),
                new PostOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE2),
                new PreInInterface(NODE1, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(3)),
                new PostOutEdge(NODE2, INTERFACE1, NODE1, INTERFACE3),
                new PreInInterface(NODE1, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(1)),
                new PostOutEdge(NODE2, INTERFACE2, NODE1, INTERFACE1),
                new PreInInterface(NODE1, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(2)),
                new PostOutEdge(NODE2, INTERFACE2, NODE1, INTERFACE2),
                new PreInInterface(NODE1, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(3)),
                new PostOutEdge(NODE2, INTERFACE2, NODE1, INTERFACE3),
                new PreInInterface(NODE1, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(1)),
                new PostOutEdge(NODE2, INTERFACE3, NODE1, INTERFACE1),
                new PreInInterface(NODE1, INTERFACE1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(2)),
                new PostOutEdge(NODE2, INTERFACE3, NODE1, INTERFACE2),
                new PreInInterface(NODE1, INTERFACE2))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new EqExpr(new TransformedVarIntExpr(SRC_INTERFACE_FIELD), i(3)),
                new PostOutEdge(NODE2, INTERFACE3, NODE1, INTERFACE3),
                new PreInInterface(NODE1, INTERFACE3))));
  }

  @Test
  public void testVisitPreOutVrf() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setIpsByNodeVrf(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(VRF1, ImmutableSet.of(IP1, IP2)),
                    NODE2,
                    ImmutableMap.of(VRF2, ImmutableSet.of(IP3, IP4))))
            .setNamedIpSpaces(ImmutableMap.of(NODE1, ImmutableMap.of(), NODE2, ImmutableMap.of()))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PreOutVrf.State.INSTANCE)));

    // PostInNotMine
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(
                    HeaderSpaceMatchExpr.matchDstIp(
                        IpWildcardSetIpSpace.builder()
                            .including(ImmutableSet.of(new IpWildcard(IP1), new IpWildcard(IP2)))
                            .build(),
                        ImmutableMap.of())),
                new PostInVrf(NODE1, VRF1),
                new PreOutVrf(NODE1, VRF1))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                new NotExpr(
                    HeaderSpaceMatchExpr.matchDstIp(
                        IpWildcardSetIpSpace.builder()
                            .including(ImmutableSet.of(new IpWildcard(IP3), new IpWildcard(IP4)))
                            .build(),
                        ImmutableMap.of())),
                new PostInVrf(NODE2, VRF2),
                new PreOutVrf(NODE2, VRF2))));
  }

  @Test
  public void testVisitPreOutEdge() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setArpTrueEdge(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        VRF1,
                        ImmutableMap.of(
                            INTERFACE1,
                            ImmutableMap.of(
                                NODE3,
                                ImmutableMap.of(INTERFACE3, b(1), INTERFACE4, b(2)),
                                NODE4,
                                ImmutableMap.of(INTERFACE3, b(3))),
                            INTERFACE2,
                            ImmutableMap.of(NODE3, ImmutableMap.of(INTERFACE3, b(4)))),
                        VRF2,
                        ImmutableMap.of(
                            INTERFACE3, ImmutableMap.of(NODE3, ImmutableMap.of(INTERFACE3, b(5))))),
                    NODE2,
                    ImmutableMap.of(
                        VRF1,
                        ImmutableMap.of(
                            INTERFACE1,
                            ImmutableMap.of(NODE3, ImmutableMap.of(INTERFACE3, b(6)))))))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(PreOutEdge.State.INSTANCE)));

    // DestinationRouting
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(1),
                new PreOutVrf(NODE1, VRF1),
                new PreOutEdge(NODE1, INTERFACE1, NODE3, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(2),
                new PreOutVrf(NODE1, VRF1),
                new PreOutEdge(NODE1, INTERFACE1, NODE3, INTERFACE4))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(3),
                new PreOutVrf(NODE1, VRF1),
                new PreOutEdge(NODE1, INTERFACE1, NODE4, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(4),
                new PreOutVrf(NODE1, VRF1),
                new PreOutEdge(NODE1, INTERFACE2, NODE3, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(5),
                new PreOutVrf(NODE1, VRF2),
                new PreOutEdge(NODE1, INTERFACE3, NODE3, INTERFACE3))));
    assertThat(
        rules,
        hasItem(
            new BasicRuleStatement(
                b(6),
                new PreOutVrf(NODE2, VRF1),
                new PreOutEdge(NODE2, INTERFACE1, NODE3, INTERFACE3))));
  }

  /** Test the transitions generated for PreOutEdgePostNat for an edge with a source nat. */
  @Test
  public void testVisitPreOutEdgePostNat_topologyInterfaceWithNAT() {
    SynthesizerInput input =
        MockSynthesizerInput.builder()
            .setEnabledEdges(ImmutableSet.of(new Edge(NODE1, INTERFACE1, NODE2, INTERFACE2)))
            .setTopologyInterfaces(ImmutableMap.of(NODE1, ImmutableSet.of(INTERFACE1)))
            .setSourceNats(
                ImmutableMap.of(
                    NODE1,
                    ImmutableMap.of(
                        INTERFACE1,
                        ImmutableList.of(Maps.immutableEntry(new AclPermit(NODE1, NAT_ACL1), B1)))))
            .build();
    List<RuleStatement> rules =
        DefaultTransitionGenerator.generateTransitions(
            input, ImmutableSet.of(PreOutEdgePostNat.State.INSTANCE));

    RuleStatement permitRule =
        new BasicRuleStatement(
            B1,
            ImmutableSet.of(
                new PreOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE2),
                new AclPermit(NODE1, NAT_ACL1)),
            new PreOutEdgePostNat(NODE1, INTERFACE1, NODE2, INTERFACE2));

    RuleStatement denyRule =
        new BasicRuleStatement(
            ImmutableSet.of(
                new PreOutEdge(NODE1, INTERFACE1, NODE2, INTERFACE2), new AclDeny(NODE1, NAT_ACL1)),
            new PreOutEdgePostNat(NODE1, INTERFACE1, NODE2, INTERFACE2));

    assertThat(rules, containsInAnyOrder(permitRule, denyRule));
  }
}
