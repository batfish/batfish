package org.batfish.z3.state.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.TransformationHeaderField;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExpr.State;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.AclLineNoMatch;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.Debug;
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
import org.batfish.z3.state.NumberedQuery;
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
import org.batfish.z3.state.Query;

public class DefaultTransitionGenerator implements StateVisitor {
  public static List<RuleStatement> generateTransitions(SynthesizerInput input, Set<State> states) {
    DefaultTransitionGenerator visitor = new DefaultTransitionGenerator(input);
    states.forEach(state -> state.accept(visitor));
    return visitor._rules.build();
  }

  private final SynthesizerInput _input;

  private ImmutableList.Builder<RuleStatement> _rules;

  public DefaultTransitionGenerator(SynthesizerInput input) {
    _input = input;
    _rules = ImmutableList.builder();
  }

  @Override
  public void visitAccept(Accept.State accept) {
    // ProjectNodeAccept
    _input
        .getEnabledNodes()
        .stream()
        .map(hostname -> new BasicRuleStatement(new NodeAccept(hostname), Accept.INSTANCE))
        .forEach(_rules::add);
  }

  @Override
  public void visitAclDeny(AclDeny.State aclDeny) {
    // MatchDenyLine
    _input
        .getAclActions()
        .forEach(
            (node, nodeAcls) ->
                nodeAcls.forEach(
                    (acl, linesActions) -> {
                      int lineNumber = 0;
                      for (LineAction linesAction : linesActions) {
                        if (linesAction == LineAction.REJECT) {
                          _rules.add(
                              new BasicRuleStatement(
                                  new AclLineMatch(node, acl, lineNumber), new AclDeny(node, acl)));
                        }
                        lineNumber++;
                      }
                    }));

    // MatchNoLines
    _input
        .getAclActions()
        .entrySet()
        .stream()
        .flatMap(
            aclActionsEntryByNode -> {
              String hostname = aclActionsEntryByNode.getKey();
              return aclActionsEntryByNode
                  .getValue()
                  .entrySet()
                  .stream()
                  .map(
                      aclActionsEntryByAclName -> {
                        String acl = aclActionsEntryByAclName.getKey();
                        List<LineAction> lineActions = aclActionsEntryByAclName.getValue();
                        AclDeny deny = new AclDeny(hostname, acl);
                        if (lineActions.isEmpty()) {
                          return new BasicRuleStatement(deny);
                        } else {
                          int lastLine = lineActions.size() - 1;
                          return new BasicRuleStatement(
                              new AclLineNoMatch(hostname, acl, lastLine), deny);
                        }
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitAclLineMatch(AclLineMatch.State aclLineMatch) {
    // MatchCurrentAndDontMatchPrevious
    _input
        .getAclConditions()
        .entrySet()
        .stream()
        .flatMap(
            aclConditionsEntryByNode -> {
              String hostname = aclConditionsEntryByNode.getKey();
              return aclConditionsEntryByNode
                  .getValue()
                  .entrySet()
                  .stream()
                  .flatMap(
                      aclConditionsEntryByAclName -> {
                        String acl = aclConditionsEntryByAclName.getKey();
                        AtomicInteger lineNumber = new AtomicInteger(0);
                        return aclConditionsEntryByAclName
                            .getValue()
                            .stream()
                            .map(
                                lineCriteria -> {
                                  int line = lineNumber.getAndIncrement();
                                  Set<StateExpr> preconditionStates =
                                      line > 0
                                          ? ImmutableSet.of(
                                              new AclLineNoMatch(hostname, acl, line - 1))
                                          : ImmutableSet.of();
                                  return new BasicRuleStatement(
                                      lineCriteria,
                                      preconditionStates,
                                      new AclLineMatch(hostname, acl, line));
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitAclLineNoMatch(AclLineNoMatch.State aclLineNoMatch) {
    _input
        .getAclConditions()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return e.getValue()
                  .entrySet()
                  .stream()
                  .flatMap(
                      e2 -> {
                        String acl = e2.getKey();
                        AtomicInteger lineNumber = new AtomicInteger(0);
                        return e2.getValue()
                            .stream()
                            .map(
                                lineCriteria -> {
                                  int line = lineNumber.getAndIncrement();
                                  Set<StateExpr> preconditionStates =
                                      line > 0
                                          ? ImmutableSet.of(
                                              new AclLineNoMatch(hostname, acl, line - 1))
                                          : ImmutableSet.of();
                                  return new BasicRuleStatement(
                                      new NotExpr(lineCriteria),
                                      preconditionStates,
                                      new AclLineNoMatch(hostname, acl, line));
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitAclPermit(AclPermit.State aclPermit) {
    // MatchPermitLine
    _input
        .getAclActions()
        .forEach(
            (hostname, aclLineActions) ->
                aclLineActions.forEach(
                    (acl, lineActions) -> {
                      AtomicInteger lineNumber = new AtomicInteger(0);
                      lineActions.forEach(
                          lineAction -> {
                            int line = lineNumber.getAndIncrement();
                            if (lineAction == LineAction.ACCEPT) {
                              _rules.add(
                                  new BasicRuleStatement(
                                      new AclLineMatch(hostname, acl, line),
                                      new AclPermit(hostname, acl)));
                            }
                          });
                    }));
  }

  @Override
  public void visitDebug(Debug.State debug) {}

  @Override
  public void visitDrop(Drop.State drop) {
    // ProjectNodeDrop
    _input
        .getEnabledNodes()
        .stream()
        .map(hostname -> new BasicRuleStatement(new NodeDrop(hostname), Drop.INSTANCE))
        .forEach(_rules::add);
  }

  @Override
  public void visitDropAcl(DropAcl.State dropAcl) {
    // CopyDropAclIn
    _rules.add(new BasicRuleStatement(DropAclIn.INSTANCE, DropAcl.INSTANCE));

    // CopyDropAclOut
    _rules.add(new BasicRuleStatement(DropAclOut.INSTANCE, DropAcl.INSTANCE));

    // ProjectNodeDropAcl (unused for now)
    //    _rules.add(
    //        new RuleStatement(
    //            new OrExpr(
    //                _input
    //                    .getEnabledNodes()
    //                    .keySet()
    //                    .stream()
    //                    .map(NodeDropAcl::new)
    //                    .collect(ImmutableList.toImmutableList())),
    //            DropAcl.INSTANCE));
  }

  @Override
  public void visitDropAclIn(DropAclIn.State dropAclIn) {
    // ProjectNodeDropAclIn
    _input
        .getEnabledNodes()
        .stream()
        .map(hostname -> new BasicRuleStatement(new NodeDropAclIn(hostname), DropAclIn.INSTANCE))
        .forEach(_rules::add);
  }

  @Override
  public void visitDropAclOut(DropAclOut.State dropAclOut) {
    // ProjectNodeDropAclOut
    _input
        .getEnabledNodes()
        .stream()
        .map(hostname -> new BasicRuleStatement(new NodeDropAclOut(hostname), DropAclOut.INSTANCE))
        .forEach(_rules::add);
  }

  @Override
  public void visitDropNoRoute(DropNoRoute.State dropNoRoute) {
    // ProjectNodeDropNoRoute
    _input
        .getEnabledNodes()
        .stream()
        .map(
            hostname -> new BasicRuleStatement(new NodeDropNoRoute(hostname), DropNoRoute.INSTANCE))
        .forEach(_rules::add);
  }

  @Override
  public void visitDropNullRoute(DropNullRoute.State dropNullRoute) {
    // ProjectNodeDropNullRoute
    _input
        .getEnabledNodes()
        .stream()
        .map(
            hostname ->
                new BasicRuleStatement(new NodeDropNullRoute(hostname), DropNullRoute.INSTANCE))
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeAccept(NodeAccept.State nodeAccept) {
    // PostInForMe
    _input
        .getEnabledNodes()
        .stream()
        .map(
            hostname ->
                new BasicRuleStatement(
                    HeaderSpaceMatchExpr.matchDstIp(
                        _input
                            .getIpsByHostname()
                            .get(hostname)
                            .stream()
                            .map(IpWildcard::new)
                            .collect(ImmutableSet.toImmutableSet())),
                    ImmutableSet.of(new PostIn(hostname)),
                    new NodeAccept(hostname)))
        .forEach(_rules::add);

    // PostOutFlowSinkInterface
    _input
        .getEnabledFlowSinks()
        .stream()
        .map(
            flowSink ->
                new BasicRuleStatement(
                    new PostOutEdge(
                        flowSink.getHostname(),
                        flowSink.getInterface(),
                        Configuration.NODE_NONE_NAME,
                        Interface.FLOW_SINK_TERMINATION_NAME),
                    new NodeAccept(flowSink.getHostname())))
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDrop(NodeDrop.State nodeDrop) {
    // CopyNodeDropAcl
    _input
        .getEnabledNodes()
        .stream()
        .map(hostname -> new BasicRuleStatement(new NodeDropAcl(hostname), new NodeDrop(hostname)))
        .forEach(_rules::add);

    // CopyNodeDropNoRoute
    _input
        .getEnabledNodes()
        .stream()
        .map(
            hostname ->
                new BasicRuleStatement(new NodeDropNoRoute(hostname), new NodeDrop(hostname)))
        .forEach(_rules::add);

    // CopyNodeDropNullRoute
    _input
        .getEnabledNodes()
        .stream()
        .map(
            hostname ->
                new BasicRuleStatement(new NodeDropNullRoute(hostname), new NodeDrop(hostname)))
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropAcl(NodeDropAcl.State nodeDropAcl) {
    // CopyNodeDropAclIn
    _input
        .getEnabledNodes()
        .stream()
        .map(
            hostname ->
                new BasicRuleStatement(new NodeDropAclIn(hostname), new NodeDropAcl(hostname)))
        .forEach(_rules::add);

    // CopyNodeDropAclOut
    _input
        .getEnabledNodes()
        .stream()
        .map(
            hostname ->
                new BasicRuleStatement(new NodeDropAclOut(hostname), new NodeDropAcl(hostname)))
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropAclIn(NodeDropAclIn.State nodeDropAclIn) {
    // FailIncomingAcl
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            topologyInterfacesEntry -> {
              String hostname = topologyInterfacesEntry.getKey();
              Map<String, String> incomingAcls = _input.getIncomingAcls().get(hostname);
              return topologyInterfacesEntry
                  .getValue()
                  .stream()
                  .filter(ifaceName -> incomingAcls.get(ifaceName) != null)
                  .map(
                      ifaceName -> {
                        String inAcl = incomingAcls.get(ifaceName);
                        return new BasicRuleStatement(
                            TrueExpr.INSTANCE,
                            ImmutableSet.of(
                                new AclDeny(hostname, inAcl),
                                new PreInInterface(hostname, ifaceName)),
                            new NodeDropAclIn(hostname));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropAclOut(NodeDropAclOut.State nodeDropAclOut) {
    _input
        .getEnabledEdges()
        .forEach(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();
              String outAcl = _input.getOutgoingAcls().get(node1).get(iface1);
              // There has to be an ACL -- no ACL is an implicit Permit.
              if (outAcl != null) {
                Set<StateExpr> postTransformationPreStates =
                    ImmutableSet.of(
                        new AclDeny(node1, outAcl),
                        new PreOutEdgePostNat(node1, iface1, node2, iface2));
                _rules.add(
                    new BasicRuleStatement(
                        TrueExpr.INSTANCE, postTransformationPreStates, new NodeDropAclOut(node1)));
              }
            });
  }

  @Override
  public void visitNodeDropNoRoute(NodeDropNoRoute.State nodeDropNoRoute) {
    // DestinationRouting
    _input
        .getFibConditions()
        .entrySet()
        .stream()
        .flatMap(
            fibConditionsByHostnameEntry -> {
              String hostname = fibConditionsByHostnameEntry.getKey();
              return fibConditionsByHostnameEntry
                  .getValue()
                  .entrySet()
                  .stream()
                  .flatMap(
                      fibConditionsByVrfEntry -> {
                        String vrfName = fibConditionsByVrfEntry.getKey();
                        return fibConditionsByVrfEntry
                            .getValue()
                            .entrySet()
                            .stream()
                            .filter(
                                fibConditionsByOutInterfaceEntry ->
                                    fibConditionsByOutInterfaceEntry
                                        .getKey()
                                        .equals(FibRow.DROP_NO_ROUTE))
                            .map(
                                fibConditionsByOutInterfaceEntry -> {
                                  BooleanExpr conditions =
                                      fibConditionsByOutInterfaceEntry
                                          .getValue()
                                          .get(NodeInterfacePair.NONE);
                                  return new BasicRuleStatement(
                                      conditions,
                                      ImmutableSet.of(
                                          new PostInVrf(hostname, vrfName), new PreOut(hostname)),
                                      new NodeDropNoRoute(hostname));
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropNullRoute(NodeDropNullRoute.State nodeDropNullRoute) {
    // DestinationRouting
    _input
        .getFibConditions()
        .entrySet()
        .stream()
        .flatMap(
            fibConditionsByHostnameEntry -> {
              String hostname = fibConditionsByHostnameEntry.getKey();
              return fibConditionsByHostnameEntry
                  .getValue()
                  .entrySet()
                  .stream()
                  .flatMap(
                      fibConditionsByVrfEntry -> {
                        String vrfName = fibConditionsByVrfEntry.getKey();
                        return fibConditionsByVrfEntry
                            .getValue()
                            .entrySet()
                            .stream()
                            .filter(
                                fibConditionsByOutInterfaceEntry -> {
                                  String outInterface = fibConditionsByOutInterfaceEntry.getKey();
                                  return CommonUtil.isLoopback(outInterface)
                                      || CommonUtil.isNullInterface(outInterface);
                                })
                            .map(
                                fibConditionsByOutInterfaceEntry -> {
                                  BooleanExpr conditions =
                                      fibConditionsByOutInterfaceEntry
                                          .getValue()
                                          .get(NodeInterfacePair.NONE);
                                  return new BasicRuleStatement(
                                      conditions,
                                      ImmutableSet.of(
                                          new PostInVrf(hostname, vrfName), new PreOut(hostname)),
                                      new NodeDropNullRoute(hostname));
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNumberedQuery(NumberedQuery.State numberedQuery) {}

  @Override
  public void visitOriginate(Originate.State originate) {
    // ProjectOriginateVrf
    _input
        .getEnabledVrfs()
        .entrySet()
        .stream()
        .flatMap(
            enabledVrfsByHostnameEntry -> {
              String hostname = enabledVrfsByHostnameEntry.getKey();
              return enabledVrfsByHostnameEntry
                  .getValue()
                  .stream()
                  .map(
                      vrfName ->
                          new BasicRuleStatement(
                              new OriginateVrf(hostname, vrfName), new Originate(hostname)));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitOriginateVrf(OriginateVrf.State originateVrf) {}

  @Override
  public void visitPostIn(PostIn.State postIn) {
    // CopyOriginate
    _input
        .getEnabledNodes()
        .stream()
        .map(hostname -> new BasicRuleStatement(new Originate(hostname), new PostIn(hostname)))
        .forEach(_rules::add);

    // ProjectPostInInterface
    _input
        .getEnabledInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            enabledInterfacesByHostnameEntry -> {
              String hostname = enabledInterfacesByHostnameEntry.getKey();
              return enabledInterfacesByHostnameEntry
                  .getValue()
                  .stream()
                  .map(
                      ifaceName ->
                          new BasicRuleStatement(
                              new PostInInterface(hostname, ifaceName), new PostIn(hostname)));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPostInInterface(PostInInterface.State postInInterface) {
    // PassIncomingAcl
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            topologyInterfacesEntry -> {
              String hostname = topologyInterfacesEntry.getKey();
              Map<String, String> incomingAcls = _input.getIncomingAcls().get(hostname);
              return topologyInterfacesEntry
                  .getValue()
                  .stream()
                  .map(
                      ifaceName -> {
                        String inAcl = incomingAcls.get(ifaceName);
                        Set<StateExpr> preconditionStates;
                        StateExpr preIn = new PreInInterface(hostname, ifaceName);
                        if (inAcl != null) {
                          preconditionStates =
                              ImmutableSet.of(new AclPermit(hostname, inAcl), preIn);
                        } else {
                          preconditionStates = ImmutableSet.of(preIn);
                        }
                        return new BasicRuleStatement(
                            preconditionStates, new PostInInterface(hostname, ifaceName));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPostInVrf(PostInVrf.State postInVrf) {
    // CopyOriginateVrf
    _input
        .getEnabledInterfacesByNodeVrf()
        .entrySet()
        .stream()
        .flatMap(
            enabledInterfacesByNodeEntry -> {
              String hostname = enabledInterfacesByNodeEntry.getKey();
              return enabledInterfacesByNodeEntry
                  .getValue()
                  .entrySet()
                  .stream()
                  .map(
                      enabledInterfacesByVrfEntry -> {
                        String vrf = enabledInterfacesByVrfEntry.getKey();
                        return new BasicRuleStatement(
                            new OriginateVrf(hostname, vrf), new PostInVrf(hostname, vrf));
                      });
            })
        .forEach(_rules::add);

    // PostInInterfaceCorrespondingVrf
    _input
        .getEnabledInterfacesByNodeVrf()
        .entrySet()
        .stream()
        .flatMap(
            enabledInterfacesByNodeEntry -> {
              String hostname = enabledInterfacesByNodeEntry.getKey();
              return enabledInterfacesByNodeEntry
                  .getValue()
                  .entrySet()
                  .stream()
                  .flatMap(
                      enabledInterfacesByVrfEntry -> {
                        String vrfName = enabledInterfacesByVrfEntry.getKey();
                        return enabledInterfacesByVrfEntry
                            .getValue()
                            .stream()
                            .map(
                                ifaceName ->
                                    new BasicRuleStatement(
                                        new PostInInterface(hostname, ifaceName),
                                        new PostInVrf(hostname, vrfName)));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPostOutEdge(PostOutEdge.State postOutEdgePostAclOut) {
    // PassOutgoingAcl
    _input
        .getEnabledEdges()
        .stream()
        .map(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();
              String outAcl = _input.getOutgoingAcls().get(node1).get(iface1);
              Set<StateExpr> aclStates =
                  outAcl == null
                      ? ImmutableSet.of(new PreOutEdgePostNat(node1, iface1, node2, iface2))
                      : ImmutableSet.of(
                          new AclPermit(node1, outAcl),
                          new PreOutEdgePostNat(node1, iface1, node2, iface2));
              return new BasicRuleStatement(
                  TrueExpr.INSTANCE, aclStates, new PostOutEdge(node1, iface1, node2, iface2));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPreOutEdgePostNat(PreOutEdgePostNat.State preOutEdgePostNat) {
    visitPreOutEdgePostNat_generateTopologyEdgeRules();
    visitPreOutEdgePostNat_generateFlowSinkRules();
  }

  private void visitPreOutEdgePostNat_generateFlowSinkRules() {
    _input
        .getEnabledFlowSinks()
        .stream()
        .filter(flowSink -> _input.getSourceNats().containsKey(flowSink.getHostname()))
        .filter(
            flowSink ->
                _input
                    .getSourceNats()
                    .get(flowSink.getHostname())
                    .containsKey(flowSink.getInterface()))
        .forEach(
            flowSink -> {
              String node1 = flowSink.getHostname();
              String iface1 = flowSink.getInterface();
              String node2 = Configuration.NODE_NONE_NAME;
              String iface2 = Interface.FLOW_SINK_TERMINATION_NAME;
              visitPreOutEdgePostNat_generateMatchSourceNatRules(node1, iface1, node2, iface2);
            });

    // Doesn't match source nat.
    _input
        .getEnabledFlowSinks()
        .forEach(
            flowSink -> {
              String node1 = flowSink.getHostname();
              String iface1 = flowSink.getInterface();
              String node2 = Configuration.NODE_NONE_NAME;
              String iface2 = Interface.FLOW_SINK_TERMINATION_NAME;
              visitPreOutEdgePostNat_generateNoMatchSourceNatRules(node1, iface1, node2, iface2);
            });
  }

  private void visitPreOutEdgePostNat_generateMatchSourceNatRules(
      String node1, String iface1, String node2, String iface2) {

    List<Entry<AclPermit, BooleanExpr>> sourceNats = _input.getSourceNats().get(node1).get(iface1);

    for (int natNumber = 0; natNumber < sourceNats.size(); natNumber++) {
      ImmutableSet.Builder<StateExpr> preStates = ImmutableSet.builder();

      preStates.add(new PreOutEdge(node1, iface1, node2, iface2));

      // does not match any previous source NAT.
      sourceNats
          .subList(0, natNumber)
          .stream()
          .map(Entry::getKey)
          .map(aclPermit -> new AclDeny(aclPermit.getHostname(), aclPermit.getAcl()))
          .forEach(preStates::add);

      // does match the current source NAT.
      preStates.add(sourceNats.get(natNumber).getKey());

      BooleanExpr transformationExpr = sourceNats.get(natNumber).getValue();

      _rules.add(
          new TransformationRuleStatement(
              transformationExpr,
              preStates.build(),
              ImmutableSet.of(),
              new PreOutEdgePostNat(node1, iface1, node2, iface2)));
    }
  }

  private void visitPreOutEdgePostNat_generateNoMatchSourceNatRules(
      String node1, String iface1, String node2, String iface2) {
    List<Entry<AclPermit, BooleanExpr>> sourceNats =
        _input
            .getSourceNats()
            .getOrDefault(node1, ImmutableMap.of())
            .getOrDefault(iface1, ImmutableList.of());

    ImmutableSet.Builder<StateExpr> preStates = ImmutableSet.builder();
    preStates.add(new PreOutEdge(node1, iface1, node2, iface2));

    sourceNats
        .stream()
        .map(Entry::getKey)
        .map(aclPermit -> new AclDeny(aclPermit.getHostname(), aclPermit.getAcl()))
        .forEach(preStates::add);

    _rules.add(
        new TransformationRuleStatement(
            new EqExpr(
                new VarIntExpr(TransformationHeaderField.NEW_SRC_IP),
                new VarIntExpr(TransformationHeaderField.NEW_SRC_IP.getCurrent())),
            preStates.build(),
            ImmutableSet.of(),
            new PreOutEdgePostNat(node1, iface1, node2, iface2)));
  }

  private void visitPreOutEdgePostNat_generateTopologyEdgeRules() {
    // Matches source nat.
    _input
        .getEnabledEdges()
        .stream()
        .filter(e -> _input.getSourceNats().containsKey(e.getNode1()))
        .filter(e -> _input.getSourceNats().get(e.getNode1()).containsKey(e.getInt1()))
        .forEach(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();
              visitPreOutEdgePostNat_generateMatchSourceNatRules(node1, iface1, node2, iface2);
            });

    // Doesn't match source nat.
    _input
        .getEnabledEdges()
        .forEach(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();

              visitPreOutEdgePostNat_generateNoMatchSourceNatRules(node1, iface1, node2, iface2);
            });
  }

  @Override
  public void visitPreInInterface(PreInInterface.State preInInterface) {
    // PostOutNeighbor
    _input
        .getEnabledEdges()
        .stream()
        .map(
            edge ->
                new BasicRuleStatement(
                    ImmutableSet.of(new PostOutEdge(edge)),
                    new PreInInterface(edge.getNode2(), edge.getInt2())))
        .forEach(_rules::add);
  }

  @Override
  public void visitPreOut(PreOut.State preOut) {
    // PostInNotMine
    _input
        .getIpsByHostname()
        .entrySet()
        .stream()
        .map(
            ipsByHostnameEntry -> {
              String hostname = ipsByHostnameEntry.getKey();
              BooleanExpr ipForeignToCurrentNode =
                  new NotExpr(
                      HeaderSpaceMatchExpr.matchDstIp(
                          ipsByHostnameEntry
                              .getValue()
                              .stream()
                              .map(IpWildcard::new)
                              .collect(ImmutableSet.toImmutableSet())));
              return new BasicRuleStatement(
                  ipForeignToCurrentNode,
                  ImmutableSet.of(new PostIn(hostname)),
                  new PreOut(hostname));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPreOutEdge(PreOutEdge.State preOutEdge) {
    // DestinationRouting
    _input
        .getFibConditions()
        .entrySet()
        .stream()
        .flatMap(
            fibConditionsByHostnameEntry -> {
              String hostname = fibConditionsByHostnameEntry.getKey();
              return fibConditionsByHostnameEntry
                  .getValue()
                  .entrySet()
                  .stream()
                  .flatMap(
                      fibConditionsByVrfEntry -> {
                        String vrfName = fibConditionsByVrfEntry.getKey();
                        return fibConditionsByVrfEntry
                            .getValue()
                            .entrySet()
                            .stream()
                            .filter(
                                fibConditionsByOutInterfaceEntry -> {
                                  String outInterface = fibConditionsByOutInterfaceEntry.getKey();
                                  /*
                                   * Loopback and Null Interfaces are handled in
                                   * visitNodeDropNullRoute.
                                   * DROP_NO_ROUTE is handled in visitNodeDropNoRoute
                                   */
                                  return !CommonUtil.isLoopback(outInterface)
                                      && !CommonUtil.isNullInterface(outInterface)
                                      && !outInterface.equals(FibRow.DROP_NO_ROUTE);
                                })
                            .flatMap(
                                fibConditionsByOutInterfaceEntry -> {
                                  String outInterface = fibConditionsByOutInterfaceEntry.getKey();
                                  return fibConditionsByOutInterfaceEntry
                                      .getValue()
                                      .entrySet()
                                      .stream()
                                      .map(
                                          fibConditionsByReceiverEntry -> {
                                            NodeInterfacePair receiver =
                                                fibConditionsByReceiverEntry.getKey();
                                            BooleanExpr conditions =
                                                fibConditionsByReceiverEntry.getValue();
                                            String inNode = receiver.getHostname();
                                            String inInterface = receiver.getInterface();
                                            return new BasicRuleStatement(
                                                conditions,
                                                ImmutableSet.of(
                                                    new PostInVrf(hostname, vrfName),
                                                    new PreOut(hostname)),
                                                new PreOutEdge(
                                                    hostname, outInterface, inNode, inInterface));
                                          });
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitQuery(Query.State query) {}
}
