package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Prefix.longestCommonPrefix;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.graph.Traverser;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A generic implementation of a Trie, specialized to keys being prefixes and values to being a set
 * of elements of type {@link DataT}.
 *
 * <p>This trie is a more restrictive version of a ddNF (disjoint difference Normal Form), where the
 * wildcard symbols can appear only after (to-the-right-of) non wildcard symbols in the bit vector.
 * E.g., 101010**, but not 1*001***
 *
 * <p>Internally, this data structure employs path compression which optimizes look-ups, since
 * branching does not have to be done on each bit of the prefix.
 */
@ParametersAreNonnullByDefault
public final class PrefixTrieMultiMap<DataT> implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nonnull private final Prefix _prefix;
  @Nonnull private final Set<DataT> _elements;

  @Nullable private PrefixTrieMultiMap<DataT> _left;
  @Nullable private PrefixTrieMultiMap<DataT> _right;

  public PrefixTrieMultiMap(Prefix prefix) {
    _elements = new HashSet<>();
    _prefix = prefix;
  }

  /** Return the key {@link Prefix} for this node */
  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }

  /** Return an immutable copy of elements at <i>this</i> node */
  @Nonnull
  @VisibleForTesting
  Set<DataT> getElements() {
    return ImmutableSet.copyOf(_elements);
  }

  private void setLeft(@Nullable PrefixTrieMultiMap<DataT> left) {
    assert left == null || legalLeftChildPrefix(_prefix, left._prefix);
    _left = left;
  }

  private void setRight(@Nullable PrefixTrieMultiMap<DataT> right) {
    assert right == null || legalRightChildPrefix(_prefix, right._prefix);
    _right = right;
  }

  /** Returns the list of non-null children for this node */
  @Nonnull
  private List<PrefixTrieMultiMap<DataT>> getChildren() {
    return Stream.of(_left, _right)
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Post-order traversal over the entries. Entries will always contain non-null keys and values.
   * The traversal may not mutate the entries (the values are immutable sets).
   */
  public void traverseEntries(BiConsumer<Prefix, Set<DataT>> consumer) {
    Traverser.<PrefixTrieMultiMap<DataT>>forTree(PrefixTrieMultiMap::getChildren)
        .depthFirstPostOrder(this)
        .forEach(node -> consumer.accept(node._prefix, node.getElements()));
  }

  /**
   * Retrieve an immutable copy of elements for the given prefix (anywhere in the subtree). Returns
   * empty set if the prefix is not in the subtree.
   *
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  @Nonnull
  public Set<DataT> getElements(Prefix p) {
    checkArgument(_prefix.containsPrefix(p), "Prefix %s does not belong to subtree %s", p, this);
    PrefixTrieMultiMap<DataT> node = findNode(p);
    return node == null ? ImmutableSet.of() : node.getElements();
  }

  /**
   * Insert an element into this subtree corresponding to a prefix {@code p}
   *
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  public boolean add(Prefix p, DataT e) {
    checkArgument(_prefix.containsPrefix(p), "Prefix %s does not belong to subtree %s", p, this);
    PrefixTrieMultiMap<DataT> node = findOrCreateNode(p);
    return node._elements.add(e);
  }

  /**
   * Insert a collection of elements into this subtree corresponding to a prefix {@code p}
   *
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  public boolean addAll(Prefix p, Collection<DataT> elements) {
    checkArgument(_prefix.containsPrefix(p), "Prefix %s does not belong to subtree %s", p, this);
    PrefixTrieMultiMap<DataT> node = findOrCreateNode(p);
    return node.addAll(elements);
  }

  /** Add a collection of elements to <i>this</i> node */
  private boolean addAll(Collection<DataT> elements) {
    return _elements.addAll(elements);
  }

  /**
   * Remove an element from the subtree
   *
   * @param p prefix the element is mapped to
   * @param e element to remove
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  public boolean remove(Prefix p, DataT e) {
    checkArgument(_prefix.containsPrefix(p), "Prefix %s does not belong to subtree %s", p, this);
    PrefixTrieMultiMap<DataT> node = findNode(p);
    return node != null && node.remove(e);
  }

  /** Remove an element from <i>this</i> node */
  private boolean remove(DataT e) {
    return _elements.remove(e);
  }

  /**
   * Replace all elements associated with prefix {@code p} with a given element.
   *
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  public boolean replaceAll(Prefix p, DataT e) {
    checkArgument(_prefix.containsPrefix(p), "Prefix %s does not belong to subtree %s", p, this);
    PrefixTrieMultiMap<DataT> node = findNode(p);
    return node != null && node.replaceAll(e);
  }

  /** Replace all elements stored at <i>this</i> node with a given element */
  private boolean replaceAll(DataT e) {
    _elements.clear();
    return _elements.add(e);
  }

  /**
   * Replace all elements associated with prefix {@code p} with a given collection of elements
   *
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  public boolean replaceAll(Prefix p, Collection<DataT> e) {
    checkArgument(_prefix.containsPrefix(p), "Prefix %s does not belong to subtree %s", p, this);
    PrefixTrieMultiMap<DataT> node = findNode(p);
    return node == null || node.replaceAll(e);
  }

  /** Replace all elements stored at <i>this</i> node with the given elements */
  private boolean replaceAll(Collection<DataT> e) {
    _elements.clear();
    return addAll(e);
  }

  /** Find or create a node for a given prefix (must be an exact match) */
  @Nonnull
  private PrefixTrieMultiMap<DataT> findOrCreateNode(Prefix prefix) {
    assert _prefix.containsPrefix(prefix);

    PrefixTrieMultiMap<DataT> node = findLongestPrefixMatchNode(prefix);
    return node._prefix.equals(prefix) ? node : node.createChild(prefix);
  }

  private @Nonnull PrefixTrieMultiMap<DataT> createChild(Prefix prefix) {
    assert _prefix.containsPrefix(prefix);
    boolean currentBit =
        Ip.getBitAtPosition(prefix.getStartIp().asLong(), _prefix.getPrefixLength());

    PrefixTrieMultiMap<DataT> node = new PrefixTrieMultiMap<>(prefix);
    if (currentBit) {
      _right = combine(node, _right);
    } else {
      _left = combine(node, _left);
    }
    return node;
  }

  /**
   * Combine two nodes into a tree -- a newly created node, and an existing node. The existing node
   * cannot be the parent of the new node.
   */
  private @Nonnull PrefixTrieMultiMap<DataT> combine(
      @Nonnull PrefixTrieMultiMap<DataT> newNode, @Nullable PrefixTrieMultiMap<DataT> oldNode) {
    // No existing node, newNode is the tree
    if (oldNode == null) {
      return newNode;
    }

    Prefix newPrefix = newNode._prefix;
    Prefix oldPrefix = oldNode._prefix;

    assert !oldPrefix.containsPrefix(newPrefix);

    /* If the newNode's prefix contains the oldNode's prefix, the existing node is a child of
     * the newNode.
     */
    if (newPrefix.containsPrefix(oldPrefix)) {
      boolean currentBit = Ip.getBitAtPosition(oldPrefix.getStartIp(), newPrefix.getPrefixLength());
      if (currentBit) {
        newNode._right = oldNode;
      } else {
        newNode._left = oldNode;
      }
      return newNode;
    }

    /* Find the least-upper-bound of the two prefixes, i.e. the one for which the newNode branches
     * one way and the oldNode branches the other.
     */
    Prefix lcp = longestCommonPrefix(newPrefix, oldPrefix);
    PrefixTrieMultiMap<DataT> parent = new PrefixTrieMultiMap<>(lcp);

    boolean newNodeRight = Ip.getBitAtPosition(newPrefix.getStartIp(), lcp.getPrefixLength());
    if (newNodeRight) {
      parent.setRight(newNode);
      parent.setLeft(oldNode);
    } else {
      parent.setRight(oldNode);
      parent.setLeft(newNode);
    }
    return parent;
  }

  @VisibleForTesting
  static boolean legalLeftChildPrefix(Prefix parentPrefix, Prefix childPrefix) {
    return parentPrefix.containsPrefix(childPrefix)
        && parentPrefix.getPrefixLength() < childPrefix.getPrefixLength()
        && !Ip.getBitAtPosition(childPrefix.getStartIp(), parentPrefix.getPrefixLength());
  }

  @VisibleForTesting
  static boolean legalRightChildPrefix(Prefix parentPrefix, Prefix childPrefix) {
    return parentPrefix.containsPrefix(childPrefix)
        && parentPrefix.getPrefixLength() < childPrefix.getPrefixLength()
        && Ip.getBitAtPosition(childPrefix.getStartIp(), parentPrefix.getPrefixLength());
  }

  /**
   * Find a node longest prefix match for a given IP address.
   *
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  @Nonnull
  public Set<DataT> longestPrefixMatch(Ip address) {
    checkArgument(
        _prefix.containsIp(address), "Ip %s does not belong to subtree %s", address, this);
    return longestPrefixMatch(address, Prefix.MAX_PREFIX_LENGTH);
  }

  /**
   * Find a node longest prefix match for a given IP address, where the length of the match is
   * bounded by {@code maxPrefixLength}
   *
   * @throws IllegalArgumentException if the input {@link Prefix} is not contained in the {@link
   *     Prefix} of this subtree.
   */
  @Nonnull
  public Set<DataT> longestPrefixMatch(Ip address, int maxPrefixLength) {
    checkArgument(
        _prefix.containsIp(address), "Ip %s does not belong to subtree %s", address, this);
    return findLongestPrefixMatchNode(Prefix.create(address, maxPrefixLength)).getElements();
  }

  /** Collect (recursively) and return all elements in this subtree. */
  @Nonnull
  public Set<DataT> getAllElements() {
    Builder<DataT> b = ImmutableSet.builder();
    collect(b);
    return b.build();
  }

  /** Recursively collect all elements in this trie */
  private void collect(ImmutableCollection.Builder<DataT> collectionBuilder) {
    if (_left != null) {
      _left.collect(collectionBuilder);
    }
    if (_right != null) {
      _right.collect(collectionBuilder);
    }
    collectionBuilder.addAll(_elements);
  }

  @Nullable
  private PrefixTrieMultiMap<DataT> findNode(Prefix p) {
    assert _prefix.containsPrefix(p);
    PrefixTrieMultiMap<DataT> node = findLongestPrefixMatchNode(p);
    return node._prefix.equals(p) ? node : null;
  }

  /** Returns the node with the longest prefix match for a given prefix. */
  @Nonnull
  PrefixTrieMultiMap<DataT> findLongestPrefixMatchNode(Prefix prefix) {
    assert this._prefix.containsPrefix(prefix);

    PrefixTrieMultiMap<DataT> node = this;
    long prefixAsLong = prefix.getStartIp().asLong();
    while (true) {
      if (node._prefix.equals(prefix)) {
        // found an exact match
        return node;
      }

      // Choose which child might have a longer match
      PrefixTrieMultiMap<DataT> child =
          Ip.getBitAtPosition(prefixAsLong, node._prefix.getPrefixLength())
              ? node._right
              : node._left;

      // Check if the child exists and matches
      if (child == null || !child._prefix.containsPrefix(prefix)) {
        return node;
      }

      // Child does have a longer match, keep looking
      node = child;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrefixTrieMultiMap<?> that = (PrefixTrieMultiMap<?>) o;
    return Objects.equals(getPrefix(), that.getPrefix())
        && Objects.equals(_elements, that._elements)
        && Objects.equals(_left, that._left)
        && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPrefix(), _elements, _left, _right);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("prefix", _prefix).toString();
  }
}
