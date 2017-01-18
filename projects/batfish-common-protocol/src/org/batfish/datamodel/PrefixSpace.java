package org.batfish.datamodel;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class PrefixSpace implements Serializable {

   private static class BitTrie implements Serializable {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private BitTrieNode _root;

      public BitTrie() {
         _root = new BitTrieNode();
      }

      public void addPrefixRange(PrefixRange prefixRange) {
         Prefix prefix = prefixRange.getPrefix();
         int prefixLength = prefix.getPrefixLength();
         BitSet bits = getAddressBits(prefix.getAddress());
         _root.addPrefixRange(prefixRange, bits, prefixLength, 0);
      }

      public void addTrieNodeSpace(BitTrieNode node) {
         if (node._left != null) {
            addTrieNodeSpace(node._left);
         }
         if (node._right != null) {
            addTrieNodeSpace(node._right);
         }
         for (PrefixRange prefixRange : node._prefixRanges) {
            addPrefixRange(prefixRange);
         }
      }

      public boolean containsPrefixRange(PrefixRange prefixRange) {
         Prefix prefix = prefixRange.getPrefix();
         int prefixLength = prefix.getPrefixLength();
         BitSet bits = getAddressBits(prefix.getAddress());
         return _root.containsPrefixRange(prefixRange, bits, prefixLength, 0);
      }

      public Set<PrefixRange> getPrefixRanges() {
         Set<PrefixRange> prefixRanges = new HashSet<>();
         _root.collectPrefixRanges(prefixRanges);
         return prefixRanges;
      }

   }

   private static class BitTrieNode implements Serializable {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private BitTrieNode _left;

      private Set<PrefixRange> _prefixRanges;

      private BitTrieNode _right;

      public BitTrieNode() {
         _prefixRanges = new HashSet<>();
      }

      public void addPrefixRange(PrefixRange prefixRange, BitSet bits,
            int prefixLength, int depth) {
         for (PrefixRange nodeRange : _prefixRanges) {
            if (nodeRange.includesPrefixRange(prefixRange)) {
               return;
            }
         }
         if (prefixLength == depth) {
            _prefixRanges.add(prefixRange);
            prune(prefixRange);
         }
         else {
            boolean currentBit = bits.get(depth);
            if (currentBit) {
               if (_right == null) {
                  _right = new BitTrieNode();
               }
               _right.addPrefixRange(prefixRange, bits, prefixLength,
                     depth + 1);
            }
            else {
               if (_left == null) {
                  _left = new BitTrieNode();
               }
               _left.addPrefixRange(prefixRange, bits, prefixLength, depth + 1);
            }
         }
      }

      public void collectPrefixRanges(Set<PrefixRange> prefixRanges) {
         prefixRanges.addAll(_prefixRanges);
         if (_left != null) {
            _left.collectPrefixRanges(prefixRanges);
         }
         if (_right != null) {
            _right.collectPrefixRanges(prefixRanges);
         }
      }

      public boolean containsPrefixRange(PrefixRange prefixRange, BitSet bits,
            int prefixLength, int depth) {
         for (PrefixRange nodeRange : _prefixRanges) {
            if (nodeRange.includesPrefixRange(prefixRange)) {
               return true;
            }
         }
         if (prefixLength == depth) {
            return false;
         }
         else {
            boolean currentBit = bits.get(depth);
            if (currentBit) {
               if (_right == null) {
                  return false;
               }
               else {
                  return _right.containsPrefixRange(prefixRange, bits,
                        prefixLength, depth + 1);
               }
            }
            else {
               if (_left == null) {
                  return false;
               }
               else {
                  return _left.containsPrefixRange(prefixRange, bits,
                        prefixLength, depth + 1);
               }
            }
         }
      }

      private boolean isEmpty() {
         return _left == null && _right == null && _prefixRanges.isEmpty();
      }

      private void prune(PrefixRange prefixRange) {
         if (_left != null) {
            _left.prune(prefixRange);
            if (_left.isEmpty()) {
               _left = null;
            }
         }
         if (_right != null) {
            _right.prune(prefixRange);
            if (_right.isEmpty()) {
               _right = null;
            }
         }
         Set<PrefixRange> oldPrefixRanges = new HashSet<>();
         oldPrefixRanges.addAll(_prefixRanges);
         for (PrefixRange oldPrefixRange : oldPrefixRanges) {
            if (!prefixRange.equals(oldPrefixRange)
                  && prefixRange.includesPrefixRange(oldPrefixRange)) {
               _prefixRanges.remove(oldPrefixRange);
            }
         }
      }

   }

   private static final int NUM_BITS = 32;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static BitSet getAddressBits(Ip address) {
      int addressAsInt = (int) (address.asLong());
      ByteBuffer b = ByteBuffer.allocate(4);
      b.order(ByteOrder.LITTLE_ENDIAN);
      b.putInt(addressAsInt);
      BitSet bitsWithHighestMostSignificant = BitSet.valueOf(b.array());
      BitSet bits = new BitSet(NUM_BITS);
      for (int i = NUM_BITS - 1, j = 0; i >= 0; i--, j++) {
         bits.set(j, bitsWithHighestMostSignificant.get(i));
      }
      return bits;
   }

   private BitTrie _trie;

   public PrefixSpace() {
      _trie = new BitTrie();
   }

   @JsonCreator
   public PrefixSpace(Set<PrefixRange> prefixRanges) {
      _trie = new BitTrie();
      for (PrefixRange prefixRange : prefixRanges) {
         _trie.addPrefixRange(prefixRange);
      }
   }

   public void addPrefix(Prefix prefix) {
      addPrefixRange(PrefixRange.fromPrefix(prefix));
   }

   public void addPrefixRange(PrefixRange prefixRange) {
      _trie.addPrefixRange(prefixRange);
   }

   public void addSpace(PrefixSpace prefixSpace) {
      _trie.addTrieNodeSpace(prefixSpace._trie._root);

   }

   public boolean containsPrefix(Prefix prefix) {
      return containsPrefixRange(PrefixRange.fromPrefix(prefix));
   }

   public boolean containsPrefixRange(PrefixRange prefixRange) {
      return _trie.containsPrefixRange(prefixRange);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      return getPrefixRanges().equals(((PrefixSpace) obj).getPrefixRanges());
   }

   @JsonValue
   public Set<PrefixRange> getPrefixRanges() {
      return _trie.getPrefixRanges();
   }

   @Override
   public int hashCode() {
      return getPrefixRanges().hashCode();
   }

   public PrefixSpace intersection(PrefixSpace intersectSpace) {
      PrefixSpace newSpace = new PrefixSpace();
      Set<PrefixRange> intersectRanges = intersectSpace.getPrefixRanges();
      for (PrefixRange intersectRange : intersectRanges) {
         if (containsPrefixRange(intersectRange)) {
            newSpace.addPrefixRange(intersectRange);
         }
      }
      return newSpace;
   }

   @JsonIgnore
   public boolean isEmpty() {
      return _trie._root.isEmpty();
   }

   public boolean overlaps(PrefixSpace intersectSpace) {
      PrefixSpace intersection = intersection(intersectSpace);
      return !intersection.isEmpty();
   }

   @Override
   public String toString() {
      return getPrefixRanges().toString();
   }

}
