package org.batfish.z3.state.visitors;

import static org.batfish.common.util.CommonUtil.forEachWithIndex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.Field;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExpr.State;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineIndependentMatch;
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
import org.batfish.z3.state.NumberedQuery;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutEdge;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutVrf;
import org.batfish.z3.state.Query;

public class DefaultTransitionGenerator implements StateVisitor {
  /* Dedicated value for the srcInterfaceField used when traffic did not enter the node through any
   * interface (or we no longer care which interface was the source).
   */
  public static final int NO_SOURCE_INTERFACE = 0;

  public static final IntExpr NOT_TRANSITED = new LitIntExpr(0, 1);

  public static final IntExpr TRANSITED = new LitIntExpr(1, 1);

  public static final Field TRANSITED_TRANSIT_NODES_FIELD = new Field("TRANSITED_TRANSIT_NODE", 1);

  public static List<RuleStatement> generateTransitions(SynthesizerInput input, Set<State> states) {
    DefaultTransitionGenerator visitor = new DefaultTransitionGenerator(input);
    states.forEach(state -> state.accept(visitor));
    return visitor._rules.build();
  }

  private final SynthesizerInput _input;

  private final ImmutableList.Builder<RuleStatement> _rules;

  public DefaultTransitionGenerator(SynthesizerInput input) {
    _input = input;
    _rules = ImmutableList.builder();
  }

  private @Nonnull BooleanExpr noSrcInterfaceConstraint() {
    Field srcInterface = _input.getSourceInterfaceField();
    return new EqExpr(
        new VarIntExpr(srcInterface), new LitIntExpr(NO_SOURCE_INTERFACE, srcInterface.getSize()));
  }

  // used to update the source interface field value (used when entering a node).
  private @Nonnull BooleanExpr transformedSrcInterfaceConstraint(
      @Nonnull String hostname, @Nonnull String iface) {
    boolean nodeHasSrcInterfaceConstraint =
        _input.getNodesWithSrcInterfaceConstraints().contains(hostname);

    return nodeHasSrcInterfaceConstraint
        ? new EqExpr(
            new TransformedVarIntExpr(_input.getSourceInterfaceField()),
            _input.getSourceInterfaceFieldValues().get(hostname).get(iface))
        : TrueExpr.INSTANCE;
  }

  // used to initialize the source interface field value (used at origination points).
  private @Nonnull BooleanExpr srcInterfaceConstraint(
      @Nonnull String hostname, @Nonnull String iface) {
    boolean nodeHasSrcInterfaceConstraint =
        _input.getNodesWithSrcInterfaceConstraints().contains(hostname);

    return nodeHasSrcInterfaceConstraint
        ? new EqExpr(
            new VarIntExpr(_input.getSourceInterfaceField()),
            _input.getSourceInterfaceFieldValues().get(hostname).get(iface))
        : noSrcInterfaceConstraint();
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
                        if (linesAction == LineAction.DENY) {
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
  public void visitAclLineIndependentMatch(AclLineIndependentMatch.State state) {
    /*
     *  For each acl line, add a rule that its match condition (as a BooleanExpr) implies its
     *  corresponding named state.
     */
    _input
        .getAclConditions()
        .forEach(
            (hostname, aclConditionsByAclName) ->
                aclConditionsByAclName.forEach(
                    (aclName, aclConditionsList) ->
                        forEachWithIndex(
                            aclConditionsList,
                            (lineNumber, lineCriteria) ->
                                _rules.add(
                                    new BasicRuleStatement(
                                        lineCriteria,
                                        new AclLineIndependentMatch(
                                            hostname, aclName, lineNumber))))));
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
                            if (lineAction == LineAction.PERMIT) {
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
  public void visitNeighborUnreachable(NeighborUnreachable.State state) {
    _input
        .getNeighborUnreachable()
        .keySet()
        .forEach(
            hostname ->
                _rules.add(
                    new BasicRuleStatement(
                        new NodeNeighborUnreachable(hostname), NeighborUnreachable.INSTANCE)));
  }

  @Override
  public void visitNodeAccept(NodeAccept.State nodeAccept) {
    // PostInForMe
    _input
        .getIpsByNodeVrf()
        .forEach(
            (hostname, nodeVrfIps) ->
                nodeVrfIps.forEach(
                    (vrf, ips) ->
                        _rules.add(
                            new BasicRuleStatement(
                                new IpSpaceMatchExpr(
                                        IpWildcardSetIpSpace.builder()
                                            .including(
                                                ips.stream()
                                                    .map(IpWildcard::new)
                                                    .collect(ImmutableSet.toImmutableSet()))
                                            .build(),
                                        _input.getNamedIpSpaces().get(hostname),
                                        Field.DST_IP)
                                    .getExpr(),
                                new PostInVrf(hostname, vrf),
                                new NodeAccept(hostname)))));
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
        .getTraversableInterfaces()
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

    // NeighborUnreachable fail OutAcl
    _input
        .getNeighborUnreachable()
        .forEach(
            (hostname, neighborUnreachableByVrf) ->
                neighborUnreachableByVrf.forEach(
                    (vrf, neighborUnreachableByOutInterface) ->
                        neighborUnreachableByOutInterface.forEach(
                            (outIface, dstIpConstraint) -> {
                              String outAcl =
                                  _input
                                      .getOutgoingAcls()
                                      .getOrDefault(hostname, ImmutableMap.of())
                                      .get(outIface);
                              if (outAcl == null) {
                                return;
                              }

                              _rules.add(
                                  new BasicRuleStatement(
                                      dstIpConstraint,
                                      ImmutableSet.of(
                                          new PreOutVrf(hostname, vrf),
                                          new AclDeny(hostname, outAcl)),
                                      new NodeDropAclOut(hostname)));
                            })));
  }

  @Override
  public void visitNodeDropNoRoute(NodeDropNoRoute.State nodeDropNoRoute) {
    // DestinationRouting
    _input
        .getRoutableIps()
        .forEach(
            (hostname, routableIpsByVrf) ->
                routableIpsByVrf.forEach(
                    (vrf, routableIps) ->
                        _rules.add(
                            new BasicRuleStatement(
                                new NotExpr(routableIps),
                                new PreOutVrf(hostname, vrf),
                                new NodeDropNoRoute(hostname)))));
  }

  @Override
  public void visitNodeDropNullRoute(NodeDropNullRoute.State nodeDropNullRoute) {
    // DestinationRouting
    _input
        .getNullRoutedIps()
        .forEach(
            (hostname, nullRoutedIpsByVrf) ->
                nullRoutedIpsByVrf.forEach(
                    (vrf, nullRoutedIps) ->
                        _rules.add(
                            new BasicRuleStatement(
                                nullRoutedIps,
                                new PreOutVrf(hostname, vrf),
                                new NodeDropNullRoute(hostname)))));
  }

  @Override
  public void visitNodeInterfaceNeighborUnreachable(NodeInterfaceNeighborUnreachable.State state) {
    _input
        .getNeighborUnreachable()
        .forEach(
            (hostname, neighborUnreachableByVrf) ->
                neighborUnreachableByVrf.forEach(
                    (vrf, neighborUnreachableByOutInterface) ->
                        neighborUnreachableByOutInterface.forEach(
                            (outIface, dstIpConstraint) -> {
                              ImmutableSet.Builder<StateExpr> preStates = ImmutableSet.builder();
                              preStates.add(new PreOutVrf(hostname, vrf));

                              // add outAcl if one exists
                              String outAcl =
                                  _input
                                      .getOutgoingAcls()
                                      .getOrDefault(hostname, ImmutableMap.of())
                                      .get(outIface);
                              if (outAcl != null) {
                                preStates.add(new AclPermit(hostname, outAcl));
                              }

                              _rules.add(
                                  new BasicRuleStatement(
                                      dstIpConstraint,
                                      preStates.build(),
                                      new NodeInterfaceNeighborUnreachable(hostname, outIface)));
                            })));
  }

  @Override
  public void visitNodeNeighborUnreachable(NodeNeighborUnreachable.State state) {
    _input
        .getNeighborUnreachable()
        .forEach(
            (hostname, neighborUnreachableByVrf) ->
                neighborUnreachableByVrf.forEach(
                    (vrf, neighborUnreachableByOutInterface) ->
                        neighborUnreachableByOutInterface.forEach(
                            (outIface, dstIpConstraint) -> {
                              _rules.add(
                                  new BasicRuleStatement(
                                      new NodeInterfaceNeighborUnreachable(hostname, outIface),
                                      new NodeNeighborUnreachable(hostname)));
                            })));
  }

  @Override
  public void visitNumberedQuery(NumberedQuery.State numberedQuery) {}

  @Override
  public void visitOriginateInterfaceLink(OriginateInterfaceLink.State originateInterface) {}

  @Override
  public void visitOriginateVrf(OriginateVrf.State originateVrf) {}

  @Override
  public void visitPostInInterface(PostInInterface.State postInInterface) {
    // PassIncomingAcl
    _input
        .getEnabledInterfaces()
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
    // Project OriginateVrf
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
                              new AndExpr(
                                  ImmutableList.of(
                                      noSrcInterfaceConstraint(),
                                      transitNodesNotTransitedConstraint())),
                              new OriginateVrf(hostname, vrfName),
                              new PostInVrf(hostname, vrfName)));
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
        /*
         * Don't generate PostOutEdge rules edges where node1 is a nonTransitNode, because
         * PostOutEdge is where the node1 becomes transited.
         */
        .filter(edge -> !_input.getNonTransitNodes().contains(edge.getNode1()))
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
              ImmutableList.Builder<BooleanExpr> preconditionsBuilder = ImmutableList.builder();

              /* If we set the source interface field, reset it now */
              if (_input.getNodesWithSrcInterfaceConstraints().contains(node1)) {
                preconditionsBuilder.add(
                    new EqExpr(
                        new TransformedVarIntExpr(_input.getSourceInterfaceField()),
                        new LitIntExpr(
                            NO_SOURCE_INTERFACE, _input.getSourceInterfaceField().getSize())));
              }

              /* If node1 is a transit node, set its flag */
              if (_input.getTransitNodes().contains(node1)) {
                preconditionsBuilder.add(
                    new EqExpr(
                        new TransformedVarIntExpr(
                            DefaultTransitionGenerator.TRANSITED_TRANSIT_NODES_FIELD),
                        TRANSITED));
              }

              List<BooleanExpr> preconditions = preconditionsBuilder.build();

              return new BasicRuleStatement(
                  preconditions.isEmpty()
                      ? TrueExpr.INSTANCE
                      : preconditions.size() == 1
                          ? preconditions.get(0)
                          : new AndExpr(preconditions),
                  aclStates,
                  new PostOutEdge(node1, iface1, node2, iface2));
            })
        .forEach(_rules::add);
  }

  @Override
  public void visitPreInInterface(PreInInterface.State preInInterface) {
    // OriginateInterfaceLink
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
                      iface ->
                          new BasicRuleStatement(
                              new AndExpr(
                                  ImmutableList.of(
                                      srcInterfaceConstraint(hostname, iface),
                                      transitNodesNotTransitedConstraint())),
                              new OriginateInterfaceLink(hostname, iface),
                              new PreInInterface(hostname, iface)));
            })
        .forEach(_rules::add);

    // PostOutNeighbor
    _input
        .getEnabledEdges()
        .stream()
        .map(
            edge ->
                new BasicRuleStatement(
                    transformedSrcInterfaceConstraint(edge.getNode2(), edge.getInt2()),
                    new PostOutEdge(edge),
                    new PreInInterface(edge.getNode2(), edge.getInt2())))
        .forEach(_rules::add);
  }

  private BooleanExpr transitNodesNotTransitedConstraint() {
    return _input.getTransitNodes().isEmpty()
        ? TrueExpr.INSTANCE
        : new EqExpr(
            new VarIntExpr(DefaultTransitionGenerator.TRANSITED_TRANSIT_NODES_FIELD),
            NOT_TRANSITED);
  }

  @Override
  public void visitPreOutVrf(PreOutVrf.State preOut) {
    // PostInNotMine
    _input
        .getIpsByNodeVrf()
        .forEach(
            (hostname, ipsByVrf) ->
                ipsByVrf.forEach(
                    (vrf, ips) -> {
                      BooleanExpr ipForeignToCurrentNode =
                          new NotExpr(
                              new IpSpaceMatchExpr(
                                      IpWildcardSetIpSpace.builder()
                                          .including(
                                              ips.stream()
                                                  .map(IpWildcard::new)
                                                  .collect(ImmutableSet.toImmutableSet()))
                                          .build(),
                                      _input.getNamedIpSpaces().get(hostname),
                                      Field.DST_IP)
                                  .getExpr());
                      _rules.add(
                          new BasicRuleStatement(
                              ipForeignToCurrentNode,
                              new PostInVrf(hostname, vrf),
                              new PreOutVrf(hostname, vrf)));
                    }));
  }

  @Override
  public void visitPreOutEdge(PreOutEdge.State preOutEdge) {
    // DestinationRouting
    _input
        .getArpTrueEdge()
        .forEach(
            (hostname, arpTrueEdgeByVrf) ->
                arpTrueEdgeByVrf.forEach(
                    (vrf, arpTrueEdgeByOutInterface) ->
                        arpTrueEdgeByOutInterface.forEach(
                            (outInterface, arpTrueEdgeByRecvNode) ->
                                arpTrueEdgeByRecvNode.forEach(
                                    (recvNode, arpTrueEdgeByRecvInterface) ->
                                        arpTrueEdgeByRecvInterface.forEach(
                                            (recvInterface, dstIpConstraint) ->
                                                _rules.add(
                                                    new BasicRuleStatement(
                                                        dstIpConstraint,
                                                        new PreOutVrf(hostname, vrf),
                                                        new PreOutEdge(
                                                            hostname,
                                                            outInterface,
                                                            recvNode,
                                                            recvInterface))))))));
  }

  @Override
  public void visitPreOutEdgePostNat(PreOutEdgePostNat.State preOutEdgePostNat) {
    visitPreOutEdgePostNat_generateTopologyEdgeRules();
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
      AclPermit aclPermit = sourceNats.get(natNumber).getKey();
      if (aclPermit != null) {
        preStates.add(sourceNats.get(natNumber).getKey());
      }

      BooleanExpr transformationExpr = sourceNats.get(natNumber).getValue();

      _rules.add(
          new BasicRuleStatement(
              transformationExpr,
              preStates.build(),
              new PreOutEdgePostNat(node1, iface1, node2, iface2)));

      if (aclPermit == null) {
        // null means accept everything, so no need to consider subsequent NATs.
        break;
      }
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
        new BasicRuleStatement(
            preStates.build(), new PreOutEdgePostNat(node1, iface1, node2, iface2)));
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
  public void visitQuery(Query.State query) {}
}
