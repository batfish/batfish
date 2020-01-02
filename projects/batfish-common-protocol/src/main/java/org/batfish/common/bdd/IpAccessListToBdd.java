package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
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

  /**
   * Converts the given {@link IpAccessList} to a {@link BDD} using the given context.
   *
   * <p>Note that if converting multiple {@link IpAccessList ACLs} with the same context,
   * considerable work can be saved by creating a single {@link IpAccessListToBdd} and reusing it.
   */
  public static BDD toBDD(
      BDDPacket pkt,
      IpAccessList acl,
      Map<String, IpAccessList> aclEnv,
      Map<String, IpSpace> ipSpaceEnv,
      BDDSourceManager bddSrcManager) {
    return new IpAccessListToBddImpl(pkt, bddSrcManager, aclEnv, ipSpaceEnv).toBdd(acl);
  }

  /**
   * Converts the given {@link IpAccessList} to a {@link BDD}.
   *
   * <p>The {@link IpAccessList} must be self-contained, with no references to source {@link
   * org.batfish.datamodel.Interface}, named {@link IpAccessList ACLs} or {@link IpSpace IP spaces}.
   *
   * <p>Note that if converting multiple {@link IpAccessList ACLs} with the same context,
   * considerable work can be saved by creating a single {@link IpAccessListToBdd} and reusing it.
   *
   * @see #toBDD(BDDPacket, IpAccessList, Map, Map, BDDSourceManager)
   */
  public static BDD toBDD(BDDPacket pkt, IpAccessList acl) {
    return toBDD(pkt, acl, ImmutableMap.of(), ImmutableMap.of(), BDDSourceManager.empty(pkt));
  }

  /** Map of ACL name to {@link PermitAndDenyBdds} of the packets explicitly matched by that ACL */
  @Nonnull private final Map<String, Supplier<PermitAndDenyBdds>> _permitAndDenyBdds;

  @Nonnull private final BDDFactory _factory;
  @Nonnull private final BDDPacket _pkt;
  @Nonnull private final BDDOps _bddOps;
  @Nonnull private final BDDSourceManager _bddSrcManager;
  @Nonnull private final HeaderSpaceToBDD _headerSpaceToBDD;
  @Nonnull private final ToBddConverter _toBddConverter;

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
    _permitAndDenyBdds = new HashMap<>();

    aclEnv.forEach(
        (name, acl) ->
            _permitAndDenyBdds.put(
                name,
                Suppliers.memoize(new NonRecursiveSupplier<>(() -> toPermitAndDenyBdds(acl)))));
    _bddOps = new BDDOps(pkt.getFactory());
    _bddSrcManager = bddSrcManager;
    _factory = pkt.getFactory();
    _headerSpaceToBDD = headerSpaceToBDD;
    _pkt = pkt;
    _toBddConverter = new ToBddConverter();
  }

  public abstract BDD toBdd(AclLineMatchExpr expr);

  public abstract PermitAndDenyBdds toPermitAndDenyBdds(AclLine line);

  protected final BDD convert(AclLineMatchExpr expr) {
    return expr.accept(_toBddConverter);
  }

  protected final PermitAndDenyBdds convert(AclLine line) {
    return line.accept(_toBddConverter);
  }

  public final BDDPacket getBDDPacket() {
    return _pkt;
  }

  @Nonnull
  public final HeaderSpaceToBDD getHeaderSpaceToBDD() {
    return _headerSpaceToBDD;
  }

  /**
   * Convert an Access Control List (ACL) to a symbolic boolean expression representing the union of
   * packets that will be permitted by the ACL. The default action in an ACL is to deny all traffic.
   */
  @Nonnull
  public final BDD toBdd(IpAccessList acl) {
    return toPermitAndDenyBdds(acl).getPermitBdd();
  }

  private PermitAndDenyBdds toPermitAndDenyBdds(IpAccessList acl) {
    return _bddOps.bddAclLines(
        acl.getLines().stream()
            .map(this::toPermitAndDenyBdds)
            .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Return the set matched by each line (and no earlier line). The last element is the set
   * unmatched by any line.
   */
  public List<BDD> reachAndMatchLines(IpAccessList acl) {
    ImmutableList.Builder<BDD> bdds = ImmutableList.builder();
    BDD reach = _pkt.getFactory().one();
    for (AclLine line : acl.getLines()) {
      BDD match = convert(line).getMatchBdd();
      bdds.add(reach.and(match));
      reach = reach.diff(match);
    }
    bdds.add(reach);
    return bdds.build();
  }

  /**
   * A visitor that does the actual conversion to BDD. Recursive calls go through the abstract
   * {@link IpAccessListToBdd#toBdd(AclLineMatchExpr)} method, allowing subclasses to intercept.
   */
  private final class ToBddConverter
      implements GenericAclLineMatchExprVisitor<BDD>, GenericAclLineVisitor<PermitAndDenyBdds> {

    /* AclLine visit methods */

    @Override
    public PermitAndDenyBdds visitAclAclLine(AclAclLine aclAclLine) {
      throw new UnsupportedOperationException();
    }

    @Override
    public PermitAndDenyBdds visitExprAclLine(ExprAclLine exprAclLine) {
      BDD matchExprBdd = visit(exprAclLine.getMatchCondition());
      return exprAclLine.getAction() == LineAction.PERMIT
          ? new PermitAndDenyBdds(matchExprBdd, _pkt.getFactory().zero())
          : new PermitAndDenyBdds(_pkt.getFactory().zero(), matchExprBdd);
    }

    /* AclLineMatchExpr visit methods */

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
      return _bddOps.orAll(
          orMatchExpr.getDisjuncts().stream()
              .map(IpAccessListToBdd.this::toBdd)
              .toArray(BDD[]::new));
    }

    @Override
    public final BDD visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      String name = permittedByAcl.getAclName();
      checkArgument(
          _permitAndDenyBdds.containsKey(name), "Undefined PermittedByAcl reference: %s", name);
      try {
        if (permittedByAcl.getDefaultAccept()) {
          // Return the BDD of packets not explicitly rejected by the referenced ACL
          return _permitAndDenyBdds.get(name).get().getDenyBdd().not();
        } else {
          return _permitAndDenyBdds.get(name).get().getPermitBdd();
        }
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
