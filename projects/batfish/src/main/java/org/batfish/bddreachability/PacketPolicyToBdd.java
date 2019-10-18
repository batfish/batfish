package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Zero;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.packet_policy.Action;
import org.batfish.datamodel.packet_policy.ActionVisitor;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.BoolExprVisitor;
import org.batfish.datamodel.packet_policy.Conjunction;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FalseExpr;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.FibLookupOutgoingInterfaceIsOneOf;
import org.batfish.datamodel.packet_policy.FibLookupOverrideLookupIp;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.StatementVisitor;
import org.batfish.datamodel.packet_policy.TrueExpr;

/**
 * Provides the ability to convert a {@link PacketPolicy} into sets of BDDs corresponding to a
 * particular {@link Action policy action}.
 */
@ParametersAreNonnullByDefault
class PacketPolicyToBdd {
  @Nonnull private final BoolExprToBdd _boolExprToBdd;
  @Nonnull private Transition _toDrop;
  @Nonnull private final Map<FibLookup, Transition> _fibLookups;
  @Nonnull private final TransformationToTransition _transformationToTransition;

  /**
   * Process a given {@link PacketPolicy} and return the {@link PacketPolicyToBdd} that expresses
   * the conversion. Examine the result of the conversion using methods such as {@link #getToDrop()}
   * and {@link #getFibLookups()}
   */
  public static PacketPolicyToBdd evaluate(
      PacketPolicy policy,
      IpAccessListToBdd ipAccessListToBdd,
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
    PacketPolicyToBdd evaluator = new PacketPolicyToBdd(ipAccessListToBdd, ipsRoutedOutInterfaces);
    evaluator.process(policy);
    return evaluator;
  }

  private PacketPolicyToBdd(
      IpAccessListToBdd ipAccessListToBdd, IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
    _boolExprToBdd = new BoolExprToBdd(ipAccessListToBdd, ipsRoutedOutInterfaces);
    _toDrop = Zero.INSTANCE;
    _fibLookups = new HashMap<>(0);
    _transformationToTransition =
        new TransformationToTransition(ipAccessListToBdd.getBDDPacket(), ipAccessListToBdd);
  }

  /** Process a given {@link PacketPolicy} */
  private void process(PacketPolicy p) {
    StatementToBdd stmtConverter = new StatementToBdd(_boolExprToBdd);
    p.getStatements().forEach(stmtConverter::visit);

    /* Handle the default action. Default action applies to the remaining packets,
     * which can be expressed as the complement of the union of packets we have already accounted
     * for.
     */
    new Collector(stmtConverter._pathTransition).visit(p.getDefaultAction().getAction());
  }

  /** Return the set of packets that is dropped by a policy */
  @Nonnull
  public Transition getToDrop() {
    return _toDrop;
  }

  /**
   * Return the sets of packets that must be processed by destination-based forwarding pipeline
   * (expressed as a {@link FibLookup} action).
   */
  @Nonnull
  public Map<FibLookup, Transition> getFibLookups() {
    return _fibLookups;
  }

  /**
   * Walks all the statements in the packet policy, statefully building up BDDs based on boolean
   * expressions that are encountered. When a {@link Return} is encountered, calls into a {@link
   * Collector}
   */
  private final class StatementToBdd implements StatementVisitor<Void> {
    private final BoolExprToBdd _boolExprToBdd;

    /**
     * Transformations and constraints the {@link PacketPolicy} applies to an input BDD along the
     * path from the root of the {@link PacketPolicy} AST to the current node being visited.
     */
    private Transition _pathTransition;

    private StatementToBdd(BoolExprToBdd boolExprToBdd) {
      _boolExprToBdd = boolExprToBdd;
      _pathTransition = IDENTITY;
    }

    @Override
    public Void visitIf(If ifStmt) {
      // Save existing constraint
      Transition reachIf = _pathTransition;
      // Convert IF guard
      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());
      _pathTransition = compose(reachIf, constraint(matchConstraint));
      // Process true statements
      ifStmt.getTrueStatements().forEach(this::visit);
      Transition fallThroughTrueBranch = _pathTransition;
      // If fell through, constrain packets with complement of match condition and move on
      _pathTransition =
          or(fallThroughTrueBranch, compose(reachIf, constraint(matchConstraint.not())));
      return null;
    }

    @Override
    public Void visitReturn(Return returnStmt) {
      new Collector(_pathTransition).visit(returnStmt.getAction());
      _pathTransition = ZERO;
      return null;
    }

    @Override
    public Void visitApplyTransformation(ApplyTransformation transformation) {
      _pathTransition =
          compose(
              _pathTransition,
              _transformationToTransition.toTransition(transformation.getTransformation()));
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

  /**
   * Updates set of packets associated with a particular action (i.e., {@link #_toDrop} and {@link
   * #_fibLookups})
   */
  private final class Collector implements ActionVisitor<Void> {
    private final Transition _transition;

    private Collector(Transition transition) {
      _transition = transition;
    }

    @Override
    public Void visitDrop(Drop drop) {
      _toDrop = or(_toDrop, _transition);
      return null;
    }

    @Override
    public Void visitFibLookup(FibLookup fibLookup) {
      _fibLookups.compute(
          fibLookup,
          (k, oldTransition) ->
              oldTransition == null ? _transition : or(oldTransition, _transition));
      return null;
    }

    @Override
    public Void visitFibLookupOverrideLookupIp(FibLookupOverrideLookupIp fibLookup) {
      // TODO: support for FibLookupOverrideLookupIp
      return null;
    }
  }
}
