package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * An {@link IpAccessListToBdd} that memoizes its {@link IpAccessListToBdd#convert} method using an
 * {@link IdentityHashMap}.
 */
public final class MemoizedIpAccessListToBdd extends IpAccessListToBdd {
  private Map<AclLine, PermitAndDenyBdds> _lineCache = new IdentityHashMap<>();
  private Map<AclLineMatchExpr, BDD> _exprCache = new IdentityHashMap<>();

  public MemoizedIpAccessListToBdd(
      BDDPacket packet,
      BDDSourceManager mgr,
      Map<String, IpAccessList> aclEnv,
      Map<String, IpSpace> namedIpSpaces) {
    super(packet, mgr, new HeaderSpaceToBDD(packet, namedIpSpaces), aclEnv);
  }

  @Override
  public PermitAndDenyBdds toPermitAndDenyBdds(AclLine line) {
    return _lineCache.computeIfAbsent(line, this::convert);
  }

  @Override
  public BDD toBdd(AclLineMatchExpr expr) {
    return _exprCache.computeIfAbsent(expr, this::convert);
  }

  @VisibleForTesting
  Optional<BDD> getMemoizedBdd(AclLineMatchExpr expr) {
    return Optional.ofNullable(_exprCache.get(expr));
  }
}
