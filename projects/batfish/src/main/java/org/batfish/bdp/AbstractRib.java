package org.batfish.bdp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.IRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.TreeMultiSet;

public abstract class AbstractRib<R extends AbstractRoute> implements IRib<R> {

  private class ByteTrie implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private ByteTrieNode _root;

    public ByteTrie() {
      _root = new ByteTrieNode(Prefix.ZERO);
    }

    public boolean containsRoute(R route) {
      Prefix prefix = route.getNetwork();
      int prefixLength = prefix.getPrefixLength();
      BitSet bits = prefix.getAddress().getAddressBits();
      return _root.containsRoute(route, bits, prefixLength, 0);
    }

    public Set<R> getLongestPrefixMatch(Ip address) {
      BitSet addressBits = address.getAddressBits();
      return _root.getLongestPrefixMatch(address, addressBits, 0);
    }

    public Set<R> getRoutes() {
      Set<R> routes = new LinkedHashSet<>();
      _root.collectRoutes(routes);
      return routes;
    }

    public boolean mergeRoute(R route) {
      Prefix prefix = route.getNetwork();
      int prefixLength = prefix.getPrefixLength();
      BitSet bits = prefix.getAddress().getAddressBits();
      return _root.mergeRoute(route, bits, prefixLength, 0);
    }
  }

  private class ByteTrieNode implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private ByteTrieNode _left;

    private Prefix _prefix;

    private ByteTrieNode _right;

    private final Set<R> _routes;

    public ByteTrieNode(Prefix prefix) {
      _routes = new HashSet<>();
      _prefix = prefix;
    }

    public void collectRoutes(Set<R> routes) {
      if (_left != null) {
        _left.collectRoutes(routes);
      }
      if (_right != null) {
        _right.collectRoutes(routes);
      }
      routes.addAll(_routes);
    }

    public boolean containsRoute(
        R route, BitSet bits, int prefixLength, int firstUnmatchedBitIndex) {
      if (prefixLength == _prefix.getPrefixLength()) {
        return _routes.contains(route);
      } else {
        boolean currentBit = bits.get(firstUnmatchedBitIndex);
        if (currentBit) {
          if (_right == null) {
            return false;
          } else {
            Prefix rightPrefix = _right._prefix;
            int rightPrefixLength = rightPrefix.getPrefixLength();
            Ip rightAddress = rightPrefix.getAddress();
            BitSet rightAddressBits = rightAddress.getAddressBits();
            int nextUnmatchedBit;
            boolean currentAddressBit = false;
            boolean currentRightAddressBit;
            for (nextUnmatchedBit = firstUnmatchedBitIndex + 1;
                nextUnmatchedBit < rightPrefixLength && nextUnmatchedBit < prefixLength;
                nextUnmatchedBit++) {
              currentAddressBit = bits.get(nextUnmatchedBit);
              currentRightAddressBit = rightAddressBits.get(nextUnmatchedBit);
              if (currentRightAddressBit != currentAddressBit) {
                break;
              }
            }
            if (nextUnmatchedBit == rightPrefixLength) {
              return _right.containsRoute(route, bits, prefixLength, nextUnmatchedBit);
            } else {
              return false;
            }
          }
        } else {
          if (_left == null) {
            return false;
          } else {
            Prefix leftPrefix = _left._prefix;
            int leftPrefixLength = leftPrefix.getPrefixLength();
            Ip leftAddress = leftPrefix.getAddress();
            BitSet leftAddressBits = leftAddress.getAddressBits();
            int nextUnmatchedBit;
            boolean currentAddressBit = false;
            boolean currentLeftAddressBit;
            for (nextUnmatchedBit = firstUnmatchedBitIndex + 1;
                nextUnmatchedBit < leftPrefixLength && nextUnmatchedBit < prefixLength;
                nextUnmatchedBit++) {
              currentAddressBit = bits.get(nextUnmatchedBit);
              currentLeftAddressBit = leftAddressBits.get(nextUnmatchedBit);
              if (currentLeftAddressBit != currentAddressBit) {
                break;
              }
            }
            if (nextUnmatchedBit == leftPrefixLength) {
              return _left.containsRoute(route, bits, prefixLength, nextUnmatchedBit);
            } else {
              return false;
            }
          }
        }
      }
    }

    private Set<R> getLongestPrefixMatch(Ip address) {
      Set<R> longestPrefixMatches = new HashSet<>();
      for (R route : _routes) {
        Prefix prefix = route.getNetwork();
        if (prefix.contains(address)) {
          longestPrefixMatches.add(route);
        }
      }
      return longestPrefixMatches;
    }

    public Set<R> getLongestPrefixMatch(Ip address, BitSet bits, int index) {
      Set<R> longestPrefixMatches = getLongestPrefixMatch(address);
      if (index == Prefix.MAX_PREFIX_LENGTH) {
        return longestPrefixMatches;
      }
      boolean currentBit = bits.get(index);
      Set<R> longerMatches = null;
      if (currentBit) {
        if (_right != null) {
          longerMatches =
              _right.getLongestPrefixMatch(address, bits, _right._prefix.getPrefixLength());
        }
      } else {
        if (_left != null) {
          longerMatches =
              _left.getLongestPrefixMatch(address, bits, _left._prefix.getPrefixLength());
        }
      }
      if (longerMatches == null || longerMatches.isEmpty()) {
        return longestPrefixMatches;
      } else {
        return longerMatches;
      }
    }

    public boolean mergeRoute(R route, BitSet bits, int prefixLength, int firstUnmatchedBitIndex) {
      if (prefixLength == _prefix.getPrefixLength()) {
        // no routes with this prefix, so just add it
        if (_routes.isEmpty()) {
          _routes.add(route);
          return true;
        } else {
          // suitability check
          R rhs = _routes.iterator().next();
          int preferenceComparison = comparePreference(route, rhs);
          if (preferenceComparison < 0) {
            // less preferable, so it doesn't get added
            return false;
          } else if (preferenceComparison == 0) {
            // equal preference, so add for multipath routing
            if (!_routes.contains(route)) {
              _routes.add(route);
              return true;
            } else {
              // route is already here, so nothing to do
              return false;
            }
          } else {
            // better than all pre-existing routes for this prefix, so
            // replace them with this one
            _routes.clear();
            _routes.add(route);
            return true;
          }
        }
      } else {
        boolean currentBit = bits.get(firstUnmatchedBitIndex);
        if (currentBit) {
          if (_right == null) {
            _right = new ByteTrieNode(route.getNetwork());
            _right._routes.add(route);
            return true;
          } else {
            Prefix rightPrefix = _right._prefix;
            int rightPrefixLength = rightPrefix.getPrefixLength();
            Ip rightAddress = rightPrefix.getAddress();
            BitSet rightAddressBits = rightAddress.getAddressBits();
            int nextUnmatchedBit;
            boolean currentAddressBit = false;
            boolean currentRightAddressBit;
            for (nextUnmatchedBit = firstUnmatchedBitIndex + 1;
                nextUnmatchedBit < rightPrefixLength && nextUnmatchedBit < prefixLength;
                nextUnmatchedBit++) {
              currentAddressBit = bits.get(nextUnmatchedBit);
              currentRightAddressBit = rightAddressBits.get(nextUnmatchedBit);
              if (currentRightAddressBit != currentAddressBit) {
                break;
              }
            }
            if (nextUnmatchedBit == rightPrefixLength) {
              return _right.mergeRoute(route, bits, prefixLength, nextUnmatchedBit);
            } else if (nextUnmatchedBit == prefixLength) {
              currentRightAddressBit = rightAddressBits.get(nextUnmatchedBit);
              ByteTrieNode oldRight = _right;
              _right = new ByteTrieNode(route.getNetwork());
              _right._routes.add(route);
              if (currentRightAddressBit) {
                _right._right = oldRight;
              } else {
                _right._left = oldRight;
              }
              return true;
            } else {
              ByteTrieNode oldRight = _right;

              Prefix newNetwork =
                  new Prefix(route.getNetwork().getAddress(), nextUnmatchedBit).getNetworkPrefix();
              _right = new ByteTrieNode(newNetwork);
              if (currentAddressBit) {
                _right._left = oldRight;
                _right._right = new ByteTrieNode(route.getNetwork());
                _right._right._routes.add(route);
              } else {
                _right._right = oldRight;
                _right._left = new ByteTrieNode(route.getNetwork());
                _right._left._routes.add(route);
              }
              return true;
            }
          }
        } else {
          if (_left == null) {
            _left = new ByteTrieNode(route.getNetwork());
            _left._routes.add(route);
            return true;
          } else {
            Prefix leftPrefix = _left._prefix;
            int leftPrefixLength = leftPrefix.getPrefixLength();
            Ip leftAddress = leftPrefix.getAddress();
            BitSet leftAddressBits = leftAddress.getAddressBits();
            int nextUnmatchedBit;
            boolean currentAddressBit = false;
            boolean currentLeftAddressBit;
            for (nextUnmatchedBit = firstUnmatchedBitIndex + 1;
                nextUnmatchedBit < leftPrefixLength && nextUnmatchedBit < prefixLength;
                nextUnmatchedBit++) {
              currentAddressBit = bits.get(nextUnmatchedBit);
              currentLeftAddressBit = leftAddressBits.get(nextUnmatchedBit);
              if (currentLeftAddressBit != currentAddressBit) {
                break;
              }
            }
            if (nextUnmatchedBit == leftPrefixLength) {
              return _left.mergeRoute(route, bits, prefixLength, nextUnmatchedBit);
            } else if (nextUnmatchedBit == prefixLength) {
              currentLeftAddressBit = leftAddressBits.get(nextUnmatchedBit);
              ByteTrieNode oldLeft = _left;
              _left = new ByteTrieNode(route.getNetwork());
              _left._routes.add(route);
              if (currentLeftAddressBit) {
                _left._right = oldLeft;
              } else {
                _left._left = oldLeft;
              }
              return true;
            } else {
              ByteTrieNode oldLeft = _left;
              Prefix newPrefix =
                  new Prefix(route.getNetwork().getAddress(), nextUnmatchedBit).getNetworkPrefix();
              _left = new ByteTrieNode(newPrefix);
              if (currentAddressBit) {
                _left._left = oldLeft;
                _left._right = new ByteTrieNode(route.getNetwork());
                _left._right._routes.add(route);
              } else {
                _left._right = oldLeft;
                _left._left = new ByteTrieNode(route.getNetwork());
                _left._left._routes.add(route);
              }
              return true;
            }
          }
        }
      }
    }

    @Override
    public String toString() {
      return _prefix.toString();
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  protected VirtualRouter _owner;

  private ByteTrie _trie;

  public AbstractRib(VirtualRouter owner) {
    _trie = new ByteTrie();
    _owner = owner;
  }

  protected final boolean containsRoute(R route) {
    return _trie.containsRoute(route);
  }

  @Override
  public final MultiSet<Prefix> getPrefixCount() {
    MultiSet<Prefix> prefixCount = new TreeMultiSet<>();
    for (R route : getRoutes()) {
      Prefix prefix = route.getNetwork();
      prefixCount.add(prefix);
    }
    return prefixCount;
  }

  @Override
  public final SortedSet<Prefix> getPrefixes() {
    SortedSet<Prefix> prefixes = new TreeSet<>();
    Set<R> routes = getRoutes();
    for (R route : routes) {
      prefixes.add(route.getNetwork());
    }
    return prefixes;
  }

  @Override
  public Set<R> getRoutes() {
    return _trie.getRoutes();
  }

  @Override
  public final Map<Integer, Map<Ip, List<AbstractRoute>>> getRoutesByPrefixPopularity() {
    Map<Integer, Map<Ip, List<AbstractRoute>>> map = new TreeMap<>();
    MultiSet<Prefix> prefixCountSet = getPrefixCount();
    for (AbstractRoute route : getRoutes()) {
      Prefix prefix = route.getNetwork();
      int prefixCount = prefixCountSet.count(prefix);
      Map<Ip, List<AbstractRoute>> byIp = map.computeIfAbsent(prefixCount, k -> new TreeMap<>());
      Ip nextHopIp = route.getNextHopIp();
      List<AbstractRoute> routesByPopularity =
          byIp.computeIfAbsent(nextHopIp, k -> new ArrayList<>());
      routesByPopularity.add(route);
    }
    return map;
  }

  @Override
  public Set<R> longestPrefixMatch(Ip address) {
    return _trie.getLongestPrefixMatch(address);
  }

  @Override
  public boolean mergeRoute(R route) {
    return _trie.mergeRoute(route);
  }

  @Override
  public final Map<Prefix, Set<Ip>> nextHopIpsByPrefix() {
    Map<Prefix, Set<Ip>> map = new TreeMap<>();
    for (AbstractRoute route : getRoutes()) {
      Prefix prefix = route.getNetwork();
      Ip nextHopIp = route.getNextHopIp();
      Set<Ip> nextHopIps = map.computeIfAbsent(prefix, k -> new TreeSet<>());
      nextHopIps.add(nextHopIp);
    }
    return map;
  }
}
