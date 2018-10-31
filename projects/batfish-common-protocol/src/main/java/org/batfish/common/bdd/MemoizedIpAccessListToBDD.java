package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * An {@link IpAccessListToBDD} that memoizes its {@link IpAccessListToBDD#visit} method using an
 * {@link IdentityHashMap}.
 */
public final class MemoizedIpAccessListToBDD extends IpAccessListToBDD {
  private Map<AclLineMatchExpr, BDD> _cache = new IdentityHashMap<>();

  public MemoizedIpAccessListToBDD(
      BDDPacket packet, Map<String, IpAccessList> aclEnv, Map<String, IpSpace> namedIpSpaces) {
    super(
        packet,
        BDDSourceManager.forInterfaces(packet, ImmutableSet.of()),
        new HeaderSpaceToBDD(
            packet,
            namedIpSpaces,
            new MemoizedIpSpaceToBDD(packet.getDstIp(), namedIpSpaces),
            new MemoizedIpSpaceToBDD(packet.getSrcIp(), namedIpSpaces)),
        aclEnv);
  }

  @Override
  public BDD visit(AclLineMatchExpr expr) {
    return _cache.computeIfAbsent(expr, super::visit);
  }

  @VisibleForTesting
  Optional<BDD> getMemoizedBdd(AclLineMatchExpr expr) {
    return Optional.ofNullable(_cache.get(expr));
  }
}
