package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.batfish.z3.expr.RuleStatement;
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
import org.batfish.z3.state.NodeNeighborUnreachable;
import org.batfish.z3.state.Originate;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostIn;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutEdge;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOut;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.visitors.DefaultTransitionGenerator;

public class Synthesizer {

  private final SynthesizerInput _input;

  private List<String> _warnings;

  public Synthesizer(SynthesizerInput input) {
    _input = input;
    _warnings = new ArrayList<>();
  }

  public SynthesizerInput getInput() {
    return _input;
  }

  public List<String> getWarnings() {
    return _warnings;
  }

  public ReachabilityProgram synthesizeNodAclProgram(String hostname, String aclName) {
    return synthesizeNodProgram(
        ImmutableList.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                _input,
                ImmutableSet.of(
                    AclDeny.State.INSTANCE,
                    AclLineMatch.State.INSTANCE,
                    AclLineNoMatch.State.INSTANCE,
                    AclPermit.State.INSTANCE))));
  }

  public ReachabilityProgram synthesizeNodDataPlaneProgram() {
    return synthesizeNodProgram(
        ImmutableList.copyOf(
            DefaultTransitionGenerator.generateTransitions(
                _input,
                ImmutableSet.of(
                    Accept.State.INSTANCE,
                    AclDeny.State.INSTANCE,
                    AclLineMatch.State.INSTANCE,
                    AclLineNoMatch.State.INSTANCE,
                    AclPermit.State.INSTANCE,
                    Drop.State.INSTANCE,
                    DropAcl.State.INSTANCE,
                    DropAclIn.State.INSTANCE,
                    DropAclOut.State.INSTANCE,
                    DropNoRoute.State.INSTANCE,
                    DropNullRoute.State.INSTANCE,
                    NeighborUnreachable.State.INSTANCE,
                    NodeAccept.State.INSTANCE,
                    NodeDrop.State.INSTANCE,
                    NodeDropAcl.State.INSTANCE,
                    NodeDropAclIn.State.INSTANCE,
                    NodeDropAclOut.State.INSTANCE,
                    NodeDropNoRoute.State.INSTANCE,
                    NodeDropNullRoute.State.INSTANCE,
                    NodeNeighborUnreachable.State.INSTANCE,
                    Originate.State.INSTANCE,
                    OriginateVrf.State.INSTANCE,
                    PostIn.State.INSTANCE,
                    PostInInterface.State.INSTANCE,
                    PostInVrf.State.INSTANCE,
                    PostOutEdge.State.INSTANCE,
                    PreInInterface.State.INSTANCE,
                    PreOut.State.INSTANCE,
                    PreOutEdge.State.INSTANCE,
                    PreOutEdgePostNat.State.INSTANCE))));
  }

  private ReachabilityProgram synthesizeNodProgram(List<RuleStatement> ruleStatements) {
    return ReachabilityProgram.builder().setRules(ruleStatements).setInput(_input).build();
  }
}
