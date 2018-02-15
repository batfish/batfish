package org.batfish.z3.state.visitors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
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

  private Configuration.Builder _cb;

  private NetworkFactory _nf;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
  }

  @Test
  public void testVisitAccept() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableMap.of(c1.getName(), c1, c2.getName(), c2))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(Accept.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(
                        ImmutableList.of(
                            new NodeAccept(c1.getName()), new NodeAccept(c2.getName()))),
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
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableMap.of(c1.getName(), c1, c2.getName(), c2))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(Drop.State.INSTANCE)));

    assertThat(
        rules,
        equalTo(
            ImmutableSet.of(
                new RuleStatement(
                    new OrExpr(
                        ImmutableList.of(new NodeDrop(c1.getName()), new NodeDrop(c2.getName()))),
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
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableMap.of(c1.getName(), c1, c2.getName(), c2))
            .build();
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
                        ImmutableList.of(
                            new NodeDropAclIn(c1.getName()), new NodeDropAclIn(c2.getName()))),
                    DropAclIn.INSTANCE))));
  }

  @Test
  public void testVisitDropAclOut() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableMap.of(c1.getName(), c1, c2.getName(), c2))
            .build();
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
                        ImmutableList.of(
                            new NodeDropAclOut(c1.getName()), new NodeDropAclOut(c2.getName()))),
                    DropAclOut.INSTANCE))));
  }

  @Test
  public void testVisitDropNoRoute() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableMap.of(c1.getName(), c1, c2.getName(), c2))
            .build();
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
                        ImmutableList.of(
                            new NodeDropNoRoute(c1.getName()), new NodeDropNoRoute(c2.getName()))),
                    DropNoRoute.INSTANCE))));
  }

  @Test
  public void testVisitDropNullRoute() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableMap.of(c1.getName(), c1, c2.getName(), c2))
            .build();
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
                            new NodeDropNullRoute(c1.getName()),
                            new NodeDropNullRoute(c2.getName()))),
                    DropNullRoute.INSTANCE))));
  }

  @Test
  public void testVisitNodeDropAcl() {
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    SynthesizerInput input =
        TestSynthesizerInput.builder()
            .setEnabledNodes(ImmutableMap.of(c1.getName(), c1, c2.getName(), c2))
            .build();
    Set<RuleStatement> rules =
        ImmutableSet.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                input, ImmutableSet.of(NodeDropAcl.State.INSTANCE)));

    Set<RuleStatement> expectedCopyNodeDropAclIn =
        ImmutableSet.of(
            new RuleStatement(new NodeDropAclIn(c1.getName()), new NodeDropAcl(c1.getName())),
            new RuleStatement(new NodeDropAclIn(c2.getName()), new NodeDropAcl(c2.getName())));

    Set<RuleStatement> expectedCopyNodeDropAclOut =
        ImmutableSet.of(
            new RuleStatement(new NodeDropAclOut(c1.getName()), new NodeDropAcl(c1.getName())),
            new RuleStatement(new NodeDropAclOut(c2.getName()), new NodeDropAcl(c2.getName())));

    assertThat(rules, equalTo(Sets.union(expectedCopyNodeDropAclIn, expectedCopyNodeDropAclOut)));
  }
}
