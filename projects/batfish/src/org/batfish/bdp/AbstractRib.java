package org.batfish.bdp;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import java.util.concurrent.ConcurrentHashMap;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.IRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.TreeMultiSet;

public abstract class AbstractRib<R extends AbstractRoute> implements IRib<R> {

   private class ByteTrie implements Serializable {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private ByteTrieNode _root;

      public ByteTrie() {
         _root = new ByteTrieNode();
      }

      public void addRoute(R route) {
         Prefix prefix = route.getNetwork();
         int prefixLength = prefix.getPrefixLength();
         BitSet bits = getAddressBits(prefix.getAddress());
         _root.addRoute(route, bits, prefixLength, 0);
      }

      public boolean containsPathFromPrefix(Prefix prefix) {
         int prefixLength = prefix.getPrefixLength();
         BitSet bits = getAddressBits(prefix.getAddress());
         return _root.containsPathFromPrefix(bits, prefixLength, 0);
      }

      public Set<R> getPrefix(Ip address, BitSet addressBits) {
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
         BitSet bits = getAddressBits(prefix.getAddress());
         return _root.mergeRoute(route, bits, prefixLength, 0);
      }

   }

   private class ByteTrieNode implements Serializable {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private ByteTrieNode _left;

      private ByteTrieNode _right;

      private final Set<R> _routes;

      public ByteTrieNode() {
         _routes = new HashSet<>();
      }

      public void addRoute(R route, BitSet bits, int prefixLength, int depth) {
         if (prefixLength == depth) {
            _routes.add(route);
         }
         else {
            boolean currentBit = bits.get(depth);
            if (currentBit) {
               if (_right == null) {
                  _right = new ByteTrieNode();
               }
               _right.addRoute(route, bits, prefixLength, depth + 1);
            }
            else {
               if (_left == null) {
                  _left = new ByteTrieNode();
               }
               _left.addRoute(route, bits, prefixLength, depth + 1);
            }
         }
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

      public boolean containsPathFromPrefix(BitSet bits, int prefixLength,
            int depth) {
         if (prefixLength == depth) {
            if (depth == 0 && _routes.isEmpty()) {
               return false;
            }
            else {
               return true;
            }
         }
         else {
            boolean currentBit = bits.get(depth);
            if (currentBit) {
               if (_right == null) {
                  return false;
               }
               else {
                  return _right.containsPathFromPrefix(bits, prefixLength,
                        depth + 1);
               }
            }
            else {
               if (_left == null) {
                  return false;
               }
               else {
                  return _left.containsPathFromPrefix(bits, prefixLength,
                        depth + 1);
               }
            }
         }
      }

      private Set<R> getLongestPrefixMatch(Ip address, BitSet bits) {
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
         Set<R> longestPrefixMatches = getLongestPrefixMatch(address, bits);
         if (index == NUM_BITS) {
            return longestPrefixMatches;
         }
         boolean currentBit = bits.get(index);
         Set<R> longerMatches = null;
         if (currentBit && _right != null) {
            longerMatches = _right.getLongestPrefixMatch(address, bits,
                  index + 1);
         }
         else if (_left != null) {
            longerMatches = _left.getLongestPrefixMatch(address, bits,
                  index + 1);
         }
         if (longerMatches == null || longerMatches.isEmpty()) {
            return longestPrefixMatches;
         }
         else {
            return longerMatches;
         }
      }

      public boolean mergeRoute(R route, BitSet bits, int prefixLength,
            int depth) {
         if (prefixLength == depth) {
            // no routes with this prefix, so just add it
            if (_routes.isEmpty()) {
               _routes.add(route);
               return true;
            }
            else {
               // suitability check
               R rhs = _routes.iterator().next();
               int preferenceComparison = comparePreference(route, rhs);
               if (preferenceComparison < 0) {
                  // less preferable, so it doesn't get added
                  return false;
               }
               else if (preferenceComparison == 0) {
                  // equal preference, so add for multipath routing
                  if (!_routes.contains(route)) {
                     _routes.add(route);
                     return true;
                  }
                  else {
                     // route is already here, so nothing to do
                     return false;
                  }
               }
               else {
                  // better than all pre-existing routes for this prefix, so
                  // replace them with this one
                  _routes.clear();
                  _routes.add(route);
                  return true;
               }
            }
         }
         else {
            boolean currentBit = bits.get(depth);
            if (currentBit) {
               if (_right == null) {
                  _right = new ByteTrieNode();
               }
               return _right.mergeRoute(route, bits, prefixLength, depth + 1);
            }
            else {
               if (_left == null) {
                  _left = new ByteTrieNode();
               }
               return _left.mergeRoute(route, bits, prefixLength, depth + 1);
            }
         }
      }

   }

   private static Map<Ip, BitSet> _addressBitsCache = new ConcurrentHashMap<>();

   private static final int NUM_BITS = 32;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static BitSet getAddressBits(Ip address) {
      BitSet bits = _addressBitsCache.get(address);
      if (bits == null) {

         int addressAsInt = (int) (address.asLong());
         ByteBuffer b = ByteBuffer.allocate(4);

         b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a
                                           // byte
                                           // buffer is always BIG_ENDIAN.
         b.putInt(addressAsInt);
         BitSet bitsWithHighestMostSignificant = BitSet.valueOf(b.array());
         bits = new BitSet(NUM_BITS);
         for (int i = NUM_BITS - 1, j = 0; i >= 0; i--, j++) {
            bits.set(j, bitsWithHighestMostSignificant.get(i));
         }
         _addressBitsCache.put(address, bits);
      }
      return bits;
   }

   private ByteTrie _trie;

   public AbstractRib() {
      _trie = new ByteTrie();
   }

   @Override
   public void addRoute(R route) {
      _trie.addRoute(route);
   }

   @Override
   public boolean containsPathFromPrefix(Prefix prefix) {
      return _trie.containsPathFromPrefix(prefix);
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
         Map<Ip, List<AbstractRoute>> byIp = map.get(prefixCount);
         if (byIp == null) {
            byIp = new TreeMap<>();
            map.put(prefixCount, byIp);
         }
         Ip nextHopIp = route.getNextHopIp();
         List<AbstractRoute> routesByPopularity = byIp.get(nextHopIp);
         if (routesByPopularity == null) {
            routesByPopularity = new ArrayList<>();
            byIp.put(nextHopIp, routesByPopularity);
         }
         routesByPopularity.add(route);
      }
      return map;
   }

   @Override
   public Set<R> longestPrefixMatch(Ip address) {
      BitSet bits = getAddressBits(address);
      return _trie.getPrefix(address, bits);
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
         Set<Ip> nextHopIps = map.get(prefix);
         if (nextHopIps == null) {
            nextHopIps = new TreeSet<>();
            map.put(prefix, nextHopIps);
         }
         nextHopIps.add(nextHopIp);
      }
      return map;
   }

}
