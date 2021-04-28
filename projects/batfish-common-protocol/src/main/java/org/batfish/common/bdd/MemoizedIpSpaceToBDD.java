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
  /**
   * The cache used for memoizing BDD conversions. We use weak keys to make the equality check be
   * based on object identity instead of deep equality. This strategy makes sense because:
   *
   * <ul>
   *   <li>Most IpSpace objects are interned, so they will be == if they are .equals().
   *   <li>Complex IpSpace objects (AclIpSpace, IpWildcardSetIpSpace) that are not interned have
   *       expensive equals methods, especially compared to conversion of mostly-cached inner
   *       objects. So it's much faster to use == and reconvert different equivalent objects.
   * </ul>
   */
  private final LoadingCache<IpSpace, BDD> _cache =
      CacheBuilder.newBuilder()
          .maximumSize(1_000_000)
          .weakKeys() // this makes equality check for keys be identity, not deep.
          .concurrencyLevel(1) // super::visit is not threadsafe, don't allocate multiple locks
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
