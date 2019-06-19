package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * An {@link IpAccessListToBdd} that memoizes its {@link IpAccessListToBdd#visit} method using an
 * {@link IdentityHashMap}.
 */
public final class MemoizedIpAccessListToBdd extends IpAccessListToBdd {
  private Map<AclLineMatchExpr, BDD> _cache = new IdentityHashMap<>();

  public MemoizedIpAccessListToBdd(
      BDDPacket packet,
      BDDSourceManager mgr,
      Map<String, IpAccessList> aclEnv,
      Map<String, IpSpace> namedIpSpaces) {
    super(packet, mgr, new HeaderSpaceToBDD(packet, namedIpSpaces), aclEnv);
  }

  @Override
  public BDD toBdd(AclLineMatchExpr expr) {
    return _cache.computeIfAbsent(expr, this::visit);
  }

  @VisibleForTesting
  Optional<BDD> getMemoizedBdd(AclLineMatchExpr expr) {
    return Optional.ofNullable(_cache.get(expr));
  }
}
