package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDInteger.makeFromIndex;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;

/** A collection of attributes describing a {@link Prefix}, represented using {@link BDD}s */
@ParametersAreNonnullByDefault
public final class BDDPrefix {

  private static final int IP_LENGTH = 32;

  /*
   * The ratio of node table size to node cache size to preserve when resizing. The default
   * value is 0, which means never resize the cache.
   */
  private static final int JFACTORY_CACHE_RATIO = 64;

  /*
   * Initial size of the BDD factory node cache. Automatically resized when the node table is,
   * to preserve the cache ratio.
   */
  private static final int JFACTORY_INITIAL_NODE_CACHE_SIZE = 1000;

  /*
   * Initial size of the BDD factory node table. Automatically resized as needed. Increasing this
   * will reduce time spent garbage collecting for large computations, but will waste memory for
   * smaller ones.
   */
  private static final int JFACTORY_INITIAL_NODE_TABLE_SIZE = 10000;
  private static final int PREFIX_LENGTH_LENGTH = 6;

  private final @Nonnull Map<Integer, String> _bitNames;
  private final @Nonnull BDDFactory _factory;
  private final @Nonnull BDDInteger _ip;
  private int _nextFreeBDDVarIdx = 0;
  private final @Nonnull BDDInteger _prefixLength;

  public BDDPrefix() {
    _factory = JFactory.init(JFACTORY_INITIAL_NODE_TABLE_SIZE, JFACTORY_INITIAL_NODE_CACHE_SIZE);
    _factory.enableReorder();
    _factory.setCacheRatio(JFACTORY_CACHE_RATIO);
    // Do not impose a maximum node table increase
    _factory.setMaxIncrease(0);

    // Make sure we have the right number of variables
    int numVars = _factory.varNum();
    int numNeeded = IP_LENGTH + PREFIX_LENGTH_LENGTH;
    if (numVars < numNeeded) {
      _factory.setVarNum(numNeeded);
    }
    _bitNames = new HashMap<>();
    _ip = allocateBDDInteger("ip", IP_LENGTH, false);
    _prefixLength = allocateBDDInteger("prefixLength", PREFIX_LENGTH_LENGTH, false);
  }

  /*
   * Helper function that builds a map from BDD variable index
   * to some more meaningful name. Helpful for debugging.
   */
  private void addBitNames(String s, int length, int index, boolean reverse) {
    for (int i = index; i < index + length; i++) {
      if (reverse) {
        _bitNames.put(i, s + (length - 1 - (i - index)));
      } else {
        _bitNames.put(i, s + (i - index + 1));
      }
    }
  }

  /**
   * Allocate a new single-bit {@link BDD} variable.
   *
   * @param name Used for debugging.
   * @return A {@link BDD} representing the sentence "this variable is true" for the new variable.
   */
  @VisibleForTesting
  BDD allocateBDDBit(String name) {
    if (_factory.varNum() < _nextFreeBDDVarIdx + 1) {
      _factory.setVarNum(_nextFreeBDDVarIdx + 1);
    }
    _bitNames.put(_nextFreeBDDVarIdx, name);
    BDD bdd = _factory.ithVar(_nextFreeBDDVarIdx);
    _nextFreeBDDVarIdx++;
    return bdd;
  }

  /**
   * Allocate a new {@link BDDInteger} variable.
   *
   * @param name Used for debugging.
   * @param bits The number of bits to allocate.
   * @param reverse If true, reverse the BDD order of the bits.
   * @return The new variable.
   */
  @VisibleForTesting
  BDDInteger allocateBDDInteger(String name, int bits, boolean reverse) {
    if (_factory.varNum() < _nextFreeBDDVarIdx + bits) {
      _factory.setVarNum(_nextFreeBDDVarIdx + bits);
    }
    BDDInteger var = makeFromIndex(_factory, bits, _nextFreeBDDVarIdx, reverse);
    addBitNames(name, bits, _nextFreeBDDVarIdx, false);
    _nextFreeBDDVarIdx += bits;
    return var;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDPrefix)) {
      return false;
    }
    BDDPrefix other = (BDDPrefix) o;

    return _ip.equals(other._ip) && _prefixLength.equals(other._prefixLength);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _prefixLength);
  }

  /**
   * Check if the first {@code length} bits match the BDDInteger representing {@code prefix}'s
   * address.
   */
  public @Nonnull BDD firstBitsEqual(BDD[] bits, Prefix prefix, int length) {
    long address = prefix.getStartIp().asLong();
    BDD result = _factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(address, i);
      if (res) {
        result = result.and(bits[i]);
      } else {
        result = result.and(bits[i].not());
      }
    }
    return result;
  }

  /** @return The {@link BDDFactory} used by this packet. */
  public BDDFactory getFactory() {
    return _factory;
  }

  public BDDInteger getIp() {
    return _ip;
  }

  public BDDInteger getPrefixLength() {
    return _prefixLength;
  }

  /** Returns a {@link BDD} reprepsenting a {@link Prefix} in the provided {@code prefixRange}. */
  public @Nonnull BDD inPrefixRange(PrefixRange prefixRange) {
    Prefix p = prefixRange.getPrefix();
    BDD prefixMatch = firstBitsEqual(_ip.getBitvec(), p, p.getPrefixLength());

    SubRange r = prefixRange.getLengthRange();
    int lower = r.getStart();
    int upper = r.getEnd();
    BDD lenMatch =
        lower == upper
            ? _prefixLength.value(lower)
            : _prefixLength.geq(lower).and(_prefixLength.leq(upper));

    return lenMatch.and(prefixMatch);
  }

  /** Returns a {@link BDD} reprepsenting a {@link Prefix} equal to the provided {@code prefix}. */
  public @Nonnull BDD isPrefix(Prefix prefix) {
    return firstBitsEqual(_ip.getBitvec(), prefix, prefix.getPrefixLength())
        .and(_prefixLength.value(prefix.getPrefixLength()));
  }

  /**
   * Returns a {@link BDD} reprepsenting a {@link Prefix} permitted by the provided {@code
   * routeFilterList}.
   */
  public @Nonnull BDD permittedByRouteFilterList(RouteFilterList routeFilterList) {
    BDD result = _factory.zero();
    List<RouteFilterLine> lines = ImmutableList.copyOf(routeFilterList.getLines()).reverse();
    for (RouteFilterLine line : lines) {
      if (!line.getIpWildcard().isPrefix()) {
        throw new BatfishException("non-prefix IpWildcards are unsupported");
      }
      PrefixRange range = new PrefixRange(line.getIpWildcard().toPrefix(), line.getLengthRange());
      BDD matches = inPrefixRange(range);
      BDD action = line.getAction() == LineAction.PERMIT ? _factory.one() : _factory.zero();
      result = matches.ite(action, result);
    }
    return result;
  }
}
