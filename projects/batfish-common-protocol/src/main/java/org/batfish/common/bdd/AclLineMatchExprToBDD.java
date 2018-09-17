package org.batfish.common.bdd;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.util.NonRecursiveSupplier.NonRecursiveSupplierException;
import org.batfish.datamodel.IpSpace;
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

/** Visit an {@link AclLineMatchExpr} and convert it to a BDD. */
@ParametersAreNonnullByDefault
public class AclLineMatchExprToBDD implements GenericAclLineMatchExprVisitor<BDD> {

  private final Map<String, Supplier<BDD>> _aclEnv;

  private final BDDOps _bddOps;

  private final BDDFactory _bddFactory;

  private final BDDPacket _bddPacket;

  private final BDDSourceManager _bddSrcManager;

  private final HeaderSpaceToBDD _headerSpaceToBDD;

  public AclLineMatchExprToBDD(
      BDDFactory bddFactory,
      BDDPacket packet,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> namedIpSpaces) {
    this(
        bddFactory,
        packet,
        aclEnv,
        BDDSourceManager.forInterfaces(packet, ImmutableSet.of()),
        new HeaderSpaceToBDD(packet, namedIpSpaces));
  }

  public AclLineMatchExprToBDD(
      BDDFactory bddFactory,
      BDDPacket packet,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> namedIpSpaces,
      BDDSourceManager bddSrcManager) {
    _aclEnv = ImmutableMap.copyOf(aclEnv);
    _bddFactory = bddFactory;
    _bddOps = new BDDOps(bddFactory);
    _bddPacket = packet;
    _bddSrcManager = bddSrcManager;
    _headerSpaceToBDD = new HeaderSpaceToBDD(packet, namedIpSpaces);
  }

  public AclLineMatchExprToBDD(
      BDDFactory bddFactory,
      BDDPacket packet,
      Map<String, Supplier<BDD>> aclEnv,
      BDDSourceManager bddSrcManager,
      HeaderSpaceToBDD headerSpaceToBDD) {
    _aclEnv = ImmutableMap.copyOf(aclEnv);
    _bddFactory = bddFactory;
    _bddOps = new BDDOps(bddFactory);
    _bddPacket = packet;
    _bddSrcManager = bddSrcManager;
    _headerSpaceToBDD = headerSpaceToBDD;
  }

  public BDDPacket getBDDPacket() {
    return _bddPacket;
  }

  @Override
  public BDD visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return _bddOps.and(
        andMatchExpr
            .getConjuncts()
            .stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BDD visitFalseExpr(FalseExpr falseExpr) {
    return _bddFactory.zero();
  }

  @Override
  public BDD visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return _headerSpaceToBDD.toBDD(matchHeaderSpace.getHeaderspace());
  }

  @Override
  public BDD visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return _bddOps.or(
        matchSrcInterface
            .getSrcInterfaces()
            .stream()
            .map(_bddSrcManager::getSourceInterfaceBDD)
            .collect(Collectors.toList()));
  }

  @Override
  public BDD visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return visit(notMatchExpr.getOperand()).not();
  }

  @Override
  public BDD visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return _bddSrcManager.getOriginatingFromDeviceBDD();
  }

  @Override
  public BDD visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return _bddOps.or(
        orMatchExpr
            .getDisjuncts()
            .stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BDD visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    String name = permittedByAcl.getAclName();
    Preconditions.checkArgument(
        _aclEnv.containsKey(name), "Undefined PermittedByAcl reference: %s", name);
    try {
      return _aclEnv.get(name).get();
    } catch (NonRecursiveSupplierException e) {
      throw new BatfishException("Circular PermittedByAcl reference: " + name);
    }
  }

  @Override
  public BDD visitTrueExpr(TrueExpr trueExpr) {
    return _bddFactory.one();
  }
}
