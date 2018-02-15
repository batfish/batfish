package org.batfish.z3.state.visitors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.TestSynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.TrueExpr;
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
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDrop;
import org.batfish.z3.state.NodeDropAcl;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.PostIn;
import org.batfish.z3.state.PostOutInterface;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class DefaultTransitionGeneratorTest {

  private static String ACL1 = "acl1";

  private static String ACL2 = "acl2";

  private static String ACL3 = "acl3";

  private static String INTERFACE1 = "interface1";

  private static String INTERFACE2 = "interface2";
  
  private static Ip IP1 = new Ip("1.1.1.1");

  private static Ip IP2 = new Ip("2.2.2.2");

  private static Ip IP3 = new Ip("3.3.3.3");

  private static Ip IP4 = new Ip("4.4.4.4");

  private static String NODE1 = "node1";

  private static String NODE2 = "node2";

  private Map<String, Map<String, Map<Integer, LineAction>>> aclActions() {
    ImmutableMap<Integer, LineAction> acl1ActionsByLine =
        ImmutableMap.of(
            0, LineAction.ACCEPT, 1, LineAction.REJECT, 2, LineAction.ACCEPT, 3, LineAction.REJECT);
    ImmutableMap<Integer, LineAction> acl2ActionsByLine =
        ImmutableMap.of(
            0, LineAction.REJECT, 1, LineAction.ACCEPT, 2, LineAction.REJECT, 3, LineAction.ACCEPT);
    ImmutableMap<String, Map<Integer, LineAction>> aclActions =
        ImmutableMap.of(ACL1, acl1ActionsByLine, ACL2, acl2ActionsByLine, ACL3, ImmutableMap.of());
    Map<String, Map<String, Map<Integer, LineAction>>> aclActionss =
        ImmutableMap.of(NODE1, aclActions, NODE2, aclActions);
    return aclActionss;
  }

  private Map<String, Map<String, Map<Integer, BooleanExpr>>> aclConditions() {
    ImmutableMap<Integer, BooleanExpr> acl1ConditionsByLine =
        ImmutableMap.of(
            0,
            TrueExpr.INSTANCE,
            1,
            FalseExpr.INSTANCE,
            2,
            TrueExpr.INSTANCE,
            3,
            FalseExpr.INSTANCE);
    ImmutableMap<Integer, BooleanExpr> acl2ConditionsByLine =
        ImmutableMap.of(
            0,
            FalseExpr.INSTANCE,
            1,
            TrueExpr.INSTANCE,
            2,
            FalseExpr.INSTANCE,
            3,
            TrueExpr.INSTANCE);
    ImmutableMap<String, Map<Integer, BooleanExpr>> aclConditions =
        ImmutableMap.of(
            ACL1, acl1ConditionsByLine, ACL2, acl2ConditionsByLine, ACL3, ImmutableMap.of());
    Map<String, Map<String, Map<Integer, BooleanExpr>>> acl =
        ImmutableMap.of(NODE1, aclConditions, NODE2, aclConditions);
    return acl;
  }

  @Before
  public void setup() {}

  @Test
  public void testVisitAccept() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(Accept.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(ImmutableList.of(new NodeAccept(NODE1), new NodeAccept(NODE2))),
                    Accept.INSTANCE))));
  }

  @Test
  public void testVisitAclDeny() {
    SynthesizerInput input = TestSynthesizerInput.builder().setAclActions(aclActions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclDeny.State.INSTANCE)));

    // MatchDenyLine
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL1, 1), new AclDeny(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL1, 3), new AclDeny(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL2, 0), new AclDeny(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL2, 2), new AclDeny(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL1, 1), new AclDeny(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL1, 3), new AclDeny(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL2, 0), new AclDeny(NODE2, ACL2))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL2, 2), new AclDeny(NODE2, ACL2))));

    // MatchNoLines
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineNoMatch(NODE1, ACL1, 3), new AclDeny(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineNoMatch(NODE1, ACL2, 3), new AclDeny(NODE1, ACL2))));
    assertThat(rules, hasItem(new RuleStatement(new AclDeny(NODE1, ACL3))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineNoMatch(NODE2, ACL1, 3), new AclDeny(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineNoMatch(NODE2, ACL2, 3), new AclDeny(NODE2, ACL2))));
    assertThat(rules, hasItem(new RuleStatement(new AclDeny(NODE1, ACL3))));
  }

  @Test
  public void testVisitAclLineMatch() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setAclConditions(aclConditions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclLineMatch.State.INSTANCE)));

    // MatchCurrentAndDontMatchPrevious
    assertThat(
        rules, hasItem(new RuleStatement(TrueExpr.INSTANCE, new AclLineMatch(NODE1, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(FalseExpr.INSTANCE, new AclLineNoMatch(NODE1, ACL1, 0))),
                new AclLineMatch(NODE1, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(TrueExpr.INSTANCE, new AclLineNoMatch(NODE1, ACL1, 1))),
                new AclLineMatch(NODE1, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(FalseExpr.INSTANCE, new AclLineNoMatch(NODE1, ACL1, 2))),
                new AclLineMatch(NODE1, ACL1, 3))));
    assertThat(
        rules, hasItem(new RuleStatement(FalseExpr.INSTANCE, new AclLineMatch(NODE1, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(TrueExpr.INSTANCE, new AclLineNoMatch(NODE1, ACL2, 0))),
                new AclLineMatch(NODE1, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(FalseExpr.INSTANCE, new AclLineNoMatch(NODE1, ACL2, 1))),
                new AclLineMatch(NODE1, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(TrueExpr.INSTANCE, new AclLineNoMatch(NODE1, ACL2, 2))),
                new AclLineMatch(NODE1, ACL2, 3))));
    assertThat(
        rules, hasItem(new RuleStatement(TrueExpr.INSTANCE, new AclLineMatch(NODE2, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(FalseExpr.INSTANCE, new AclLineNoMatch(NODE2, ACL1, 0))),
                new AclLineMatch(NODE2, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(TrueExpr.INSTANCE, new AclLineNoMatch(NODE2, ACL1, 1))),
                new AclLineMatch(NODE2, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(FalseExpr.INSTANCE, new AclLineNoMatch(NODE2, ACL1, 2))),
                new AclLineMatch(NODE2, ACL1, 3))));
    assertThat(
        rules, hasItem(new RuleStatement(FalseExpr.INSTANCE, new AclLineMatch(NODE2, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(TrueExpr.INSTANCE, new AclLineNoMatch(NODE2, ACL2, 0))),
                new AclLineMatch(NODE2, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(FalseExpr.INSTANCE, new AclLineNoMatch(NODE2, ACL2, 1))),
                new AclLineMatch(NODE2, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(TrueExpr.INSTANCE, new AclLineNoMatch(NODE2, ACL2, 2))),
                new AclLineMatch(NODE2, ACL2, 3))));
  }

  @Test
  public void testVisitAclLineNoMatch() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setAclConditions(aclConditions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclLineNoMatch.State.INSTANCE)));

    assertThat(
        rules,
        hasItem(
            new RuleStatement(new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL1, 0))),
                new AclLineNoMatch(NODE1, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL1, 1))),
                new AclLineNoMatch(NODE1, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL1, 2))),
                new AclLineNoMatch(NODE1, ACL1, 3))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL2, 0))),
                new AclLineNoMatch(NODE1, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL2, 1))),
                new AclLineNoMatch(NODE1, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE1, ACL2, 2))),
                new AclLineNoMatch(NODE1, ACL2, 3))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL1, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL1, 0))),
                new AclLineNoMatch(NODE2, ACL1, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL1, 1))),
                new AclLineNoMatch(NODE2, ACL1, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL1, 2))),
                new AclLineNoMatch(NODE2, ACL1, 3))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL2, 0))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL2, 0))),
                new AclLineNoMatch(NODE2, ACL2, 1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(FalseExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL2, 1))),
                new AclLineNoMatch(NODE2, ACL2, 2))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new NotExpr(TrueExpr.INSTANCE), new AclLineNoMatch(NODE2, ACL2, 2))),
                new AclLineNoMatch(NODE2, ACL2, 3))));
  }

  @Test
  public void testVisitAclPermit() {
    SynthesizerInput input = TestSynthesizerInput.builder().setAclActions(aclActions()).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(AclPermit.State.INSTANCE)));

    // MatchPermitLine
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL1, 0), new AclPermit(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL1, 2), new AclPermit(NODE1, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL2, 1), new AclPermit(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE1, ACL2, 3), new AclPermit(NODE1, ACL2))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL1, 0), new AclPermit(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL1, 2), new AclPermit(NODE2, ACL1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL2, 1), new AclPermit(NODE2, ACL2))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new AclLineMatch(NODE2, ACL2, 3), new AclPermit(NODE2, ACL2))));
  }

  @Test
  public void testVisitDrop() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(Drop.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(ImmutableList.of(new NodeDrop(NODE1), new NodeDrop(NODE2))),
                    Drop.INSTANCE))));
  }

  @Test
  public void testVisitDropAcl() {
    SynthesizerInput input = TestSynthesizerInput.builder().build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropAcl.State.INSTANCE)));

    Set<RuleStatement> expectedCopyDropAclIn =
        ImmutableSet.of(new RuleStatement(DropAclIn.INSTANCE, DropAcl.INSTANCE));

    Set<RuleStatement> expectedCopyDropAclOut =
        ImmutableSet.of(new RuleStatement(DropAclOut.INSTANCE, DropAcl.INSTANCE));

    assertThat(rules, equalTo(Sets.union(expectedCopyDropAclIn, expectedCopyDropAclOut)));
  }

  @Test
  public void testVisitDropAclIn() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropAclIn.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(
                        ImmutableList.of(new NodeDropAclIn(NODE1), new NodeDropAclIn(NODE2))),
                    DropAclIn.INSTANCE))));
  }

  @Test
  public void testVisitDropAclOut() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropAclOut.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(
                        ImmutableList.of(new NodeDropAclOut(NODE1), new NodeDropAclOut(NODE2))),
                    DropAclOut.INSTANCE))));
  }

  @Test
  public void testVisitDropNoRoute() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropNoRoute.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(
                        ImmutableList.of(new NodeDropNoRoute(NODE1), new NodeDropNoRoute(NODE2))),
                    DropNoRoute.INSTANCE))));
  }

  @Test
  public void testVisitDropNullRoute() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(DropNullRoute.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(
                        ImmutableList.of(
                            new NodeDropNullRoute(NODE1), new NodeDropNullRoute(NODE2))),
                    DropNullRoute.INSTANCE))));
  }

  @Test
  public void testVisitNodeAccept() {
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableSet.of(NODE1, NODE2))
            .setEnabledFlowSinks(
                ImmutableSet.of(
                    new NodeInterfacePair(NODE1, INTERFACE1),
                    new NodeInterfacePair(NODE1, INTERFACE2),
                    new NodeInterfacePair(NODE2, INTERFACE1),
                    new NodeInterfacePair(NODE2, INTERFACE2)))
            .setIpsByHostname(
                ImmutableMap.of(NODE1, ImmutableSet.of(IP1, IP2), NODE2, ImmutableSet.of(IP3, IP4)))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeAccept.State.INSTANCE)));

    // PostInForMe
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new PostIn(NODE1),
                        HeaderSpaceMatchExpr.matchDstIp(
                            ImmutableSet.of(new IpWildcard(IP1), new IpWildcard(IP2))))),
                new NodeAccept(NODE1))));
    assertThat(
        rules,
        hasItem(
            new RuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new PostIn(NODE2),
                        HeaderSpaceMatchExpr.matchDstIp(
                            ImmutableSet.of(new IpWildcard(IP3), new IpWildcard(IP4))))),
                new NodeAccept(NODE2))));

    // PostOutFlowSinkInterface
    assertThat(
        rules,
        hasItem(new RuleStatement(new PostOutInterface(NODE1, INTERFACE1), new NodeAccept(NODE1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new PostOutInterface(NODE1, INTERFACE2), new NodeAccept(NODE1))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new PostOutInterface(NODE2, INTERFACE1), new NodeAccept(NODE2))));
    assertThat(
        rules,
        hasItem(new RuleStatement(new PostOutInterface(NODE2, INTERFACE2), new NodeAccept(NODE2))));

  }

  @Test
  public void testVisitNodeDropAcl() {
    SynthesizerInput input =
        TestSynthesizerInput.builder().setEnabledNodes(ImmutableSet.of(NODE1, NODE2)).build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropAcl.State.INSTANCE)));

    Set<RuleStatement> expectedCopyNodeDropAclIn =
        ImmutableSet.of(
            new RuleStatement(new NodeDropAclIn(NODE1), new NodeDropAcl(NODE1)),
            new RuleStatement(new NodeDropAclIn(NODE2), new NodeDropAcl(NODE2)));

    Set<RuleStatement> expectedCopyNodeDropAclOut =
        ImmutableSet.of(
            new RuleStatement(new NodeDropAclOut(NODE1), new NodeDropAcl(NODE1)),
            new RuleStatement(new NodeDropAclOut(NODE2), new NodeDropAcl(NODE2)));

    assertThat(rules, equalTo(Sets.union(expectedCopyNodeDropAclIn, expectedCopyNodeDropAclOut)));
  }
}
