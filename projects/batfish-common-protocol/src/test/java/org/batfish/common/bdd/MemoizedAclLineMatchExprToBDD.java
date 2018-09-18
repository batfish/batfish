package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * An {@link AclLineMatchExprToBDD} that memoizes its {@link AclLineMatchExprToBDD#visit} method
 * using an {@link IdentityHashMap}.
 */
public final class MemoizedAclLineMatchExprToBDD extends AclLineMatchExprToBDD {
  private Map<AclLineMatchExpr, BDD> _cache = new IdentityHashMap<>();

  public MemoizedAclLineMatchExprToBDD(
      BDDFactory factory,
      BDDPacket packet,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> namedIpSpaces) {
    super(
        factory,
        packet,
        aclEnv,
        BDDSourceManager.forInterfaces(packet, ImmutableSet.of()),
        new HeaderSpaceToBDD(
            packet,
            namedIpSpaces,
            new MemoizedIpSpaceToBDD(factory, packet.getDstIp(), namedIpSpaces),
            new MemoizedIpSpaceToBDD(factory, packet.getSrcIp(), namedIpSpaces)));
  }

  public MemoizedAclLineMatchExprToBDD(
      BDDFactory factory,
      BDDPacket packet,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> namedIpSpaces,
      @Nonnull BDDSourceManager bddSrcManager) {
    super(
        factory,
        packet,
        aclEnv,
        bddSrcManager,
        new HeaderSpaceToBDD(
            packet,
            namedIpSpaces,
            new MemoizedIpSpaceToBDD(factory, packet.getDstIp(), namedIpSpaces),
            new MemoizedIpSpaceToBDD(factory, packet.getSrcIp(), namedIpSpaces)));
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
