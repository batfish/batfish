package org.batfish.datamodel;

import static org.parboiled.common.Preconditions.checkArgument;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.ImmutableBDDInteger;

public class PrefixCompressor {
  static Prefix leftExtension(Prefix p) {
    checkArgument(p.getPrefixLength() < Prefix.MAX_PREFIX_LENGTH);
    return Prefix.create(p.getStartIp(), p.getPrefixLength() + 1);
  }

  static Prefix rightExtension(Prefix p) {
    checkArgument(p.getPrefixLength() < Prefix.MAX_PREFIX_LENGTH);
    return Prefix.create(p.getEndIp(), p.getPrefixLength() + 1);
  }

  static IpWildcard bitsBetween(Prefix p1, Prefix p2) {
    checkArgument(p1.containsPrefix(p2));
    // everything shorter than p1 is wildcarded
    long p1WildBits = -1L << (Prefix.MAX_PREFIX_LENGTH - p1.getPrefixLength());
    // make sure it's a valid mask (no extra wildcard bits set)
    p1WildBits &= 0xFFFFFFFFL;
    // everything longer than p2 is wildcarded
    long p2WildBits = (1L << (Prefix.MAX_PREFIX_LENGTH - p2.getPrefixLength())) - 1;
    return IpWildcard.ipWithWildcardMask(p2.getStartIp(), p1WildBits | p2WildBits);
  }

  public static PrefixTrieMultiMap<IpWildcard> buildDependentBitsTrie(
      PrefixTrieMultiMap<Prefix> knownPrefixes) {
    PrefixTrieMultiMap<IpWildcard> depsTrie = new PrefixTrieMultiMap<>();

    Prefix rootResult =
        knownPrefixes.fold(
            new PrefixTrieMultiMap.FoldOperator<Prefix, Prefix>() {
              @Nonnull
              @Override
              public Prefix fold(
                  Prefix prefix,
                  Set<Prefix> elems,
                  @Nullable Prefix leftResult,
                  @Nullable Prefix rightResult) {
                int childLen = prefix.getPrefixLength() + 1;
                if (leftResult != null && childLen < leftResult.getPrefixLength()) {
                  Prefix leftChild = leftExtension(prefix);
                  depsTrie.put(leftChild, bitsBetween(leftChild, leftResult));
                }
                if (rightResult != null && childLen < rightResult.getPrefixLength()) {
                  Prefix rightChild = rightExtension(prefix);
                  depsTrie.put(rightChild, bitsBetween(rightChild, rightResult));
                }
                return prefix;
              }
            });

    if (rootResult != null && rootResult.getPrefixLength() > 0) {
      depsTrie.put(Prefix.ZERO, bitsBetween(Prefix.ZERO, rootResult));
    }

    return depsTrie;
  }

  static IpWildcard compress(PrefixTrieMultiMap<IpWildcard> trie, Prefix prefix) {
    Compressor compressor = new Compressor(prefix);
    trie.traverseEntries(
        compressor::eraseDependentBits, (triePrefix, wcs) -> triePrefix.containsPrefix(prefix));
    return compressor.getCompressedPrefix();
  }

  static Ip inflate(PrefixTrieMultiMap<IpWildcard> trie, Ip ip) {
    Inflator inflator = new Inflator(ip);
    trie.traverseEntries(
        inflator::setDependentBits, (triePrefix, wcs) -> triePrefix.containsIp(inflator.getIp()));
    return inflator.getIp();
  }

  static Ip minEquivalentIp(
      PrefixTrieMultiMap<Prefix> trie, Ip ip, ImmutableBDDInteger bddInteger) {
    LongestMatchingKey lpmKey = new LongestMatchingKey(ip);
    trie.traverseEntries(lpmKey::traverse, (prefix, wcs) -> prefix.containsIp(ip));

    Prefix lpm = lpmKey._prefix;
    LongerKeysCollector longerKeysCollector = new LongerKeysCollector(lpm);
    trie.traverseEntries(
        longerKeysCollector::visit,
        (prefix, wcs) -> prefix.containsPrefix(lpm) || lpm.containsPrefix(prefix));

    BDD bdd =
        bddInteger
            .toBDD(lpm)
            .diffWith(
                bddInteger
                    .getFactory()
                    .orAllAndFree(
                        longerKeysCollector._longerKeys.stream()
                            .map(bddInteger::toBDD)
                            .collect(Collectors.toList())));
    return Ip.create(bddInteger.getValueSatisfying(bdd).get());
  }

  private static class LongerKeysCollector {
    private final Prefix _prefix;
    private final List<Prefix> _longerKeys;

    private LongerKeysCollector(Prefix prefix) {
      _prefix = prefix;
      _longerKeys = new ArrayList<>();
    }

    <T> void visit(Prefix prefix, Set<T> wildcards) {
      if (prefix.containsPrefix(_prefix)) {
        return;
      }
      assert _prefix.containsPrefix(prefix);
      assert !wildcards.isEmpty();
      _longerKeys.add(prefix);
    }
  }

  private static class LongestMatchingKey {
    private final Ip _ip;
    private @Nullable Prefix _prefix;

    LongestMatchingKey(Ip ip) {
      _ip = ip;
    }

    <T> void traverse(Prefix prefix, Set<T> wildcards) {
      checkArgument(prefix.containsIp(_ip));
      if (_prefix == null || _prefix.getPrefixLength() < prefix.getPrefixLength()) {
        _prefix = prefix;
      }
    }
  }

  private static class Compressor {
    private final Ip _ip;
    long _wildcardMask;

    Compressor(Prefix prefix) {
      _ip = prefix.getStartIp();
      _wildcardMask = prefix.getPrefixWildcard().asLong();
    }

    public IpWildcard getCompressedPrefix() {
      return IpWildcard.ipWithWildcardMask(_ip, _wildcardMask);
    }

    public void eraseDependentBits(Prefix matchedPrefix, Set<IpWildcard> ipWildcards) {
      IpWildcard depBits = Iterables.getOnlyElement(ipWildcards);
      assert depBits.containsIp(_ip) : "Input does not Ip have the dependent bits";
      _wildcardMask |= depBits.getMask();
    }
  }

  private static class Inflator {
    private Ip _ip;

    Inflator(Ip ip) {
      _ip = ip;
    }

    public void setDependentBits(Prefix matchedPrefix, Set<IpWildcard> ipWildcards) {
      IpWildcard depBits = Iterables.getOnlyElement(ipWildcards);
      assert matchedPrefix.containsIp(_ip);
      _ip = Ip.create(_ip.asLong() | depBits.getIp().asLong());
    }

    public Ip getIp() {
      return _ip;
    }
  }
}
