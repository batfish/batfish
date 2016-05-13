package org.batfish.protocoldependency;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class RoutingTable {

   private static class ByteTrie {

      private ByteTrieNode _root;

      public ByteTrie() {
         _root = new ByteTrieNode();
      }

      public void addDependentRoute(DependentRoute dependentRoute) {
         Prefix prefix = dependentRoute.getPrefix();
         int prefixLength = prefix.getPrefixLength();
         BitSet bits = getAddressBits(prefix.getAddress());
         _root.addDependentRoute(dependentRoute, bits, prefixLength, 0);
      }

      public Set<DependentRoute> getPrefix(Ip address, BitSet addressBits) {
         return _root.getLongestPrefixMatch(address, addressBits, 0);
      }

   }

   private static class ByteTrieNode {

      private final Set<DependentRoute> _dependentRoutes;

      private ByteTrieNode _left;

      private ByteTrieNode _right;

      public ByteTrieNode() {
         _dependentRoutes = new HashSet<DependentRoute>();
      }

      public void addDependentRoute(DependentRoute dependentRoute, BitSet bits,
            int prefixLength, int depth) {
         if (prefixLength == depth) {
            _dependentRoutes.add(dependentRoute);
         }
         else {
            boolean currentBit = bits.get(depth);
            if (currentBit) {
               if (_right == null) {
                  _right = new ByteTrieNode();
               }
               _right.addDependentRoute(dependentRoute, bits, prefixLength,
                     depth + 1);
            }
            else {
               if (_left == null) {
                  _left = new ByteTrieNode();
               }
               _left.addDependentRoute(dependentRoute, bits, prefixLength,
                     depth + 1);
            }
         }
      }

      private Set<DependentRoute> getLongestPrefixMatch(Ip address, BitSet bits) {
         Set<DependentRoute> longestPrefixMatches = new HashSet<DependentRoute>();
         for (DependentRoute dependentRoute : _dependentRoutes) {
            Prefix prefix = dependentRoute.getPrefix();
            if (prefix.contains(address)) {
               longestPrefixMatches.add(dependentRoute);
            }
         }
         return longestPrefixMatches;
      }

      public Set<DependentRoute> getLongestPrefixMatch(Ip address, BitSet bits,
            int index) {
         Set<DependentRoute> longestPrefixMatches = getLongestPrefixMatch(
               address, bits);
         if (index == NUM_BITS) {
            return longestPrefixMatches;
         }
         boolean currentBit = bits.get(index);
         Set<DependentRoute> longerMatches = null;
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

   }

   private static final int NUM_BITS = 32;

   private static BitSet getAddressBits(Ip address) {
      int addressAsInt = (int) (address.asLong());
      ByteBuffer b = ByteBuffer.allocate(4);

      b.order(ByteOrder.LITTLE_ENDIAN); // optional, the initial order of a byte
                                        // buffer is always BIG_ENDIAN.
      b.putInt(addressAsInt);
      BitSet bitsWithHighestMostSignificant = BitSet.valueOf(b.array());
      BitSet bits = new BitSet(NUM_BITS);
      for (int i = NUM_BITS - 1, j = 0; i >= 0; i--, j++) {
         bits.set(j, bitsWithHighestMostSignificant.get(i));
      }
      return bits;
   }

   private ByteTrie _trie;

   public RoutingTable() {
      _trie = new ByteTrie();
   }

   public void addDependentRoute(DependentRoute dependentRoute) {
      _trie.addDependentRoute(dependentRoute);
   }

   public Set<DependentRoute> longestPrefixMatch(Ip address) {
      BitSet bits = getAddressBits(address);
      return _trie.getPrefix(address, bits);
   }

}
