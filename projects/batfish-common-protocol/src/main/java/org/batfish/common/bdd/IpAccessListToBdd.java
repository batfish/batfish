package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.common.util.NonRecursiveSupplier.NonRecursiveSupplierException;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/**
 * Converts {@link IpAccessList IpAccessLists} or {@link org.batfish.datamodel.acl.AclLineMatchExpr
 * AclLineMatchExprs} to {@link BDD}.
 */
@ParametersAreNonnullByDefault
public abstract class IpAccessListToBdd {

  @Nonnull private final Map<String, Supplier<BDD>> _aclEnv;

  @Nonnull private final BDDFactory _factory;

  @Nonnull private final BDDPacket _pkt;

  @Nonnull private final BDDOps _bddOps;

  @Nonnull private final BDDSourceManager _bddSrcManager;

  @Nonnull private final HeaderSpaceToBDD _headerSpaceToBDD;

  @Nonnull private final Visitor _visitor;

  protected IpAccessListToBdd(
      @Nonnull BDDPacket pkt,
      @Nonnull BDDSourceManager bddSrcManager,
      @Nonnull Map<String, IpAccessList> aclEnv,
      @Nonnull Map<String, IpSpace> ipSpaceEnv) {
    this(pkt, bddSrcManager, new HeaderSpaceToBDD(pkt, ipSpaceEnv), aclEnv);
  }

  protected IpAccessListToBdd(
      @Nonnull BDDPacket pkt,
      @Nonnull BDDSourceManager bddSrcManager,
      @Nonnull HeaderSpaceToBDD headerSpaceToBDD,
      @Nonnull Map<String, IpAccessList> aclEnv) {
    /*
     * Use suppliers to convert each ACL in the environment on demand. Memoize to avoid converting
     * an ACL more than once. ACLs can refer to other ACLs in the environment, but if there is a
     * cyclic reference (direct or indirect), NonRecursiveSupplier will throw an exception (to avoid
     * going into an infinite loop).
     */
    _aclEnv = new HashMap<>();
    aclEnv.forEach(
        (name, acl) ->
            _aclEnv.put(name, Suppliers.memoize(new NonRecursiveSupplier<>(() -> toBdd(acl)))));
    _bddOps = new BDDOps(pkt.getFactory());
    _bddSrcManager = bddSrcManager;
    _factory = pkt.getFactory();
    _headerSpaceToBDD = headerSpaceToBDD;
    _pkt = pkt;
    _visitor = new Visitor();
  }

  public abstract BDD toBdd(AclLineMatchExpr expr);

  protected final BDD visit(AclLineMatchExpr expr) {
    return expr.accept(_visitor);
  }

  public final BDDPacket getBDDPacket() {
    return _pkt;
  }

  @Nonnull
  public final HeaderSpaceToBDD getHeaderSpaceToBDD() {
    return _headerSpaceToBDD;
  }

  /**
   * Convert an Access Control List (ACL) to a symbolic boolean expression. The default action in an
   * ACL is to deny all traffic.
   */
  @Nonnull
  public final BDD toBdd(IpAccessList acl) {
    BDDFactory bddFactory = _pkt.getFactory();
    BDD result = bddFactory.zero();
    LineAction currentAction = LineAction.PERMIT;
    List<BDD> lineBddsWithCurrentAction = new ArrayList<>();
    for (IpAccessListLine line : Lists.reverse(acl.getLines())) {
      LineAction lineAction = line.getAction();
      if (lineAction != currentAction) {
        if (currentAction == LineAction.PERMIT) {
          lineBddsWithCurrentAction.add(result);
          result = _bddOps.multiOr(lineBddsWithCurrentAction);
        } else {
          result = result.diff(_bddOps.multiOr(lineBddsWithCurrentAction));
        }
        currentAction = lineAction;
        lineBddsWithCurrentAction.clear();
      }
      lineBddsWithCurrentAction.add(toBdd(line.getMatchCondition()));
    }

    if (currentAction == LineAction.PERMIT) {
      lineBddsWithCurrentAction.add(result);
      result = _bddOps.multiOr(lineBddsWithCurrentAction);
    } else {
      result = result.diff(_bddOps.multiOr(lineBddsWithCurrentAction));
    }

    return result;
  }

  /**
   * Return the set matched by each line (and no earlier line). The last element is the set
   * unmatched by any line.
   */
  public List<BDD> reachAndMatchLines(IpAccessList acl) {
    ImmutableList.Builder<BDD> bdds = ImmutableList.builder();
    BDD reach = _pkt.getFactory().one();
    for (IpAccessListLine line : acl.getLines()) {
      BDD match = visit(line.getMatchCondition());
      bdds.add(reach.and(match));
      reach = reach.diff(match);
    }
    bdds.add(reach);
    return bdds.build();
  }

  /**
   * A visitor that does the actual conversion to BDD. Recursive calls go through the abstract
   * {@link IpAccessListToBdd#toBdd(AclLineMatchExpr)} method, allowing subclasses to intercerpt.
   */
  private final class Visitor implements GenericAclLineMatchExprVisitor<BDD> {
    @Override
    public final BDD visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return _bddOps.and(
          andMatchExpr.getConjuncts().stream()
              .map(IpAccessListToBdd.this::toBdd)
              .collect(ImmutableList.toImmutableList()));
    }

    @Override
    public final BDD visitFalseExpr(FalseExpr falseExpr) {
      return _factory.zero();
    }

    @Override
    public final BDD visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return _headerSpaceToBDD.toBDD(matchHeaderSpace.getHeaderspace());
    }

    @Override
    public final BDD visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return _bddOps.or(
          matchSrcInterface.getSrcInterfaces().stream()
              .map(_bddSrcManager::getSourceInterfaceBDD)
              .collect(Collectors.toList()));
    }

    @Override
    public final BDD visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return toBdd(notMatchExpr.getOperand()).not();
    }

    @Override
    public final BDD visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return _bddSrcManager.getOriginatingFromDeviceBDD();
    }

    @Override
    public final BDD visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return _bddOps.multiOr(
          orMatchExpr.getDisjuncts().stream()
              .map(IpAccessListToBdd.this::toBdd)
              .toArray(BDD[]::new));
    }

    @Override
    public final BDD visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      String name = permittedByAcl.getAclName();
      checkArgument(_aclEnv.containsKey(name), "Undefined PermittedByAcl reference: %s", name);
      try {
        return _aclEnv.get(name).get();
      } catch (NonRecursiveSupplierException e) {
        throw new BatfishException("Circular PermittedByAcl reference: " + name);
      }
    }

    @Override
    public final BDD visitTrueExpr(TrueExpr trueExpr) {
      return _factory.one();
    }
  }
}
