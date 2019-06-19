package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpSpace;

/** An {@link IpSpaceToBDD} that memoizes its {@link IpSpaceToBDD#visit} method. */
public final class MemoizedIpSpaceToBDD extends IpSpaceToBDD {
  private final LoadingCache<IpSpace, BDD> _cache =
      CacheBuilder.newBuilder()
          .maximumSize(1_000_000)
          .concurrencyLevel(1) // The underlying BDD is not threadsafe.
          .build(CacheLoader.from(super::visit));

  public MemoizedIpSpaceToBDD(BDDInteger var, Map<String, IpSpace> namedIpSpaces) {
    super(var, namedIpSpaces);
  }

  @Override
  public BDD visit(IpSpace ipSpace) {
    return _cache.getUnchecked(ipSpace);
  }

  @VisibleForTesting
  Optional<BDD> getMemoizedBdd(IpSpace ipSpace) {
    return Optional.ofNullable(_cache.getIfPresent(ipSpace));
  }
}
