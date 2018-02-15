package org.batfish.z3.state.visitors;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.StateExpr.State;
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
import org.batfish.z3.state.NodeTransit;
import org.batfish.z3.state.NumberedQuery;
import org.batfish.z3.state.Originate;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostIn;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutInterface;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOut;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutInterface;
import org.batfish.z3.state.Query;

public class DefaultTransitionGenerator implements StateVisitor {

  public static List<RuleStatement> generateTransitions(SynthesizerInput input, Set<State> states) {
    DefaultTransitionGenerator visitor = new DefaultTransitionGenerator(input);
    states.forEach(state -> state.accept(visitor));
    return visitor._rules.build();
  }

  private static boolean isLoopbackInterface(String ifaceName) {
    return ifaceName.toLowerCase().startsWith("lo");
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
    _rules.add(
        new RuleStatement(
            new OrExpr(
                _input
                    .getEnabledNodes()
                    .keySet()
                    .stream()
                    .map(NodeAccept::new)
                    .collect(ImmutableList.toImmutableList())),
            Accept.INSTANCE));
  }

  @Override
  public void visitAclDeny(AclDeny.State aclDeny) {
    // MatchDenyLine
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
                  .flatMap(
                      aclActionsEntryByAclName -> {
                        String acl = aclActionsEntryByAclName.getKey();
                        return aclActionsEntryByAclName
                            .getValue()
                            .entrySet()
                            .stream()
                            .filter(lineEntry -> lineEntry.getValue() == LineAction.REJECT)
                            .map(
                                lineEntry ->
                                    new RuleStatement(
                                        new AclLineMatch(hostname, acl, lineEntry.getKey()),
                                        new AclDeny(hostname, acl)));
                      });
            })
        .forEach(_rules::add);

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
                        Map<Integer, LineAction> lineActions = aclActionsEntryByAclName.getValue();
                        AclDeny deny = new AclDeny(hostname, acl);
                        if (lineActions.isEmpty()) {
                          return new RuleStatement(deny);
                        } else {
                          int lastLine = lineActions.size() - 1;
                          if (lineActions.get(lastLine) == LineAction.ACCEPT) {
                            return new RuleStatement(
                                new AclLineNoMatch(hostname, acl, lastLine), deny);
                          } else {
                            return null;
                          }
                        }
                      })
                  .filter(r -> r != null);
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
                        return aclConditionsEntryByAclName
                            .getValue()
                            .entrySet()
                            .stream()
                            .map(
                                aclConditionsEntryByLine -> {
                                  int line = aclConditionsEntryByLine.getKey();
                                  BooleanExpr lineCriteria = aclConditionsEntryByLine.getValue();
                                  BooleanExpr antecedent =
                                      line > 0
                                          ? new AndExpr(
                                              ImmutableList.of(
                                                  lineCriteria,
                                                  new AclLineNoMatch(hostname, acl, line - 1)))
                                          : lineCriteria;
                                  return new RuleStatement(
                                      antecedent, new AclLineMatch(hostname, acl, line));
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
                        return e2.getValue()
                            .entrySet()
                            .stream()
                            .map(
                                e3 -> {
                                  int line = e3.getKey();
                                  BooleanExpr lineCriteria = new NotExpr(e3.getValue());
                                  BooleanExpr antecedent =
                                      line > 0
                                          ? new AndExpr(
                                              ImmutableList.of(
                                                  lineCriteria,
                                                  new AclLineNoMatch(hostname, acl, line - 1)))
                                          : lineCriteria;
                                  return new RuleStatement(
                                      antecedent, new AclLineNoMatch(hostname, acl, line));
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
        .entrySet()
        .stream()
        .flatMap(
            aclActionsEntryByNode -> {
              String hostname = aclActionsEntryByNode.getKey();
              return aclActionsEntryByNode
                  .getValue()
                  .entrySet()
                  .stream()
                  .flatMap(
                      aclActionsEntryByAclName -> {
                        String acl = aclActionsEntryByAclName.getKey();
                        return aclActionsEntryByAclName
                            .getValue()
                            .entrySet()
                            .stream()
                            .filter(lineEntry -> lineEntry.getValue() == LineAction.ACCEPT)
                            .map(
                                lineEntry ->
                                    new RuleStatement(
                                        new AclLineMatch(hostname, acl, lineEntry.getKey()),
                                        new AclPermit(hostname, acl)));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitDebug(Debug.State debug) {}

  @Override
  public void visitDrop(Drop.State drop) {
    // ProjectNodeDrop
    _rules.add(
        new RuleStatement(
            new OrExpr(
                _input
                    .getEnabledNodes()
                    .keySet()
                    .stream()
                    .map(NodeDrop::new)
                    .collect(ImmutableList.toImmutableList())),
            Drop.INSTANCE));
  }

  @Override
  public void visitDropAcl(DropAcl.State dropAcl) {
    // CopyDropAclIn
    _rules.add(new RuleStatement(DropAclIn.INSTANCE, DropAcl.INSTANCE));

    // CopyDropAclOut
    _rules.add(new RuleStatement(DropAclOut.INSTANCE, DropAcl.INSTANCE));

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
    _rules.add(
        new RuleStatement(
            new OrExpr(
                _input
                    .getEnabledNodes()
                    .keySet()
                    .stream()
                    .map(NodeDropAclIn::new)
                    .collect(ImmutableList.toImmutableList())),
            DropAclIn.INSTANCE));
  }

  @Override
  public void visitDropAclOut(DropAclOut.State dropAclOut) {
    // ProjectNodeDropAclOut
    _rules.add(
        new RuleStatement(
            new OrExpr(
                _input
                    .getEnabledNodes()
                    .keySet()
                    .stream()
                    .map(NodeDropAclOut::new)
                    .collect(ImmutableList.toImmutableList())),
            DropAclOut.INSTANCE));
  }

  @Override
  public void visitDropNoRoute(DropNoRoute.State dropNoRoute) {
    // ProjectNodeDropNoRoute
    _rules.add(
        new RuleStatement(
            new OrExpr(
                _input
                    .getEnabledNodes()
                    .keySet()
                    .stream()
                    .map(NodeDropNoRoute::new)
                    .collect(ImmutableList.toImmutableList())),
            DropNoRoute.INSTANCE));
  }

  @Override
  public void visitDropNullRoute(DropNullRoute.State dropNullRoute) {
    // ProjectNodeDropNullRoute
    _rules.add(
        new RuleStatement(
            new OrExpr(
                _input
                    .getEnabledNodes()
                    .keySet()
                    .stream()
                    .map(NodeDropNullRoute::new)
                    .collect(ImmutableList.toImmutableList())),
            DropNullRoute.INSTANCE));
  }

  @Override
  public void visitNodeAccept(NodeAccept.State nodeAccept) {
    // PostInForMe
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .map(
            e ->
                new RuleStatement(
                    new AndExpr(
                        ImmutableList.of(
                            new PostIn(e.getKey()),
                            HeaderSpaceMatchExpr.matchDstIp(
                                _input
                                    .getIpsByHostname()
                                    .get(e.getKey())
                                    .stream()
                                    .map(IpWildcard::new)
                                    .collect(ImmutableSet.toImmutableSet())))),
                    new NodeAccept(e.getKey())))
        .forEach(_rules::add);

    // PostOutFlowSinkInterface
    _input
        .getEnabledFlowSinks()
        .stream()
        .map(
            niPair ->
                new RuleStatement(
                    new PostOutInterface(niPair.getHostname(), niPair.getInterface()),
                    new NodeAccept(niPair.getHostname())))
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDrop(NodeDrop.State nodeDrop) {
    // CopyNodeDropAcl
    _input
        .getEnabledNodes()
        .keySet()
        .stream()
        .map(hostname -> new RuleStatement(new NodeDropAcl(hostname), new NodeDrop(hostname)))
        .forEach(_rules::add);

    // CopyNodeDropNoRoute
    _input
        .getEnabledNodes()
        .keySet()
        .stream()
        .map(hostname -> new RuleStatement(new NodeDropNoRoute(hostname), new NodeDrop(hostname)))
        .forEach(_rules::add);

    // CopyNodeDropNullRoute
    _input
        .getEnabledNodes()
        .keySet()
        .stream()
        .map(hostname -> new RuleStatement(new NodeDropNullRoute(hostname), new NodeDrop(hostname)))
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropAcl(NodeDropAcl.State nodeDropAcl) {
    // CopyNodeDropAclIn
    _input
        .getEnabledNodes()
        .keySet()
        .stream()
        .map(hostname -> new RuleStatement(new NodeDropAclIn(hostname), new NodeDropAcl(hostname)))
        .forEach(_rules::add);

    // CopyNodeDropAclOut
    _input
        .getEnabledNodes()
        .keySet()
        .stream()
        .map(hostname -> new RuleStatement(new NodeDropAclOut(hostname), new NodeDropAcl(hostname)))
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
            e -> {
              String hostname = e.getKey();
              return e.getValue()
                  .stream()
                  .filter(i -> i.getIncomingFilterName() != null)
                  .map(
                      i -> {
                        String inAcl = i.getIncomingFilterName();
                        return new RuleStatement(
                            new AndExpr(
                                ImmutableList.of(
                                    new AclDeny(hostname, inAcl),
                                    new PreInInterface(hostname, i.getName()))),
                            new NodeDropAclIn(hostname));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropAclOut(NodeDropAclOut.State nodeDropAclOut) {
    // FailOutgoingAcl
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return e.getValue()
                  .stream()
                  .filter(i -> i.getOutgoingFilterName() != null)
                  .map(
                      i -> {
                        String outAcl = i.getOutgoingFilterName();
                        return new RuleStatement(
                            new AndExpr(
                                ImmutableList.of(
                                    new AclDeny(hostname, outAcl),
                                    new PreOutInterface(hostname, i.getName()))),
                            new NodeDropAclOut(hostname));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropNoRoute(NodeDropNoRoute.State nodeDropNoRoute) {
    // DestinationRouting
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .filter(e -> _input.getFibs().containsKey(e.getKey()))
        .flatMap(
            e -> {
              String hostname = e.getKey();
              Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>> fibConditionsByVrf =
                  _input.getFibConditions().get(hostname);
              return _input
                  .getEnabledVrfs()
                  .get(hostname)
                  .keySet()
                  .stream()
                  .filter(Predicates.not(_input.getFibs().get(hostname)::containsKey))
                  .flatMap(
                      vrfName -> {
                        Map<String, Map<NodeInterfacePair, BooleanExpr>> fibConditionsByInterface =
                            fibConditionsByVrf.get(vrfName);
                        return fibConditionsByInterface
                            .entrySet()
                            .stream()
                            .filter(e2 -> e2.getKey().equals(FibRow.DROP_NO_ROUTE))
                            .map(
                                e2 -> {
                                  String outInterface = e2.getKey();
                                  BooleanExpr conditions =
                                      fibConditionsByInterface
                                          .get(outInterface)
                                          .get(NodeInterfacePair.NONE);
                                  return new RuleStatement(
                                      conditions, new NodeDropNoRoute(hostname));
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeDropNullRoute(NodeDropNullRoute.State nodeDropNullRoute) {
    // DestinationRouting
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .filter(e -> _input.getFibs().containsKey(e.getKey()))
        .flatMap(
            e -> {
              String hostname = e.getKey();
              Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>> fibConditionsByVrf =
                  _input.getFibConditions().get(hostname);
              return _input
                  .getEnabledVrfs()
                  .get(hostname)
                  .keySet()
                  .stream()
                  .filter(Predicates.not(_input.getFibs().get(hostname)::containsKey))
                  .flatMap(
                      vrfName -> {
                        Map<String, Map<NodeInterfacePair, BooleanExpr>> fibConditionsByInterface =
                            fibConditionsByVrf.get(vrfName);
                        return fibConditionsByInterface
                            .entrySet()
                            .stream()
                            .filter(
                                e2 -> {
                                  String outInterface = e2.getKey();
                                  return isLoopbackInterface(outInterface)
                                      || CommonUtil.isNullInterface(outInterface);
                                })
                            .map(
                                e2 -> {
                                  String outInterface = e2.getKey();
                                  BooleanExpr conditions =
                                      fibConditionsByInterface
                                          .get(outInterface)
                                          .get(NodeInterfacePair.NONE);
                                  return new RuleStatement(
                                      conditions, new NodeDropNullRoute(hostname));
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNodeTransit(NodeTransit.State nodeTransit) {
    // ProjectPostOutInterface
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return _input
                  .getEnabledInterfaces()
                  .get(hostname)
                  .keySet()
                  .stream()
                  .map(
                      ifaceName ->
                          new RuleStatement(
                              new PostOutInterface(hostname, ifaceName),
                              new NodeTransit(hostname)));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitNumberedQuery(NumberedQuery.State numberedQuery) {}

  @Override
  public void visitOriginate(Originate.State originate) {
    // ProjectOriginateVrf
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return _input
                  .getEnabledVrfs()
                  .get(hostname)
                  .keySet()
                  .stream()
                  .map(
                      vrfName ->
                          new RuleStatement(
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
        .keySet()
        .stream()
        .map(hostname -> new RuleStatement(new Originate(hostname), new PostIn(hostname)))
        .forEach(_rules::add);

    // ProjectPostInInterface
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return _input
                  .getEnabledInterfaces()
                  .get(hostname)
                  .keySet()
                  .stream()
                  .map(
                      ifaceName ->
                          new RuleStatement(
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
            e -> {
              String hostname = e.getKey();
              return e.getValue()
                  .stream()
                  .map(
                      i -> {
                        String inAcl = i.getIncomingFilterName();
                        BooleanExpr antecedent;
                        BooleanExpr preIn = new PreInInterface(hostname, i.getName());
                        if (inAcl != null) {
                          antecedent =
                              new AndExpr(ImmutableList.of(new AclPermit(hostname, inAcl), preIn));
                        } else {
                          antecedent = preIn;
                        }
                        return new RuleStatement(
                            antecedent, new PostInInterface(hostname, i.getName()));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPostInVrf(PostInVrf.State postInVrf) {
    // CopyOriginateVrf
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return _input
                  .getEnabledVrfs()
                  .get(hostname)
                  .keySet()
                  .stream()
                  .map(
                      vrf ->
                          new RuleStatement(
                              new OriginateVrf(hostname, vrf), new PostInVrf(hostname, vrf)));
            })
        .forEach(_rules::add);

    // PostInInterfaceCorrespondingVrf
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return _input
                  .getEnabledInterfaces()
                  .get(hostname)
                  .entrySet()
                  .stream()
                  .map(
                      ei ->
                          new RuleStatement(
                              new PostInInterface(hostname, ei.getKey()),
                              new PostInVrf(hostname, ei.getValue().getVrfName())));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPostOutInterface(PostOutInterface.State postOutInterface) {
    // PassOutgoingAcl
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            e -> {
              String hostname = e.getKey();
              return e.getValue()
                  .stream()
                  .map(
                      i -> {
                        String ifaceName = i.getName();
                        String outAcl = i.getOutgoingFilterName();
                        BooleanExpr antecedent;
                        BooleanExpr preOut = new PreOutInterface(hostname, ifaceName);
                        if (outAcl != null) {
                          antecedent =
                              new AndExpr(
                                  ImmutableList.of(new AclPermit(hostname, outAcl), preOut));
                        } else {
                          antecedent = preOut;
                        }
                        return new RuleStatement(
                            antecedent, new PostOutInterface(hostname, ifaceName));
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPreInInterface(PreInInterface.State preInInterface) {
    // PostOutNeighbor
    _input
        .getEnabledEdges()
        .stream()
        .filter(e -> !_input.getEnabledFlowSinks().contains(e.getInterface1()))
        .filter(e -> !_input.getEnabledFlowSinks().contains(e.getInterface2()))
        .map(
            edge ->
                new RuleStatement(
                    new AndExpr(
                        ImmutableList.of(
                            new PreOutEdge(edge),
                            new PostOutInterface(edge.getNode1(), edge.getInt1()))),
                    new PreInInterface(edge.getNode2(), edge.getInt2())))
        .forEach(_rules::add);
  }

  @Override
  public void visitPreOut(PreOut.State preOut) {
    // PostInNotMine
    _input
        .getEnabledNodes()
        .keySet()
        .stream()
        .map(
            hostname -> {
              BooleanExpr ipForeignToCurrentNode =
                  new NotExpr(
                      HeaderSpaceMatchExpr.matchDstIp(
                          _input
                              .getIpsByHostname()
                              .get(hostname)
                              .stream()
                              .map(IpWildcard::new)
                              .collect(ImmutableSet.toImmutableSet())));
              return new RuleStatement(
                  new AndExpr(ImmutableList.of(new PostIn(hostname), ipForeignToCurrentNode)),
                  new PreOut(hostname));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPreOutEdge(PreOutEdge.State preOutEdge) {
    // DestinationRouting
    _input
        .getEnabledNodes()
        .entrySet()
        .stream()
        .filter(e -> _input.getFibs().containsKey(e.getKey()))
        .flatMap(
            e -> {
              String hostname = e.getKey();
              Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>> fibConditionsByVrf =
                  _input.getFibConditions().get(hostname);
              return _input
                  .getEnabledVrfs()
                  .get(hostname)
                  .keySet()
                  .stream()
                  .filter(_input.getFibs().get(hostname)::containsKey)
                  .flatMap(
                      vrfName -> {
                        Map<String, Map<NodeInterfacePair, BooleanExpr>> fibConditionsByInterface =
                            fibConditionsByVrf.get(vrfName);
                        return fibConditionsByInterface
                            .entrySet()
                            .stream()
                            .filter(
                                e2 -> {
                                  String outInterface = e2.getKey();
                                  return !isLoopbackInterface(outInterface)
                                      && !CommonUtil.isNullInterface(outInterface)
                                      && !outInterface.equals(FibRow.DROP_NO_ROUTE);
                                })
                            .flatMap(
                                e2 -> {
                                  String outInterface = e2.getKey();
                                  Map<NodeInterfacePair, BooleanExpr> fibConditionsByReceiver =
                                      fibConditionsByInterface.get(outInterface);
                                  return fibConditionsByReceiver
                                      .entrySet()
                                      .stream()
                                      .map(
                                          e3 -> {
                                            NodeInterfacePair receiver = e3.getKey();
                                            BooleanExpr conditions = e3.getValue();
                                            String inNode = receiver.getHostname();
                                            String inInterface = receiver.getInterface();
                                            return new RuleStatement(
                                                conditions,
                                                new PreOutEdge(
                                                    hostname, outInterface, inNode, inInterface));
                                          });
                                });
                      });
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPreOutInterface(PreOutInterface.State preOutInterface) {
    // ProjectPreOutEdgeForFlowSinks
    _input
        .getEnabledFlowSinks()
        .stream()
        .map(
            flowSink ->
                new RuleStatement(
                    new PreOutEdge(
                        flowSink.getHostname(),
                        flowSink.getInterface(),
                        Configuration.NODE_NONE_NAME,
                        Interface.FLOW_SINK_TERMINATION_NAME),
                    new PreOutInterface(flowSink.getHostname(), flowSink.getInterface())))
        .forEach(_rules::add);

    // ProjectPreOutEdgeForTopologyEdges
    _input
        .getEnabledEdges()
        .stream()
        .map(
            edge ->
                new RuleStatement(
                    new PreOutEdge(edge), new PreOutInterface(edge.getNode1(), edge.getInt1())))
        .forEach(_rules::add);
  }

  @Override
  public void visitQuery(Query.State query) {}
}
