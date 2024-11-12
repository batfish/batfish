package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toArrayList;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSourceIp;
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
  private static final Logger LOGGER = LogManager.getLogger(IpAccessListToBdd.class);

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

  /** Map of ACL name to {@link PermitAndDenyBdds} of the packets explicitly matched by that ACL */
  private final @Nonnull Map<String, Supplier<PermitAndDenyBdds>> _permitAndDenyBdds;

  private final @Nonnull BDDFactory _factory;
  private final @Nonnull BDDOps _bddOps;
  private final @Nonnull BDDPacket _pkt;
  private final @Nonnull BDDSourceManager _bddSrcManager;
  private final @Nonnull HeaderSpaceToBDD _headerSpaceToBDD;
  private final @Nonnull ToBddConverter _toBddConverter;

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
    _bddSrcManager = bddSrcManager;
    _factory = pkt.getFactory();
    _bddOps = new BDDOps(_factory);
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

  public final @Nonnull HeaderSpaceToBDD getHeaderSpaceToBDD() {
    return _headerSpaceToBDD;
  }

  /**
   * Convert an Access Control List (ACL) to a symbolic boolean expression representing the union of
   * packets that will be permitted by the ACL. The default action in an ACL is to deny all traffic.
   */
  public final @Nonnull BDD toBdd(IpAccessList acl) {
    return getPermitAndDenyBdds(acl.getName()).getPermitBdd();
  }

  private PermitAndDenyBdds computePermitAndDenyBdds(IpAccessList acl) {
    BDD permitBdd = _factory.zero();
    BDD denyBdd = _factory.zero();

    LineAction currentAction = LineAction.PERMIT;
    List<BDD> lineBddsWithCurrentAction = new LinkedList<>();

    BiFunction<BDD, BDD, Void> finalizeBlock =
        (BDD sameActionBdd, BDD otherActionBdd) -> {
          BDD blockBdd = _factory.orAllAndFree(lineBddsWithCurrentAction);
          otherActionBdd.diffEq(blockBdd);
          sameActionBdd.orWith(blockBdd);
          lineBddsWithCurrentAction.clear();
          return null;
        };

    // we need to get the permit and deny BDDs for each line. extractLinePermitAndDenyBdds will do
    // that, and store the result in linePermitAndDenyBdds (index 0=permit, 1=deny).
    // For ExprAclLines (which cannot both permit and deny), one of the two BDDs will be null.
    BDD[] linePermitAndDenyBdds = new BDD[2];
    GenericAclLineVisitor<Void> extractLinePermitAndDenyBdds =
        new GenericAclLineVisitor<Void>() {
          @Override
          public Void visitAclAclLine(AclAclLine aclAclLine) {
            PermitAndDenyBdds permitAndDenyBdds =
                _permitAndDenyBdds.get(aclAclLine.getAclName()).get();
            linePermitAndDenyBdds[0] = permitAndDenyBdds.getPermitBdd();
            linePermitAndDenyBdds[1] = permitAndDenyBdds.getDenyBdd();
            return null;
          }

          @Override
          public Void visitExprAclLine(ExprAclLine exprAclLine) {
            // dont go through PermitAndDenyBdds for ExprAclLine, since it will leak BDDs
            BDD exprBdd = exprAclLine.getMatchCondition().accept(_toBddConverter);
            if (exprAclLine.getAction() == LineAction.PERMIT) {
              linePermitAndDenyBdds[0] = exprBdd;
              linePermitAndDenyBdds[1] = null;
            } else {
              linePermitAndDenyBdds[0] = null;
              linePermitAndDenyBdds[1] = exprBdd;
            }
            return null;
          }
        };

    for (AclLine line : Lists.reverse(acl.getLines())) {
      line.accept(extractLinePermitAndDenyBdds);
      BDD linePermitBdd = linePermitAndDenyBdds[0];
      BDD lineDenyBdd = linePermitAndDenyBdds[1];

      switch (currentAction) {
        case PERMIT:
          if (lineDenyBdd == null) {
            lineBddsWithCurrentAction.add(linePermitBdd);
          } else {
            if (linePermitBdd != null) {
              // line permits and denies (i.e. AclAclLine)
              lineBddsWithCurrentAction.add(linePermitBdd);
            }
            finalizeBlock.apply(permitBdd, denyBdd);

            // start a new deny block
            currentAction = LineAction.DENY;
            lineBddsWithCurrentAction.add(lineDenyBdd);
          }
          break;
        case DENY:
          if (linePermitBdd == null) {
            lineBddsWithCurrentAction.add(lineDenyBdd);
          } else {
            if (lineDenyBdd != null) {
              // line permits and denies (i.e. AclAclLine)
              lineBddsWithCurrentAction.add(lineDenyBdd);
            }
            finalizeBlock.apply(denyBdd, permitBdd);

            // start a new permit block
            currentAction = LineAction.PERMIT;
            lineBddsWithCurrentAction.add(linePermitBdd);
          }
          break;
        default:
          throw new IllegalStateException("Unexpected LineAction " + currentAction);
      }
    }

    // complete the last piece
    switch (currentAction) {
      case PERMIT:
        finalizeBlock.apply(permitBdd, denyBdd);
        break;
      case DENY:
        finalizeBlock.apply(denyBdd, permitBdd);
        break;
      default:
        throw new IllegalStateException("Unexpected LineAction " + currentAction);
    }

    return new PermitAndDenyBdds(permitBdd, denyBdd);
  }

  private PermitAndDenyBdds toPermitAndDenyBdds(IpAccessList acl) {
    long t = System.currentTimeMillis();
    PermitAndDenyBdds result = computePermitAndDenyBdds(acl);
    t = System.currentTimeMillis() - t;
    LOGGER.debug(
        "toPermitAndDenyBdds({}): {}ms  ({} lines, {} values for tracking sources)",
        acl.getName(),
        t,
        acl.getLines().size(),
        _bddSrcManager.getFiniteDomain().getValueBdds().size());
    return result;
  }

  private PermitAndDenyBdds getPermitAndDenyBdds(String aclName) {
    checkArgument(
        _permitAndDenyBdds.containsKey(aclName), "Undefined filter reference: %s", aclName);
    try {
      return _permitAndDenyBdds.get(aclName).get();
    } catch (NonRecursiveSupplierException e) {
      throw new BatfishException("Circular filter reference: " + aclName);
    }
  }

  /**
   * Return the {@link PermitAndDenyBdds} matched by each line (and no earlier line). The last
   * element is a {@link PermitAndDenyBdds} representing default action: permits nothing, denies all
   * unmatched packets.
   */
  public List<PermitAndDenyBdds> reachAndMatchLines(IpAccessList acl) {
    ImmutableList.Builder<PermitAndDenyBdds> bdds = ImmutableList.builder();
    BDD reach = _pkt.getFactory().one();
    for (AclLine line : acl.getLines()) {
      PermitAndDenyBdds match = convert(line);
      bdds.add(match.and(reach));
      reach = reach.diff(match.getMatchBdd());
    }
    bdds.add(new PermitAndDenyBdds(_pkt.getFactory().zero(), reach));
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
      return _permitAndDenyBdds.get(aclAclLine.getAclName()).get();
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
      List<AclLineMatchExpr> exprs = andMatchExpr.getConjuncts();
      if (exprs == null || exprs.isEmpty()) {
        return _factory.one();
      }
      int size = exprs.size();
      if (size == 1) {
        return toBdd(exprs.iterator().next());
      }
      if (size == 2) {
        Iterator<AclLineMatchExpr> iter = exprs.iterator();
        return toBdd(iter.next()).andWith(toBdd(iter.next()));
      }
      return _factory.andAllAndFree(toArrayList(exprs, IpAccessListToBdd.this::toBdd));
    }

    @Override
    public BDD visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      return getPermitAndDenyBdds(deniedByAcl.getAclName()).getPermitBdd().not();
    }

    @Override
    public final BDD visitFalseExpr(FalseExpr falseExpr) {
      return _factory.zero();
    }

    @Override
    public BDD visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
      return _headerSpaceToBDD.getDstIpSpaceToBdd().visit(matchDestinationIp.getIps());
    }

    @Override
    public final BDD visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return _headerSpaceToBDD.toBDD(matchHeaderSpace.getHeaderspace());
    }

    @Override
    public BDD visitMatchSourceIp(MatchSourceIp matchSourceIp) {
      return _headerSpaceToBDD.getSrcIpSpaceToBdd().visit(matchSourceIp.getIps());
    }

    @Override
    public final BDD visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      Set<String> ifaces = matchSrcInterface.getSrcInterfaces();
      int size = ifaces.size();
      if (size == 0) {
        return _factory.zero();
      }
      if (size == 1) {
        return _bddSrcManager.getSourceInterfaceBDD(ifaces.iterator().next()).id();
      }
      if (size == 2) {
        Iterator<String> iter = ifaces.iterator();
        BDD bdd1 = _bddSrcManager.getSourceInterfaceBDD(iter.next());
        BDD bdd2 = _bddSrcManager.getSourceInterfaceBDD(iter.next());
        return bdd1.or(bdd2);
      }
      return _factory.orAll(toArrayList(ifaces, _bddSrcManager::getSourceInterfaceBDD));
    }

    @Override
    public final BDD visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return toBdd(notMatchExpr.getOperand()).notEq();
    }

    @Override
    public final BDD visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return _bddSrcManager.getOriginatingFromDeviceBDD().id();
    }

    @Override
    public final BDD visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return _bddOps.mapAndOrAll(orMatchExpr.getDisjuncts(), IpAccessListToBdd.this::toBdd);
    }

    @Override
    public final BDD visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return getPermitAndDenyBdds(permittedByAcl.getAclName()).getPermitBdd();
    }

    @Override
    public final BDD visitTrueExpr(TrueExpr trueExpr) {
      return _factory.one();
    }
  }
}
