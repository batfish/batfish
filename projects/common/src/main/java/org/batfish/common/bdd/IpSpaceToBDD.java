package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.visitors.IpSpaceCanContainReferences.ipSpaceCanContainReferences;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Visitor that converts an {@link IpSpace} to a {@link BDD}. Its constructor takes a {@link
 * BDDInteger} that should will be constrained to be in the space.
 *
 * <p>For all implementations, it must be the case that all returned {@link BDD} objects are owned
 * by the caller and may be freed.
 */
public final class IpSpaceToBDD implements GenericIpSpaceVisitor<BDD> {

  private final BDDInteger _bddInteger;

  private final BDDOps _bddOps;

  private final BDDFactory _factory;

  private final Map<String, Supplier<BDD>> _namedIpSpaceBDDs;

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
          .concurrencyLevel(1) // visit is not threadsafe, don't allocate multiple locks
          .removalListener(removal -> ((BDD) removal.getValue()).free())
          .build(CacheLoader.from(ipSpace -> ipSpace.accept(this)));

  private final @Nullable IpSpaceToBDD _nonRefIpSpaceToBDD;

  /**
   * Create a {@link IpSpaceToBDD} instance for {@link IpSpace IP spaces} that may contain
   * references to named IP spaces in the input map. Leaf IP Spaces (i.e. those guaranteed not to
   * contain references) are converted using the input {@code nonRefIpSpaceToBDD}.
   */
  public IpSpaceToBDD(IpSpaceToBDD nonRefIpSpaceToBDD, Map<String, IpSpace> namedIpSpaces) {
    _bddInteger = nonRefIpSpaceToBDD._bddInteger;
    _factory = _bddInteger.getFactory();
    _bddOps = new BDDOps(_factory);
    _namedIpSpaceBDDs =
        toImmutableMap(
            namedIpSpaces,
            Entry::getKey,
            entry -> Suppliers.memoize(() -> visit(entry.getValue())));
    _nonRefIpSpaceToBDD = nonRefIpSpaceToBDD;
  }

  /**
   * Create an {@link IpSpaceToBDD} instance for {@link IpSpace IP spaces} that do not contain
   * references.
   */
  public IpSpaceToBDD(BDDInteger var) {
    _bddInteger = var;
    _factory = var.getFactory();
    _bddOps = new BDDOps(_factory);
    _namedIpSpaceBDDs = ImmutableMap.of();
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

  public BDDInteger getBDDInteger() {
    return _bddInteger;
  }

  public BDD toBDD(Ip ip) {
    return visit(ip.toIpSpace());
  }

  public BDD toBDD(IpWildcard ipWildcard) {
    return visit(ipWildcard.toIpSpace());
  }

  public BDD toBDD(Prefix prefix) {
    return visit(prefix.toIpSpace());
  }

  @Override
  public BDD visitAclIpSpace(AclIpSpace aclIpSpace) {
    int size = aclIpSpace.getLines().size();
    List<BDD> lineBdds = new ArrayList<>(size);
    List<LineAction> lineActions = new ArrayList<>(size);
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      lineActions.add(line.getAction());
      lineBdds.add(visit(line.getIpSpace()));
    }
    return _bddOps.bddAclLines(lineBdds, lineActions);
  }

  @Override
  public BDD visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return _factory.zero();
  }

  @Override
  public BDD visitIpIpSpace(IpIpSpace ipIpSpace) {
    return _bddInteger.toBDD(ipIpSpace.getIp());
  }

  @Override
  public BDD visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return _bddInteger.toBDD(ipWildcardIpSpace.getIpWildcard());
  }

  @Override
  public BDD visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String name = ipSpaceReference.getName();
    checkArgument(_namedIpSpaceBDDs.containsKey(name), "Undefined IpSpace reference: %s", name);
    return _namedIpSpaceBDDs.get(name).get().id();
  }

  @Override
  public BDD visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    if (ipWildcardSetIpSpace.getWhitelist().isEmpty()) {
      return _factory.zero();
    }
    BDD whitelist = _bddOps.mapAndOrAll(ipWildcardSetIpSpace.getWhitelist(), this::toBDD);
    if (ipWildcardSetIpSpace.getBlacklist().isEmpty()) {
      // short-circuit
      return whitelist;
    }
    return whitelist.diffWith(
        _bddOps.mapAndOrAll(ipWildcardSetIpSpace.getBlacklist(), this::toBDD));
  }

  @Override
  public BDD visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return _bddInteger.toBDD(prefixIpSpace.getPrefix());
  }

  @Override
  public BDD visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return _factory.one();
  }

  /** Test method only, does not transfer ownership of returned BDD. */
  @VisibleForTesting
  Optional<BDD> getMemoizedBddForTesting(IpSpace ipSpace) {
    return Optional.ofNullable(_cache.getIfPresent(ipSpace));
  }
}
