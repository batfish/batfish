package org.batfish.bddreachability;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.packet_policy.Action;
import org.batfish.datamodel.packet_policy.ActionVisitor;
import org.batfish.datamodel.packet_policy.BoolExprVisitor;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.StatementVisitor;

/**
 * Provides the ability to convert a {@link PacketPolicy} into sets of BDDs corresponding to a
 * particular {@link Action policy action}.
 */
@ParametersAreNonnullByDefault
class PacketPolicyToBdd {

  @Nonnull private final BDDPacket _bddPacket;
  @Nonnull private final BoolExprToBdd _boolExprToBdd;
  @Nonnull private final BDD _zero;

  @Nonnull private BDD _toDrop;
  @Nonnull private final Map<FibLookup, BDD> _fibLookups;

  /**
   * Process a given {@link PacketPolicy} and return the {@link PacketPolicyToBdd} that expresses
   * the conversion. Examine the result of the conversion using methods such as {@link #getToDrop()}
   * and {@link #getFibLookups()}
   */
  public static PacketPolicyToBdd evaluate(
      PacketPolicy policy, BDDPacket packet, IpAccessListToBdd ipAccessListToBdd) {
    PacketPolicyToBdd evaluator = new PacketPolicyToBdd(packet, ipAccessListToBdd);
    evaluator.process(policy);
    return evaluator;
  }

  private PacketPolicyToBdd(BDDPacket packet, IpAccessListToBdd ipAccessListToBdd) {
    _bddPacket = packet;
    _boolExprToBdd = new BoolExprToBdd(ipAccessListToBdd);
    _zero = _bddPacket.getFactory().zero();
    _toDrop = _zero;
    _fibLookups = new HashMap<>(0);
  }

  /** Process a given {@link PacketPolicy} */
  private void process(PacketPolicy p) {
    StatementToBdd stmtConverter = new StatementToBdd(_boolExprToBdd);
    p.getStatements().forEach(stmtConverter::visit);

    /* Handle the default action. Default action applies to the remaining packets,
     * which can be expressed as the complement of the union of packets we have already accounted
     * for.
     */
    new Collector(firstNonNull(BDDOps.orNull(_fibLookups.values()), _zero).nor(_toDrop))
        .visit(p.getDefaultAction().getAction());
  }

  /** Return the set of packets that is dropped by a policy */
  @Nonnull
  public BDD getToDrop() {
    return _toDrop;
  }

  /**
   * Return the sets of packets that must be processed by destination-based forwarding pipeline
   * (expressed as a {@link FibLookup} action).
   */
  @Nonnull
  public Map<FibLookup, BDD> getFibLookups() {
    return _fibLookups;
  }

  /**
   * Walks all the statements in the packet policy, statefully building up BDDs based on boolean
   * expressions that are encountered. When a {@link Return} is encountered, calls into a {@link
   * Collector}
   */
  private final class StatementToBdd implements StatementVisitor<Void> {
    private final BoolExprToBdd _boolExprToBdd;
    private final BDD _identity;
    private BDD _currentConstraint;

    private StatementToBdd(BoolExprToBdd boolExprToBdd) {
      _boolExprToBdd = boolExprToBdd;
      _identity = _bddPacket.getFactory().one();
      _currentConstraint = _identity;
    }

    @Override
    public Void visitIf(If ifStmt) {
      // Save existing constraint
      BDD oldConstraint = _currentConstraint;
      // Convert IF guard
      BDD matchConstraint = _boolExprToBdd.visit(ifStmt.getMatchCondition());
      _currentConstraint = _currentConstraint.and(matchConstraint);
      // Process true statements
      ifStmt.getTrueStatements().forEach(this::visit);
      // If fell through, constrain packets with complement of match condition and move on
      _currentConstraint = oldConstraint.diff(matchConstraint);
      return null;
    }

    @Override
    public Void visitReturn(Return returnStmt) {
      new Collector(_currentConstraint).visit(returnStmt.getAction());
      return null;
    }
  }

  /** Converts boolean expressions to BDDs */
  private static final class BoolExprToBdd implements BoolExprVisitor<BDD> {
    private final IpAccessListToBdd _ipAccessListToBdd;

    private BoolExprToBdd(IpAccessListToBdd ipAccessListToBdd) {
      _ipAccessListToBdd = ipAccessListToBdd;
    }

    @Override
    public BDD visitPacketMatchExpr(PacketMatchExpr expr) {
      return _ipAccessListToBdd.toBdd(expr.getExpr());
    }
  }

  /**
   * Updates set of packets associated with a particular action (i.e., {@link #_toDrop} and {@link
   * #_fibLookups})
   */
  private final class Collector implements ActionVisitor<Void> {
    private final BDD _constraint;

    private Collector(BDD constraint) {
      _constraint = constraint;
    }

    @Override
    public Void visitDrop(Drop drop) {
      _toDrop = _toDrop.or(_constraint);
      return null;
    }

    @Override
    public Void visitFibLookup(FibLookup fibLookup) {
      _fibLookups.compute(
          fibLookup,
          (k, oldConstraint) ->
              oldConstraint == null ? _constraint : oldConstraint.or(_constraint));
      return null;
    }
  }
}
