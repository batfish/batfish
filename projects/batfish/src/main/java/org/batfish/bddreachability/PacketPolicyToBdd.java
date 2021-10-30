package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.constraint;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
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
  @Nonnull private final PacketPolicy _policy;
  @Nonnull private final BoolExprToBdd _boolExprToBdd;
  @Nonnull private final TransformationToTransition _transformationToTransition;
  private final String _hostname;
  private final ImmutableList.Builder<Edge> _edges;
  private final ImmutableSet.Builder<PacketPolicyAction> _actions;
  private PacketPolicyStatement _currentStatement;

  public static final class BddPacketPolicy {
    private final List<Edge> _edges;
    private final Set<PacketPolicyAction> _actions;

    public BddPacketPolicy(List<Edge> edges, Set<PacketPolicyAction> actions) {
      _edges = edges;
      _actions = actions;
    }

    public List<Edge> getEdges() {
      return _edges;
    }

    public Set<PacketPolicyAction> getActions() {
      return _actions;
    }
  }

  /**
   * Process a given {@link PacketPolicy} and return the {@link PacketPolicyToBdd} that expresses
   * the conversion. Examine the result of the conversion using methods such as
   */
  public static BddPacketPolicy evaluate(
      String hostname,
      PacketPolicy policy,
      IpAccessListToBdd ipAccessListToBdd,
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
    PacketPolicyToBdd evaluator =
        new PacketPolicyToBdd(hostname, policy, ipAccessListToBdd, ipsRoutedOutInterfaces);
    evaluator.process(policy);
    return new BddPacketPolicy(evaluator._edges.build(), evaluator._actions.build());
  }

  private PacketPolicyToBdd(
      String hostname,
      PacketPolicy policy,
      IpAccessListToBdd ipAccessListToBdd,
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
    _hostname = hostname;
    _policy = policy;
    _currentStatement = new PacketPolicyStatement(_hostname, _policy.getName(), 0);
    _boolExprToBdd = new BoolExprToBdd(ipAccessListToBdd, ipsRoutedOutInterfaces);
    _transformationToTransition =
        new TransformationToTransition(ipAccessListToBdd.getBDDPacket(), ipAccessListToBdd);
    _edges = ImmutableList.builder();
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
    if (!stmtConverter._pathConstraint.isZero()) {
      // add edge to default action
      _edges.add(
          new Edge(
              currentStatement(),
              new PacketPolicyAction(_hostname, p.getName(), p.getDefaultAction().getAction()),
              stmtConverter._pathConstraint));
    }
  }

  private PacketPolicyStatement currentStatement() {
    return _currentStatement;
  }

  private PacketPolicyStatement nextStatement() {
    _currentStatement =
        new PacketPolicyStatement(_hostname, _policy.getName(), _currentStatement.getId() + 1);
    return _currentStatement;
  }

  private void addEdge(StateExpr source, StateExpr target, BDD bdd) {
    addEdge(source, target, constraint(bdd));
  }

  private void addEdge(StateExpr source, StateExpr target, Transition transition) {
    if (transition == ZERO) {
      return;
    }
    _edges.add(new Edge(source, target, transition));
    if (target instanceof PacketPolicyAction) {
      _actions.add((PacketPolicyAction) target);
    }
  }

  /**
   * Walks all the statements in the packet policy, statefully building up BDDs based on boolean
   * expressions that are encountered. When a {@link Return} is encountered, calls into a
   */
  private class StatementToBdd implements StatementVisitor<Void> {
    private final BoolExprToBdd _boolExprToBdd;
    private BDD _pathConstraint;

    private StatementToBdd(BoolExprToBdd boolExprToBdd) {
      _boolExprToBdd = boolExprToBdd;
      _pathConstraint = _boolExprToBdd._ipAccessListToBdd.getBDDPacket().getFactory().one();
    }

    public void visitStatements(List<Statement> statements) {
      for (Statement statement : statements) {
        visit(statement);
        if (_pathConstraint.isZero()) {
          // does not fall through, so exit immediately
          return;
        }
      }
    }

    @Override
    public Void visit(Statement stmt) {
      // if this happens, we're generating dead parts of the graph. No need to crash in prod, so
      // using assert.
      assert !_pathConstraint.isZero() : "Should not convert unreachable statements to BDD";
      return stmt.accept(this);
    }

    @Override
    public Void visitIf(If ifStmt) {
      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());
      BDD thenConstraint = _pathConstraint.and(matchConstraint);
      BDD elseConstraint = _pathConstraint.diff(matchConstraint);

      if (thenConstraint.isZero()) {
        _pathConstraint = elseConstraint;
        return null;
      }

      PacketPolicyStatement inSt = currentStatement();

      // initialize pathConstraint for then branch
      _pathConstraint = thenConstraint;
      visitStatements(ifStmt.getTrueStatements());

      if (!_pathConstraint.isZero()) {
        // the then branch falls through
        // allocate a new statement node to fan into
        PacketPolicyStatement fallThroughSt = currentStatement();
        PacketPolicyStatement nextSt = nextStatement();
        addEdge(inSt, nextSt, elseConstraint);
        addEdge(fallThroughSt, nextSt, _pathConstraint);
        _pathConstraint = _pathConstraint.getFactory().one();
      } else {
        _pathConstraint = elseConstraint;
      }
      return null;
    }

    @Override
    public Void visitReturn(Return returnStmt) {
      addEdge(
          currentStatement(),
          new PacketPolicyAction(_hostname, _policy.getName(), returnStmt.getAction()),
          _pathConstraint);
      // does not fall through
      _pathConstraint = _pathConstraint.getFactory().zero();
      return null;
    }

    @Override
    public Void visitApplyFilter(ApplyFilter applyFilter) {
      BDD permitBdd =
          _boolExprToBdd._ipAccessListToBdd.toBdd(new PermittedByAcl(applyFilter.getFilter()));
      addEdge(
          currentStatement(),
          new PacketPolicyAction(_hostname, _policy.getName(), Drop.instance()),
          _pathConstraint.diff(permitBdd));
      _pathConstraint = _pathConstraint.and(permitBdd);
      return null;
    }

    @Override
    public Void visitApplyTransformation(ApplyTransformation transformation) {
      if (!_pathConstraint.isOne()) {
        // allocate a new statement and apply the path constraint.
        addEdge(currentStatement(), nextStatement(), _pathConstraint);
        _pathConstraint = _pathConstraint.getFactory().one();
      }
      PacketPolicyStatement preTransformation = currentStatement();
      PacketPolicyStatement postTransformation = nextStatement();
      Transition transition =
          _transformationToTransition.toTransition(transformation.getTransformation());
      addEdge(preTransformation, postTransformation, transition);
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
