package org.batfish.minesweeper.abstraction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class PrefixTrie implements Serializable {

  private class ByteTrie implements Serializable {

    private ByteTrieNode _root;

    public ByteTrie() {
      _root = new ByteTrieNode();
    }

    public void addPrefix(Prefix prefix) {
      int prefixLength = prefix.getPrefixLength();
      long bits = prefix.getStartIp().asLong();
      _root.addPrefix(prefix, bits, prefixLength, 0);
    }

    public boolean containsPathFromPrefix(Prefix prefix) {
      int prefixLength = prefix.getPrefixLength();
      long bits = prefix.getStartIp().asLong();
      return _root.containsPathFromPrefix(bits, prefixLength, 0);
    }

    public Prefix getLongestPrefixMatch(Ip address) {
      long addressBits = address.asLong();
      return _root.getLongestPrefixMatch(address, addressBits, 0);
    }
  }

  private class ByteTrieNode implements Serializable {

    private ByteTrieNode _left;

    private Prefix _prefix;

    private ByteTrieNode _right;

    public void addPrefix(Prefix prefix, long bits, int prefixLength, int depth) {
      if (prefixLength == depth) {
        _prefix = prefix;
        return;
      } else {
        boolean currentBit = Ip.getBitAtPosition(bits, depth);
        if (currentBit) {
          if (_right == null) {
            _right = new ByteTrieNode();
          }
          _right.addPrefix(prefix, bits, prefixLength, depth + 1);
        } else {
          if (_left == null) {
            _left = new ByteTrieNode();
          }
          _left.addPrefix(prefix, bits, prefixLength, depth + 1);
        }
      }
    }

    public boolean containsPathFromPrefix(long bits, int prefixLength, int depth) {
      if (prefixLength == depth) {
        if (depth == 0 && _prefix == null) {
          return false;
        } else {
          return true;
        }
      } else {
        boolean currentBit = Ip.getBitAtPosition(bits, depth);
        if (currentBit) {
          if (_right == null) {
            return false;
          } else {
            return _right.containsPathFromPrefix(bits, prefixLength, depth + 1);
          }
        } else {
          if (_left == null) {
            return false;
          } else {
            return _left.containsPathFromPrefix(bits, prefixLength, depth + 1);
          }
        }
      }
    }

    @Nullable
    private Prefix getLongestPrefixMatch(Ip address) {
      if (_prefix != null && _prefix.containsIp(address)) {
        return _prefix;
      } else {
        return null;
      }
    }

    public Prefix getLongestPrefixMatch(Ip address, long bits, int index) {
      Prefix longestPrefixMatch = getLongestPrefixMatch(address);
      if (index == Prefix.MAX_PREFIX_LENGTH) {
        return longestPrefixMatch;
      }
      boolean currentBit = Ip.getBitAtPosition(bits, index);
      Prefix longerMatch = null;
      if (currentBit) {
        if (_right != null) {
          longerMatch = _right.getLongestPrefixMatch(address, bits, index + 1);
        }
      } else {
        if (_left != null) {
          longerMatch = _left.getLongestPrefixMatch(address, bits, index + 1);
        }
      }
      if (longerMatch == null) {
        return longestPrefixMatch;
      } else {
        return longerMatch;
      }
    }
  }

  private SortedSet<Prefix> _prefixes;

  private ByteTrie _trie;

  public PrefixTrie() {
    _trie = new ByteTrie();
    _prefixes = Collections.emptySortedSet();
  }

  @JsonCreator
  public PrefixTrie(SortedSet<Prefix> prefixes) {
    _prefixes = ImmutableSortedSet.copyOf(prefixes);
    _trie = new ByteTrie();
    for (Prefix prefix : _prefixes) {
      _trie.addPrefix(prefix);
    }
  }

  public boolean containsIp(Ip address) {
    return _trie.getLongestPrefixMatch(address) != null;
  }

  public boolean containsPathFromPrefix(Prefix prefix) {
    return _trie.containsPathFromPrefix(prefix);
  }

  public Prefix getLongestPrefixMatch(Ip address) {
    return _trie.getLongestPrefixMatch(address);
  }

  @JsonValue
  public SortedSet<Prefix> getPrefixes() {
    return _prefixes;
  }
}
