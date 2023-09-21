package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;
import static org.batfish.bddreachability.transition.Transitions.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
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
  private final @Nonnull PacketPolicy _policy;
  private final @Nonnull BoolExprToBdd _boolExprToBdd;
  private final @Nonnull TransformationToTransition _transformationToTransition;
  private final String _hostname;
  private final String _vrf;
  private final Table<StateExpr, StateExpr, List<Transition>> _edges;
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
    return new BddPacketPolicy(buildEdges(evaluator._edges), evaluator._actions.build());
  }

  /**
   * Build a list of Edges (with no parallel edges) from the input transition table. All parallel
   * transitions are merged using {@link Transitions#or(Collection)}.
   */
  private static List<Edge> buildEdges(
      Table<StateExpr, StateExpr, List<Transition>> transitionTable) {
    return transitionTable.cellSet().stream()
        .map(
            cell ->
                new Edge(cell.getRowKey(), cell.getColumnKey(), Transitions.or(cell.getValue())))
        .collect(ImmutableList.toImmutableList());
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
    _edges = HashBasedTable.create();
    _actions = ImmutableSet.builder();
  }

  /** Process a given {@link PacketPolicy} */
  private void process(PacketPolicy p) {
    StatementToBdd stmtConverter = new StatementToBdd(_boolExprToBdd);

    stmtConverter.visitStatements(p.getStatements());

    /* Handle the default action. Default action applies to the remaining packets,
     * which can be expressed as the complement of the union of packets we have already accounted
     * for.
     */
    if (stmtConverter._nextEdgeTransition != ZERO) {
      // add edge to default action
      addEdge(
          stmtConverter.currentStatement(),
          new PacketPolicyAction(_hostname, _vrf, p.getName(), p.getDefaultAction().getAction()),
          stmtConverter._nextEdgeTransition);
    }
  }

  private void addEdge(StateExpr source, StateExpr target, Transition transition) {
    if (transition == ZERO) {
      return;
    }
    List<Transition> transitions = _edges.get(source, target);
    if (transitions == null) {
      transitions = new ArrayList<>();
      _edges.put(source, target, transitions);
    }
    transitions.add(transition);
    if (target instanceof PacketPolicyAction) {
      _actions.add((PacketPolicyAction) target);
    }
  }

  /**
   * Observationally, the graph construction time for large PacketPolicies is much less if we
   * arbitrarily introduce breaks in the statement list.
   *
   * <p>Used in {@link StatementToBdd#visitStatements(List)}.
   */
  static final int STATEMENTS_BEFORE_BREAK = 100;

  /**
   * Walks all the statements in the packet policy, statefully building up BDDs based on boolean
   * expressions that are encountered. When a {@link Return} is encountered, calls into a
   */
  private class StatementToBdd implements StatementVisitor<Void> {
    private final BoolExprToBdd _boolExprToBdd;
    private int _statementCounter = 0;
    private PacketPolicyStatement _currentStatement;

    /* The transition of the (not yet created) edge leading out of currentStatement() the next statement of the policy.
     * We update this transition instead of creating new states/edges when possible. It's not possible if the policy
     * returns, or if multiple statements lead into the next statement (i.e. due to fallthrough from the then branch of
     * an if statement).
     */
    private Transition _nextEdgeTransition;

    private StatementToBdd(BoolExprToBdd boolExprToBdd) {
      _boolExprToBdd = boolExprToBdd;
      _currentStatement =
          new PacketPolicyStatement(_hostname, _vrf, _policy.getName(), _statementCounter++);
      _nextEdgeTransition = IDENTITY;
    }

    public void visitStatements(List<Statement> statements) {
      // count how many statements have been processed for the current statement.
      int counter = 0;
      PacketPolicyStatement currentStatement = currentStatement();
      for (Statement statement : statements) {
        visit(statement);
        if (_nextEdgeTransition == ZERO) {
          // does not fall through, so exit immediately
          return;
        }
        if (currentStatement != currentStatement()) {
          // statement updated current statement; reset the counter
          currentStatement = _currentStatement;
          counter = 0;
        } else {
          ++counter;
          if (counter % STATEMENTS_BEFORE_BREAK == 0) {
            addEdge(currentStatement(), nextStatement(), _nextEdgeTransition);
            _nextEdgeTransition = IDENTITY;
          }
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
      assert _nextEdgeTransition != ZERO : "Should not convert unreachable statements to BDD";
      return stmt.accept(this);
    }

    @Override
    public Void visitIf(If ifStmt) {
      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());
      Transition thenConstraint = constraint(matchConstraint);
      Transition elseConstraint = constraint(matchConstraint.not());
      @Nullable Transition thenTransition = mergeComposed(_nextEdgeTransition, thenConstraint);
      Transition elseTransition;
      if (thenTransition == null) {
        // cannot apply a constraint after _nextEdgeTransition, so insert a state
        addEdge(currentStatement(), nextStatement(), _nextEdgeTransition);
        thenTransition = thenConstraint;
        elseTransition = elseConstraint;
      } else {
        elseTransition =
            checkNotNull(
                mergeComposed(_nextEdgeTransition, elseConstraint),
                "merge thenConstraint succeeded but elseConstraint failed");
      }

      if (thenTransition == ZERO) {
        _nextEdgeTransition = elseTransition;
        return null;
      }

      PacketPolicyStatement ifSt = currentStatement();

      // initialize pathConstraint for then branch
      _nextEdgeTransition = thenTransition;
      visitStatements(ifStmt.getTrueStatements());
      Transition fallThroughTransition = _nextEdgeTransition;

      if (elseTransition != ZERO && fallThroughTransition != ZERO) {
        // the then branch falls through
        PacketPolicyStatement fallThroughSt = currentStatement();
        if (fallThroughSt.equals(ifSt)) {
          _nextEdgeTransition = or(elseTransition, fallThroughTransition);
        } else {
          // allocate a new statement node to fan into
          PacketPolicyStatement nextSt = nextStatement();
          addEdge(ifSt, nextSt, elseTransition);
          addEdge(fallThroughSt, nextSt, fallThroughTransition);
          _nextEdgeTransition = IDENTITY;
        }
      } else if (elseTransition != ZERO) {
        // fallThroughTransition is ZERO
        _currentStatement = ifSt;
        _nextEdgeTransition = elseTransition;
      } else {
        // elseTransition is ZERO.
        _nextEdgeTransition = fallThroughTransition;
      }
      return null;
    }

    @Override
    public Void visitReturn(Return returnStmt) {
      addEdge(
          currentStatement(),
          new PacketPolicyAction(_hostname, _vrf, _policy.getName(), returnStmt.getAction()),
          _nextEdgeTransition);
      // does not fall through
      _nextEdgeTransition = ZERO;
      return null;
    }

    @Override
    public Void visitApplyFilter(ApplyFilter applyFilter) {
      BDD permitBdd =
          _boolExprToBdd._ipAccessListToBdd.toBdd(new PermittedByAcl(applyFilter.getFilter()));
      Transition permitConstraint = constraint(permitBdd);
      Transition denyConstraint = constraint(permitBdd.not());
      @Nullable Transition permitTransition = mergeComposed(_nextEdgeTransition, permitConstraint);
      Transition denyTransition;
      if (permitTransition == null) {
        // cannot apply a constraint after _nextEdgeTransition, so insert a state
        addEdge(currentStatement(), nextStatement(), _nextEdgeTransition);
        permitTransition = permitConstraint;
        denyTransition = denyConstraint;
      } else {
        denyTransition =
            checkNotNull(
                mergeComposed(_nextEdgeTransition, denyConstraint),
                "merge permitConstraint succeeded but denyConstraint failed");
      }

      addEdge(
          currentStatement(),
          new PacketPolicyAction(_hostname, _vrf, _policy.getName(), Drop.instance()),
          denyTransition);
      _nextEdgeTransition = permitTransition;
      return null;
    }

    @Override
    public Void visitApplyTransformation(ApplyTransformation transformation) {
      Transition transition =
          _transformationToTransition.toTransition(transformation.getTransformation());
      @Nullable Transition merged = Transitions.mergeComposed(_nextEdgeTransition, transition);

      if (merged == null) {
        // can't cleanly merge the transformation transition into _nextEdgeTransition, so create a
        // new state
        addEdge(currentStatement(), nextStatement(), _nextEdgeTransition);
        _nextEdgeTransition = transition;
      } else {
        _nextEdgeTransition = merged;
      }
      return null;
    }
  }

  /** Converts boolean expressions to BDDs */
  @VisibleForTesting
  static final class BoolExprToBdd implements BoolExprVisitor<BDD> {
    private final @Nonnull IpAccessListToBdd _ipAccessListToBdd;
    private final @Nonnull IpsRoutedOutInterfaces _ipsRoutedOutInterfaces;

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
      return ops.or(
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
