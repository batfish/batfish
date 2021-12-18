package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;
import static org.batfish.bddreachability.transition.Transitions.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.transition.Or;
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.packet_policy.Action;
import org.batfish.datamodel.packet_policy.ApplyFilter;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.BoolExprVisitor;
import org.batfish.datamodel.packet_policy.Conjunction;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FalseExpr;
import org.batfish.datamodel.packet_policy.FibLookupOutgoingInterfaceIsOneOf;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.Statement;
import org.batfish.datamodel.packet_policy.StatementVisitor;
import org.batfish.datamodel.packet_policy.TrueExpr;
import org.batfish.symbolic.state.PacketPolicyAction;
import org.batfish.symbolic.state.PacketPolicyStatement;
import org.batfish.symbolic.state.StateExpr;

/**
 * Provides the ability to convert a {@link PacketPolicy} into sets of BDDs corresponding to a
 * particular {@link Action policy action}.
 */
@ParametersAreNonnullByDefault
class PacketPolicyToBdd {
  @Nonnull private final PacketPolicy _policy;
  @Nonnull private final BoolExprToBdd _boolExprToBdd;
  @Nonnull private final TransformationToTransition _transformationToTransition;
  private final String _hostname;
  private final String _vrf;
  private final Table<StateExpr, StateExpr, ArrayList<Transition>> _transitions;
  private final ImmutableSet.Builder<PacketPolicyAction> _actions;

  /**
   * A subgraph (forming a DAG) for a particular {@link PacketPolicy} on a particular node/VRF. The
   * subgraph is rooted at the {@link PacketPolicyStatement} with id 0, and has one or more {@link
   * PacketPolicyAction} leaves. Every {@link PacketPolicyAction} must be a leaf (i.e. have no
   * out-edges). There can be any number of {@link PacketPolicyStatement} states between the root
   * and a leaf.
   */
  public static final class BddPacketPolicy {
    private final List<Edge> _edges;
    private final Set<PacketPolicyAction> _actions;

    public BddPacketPolicy(List<Edge> edges, Set<PacketPolicyAction> actions) {
      _edges = edges;
      _actions = actions;
      // Sanity check edges and actions.
      assert edges.stream()
              .map(Edge::getPostState)
              .filter(s -> s instanceof PacketPolicyAction)
              .map(s -> (PacketPolicyAction) s)
              .collect(Collectors.toSet())
              .equals(actions)
          : "actions and edges are not consistent";
    }

    public List<Edge> getEdges() {
      return _edges;
    }

    public Set<PacketPolicyAction> getActions() {
      return _actions;
    }
  }

  /**
   * Process a given {@link PacketPolicy} and return the {@link BddPacketPolicy} that expresses the
   * conversion.
   */
  public static BddPacketPolicy evaluate(
      String hostname,
      String vrf,
      PacketPolicy policy,
      IpAccessListToBdd ipAccessListToBdd,
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
    PacketPolicyToBdd evaluator =
        new PacketPolicyToBdd(hostname, vrf, policy, ipAccessListToBdd, ipsRoutedOutInterfaces);
    evaluator.process2(policy);
    return new BddPacketPolicy(evaluator.getEdges(), evaluator._actions.build());
  }

  private PacketPolicyToBdd(
      String hostname,
      String vrf,
      PacketPolicy policy,
      IpAccessListToBdd ipAccessListToBdd,
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
    _hostname = hostname;
    _policy = policy;
    _vrf = vrf;
    _boolExprToBdd = new BoolExprToBdd(ipAccessListToBdd, ipsRoutedOutInterfaces);
    _transformationToTransition =
        new TransformationToTransition(ipAccessListToBdd.getBDDPacket(), ipAccessListToBdd);
    _transitions = HashBasedTable.create();
    _actions = ImmutableSet.builder();
  }

  private static final Logger LOGGER = LogManager.getLogger(PacketPolicyToBdd.class);

  private void process(PacketPolicy p) {
    BottomUpStatementToBdd stmtConverter =
        new BottomUpStatementToBdd(
            new PacketPolicyAction(_hostname, _vrf, p.getName(), p.getDefaultAction().getAction()));
    stmtConverter.visitStatements(p.getStatements());
    StateExpr src = new PacketPolicyStatement(_hostname, _vrf, _policy.getName(), 0);
    LOGGER.info("bottom up computed {} out edges", stmtConverter._outTransitionsByTarget.size());
    stmtConverter._edges.forEach(
        e -> addEdge(e.getPreState(), e.getPostState(), e.getTransition()));
    stmtConverter
        ._outTransitionsByTarget
        .asMap()
        .forEach(
            (tgt, transitions) -> {
              LOGGER.info("combining {} transitions to {}", transitions.size(), tgt);
              Transition trans = or(transitions.stream());
              if (trans != ZERO) {
                addEdge(src, tgt, trans);
              }
            });
  }
  /** Process a given {@link PacketPolicy} */
  private void process2(PacketPolicy p) {
    StatementToBdd stmtConverter = new StatementToBdd(_boolExprToBdd);

    stmtConverter.visitStatements(p.getStatements());

    Map<Integer, Long> numBranchesByNumOccurrences =
        stmtConverter.thenBranchOccurrences.entrySet().stream()
            .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.counting()));
    numBranchesByNumOccurrences.forEach(
        (numOccs, numBranches) -> {
          LOGGER.info(
              "{} nontrivial non-fallthrough branches had {} occurrences", numBranches, numOccs);
        });
    stmtConverter.thenBranchOccurrences.forEach(
        (branch, occs) -> {
          if (occs > 1) {
            LOGGER.info("branch with {} occurrences:\n{}", occs, branch);
          }
        });

    /* Handle the default action. Default action applies to the remaining packets,
     * which can be expressed as the complement of the union of packets we have already accounted
     * for.
     */
    if (stmtConverter._reachableBddsByTopLevel != null) {
      stmtConverter._outEdges.put(
          stmtConverter.currentStatement(),
          new OutEdgeInfo(
              stmtConverter._reachableBddsByTopLevel,
              stmtConverter._outTransition,
              new PacketPolicyAction(
                  _hostname, _vrf, p.getName(), p.getDefaultAction().getAction())));
    }

    stmtConverter
        ._outEdges
        .asMap()
        .forEach(
            (srcState, outEdgesInfo) -> {
              // I have a bunch of OutEdgeInfos. what's the right way to branch?
              // if they're all branching at the same level, great. group by that and then go.
              Map<Integer, Integer> outEdgeCountByLevel = new HashMap<>();
              Multimap<Integer, BDD> outEdgeConstraintsByLevel = HashMultimap.create();
              outEdgesInfo.forEach(
                  outEdgeInfo ->
                      outEdgeInfo._guardsByTopVar.forEach(
                          (level, bdd) -> {
                            outEdgeCountByLevel.compute(
                                level, (unused, count) -> count == null ? 1 : count + 1);
                            outEdgeConstraintsByLevel.put(level, bdd);
                          }));

              Map<Integer, Integer> numOutEdgeConstraintsByLevel =
                  CollectionUtil.toImmutableMap(
                      outEdgeConstraintsByLevel.asMap(),
                      Map.Entry::getKey,
                      entry -> entry.getValue().size());

              LOGGER.info(
                  "out edges from {}...\n"
                      + " with constraints by level: {}\n"
                      + " unique constraints per level: {}",
                  srcState,
                  outEdgeCountByLevel,
                  numOutEdgeConstraintsByLevel);

              // just add the edges for now -- don't want to think hard
              outEdgesInfo.forEach(
                  outEdgeInfo -> {
                    Transition t = outEdgeInfo._transition;
                    if (!outEdgeInfo._guardsByTopVar.isEmpty()) {
                      t =
                          compose(
                              constraint(BDDOps.andNull(outEdgeInfo._guardsByTopVar.values())), t);
                    }
                    addEdge(srcState, outEdgeInfo._target, t);
                  });
            });
  }

  private void addEdge(StateExpr source, StateExpr target, Transition transition) {
    if (transition == ZERO) {
      return;
    }
    ArrayList<Transition> transitions = _transitions.get(source, target);
    if (transitions == null) {
      transitions = new ArrayList<>();
      _transitions.put(source, target, transitions);
    }
    transitions.add(transition);
    if (target instanceof PacketPolicyAction) {
      _actions.add((PacketPolicyAction) target);
    }
  }

  private List<Edge> getEdges() {
    return _transitions.cellSet().stream()
        .map(cell -> new Edge(cell.getRowKey(), cell.getColumnKey(), or(cell.getValue().stream())))
        .collect(Collectors.toList());
  }

  private class BottomUpStatementToBdd implements StatementVisitor<Void> {
    private Multimap<StateExpr, Transition> _outTransitionsByTarget = HashMultimap.create();
    private final List<Edge> _edges = new ArrayList<>();

    private int _statementCounter = 1;
    private PacketPolicyStatement _currentStatement;

    private PacketPolicyStatement nextStatement() {
      _currentStatement =
          new PacketPolicyStatement(_hostname, _vrf, _policy.getName(), _statementCounter++);
      return _currentStatement;
    }

    BottomUpStatementToBdd(StateExpr fallThrough) {
      _outTransitionsByTarget.put(fallThrough, IDENTITY);
      _currentStatement =
          new PacketPolicyStatement(_hostname, _vrf, _policy.getName(), _statementCounter++);
    }

    public void visitStatements(List<Statement> statements) {
      for (Statement statement : Lists.reverse(statements)) {
        visit(statement);
      }
    }

    private void addOutTransition(Transition transition, StateExpr successor) {
      _outTransitionsByTarget.put(successor, transition);
    }

    private void constrainOutTransitions(BDD constraintBdd) {
      beforeOutTransitions(constraint(constraintBdd));
    }

    private void notConstraintBeforeOutTransitions(BDD bddNot) {
      // TODO ensure composes cleanly
      List<StateExpr> successors = ImmutableList.copyOf(_outTransitionsByTarget.keySet());
      for (StateExpr successor : successors) {
        Collection<Transition> after = _outTransitionsByTarget.get(successor);

        LOGGER.info(
            "notConstraintBeforeOutTransitions: {} transitions to successor {}",
            after.size(),
            successor);

        if (after.size() > 200) {
          int oldSize = after.size();
          Transition orAfter = or(after.stream());
          after =
              orAfter instanceof Or ? ((Or) orAfter).getTransitions() : ImmutableList.of(orAfter);
          LOGGER.info("Compressed from {} to {} transitions", oldSize, after.size());
        }

        List<Transition> transitions = new ArrayList<>(after.size());
        boolean mergeFailed = false;
        for (Transition t : after) {
          Transition newT = t.andNotBefore(bddNot);
          if (newT == null) {
            mergeFailed = true;
            break;
          }
          if (newT != ZERO) {
            transitions.add(newT);
          }
        }

        if (!mergeFailed) {
          _outTransitionsByTarget.replaceValues(successor, transitions);
          continue;
        }

        StateExpr stateExpr = nextStatement();
        LOGGER.info(
            "failed to apply not constraint before {} transitions to {}. splicing {}",
            after.size(),
            successor,
            stateExpr);
        _outTransitionsByTarget.removeAll(successor);
        _edges.add(new Edge(stateExpr, successor, or(after.stream())));
        _outTransitionsByTarget.put(stateExpr, constraint(bddNot.not()));
      }
    }

    private void beforeOutTransitions(Transition before) {
      // TODO ensure composes cleanly
      List<StateExpr> successors = ImmutableList.copyOf(_outTransitionsByTarget.keySet());
      for (StateExpr successor : successors) {
        Collection<Transition> after = _outTransitionsByTarget.get(successor);
        LOGGER.info(
            "beforeOutTransitions: {} transitions to successor {}", after.size(), successor);

        List<Transition> transitions = new ArrayList<>(after.size());
        boolean mergeFailed = false;
        for (Transition t : after) {
          Transition newT = mergeComposed(before, t);
          if (newT == null) {
            mergeFailed = true;
            break;
          }
          if (newT != ZERO) {
            transitions.add(newT);
          }
        }
        if (!mergeFailed) {
          _outTransitionsByTarget.replaceValues(successor, transitions);
          continue;
        }
        StateExpr stateExpr = nextStatement();
        LOGGER.info(
            "failed to merge transition {} into {} transitions to {}. splicing {}",
            before.getClass().getSimpleName(),
            after.size(),
            successor,
            stateExpr);
        _outTransitionsByTarget.removeAll(successor);
        _edges.add(new Edge(stateExpr, successor, or(after.stream())));
        _outTransitionsByTarget.put(stateExpr, before);
      }
    }

    @Override
    public Void visitApplyFilter(ApplyFilter applyFilter) {
      BDD permitBdd =
          _boolExprToBdd._ipAccessListToBdd.toBdd(new PermittedByAcl(applyFilter.getFilter()));
      constrainOutTransitions(permitBdd);
      addOutTransition(
          constraint(permitBdd.not()),
          new PacketPolicyAction(_hostname, _vrf, _policy.getName(), Drop.instance()));
      return null;
    }

    @Override
    public Void visitApplyTransformation(ApplyTransformation transformation) {
      Transition transition =
          _transformationToTransition.toTransition(transformation.getTransformation());
      beforeOutTransitions(transition);
      return null;
    }

    @Override
    public Void visitIf(If ifStmt) {
      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());

      // copy out transitions if there's fall through
      // if it's empty (noop), can skip. this shouldn't happen, but we don't control conversion
      Multimap<StateExpr, Transition> thenBranchOutTransitionsByTarget =
          ifStmt.getTrueStatements().isEmpty()
                  || Iterables.getLast(ifStmt.getTrueStatements()) instanceof Return
              ? HashMultimap.create()
              : HashMultimap.create(_outTransitionsByTarget);

      notConstraintBeforeOutTransitions(matchConstraint);
      Multimap<StateExpr, Transition> elseBranchOutTransitionsByTarget = _outTransitionsByTarget;

      // compute then branch
      _outTransitionsByTarget = thenBranchOutTransitionsByTarget;
      visitStatements(ifStmt.getTrueStatements());
      constrainOutTransitions(matchConstraint);

      // merge in else branch
      _outTransitionsByTarget.putAll(elseBranchOutTransitionsByTarget);
      return null;
    }

    @Override
    public Void visitReturn(Return returnStmt) {
      _outTransitionsByTarget
          .clear(); // does not fall through. TODO if we are clearing nontrivial, should avoid that.
      addOutTransition(
          IDENTITY,
          new PacketPolicyAction(_hostname, _vrf, _policy.getName(), returnStmt.getAction()));
      return null;
    }
  }

  private static final class OutEdgeInfo {
    final Map<Integer, BDD> _guardsByTopVar;
    final Transition _transition;
    final StateExpr _target;

    private OutEdgeInfo(Map<Integer, BDD> guardsByTopVar, Transition transition, StateExpr target) {
      _guardsByTopVar = ImmutableMap.copyOf(guardsByTopVar);
      _transition = transition;
      _target = target;
    }
  }

  /**
   * Walks all the statements in the packet policy, statefully building up BDDs based on boolean
   * expressions that are encountered. When a {@link Return} is encountered, calls into a
   */
  private class StatementToBdd implements StatementVisitor<Void> {
    private final BoolExprToBdd _boolExprToBdd;
    private int _statementCounter = 0;
    private PacketPolicyStatement _currentStatement;

    Map<List<Statement>, Integer> thenBranchOccurrences = new HashMap<>();

    // empty map means unconstrained. null means zero
    Map<Integer, BDD> _reachableBddsByTopLevel = new HashMap<>();
    Transition _outTransition = IDENTITY;

    Multimap<StateExpr, OutEdgeInfo> _outEdges = HashMultimap.create();

    private StatementToBdd(BoolExprToBdd boolExprToBdd) {
      _boolExprToBdd = boolExprToBdd;
      _currentStatement =
          new PacketPolicyStatement(_hostname, _vrf, _policy.getName(), _statementCounter++);
      BDDFactory factory = _boolExprToBdd._ipAccessListToBdd.getBDDPacket().getFactory();
    }

    public void visitStatements(List<Statement> statements) {
      for (Statement statement : statements) {
        visit(statement);
        if (_reachableBddsByTopLevel == null) {
          // does not fall through, so exit immediately
          return;
        }
      }
    }

    private PacketPolicyStatement currentStatement() {
      return _currentStatement;
    }

    private PacketPolicyStatement nextStatement() {
      _currentStatement =
          new PacketPolicyStatement(_hostname, _vrf, _policy.getName(), _statementCounter++);
      return _currentStatement;
    }

    @Override
    public Void visit(Statement stmt) {
      // if this happens, we're generating dead parts of the graph. No need to crash in prod, so
      // using assert.
      assert _reachableBddsByTopLevel != null : "Should not convert unreachable statements to BDD";
      return stmt.accept(this);
    }

    @Override
    public Void visitIf(If ifStmt) {
      if (_outTransition != IDENTITY) {
        // cannot constrain after transition
        PacketPolicyStatement src = _currentStatement;
        PacketPolicyStatement tgt = nextStatement();
        _outEdges.put(src, new OutEdgeInfo(_reachableBddsByTopLevel, _outTransition, tgt));
        _reachableBddsByTopLevel.clear();
        _outTransition = IDENTITY;
      }

      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());

      int matchConstraintLevel = matchConstraint.level();
      BDD oldReach = _reachableBddsByTopLevel.get(matchConstraintLevel);

      BDD thenReach = oldReach == null ? matchConstraint : oldReach.and(matchConstraint);
      BDD elseReach = oldReach == null ? matchConstraint.not() : oldReach.diff(matchConstraint);

      Map<Integer, BDD> thenReachable = null;
      if (!thenReach.isZero()) {
        thenReachable = new HashMap<>(_reachableBddsByTopLevel);
        thenReachable.put(matchConstraintLevel, thenReach);
      }

      Map<Integer, BDD> elseReachable = null;
      if (!elseReach.isZero()) {
        elseReachable = new HashMap<>(_reachableBddsByTopLevel);
        elseReachable.put(matchConstraintLevel, elseReach);
      }

      if (thenReachable == null) {
        _reachableBddsByTopLevel = elseReachable;
        return null;
      }

      PacketPolicyStatement ifSt = currentStatement();

      // initialize pathConstraint for then branch
      _reachableBddsByTopLevel = thenReachable;
      visitStatements(ifStmt.getTrueStatements());

      Map<Integer, BDD> thenFallThroughReachable = _reachableBddsByTopLevel;
      Transition thenFallThroughTrans = _outTransition;

      if (elseReachable != null && thenFallThroughReachable != null) {
        // assume out transition is nonnull, and thus we need to create a new node

        // the then branch falls through
        // allocate a new statement node to fan into
        PacketPolicyStatement thenFallThroughSt = currentStatement();
        PacketPolicyStatement nextSt = nextStatement();

        // invariant: else branch transition is IDENTITY (because at the beginning of this method we
        // add a node if not)
        _outEdges.put(ifSt, new OutEdgeInfo(elseReachable, IDENTITY, nextSt));
        _outEdges.put(
            thenFallThroughSt,
            new OutEdgeInfo(thenFallThroughReachable, thenFallThroughTrans, nextSt));
        _reachableBddsByTopLevel.clear();
        _outTransition = IDENTITY;
      } else if (elseReachable != null) {
        _currentStatement = ifSt;
        _reachableBddsByTopLevel = elseReachable;
        _outTransition = IDENTITY;
      } else if (thenFallThroughTrans != null) {
        return null;
      } else {
        // both branches are zero
        _reachableBddsByTopLevel = null;
        _outTransition = null;
      }
      return null;
    }

    @Override
    public Void visitReturn(Return returnStmt) {
      _outEdges.put(
          currentStatement(),
          new OutEdgeInfo(
              _reachableBddsByTopLevel,
              _outTransition,
              new PacketPolicyAction(_hostname, _vrf, _policy.getName(), returnStmt.getAction())));
      // does not fall through
      _reachableBddsByTopLevel = null;
      _outTransition = null;
      return null;
    }

    @Override
    public Void visitApplyFilter(ApplyFilter applyFilter) {
      if (_outTransition != IDENTITY) {
        // cannot constrain after transition
        PacketPolicyStatement src = currentStatement();
        PacketPolicyStatement target = nextStatement();
        _outEdges.put(src, new OutEdgeInfo(_reachableBddsByTopLevel, _outTransition, target));
        _reachableBddsByTopLevel.clear();
        _outTransition = IDENTITY;
      }

      BDD permitBdd =
          _boolExprToBdd._ipAccessListToBdd.toBdd(new PermittedByAcl(applyFilter.getFilter()));

      Map<Integer, BDD> reachAndDeny = new HashMap<>(_reachableBddsByTopLevel);
      reachAndDeny.compute(
          permitBdd.level(),
          (level, oldReach) -> oldReach == null ? permitBdd.not() : oldReach.diff(permitBdd));

      _outEdges.put(
          _currentStatement,
          new OutEdgeInfo(
              reachAndDeny,
              IDENTITY,
              new PacketPolicyAction(_hostname, _vrf, _policy.getName(), Drop.instance())));

      _reachableBddsByTopLevel.compute(
          permitBdd.level(),
          (level, oldReach) -> oldReach == null ? permitBdd : oldReach.and(permitBdd));
      return null;
    }

    @Override
    public Void visitApplyTransformation(ApplyTransformation transformation) {
      Transition transition =
          _transformationToTransition.toTransition(transformation.getTransformation());
      _outTransition = compose(_outTransition, transition);
      return null;
    }
  }

  /** Converts boolean expressions to BDDs */
  @VisibleForTesting
  static final class BoolExprToBdd implements BoolExprVisitor<BDD> {
    @Nonnull private final IpAccessListToBdd _ipAccessListToBdd;
    @Nonnull private final IpsRoutedOutInterfaces _ipsRoutedOutInterfaces;

    BoolExprToBdd(
        IpAccessListToBdd ipAccessListToBdd, IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
      _ipAccessListToBdd = ipAccessListToBdd;
      _ipsRoutedOutInterfaces = ipsRoutedOutInterfaces;
    }

    @Override
    public BDD visitPacketMatchExpr(PacketMatchExpr expr) {
      return _ipAccessListToBdd.toBdd(expr.getExpr());
    }

    @Override
    public BDD visitTrueExpr(TrueExpr expr) {
      return _ipAccessListToBdd.getBDDPacket().getFactory().one();
    }

    @Override
    public BDD visitFalseExpr(FalseExpr expr) {
      return _ipAccessListToBdd.getBDDPacket().getFactory().zero();
    }

    @Override
    public BDD visitFibLookupOutgoingInterfaceIsOneOf(FibLookupOutgoingInterfaceIsOneOf expr) {
      BDDPacket bddPacket = _ipAccessListToBdd.getBDDPacket();
      IpSpaceToBDD dst = bddPacket.getDstIpSpaceToBDD();
      BDDOps ops = new BDDOps(bddPacket.getFactory());
      return ops.orAll(
          expr.getInterfaceNames().stream()
              .map(_ipsRoutedOutInterfaces::getIpsRoutedOutInterface)
              .map(dst::visit)
              .collect(ImmutableList.toImmutableList()));
    }

    @Override
    public BDD visitConjunction(Conjunction expr) {
      return expr.getConjuncts().stream()
          .map(this::visit)
          .reduce(_ipAccessListToBdd.getBDDPacket().getFactory().one(), BDD::and);
    }
  }
}
