package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineIndependentMatch;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.AclLineNoMatch;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.DropAcl;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.NeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDrop;
import org.batfish.z3.state.NodeDropAcl;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.NodeNeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutEdge;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutVrf;
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

  public ReachabilityProgram synthesizeNodProgram() {
    // Only want these 5 states if NoD program is for ACL reachability (only non-data-plane type)
    ImmutableSet.Builder<StateExpr.State> builder =
        ImmutableSet.<StateExpr.State>builder()
            .addAll(
                ImmutableSet.of(
                    AclDeny.State.INSTANCE,
                    AclLineIndependentMatch.State.INSTANCE,
                    AclLineMatch.State.INSTANCE,
                    AclLineNoMatch.State.INSTANCE,
                    AclPermit.State.INSTANCE));
    // Otherwise, we also want these other states
    if (_input.isDataPlane()) {
      builder.addAll(
          ImmutableSet.of(
              Accept.State.INSTANCE,
              Drop.State.INSTANCE,
              DropAcl.State.INSTANCE,
              DropAclIn.State.INSTANCE,
              DropAclOut.State.INSTANCE,
              DropNoRoute.State.INSTANCE,
              DropNullRoute.State.INSTANCE,
              NeighborUnreachableOrExitsNetwork.State.INSTANCE,
              NodeAccept.State.INSTANCE,
              NodeDrop.State.INSTANCE,
              NodeDropAcl.State.INSTANCE,
              NodeDropAclIn.State.INSTANCE,
              NodeDropAclOut.State.INSTANCE,
              NodeDropNoRoute.State.INSTANCE,
              NodeDropNullRoute.State.INSTANCE,
              NodeInterfaceNeighborUnreachableOrExitsNetwork.State.INSTANCE,
              NodeNeighborUnreachableOrExitsNetwork.State.INSTANCE,
              OriginateVrf.State.INSTANCE,
              PostInInterface.State.INSTANCE,
              PostInVrf.State.INSTANCE,
              PostOutEdge.State.INSTANCE,
              PreInInterface.State.INSTANCE,
              PreOutEdge.State.INSTANCE,
              PreOutEdgePostNat.State.INSTANCE,
              PreOutVrf.State.INSTANCE));
    }
    return synthesizeNodProgram(
        ImmutableList.copyOf(
            DefaultTransitionGenerator.generateTransitions(_input, builder.build())));
  }

  private ReachabilityProgram synthesizeNodProgram(List<RuleStatement> ruleStatements) {
    return ReachabilityProgram.builder().setRules(ruleStatements).setInput(_input).build();
  }
}
