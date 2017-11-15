package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;

public class PrefixSpace implements Serializable {

  private static class BitTrie implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private BitTrieNode _root;

    public BitTrie() {
      _root = new BitTrieNode();
    }

    public void addPrefixRange(PrefixRange prefixRange) {
      Prefix prefix = prefixRange.getPrefix();
      int prefixLength = prefix.getPrefixLength();
      BitSet bits = getAddressBits(prefix.getAddress());
      int minLength = prefixRange.getLengthRange().getStart();
      int maxLength = prefixRange.getLengthRange().getEnd();
      int matchLength = prefixRange.getPrefix().getPrefixLength();
      for (int currentLength = minLength;
          currentLength < matchLength && currentLength <= maxLength;
          currentLength++) {
        Prefix currentPrefix = new Prefix(prefix.getAddress(), currentLength).getNetworkPrefix();
        PrefixRange currentPrefixRange = PrefixRange.fromPrefix(currentPrefix);
        BitSet currentBits = getAddressBits(currentPrefix.getAddress());
        _root.addPrefixRange(currentPrefixRange, currentBits, currentLength, 0);
      }
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

    /** */
    private static final long serialVersionUID = 1L;

    private BitTrieNode _left;

    private Set<PrefixRange> _prefixRanges;

    private BitTrieNode _right;

    public BitTrieNode() {
      _prefixRanges = new HashSet<>();
    }

    public void addPrefixRange(PrefixRange prefixRange, BitSet bits, int prefixLength, int depth) {
      for (PrefixRange nodeRange : _prefixRanges) {
        if (nodeRange.includesPrefixRange(prefixRange)) {
          return;
        }
      }
      if (prefixLength == depth) {
        _prefixRanges.add(prefixRange);
        prune(prefixRange);
      } else {
        boolean currentBit = bits.get(depth);
        if (currentBit) {
          if (_right == null) {
            _right = new BitTrieNode();
          }
          _right.addPrefixRange(prefixRange, bits, prefixLength, depth + 1);
        } else {
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

    public boolean containsPrefixRange(
        PrefixRange prefixRange, BitSet bits, int prefixLength, int depth) {
      for (PrefixRange nodeRange : _prefixRanges) {
        if (nodeRange.includesPrefixRange(prefixRange)) {
          return true;
        }
      }
      if (prefixLength == depth) {
        return false;
      } else {
        boolean currentBit = bits.get(depth);
        if (currentBit) {
          if (_right == null) {
            return false;
          } else {
            return _right.containsPrefixRange(prefixRange, bits, prefixLength, depth + 1);
          }
        } else {
          if (_left == null) {
            return false;
          } else {
            return _left.containsPrefixRange(prefixRange, bits, prefixLength, depth + 1);
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

  /** */
  private static final long serialVersionUID = 1L;

  /**
   * Converts an IPv4 address into a {@link BitSet} useful for prefix matching. The highest bit of
   * the address is the lowest bit of the bitset: the address 128.0.0.0 when converted to a {@link
   * BitSet} has only the lowest bit set.
   */
  // visible for testing
  static BitSet getAddressBits(Ip address) {
    return BitSet.valueOf(new long[] {Integer.reverse((int) address.asLong()) & 0xffffffffL});
  }

  private transient ConcurrentMap<Prefix, Boolean> _cache;

  private BitTrie _trie;

  public PrefixSpace() {
    _trie = new BitTrie();
    _cache = new ConcurrentHashMap<>();
  }

  @JsonCreator
  public PrefixSpace(Set<PrefixRange> prefixRanges) {
    this();
    for (PrefixRange prefixRange : prefixRanges) {
      _trie.addPrefixRange(prefixRange);
    }
  }

  public void addPrefix(Prefix prefix) {
    addPrefixRange(PrefixRange.fromPrefix(prefix));
  }

  /**
   * Adds the given prefix range into the {@code BitTrie} representing this {@code PrefixSpace}.
   *
   * @param prefixRange Range of prefixes to add
   */
  public void addPrefixRange(PrefixRange prefixRange) {
    _trie.addPrefixRange(prefixRange);
  }

  public void addSpace(PrefixSpace prefixSpace) {
    _trie.addTrieNodeSpace(prefixSpace._trie._root);
  }

  public boolean containsPrefix(Prefix prefix) {
    @Nullable Boolean result = _cache.get(prefix);
    if (result != null) {
      return result;
    } else {
      boolean contained = containsPrefixRange(PrefixRange.fromPrefix(prefix));
      _cache.put(prefix, contained);
      return contained;
    }
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

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    _cache = new ConcurrentHashMap<>();
  }

  @Override
  public String toString() {
    return getPrefixRanges().toString();
  }
}
