package org.batfish.minesweeper.abstraction;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

/**
 * A specific kind of prefix match used only by the abstraction code Does *not* correspond to any
 * real configuration feature
 *
 * <p>Checks if a given prefix has any overlap with another set of prefixes. To check this, it
 * encodes the set of prefixes as a prefix trie.
 *
 * <p>For example, if the abstraction wants to restrict to prefixes that overlap with 1.2.3.0/24,
 * then it will allow any of the following: 0.0.0.0/0, 0.0.0.0/1, ..., 1.2.3.0/24, 1.2.3.0/25,
 * 1.2.3.0/32, 1.2.3.1/32, ...
 *
 * <p>This check gets broken down into two checks on the prefix trie. Either (1) the prefix is a
 * subsequence of another (i.e., 1.2.3.0/23), or (2) the address is contained in the trie (i.e.,
 * 1.2.3.4).
 */
public final class AbstractionPrefixSet extends PrefixSetExpr {

  private PrefixTrie _prefixTrie;

  @JsonCreator
  private AbstractionPrefixSet() {}

  public AbstractionPrefixSet(PrefixTrie prefixTrie) {
    _prefixTrie = prefixTrie;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof AbstractionPrefixSet)) {
      return false;
    }
    AbstractionPrefixSet other = (AbstractionPrefixSet) obj;
    return Objects.equals(_prefixTrie, other._prefixTrie);
  }

  public PrefixTrie getPrefixTrie() {
    return _prefixTrie;
  }

  public void setPrefixTrie(PrefixTrie prefixTrie) {
    _prefixTrie = prefixTrie;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_prefixTrie == null) ? 0 : _prefixTrie.hashCode());
    return result;
  }

  @Override
  public boolean matches(Prefix prefix, Environment environment) {
    return _prefixTrie.containsPathFromPrefix(prefix)
        || _prefixTrie.containsIp(prefix.getStartIp());
  }
}
