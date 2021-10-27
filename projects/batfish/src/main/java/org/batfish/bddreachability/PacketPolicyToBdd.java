package org.batfish.bddreachability;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
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
import org.batfish.datamodel.packet_policy.Action;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.BoolExprVisitor;
import org.batfish.datamodel.packet_policy.Conjunction;
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
  private final List<Edge> _edges;
  private PacketPolicyStatement _currentStatement;

  /**
   * Process a given {@link PacketPolicy} and return the {@link PacketPolicyToBdd} that expresses
   * the conversion. Examine the result of the conversion using methods such as
   */
  public static List<Edge> evaluate(
      String hostname,
      PacketPolicy policy,
      IpAccessListToBdd ipAccessListToBdd,
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces) {
    PacketPolicyToBdd evaluator =
        new PacketPolicyToBdd(hostname, policy, ipAccessListToBdd, ipsRoutedOutInterfaces);
    evaluator.process(policy);
    return evaluator._edges;
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
    _edges = new ArrayList<>();
  }

  /** Process a given {@link PacketPolicy} */
  private void process(PacketPolicy p) {
    StatementToBdd stmtConverter = new StatementToBdd(_boolExprToBdd);

    boolean fallThrough = stmtConverter.visitStatements(p.getStatements());

    /* Handle the default action. Default action applies to the remaining packets,
     * which can be expressed as the complement of the union of packets we have already accounted
     * for.
     */
    if (fallThrough) {
      // add edge to default action
      _edges.add(
          new Edge(
              currentStatement(),
              new PacketPolicyAction(_hostname, p.getName(), p.getDefaultAction().getAction())));
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

  /**
   * Walks all the statements in the packet policy, statefully building up BDDs based on boolean
   * expressions that are encountered. When a {@link Return} is encountered, calls into a
   */
  private class StatementToBdd implements StatementVisitor<Boolean> {
    private final BoolExprToBdd _boolExprToBdd;

    private StatementToBdd(BoolExprToBdd boolExprToBdd) {
      _boolExprToBdd = boolExprToBdd;
    }

    public boolean visitStatements(List<Statement> statements) {
      for (Statement statement : statements) {
        if (!visit(statement)) {
          // does not fall through, so exit immediately
          return false;
        }
      }
      return true; // fall-through
    }

    @Override
    public Boolean visitIf(If ifStmt) {
      PacketPolicyStatement ifSt = currentStatement();
      PacketPolicyStatement thenSt = nextStatement();

      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());
      _edges.add(new Edge(ifSt, thenSt, matchConstraint));

      boolean fallThrough = visitStatements(ifStmt.getTrueStatements());

      PacketPolicyStatement fallThroughSt = currentStatement();
      PacketPolicyStatement nextSt = nextStatement();
      _edges.add(new Edge(ifSt, nextSt, matchConstraint.not()));

      if (fallThrough) {
        _edges.add(new Edge(fallThroughSt, nextSt));
      }

      // nextSt falls through to next statement if there is one
      return true;
    }

    @Override
    public Boolean visitReturn(Return returnStmt) {
      _edges.add(
          new Edge(
              currentStatement(),
              new PacketPolicyAction(_hostname, _policy.getName(), returnStmt.getAction())));
      return false; // does not fall through
    }

    @Override
    public Boolean visitApplyTransformation(ApplyTransformation transformation) {
      PacketPolicyStatement preTransformation = currentStatement();
      PacketPolicyStatement postTransformation = nextStatement();
      Transition transition =
          _transformationToTransition.toTransition(transformation.getTransformation());
      _edges.add(new Edge(preTransformation, postTransformation, transition));
      return true; // fall through
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
