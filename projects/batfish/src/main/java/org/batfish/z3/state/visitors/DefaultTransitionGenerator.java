package org.batfish.z3.state.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
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
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.StateExpr.State;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.expr.TransformedBasicRuleStatement;
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
                                    new BasicRuleStatement(
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
                        return aclConditionsEntryByAclName
                            .getValue()
                            .entrySet()
                            .stream()
                            .map(
                                aclConditionsEntryByLine -> {
                                  int line = aclConditionsEntryByLine.getKey();
                                  BooleanExpr lineCriteria = aclConditionsEntryByLine.getValue();
                                  Set<BasicStateExpr> preconditionStates =
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
                        return e2.getValue()
                            .entrySet()
                            .stream()
                            .map(
                                e3 -> {
                                  int line = e3.getKey();
                                  BooleanExpr lineCriteria = new NotExpr(e3.getValue());
                                  Set<BasicStateExpr> preconditionStates =
                                      line > 0
                                          ? ImmutableSet.of(
                                              new AclLineNoMatch(hostname, acl, line - 1))
                                          : ImmutableSet.of();
                                  return new BasicRuleStatement(
                                      lineCriteria,
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
                                    new BasicRuleStatement(
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
            niPair ->
                new TransformedBasicRuleStatement(
                    new PostOutInterface(niPair.getHostname(), niPair.getInterface()),
                    new NodeAccept(niPair.getHostname())))
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
    // FailOutgoingAclNoMatchSrcNat
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            topologyInterfacesEntry -> {
              String hostname = topologyInterfacesEntry.getKey();
              Map<String, List<Entry<Optional<BasicStateExpr>, BooleanExpr>>>
                  sourceNatsByInterface = _input.getSourceNats().get(hostname);
              Map<String, String> outgoingAcls = _input.getOutgoingAcls().get(hostname);
              return topologyInterfacesEntry
                  .getValue()
                  .stream()
                  .filter(ifaceName -> outgoingAcls.get(ifaceName) != null)
                  .filter(
                      ifaceName ->
                          sourceNatsByInterface
                              .get(ifaceName)
                              .stream()
                              .map(Entry::getKey)
                              .allMatch(Optional::isPresent))
                  .map(
                      ifaceName -> {
                        List<Entry<Optional<BasicStateExpr>, BooleanExpr>> sourceNats =
                            sourceNatsByInterface.get(ifaceName);
                        ImmutableSet.Builder<BasicStateExpr> preconditionStates =
                            ImmutableSet.builder();
                        sourceNats
                            .stream()
                            .map(Entry::getKey)
                            .map(
                                preconditionPreTransformationState -> {
                                  AclPermit aclPermit =
                                      (AclPermit) preconditionPreTransformationState.get();
                                  return new AclDeny(aclPermit.getHostname(), aclPermit.getAcl());
                                })
                            .forEach(preconditionStates::add);
                        preconditionStates.add(new PreOutInterface(hostname, ifaceName));
                        preconditionStates.add(new AclDeny(hostname, outgoingAcls.get(ifaceName)));
                        return new BasicRuleStatement(
                            preconditionStates.build(), new NodeDropAclOut(hostname));
                      });
            })
        .forEach(_rules::add);

    // FailOutgoingAclMatchSrcNat
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            topologyInterfacesEntry -> {
              String hostname = topologyInterfacesEntry.getKey();
              Map<String, List<Entry<Optional<BasicStateExpr>, BooleanExpr>>>
                  sourceNatsByInterface = _input.getSourceNats().get(hostname);
              Map<String, String> outgoingAcls = _input.getOutgoingAcls().get(hostname);
              return topologyInterfacesEntry
                  .getValue()
                  .stream()
                  .filter(ifaceName -> outgoingAcls.get(ifaceName) != null)
                  .flatMap(
                      ifaceName -> {
                        List<Entry<Optional<BasicStateExpr>, BooleanExpr>> sourceNats =
                            sourceNatsByInterface.get(ifaceName);
                        return IntStream.range(0, sourceNats.size())
                            .filter(
                                sourceNatIndex ->
                                    sourceNatIndex == 0
                                        || sourceNats
                                            .subList(0, sourceNatIndex)
                                            .stream()
                                            .map(Entry::getKey)
                                            .allMatch(Optional::isPresent))
                            .mapToObj(
                                sourceNatIndex -> {
                                  Entry<Optional<BasicStateExpr>, BooleanExpr>
                                      currentSourceNatEntry = sourceNats.get(sourceNatIndex);
                                  ImmutableSet.Builder<BasicStateExpr>
                                      preconditionPreTransformationStates = ImmutableSet.builder();
                                  Optional<BasicStateExpr> matchCurrentEntry =
                                      currentSourceNatEntry.getKey();
                                  BooleanExpr preconditionStateIndependent =
                                      currentSourceNatEntry.getValue();
                                  if (matchCurrentEntry.isPresent()) {
                                    preconditionPreTransformationStates.add(
                                        matchCurrentEntry.get());
                                  }
                                  if (sourceNatIndex > 0) {
                                    sourceNats
                                        .subList(0, sourceNatIndex)
                                        .stream()
                                        .map(Entry::getKey)
                                        .map(Optional::get)
                                        .map(aclPermit -> (AclPermit) aclPermit)
                                        .map(
                                            aclPermit ->
                                                new AclDeny(
                                                    aclPermit.getHostname(), aclPermit.getAcl()))
                                        .forEach(preconditionPreTransformationStates::add);
                                  }
                                  preconditionPreTransformationStates.add(
                                      new PreOutInterface(hostname, ifaceName));
                                  return new TransformedBasicRuleStatement(
                                      preconditionStateIndependent,
                                      preconditionPreTransformationStates.build(),
                                      ImmutableSet.of(
                                          new AclDeny(hostname, outgoingAcls.get(ifaceName))),
                                      ImmutableSet.of(),
                                      new NodeDropAclOut(hostname));
                                });
                      });
            })
        .forEach(_rules::add);
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
                                  return isLoopbackInterface(outInterface)
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
  public void visitNodeTransit(NodeTransit.State nodeTransit) {
    // ProjectPostOutInterface
    _input
        .getEnabledInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            enabledInterfacesEntry -> {
              String hostname = enabledInterfacesEntry.getKey();
              return enabledInterfacesEntry
                  .getValue()
                  .stream()
                  .map(
                      ifaceName ->
                          new TransformedBasicRuleStatement(
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
                        Set<BasicStateExpr> preconditionStates;
                        BasicStateExpr preIn = new PreInInterface(hostname, ifaceName);
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
  public void visitPostOutInterface(PostOutInterface.State postOutInterface) {
    // PassOutgoingAclNoMatchSrcNat
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            topologyInterfacesEntry -> {
              String hostname = topologyInterfacesEntry.getKey();
              Map<String, List<Entry<Optional<BasicStateExpr>, BooleanExpr>>>
                  sourceNatsByInterface = _input.getSourceNats().get(hostname);
              Map<String, String> outgoingAcls = _input.getOutgoingAcls().get(hostname);
              return topologyInterfacesEntry
                  .getValue()
                  .stream()
                  .filter(
                      ifaceName ->
                          sourceNatsByInterface
                              .get(ifaceName)
                              .stream()
                              .map(Entry::getKey)
                              .allMatch(Optional::isPresent))
                  .map(
                      ifaceName -> {
                        List<Entry<Optional<BasicStateExpr>, BooleanExpr>> sourceNats =
                            sourceNatsByInterface.get(ifaceName);
                        ImmutableSet.Builder<BasicStateExpr> preconditionPreTransformationStates =
                            ImmutableSet.builder();
                        sourceNats
                            .stream()
                            .map(Entry::getKey)
                            .map(
                                preconditionPreTransformationState -> {
                                  AclPermit aclPermit =
                                      (AclPermit) preconditionPreTransformationState.get();
                                  return new AclDeny(aclPermit.getHostname(), aclPermit.getAcl());
                                })
                            .forEach(preconditionPreTransformationStates::add);
                        String outAcl = outgoingAcls.get(ifaceName);
                        if (outAcl != null) {
                          preconditionPreTransformationStates.add(new AclPermit(hostname, outAcl));
                        }
                        preconditionPreTransformationStates.add(
                            new PreOutInterface(hostname, ifaceName));
                        return new TransformationRuleStatement(
                            new EqExpr(
                                new VarIntExpr(TransformationHeaderField.NEW_SRC_IP),
                                new VarIntExpr(TransformationHeaderField.NEW_SRC_IP.getCurrent())),
                            preconditionPreTransformationStates.build(),
                            ImmutableSet.of(),
                            ImmutableSet.of(),
                            new PostOutInterface(hostname, ifaceName));
                      });
            })
        .forEach(_rules::add);

    // PassOutgoingAclMatchSrcNat
    _input
        .getTopologyInterfaces()
        .entrySet()
        .stream()
        .flatMap(
            topologyInterfacesEntry -> {
              String hostname = topologyInterfacesEntry.getKey();
              Map<String, List<Entry<Optional<BasicStateExpr>, BooleanExpr>>>
                  sourceNatsByInterface = _input.getSourceNats().get(hostname);
              Map<String, String> outgoingAcls = _input.getOutgoingAcls().get(hostname);
              return topologyInterfacesEntry
                  .getValue()
                  .stream()
                  .flatMap(
                      ifaceName -> {
                        List<Entry<Optional<BasicStateExpr>, BooleanExpr>> sourceNats =
                            sourceNatsByInterface.get(ifaceName);
                        return IntStream.range(0, sourceNats.size())
                            .filter(
                                sourceNatIndex ->
                                    sourceNatIndex == 0
                                        || sourceNats
                                            .subList(0, sourceNatIndex)
                                            .stream()
                                            .map(Entry::getKey)
                                            .allMatch(Optional::isPresent))
                            .mapToObj(
                                sourceNatIndex -> {
                                  Entry<Optional<BasicStateExpr>, BooleanExpr>
                                      currentSourceNatEntry = sourceNats.get(sourceNatIndex);
                                  ImmutableSet.Builder<BasicStateExpr>
                                      preconditionPreTransformationStates = ImmutableSet.builder();
                                  Optional<BasicStateExpr> matchCurrentEntry =
                                      currentSourceNatEntry.getKey();
                                  BooleanExpr preconditionStateIndependent =
                                      currentSourceNatEntry.getValue();
                                  if (matchCurrentEntry.isPresent()) {
                                    preconditionPreTransformationStates.add(
                                        matchCurrentEntry.get());
                                  }
                                  if (sourceNatIndex > 0) {
                                    sourceNats
                                        .subList(0, sourceNatIndex)
                                        .stream()
                                        .map(Entry::getKey)
                                        .map(Optional::get)
                                        .map(aclPermit -> (AclPermit) aclPermit)
                                        .map(
                                            aclPermit ->
                                                new AclDeny(
                                                    aclPermit.getHostname(), aclPermit.getAcl()))
                                        .forEach(preconditionPreTransformationStates::add);
                                  }
                                  String outAcl = outgoingAcls.get(ifaceName);
                                  Set<BasicStateExpr> preconditionPostTransformationStates =
                                      outAcl != null
                                          ? ImmutableSet.of(new AclPermit(hostname, outAcl))
                                          : ImmutableSet.of();
                                  preconditionPreTransformationStates.add(
                                      new PreOutInterface(hostname, ifaceName));
                                  return new TransformationRuleStatement(
                                      preconditionStateIndependent,
                                      preconditionPreTransformationStates.build(),
                                      preconditionPostTransformationStates,
                                      ImmutableSet.of(),
                                      new PostOutInterface(hostname, ifaceName));
                                });
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
                new TransformedBasicRuleStatement(
                    TrueExpr.INSTANCE,
                    ImmutableSet.of(new PreOutEdge(edge)),
                    ImmutableSet.of(),
                    ImmutableSet.of(new PostOutInterface(edge.getNode1(), edge.getInt1())),
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
                                  return !isLoopbackInterface(outInterface)
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
  public void visitPreOutInterface(PreOutInterface.State preOutInterface) {
    // ProjectPreOutEdgeForFlowSinks
    _input
        .getEnabledFlowSinks()
        .stream()
        .map(
            flowSink ->
                new BasicRuleStatement(
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
                new BasicRuleStatement(
                    new PreOutEdge(edge), new PreOutInterface(edge.getNode1(), edge.getInt1())))
        .forEach(_rules::add);
  }

  @Override
  public void visitQuery(Query.State query) {}
}
