package org.batfish.common.bdd;

import static org.batfish.datamodel.visitors.IpSpaceCanContainReferences.ipSpaceCanContainReferences;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
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

  private final @Nullable IpSpaceToBDD _nonRefIpSpaceToBDD;

  /**
   * Create a {@link MemoizedIpSpaceToBDD} instance for {@link IpSpace IP spaces} that may contain
   * references to named IP spaces in the input map. Leaf IP Spaces (i.e. those guaranteed not to
   * contain references) are converted using the input {@code nonRefIpSpaceToBDD}.
   */
  public MemoizedIpSpaceToBDD(IpSpaceToBDD nonRefIpSpaceToBDD, Map<String, IpSpace> namedIpSpaces) {
    super(nonRefIpSpaceToBDD.getBDDInteger(), namedIpSpaces);
    _nonRefIpSpaceToBDD = nonRefIpSpaceToBDD;
  }

  /**
   * Create a {@Link MemoizedIpSpaceToBDD} instance for {@link IpSpace IP spaces} that do not
   * contain references.
   */
  public MemoizedIpSpaceToBDD(BDDInteger var) {
    super(var, ImmutableMap.of());
    _nonRefIpSpaceToBDD = null;
  }

  @Override
  public BDD visit(IpSpace ipSpace) {
    if (_nonRefIpSpaceToBDD == null || ipSpaceCanContainReferences(ipSpace)) {
      // Use local cache. Make a copy so that the caller owns it.
      return _cache.getUnchecked(ipSpace).id();
    }
    return _nonRefIpSpaceToBDD.visit(ipSpace);
  }

  /** Test method only, does not transfer ownership of returned BDD. */
  @VisibleForTesting
  Optional<BDD> getMemoizedBddForTesting(IpSpace ipSpace) {
    return Optional.ofNullable(_cache.getIfPresent(ipSpace));
  }
}
