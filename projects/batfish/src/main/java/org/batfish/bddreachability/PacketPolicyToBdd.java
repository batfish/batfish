package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;
import static org.batfish.bddreachability.transition.Transitions.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.topology.broadcast.Edges;
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
    evaluator.process(policy);
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
    stmtConverter._outTransitionsByTarget.forEach(
        (tgt, trans) -> {
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
    if (stmtConverter._currentStatementOutTransition != ZERO) {
      // add edge to default action
      addEdge(
          stmtConverter.currentStatement(),
          new PacketPolicyAction(_hostname, _vrf, p.getName(), p.getDefaultAction().getAction()),
          stmtConverter._currentStatementOutTransition);
    }
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
    private Map<StateExpr, Transition> _outTransitionsByTarget = new HashMap<>();
    private final List<Edges> _edges = new ArrayList<>();
    private final Set<PacketPolicyAction> _actions = new HashSet<>();

    BottomUpStatementToBdd(StateExpr fallThrough) {
      _outTransitionsByTarget.put(fallThrough, IDENTITY);
    }

    public void visitStatements(List<Statement> statements) {
      for (Statement statement : Lists.reverse(statements)) {
        visit(statement);
      }
    }

    private void addOutTransition(Transition transition, StateExpr successor) {
      _outTransitionsByTarget.compute(
          successor,
          (succ, oldTransition) ->
              oldTransition == null ? transition : or(transition, oldTransition));
    }

    private void constrainOutTransitions(BDD constraintBdd) {
      beforeOutTransitions(constraint(constraintBdd));
    }

    private void beforeOutTransitions(Transition before) {
      _outTransitionsByTarget.replaceAll(
          (succ, transition) -> checkNotNull(mergeComposed(before, transition)));
    }

    @Override
    public Void visitApplyFilter(ApplyFilter applyFilter) {
      BDD permitBdd =
          _boolExprToBdd._ipAccessListToBdd.toBdd(new PermittedByAcl(applyFilter.getFilter()));
      constrainOutTransitions(permitBdd.not());
      addOutTransition(
          constraint(permitBdd),
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
      Map<StateExpr, Transition> thenBranchOutTransitionsByTarget =
          ifStmt.getTrueStatements().isEmpty()
                  || Iterables.getLast(ifStmt.getTrueStatements()) instanceof Return
              ? new HashMap<>()
              : new HashMap<>(_outTransitionsByTarget);

      constrainOutTransitions(matchConstraint.not());
      Map<StateExpr, Transition> elseBranchOutTransitionsByTarget = _outTransitionsByTarget;

      // compute then branch
      _outTransitionsByTarget = thenBranchOutTransitionsByTarget;
      visitStatements(ifStmt.getTrueStatements());

      // merge in else branch
      elseBranchOutTransitionsByTarget.forEach(
          (succ, trans) ->
              _outTransitionsByTarget.compute(
                  succ, (k, oldTrans) -> oldTrans == null ? trans : or(oldTrans, trans)));
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

  /**
   * Walks all the statements in the packet policy, statefully building up BDDs based on boolean
   * expressions that are encountered. When a {@link Return} is encountered, calls into a
   */
  private class StatementToBdd implements StatementVisitor<Void> {
    private final BoolExprToBdd _boolExprToBdd;
    private int _statementCounter = 0;
    private PacketPolicyStatement _currentStatement;

    /* The transition of the (not yet created) edge leading out of currentStatement() the next statement of the policy.
     * We update this constraint instead of creating new states/edges when possible. It's not possible if the policy
     * returns, or if multiple statements lead into the next statement (i.e. due to fallthrough from the then branch of
     * an if statement), or after transformation statements (because in transformations are not expressible as a
     * constraint).
     */
    private Transition _currentStatementOutTransition;

    private BDD _one;
    private BDD _zero;

    Map<List<Statement>, Integer> thenBranchOccurrences = new HashMap<>();

    private StatementToBdd(BoolExprToBdd boolExprToBdd) {
      _boolExprToBdd = boolExprToBdd;
      _currentStatement =
          new PacketPolicyStatement(_hostname, _vrf, _policy.getName(), _statementCounter++);
      BDDFactory factory = _boolExprToBdd._ipAccessListToBdd.getBDDPacket().getFactory();
      _one = factory.one();
      _zero = factory.zero();
      _currentStatementOutTransition = IDENTITY;
    }

    public void visitStatements(List<Statement> statements) {
      for (Statement statement : statements) {
        visit(statement);
        if (_currentStatementOutTransition == ZERO) {
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
      assert _currentStatementOutTransition != ZERO
          : "Should not convert unreachable statements to BDD";
      return stmt.accept(this);
    }

    @Override
    public Void visitIf(If ifStmt) {
      if (ifStmt.getTrueStatements().stream().anyMatch(stmt -> !(stmt instanceof Return))
          && Iterables.getLast(ifStmt.getTrueStatements()) instanceof Return) {
        // nontrivial and non-fall-through
        thenBranchOccurrences.compute(
            ifStmt.getTrueStatements(), (k, oldCount) -> oldCount == null ? 1 : oldCount + 1);
      }
      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());
      // invariant: _currentStatementOutTransition always composes cleanly with a constraint
      Transition thenTrans =
          Transitions.mergeComposed(_currentStatementOutTransition, constraint(matchConstraint));
      Transition elseTrans =
          Transitions.mergeComposed(
              _currentStatementOutTransition,
              // TODO could add a negatedConstraint edge
              constraint(matchConstraint.not()));

      assert thenTrans != null;
      assert elseTrans != null;

      if (thenTrans == ZERO) {
        _currentStatementOutTransition = elseTrans;
        return null;
      }

      PacketPolicyStatement ifSt = currentStatement();

      // initialize pathConstraint for then branch
      _currentStatementOutTransition = thenTrans;
      visitStatements(ifStmt.getTrueStatements());
      Transition thenFallThroughTrans = _currentStatementOutTransition;

      if (elseTrans != ZERO && thenFallThroughTrans != ZERO) {
        // the then branch falls through
        // allocate a new statement node to fan into
        PacketPolicyStatement thenFallThroughSt = currentStatement();
        PacketPolicyStatement nextSt = nextStatement();
        addEdge(ifSt, nextSt, elseTrans);
        addEdge(thenFallThroughSt, nextSt, thenFallThroughTrans);
        _currentStatementOutTransition = IDENTITY;
      } else if (elseTrans != ZERO) {
        _currentStatement = ifSt;
        _currentStatementOutTransition = elseTrans;
      } else if (thenFallThroughTrans != ZERO) {
        _currentStatementOutTransition = thenFallThroughTrans;
      } else {
        // both branches are zero
        _currentStatementOutTransition = ZERO;
      }
      return null;
    }

    @Override
    public Void visitReturn(Return returnStmt) {
      addEdge(
          currentStatement(),
          new PacketPolicyAction(_hostname, _vrf, _policy.getName(), returnStmt.getAction()),
          _currentStatementOutTransition);
      // does not fall through
      _currentStatementOutTransition = ZERO;
      return null;
    }

    @Override
    public Void visitApplyFilter(ApplyFilter applyFilter) {
      BDD permitBdd =
          _boolExprToBdd._ipAccessListToBdd.toBdd(new PermittedByAcl(applyFilter.getFilter()));
      addEdge(
          currentStatement(),
          new PacketPolicyAction(_hostname, _vrf, _policy.getName(), Drop.instance()),
          mergeComposed(_currentStatementOutTransition, constraint(permitBdd.not())));
      _currentStatementOutTransition =
          mergeComposed(_currentStatementOutTransition, constraint(permitBdd));
      return null;
    }

    @Override
    public Void visitApplyTransformation(ApplyTransformation transformation) {
      Transition transition =
          _transformationToTransition.toTransition(transformation.getTransformation());
      _currentStatementOutTransition = compose(_currentStatementOutTransition, transition);
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
