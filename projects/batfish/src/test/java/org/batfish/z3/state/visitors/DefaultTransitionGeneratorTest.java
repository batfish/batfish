package org.batfish.z3.state.visitors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.TestSynthesizerInput;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.state.Accept;
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
import org.junit.Before;
import org.junit.Test;

public class DefaultTransitionGeneratorTest {

  private static String NODE1 = "node1";

  private static String NODE2 = "node2";

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

  //  @Test
  //  public void testVisitAclDeny() {
  //    SynthesizerInput input =
  //        TestSynthesizerInput.builder().setAclActions(ImmutableMap.of()).build();
  //    Set<RuleStatement> rules =
  //        ImmutableSet.copyOf(
  //            DefaultTransitionGenerator.generateTransitions(
  //                input, ImmutableSet.of(Accept.State.INSTANCE)));
  //
  //    Set<RuleStatement> expectedMatchDenyLine = ImmutableSet.of();
  //
  //    Set<RuleStatement> expectedMatchNoLines = ImmutableSet.of();
  //
  //    assertThat(rules, equalTo(Sets.union(expectedMatchDenyLine, expectedMatchNoLines)));
  //  }

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
