package org.batfish.datamodel;

import static org.batfish.datamodel.Prefix.longestCommonPrefix;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.graph.Traverser;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A generic implementation of a Trie, specialized to keys being prefixes and values to being a set
 * of elements of type {@link T}.
 *
 * <p>This trie is a more restrictive version of a ddNF (disjoint difference Normal Form), where the
 * wildcard symbols can appear only after (to-the-right-of) non wildcard symbols in the bit vector.
 * E.g., 101010**, but not 1*001***
 *
 * <p>Internally, this data structure employs path compression which optimizes look-ups, since
 * branching does not have to be done on each bit of the prefix.
 */
@ParametersAreNonnullByDefault
public final class PrefixTrieMultiMap<T> implements Serializable {

  /**
   * Combine two nodes into a tree -- a newly created node, and an existing node. The existing node
   * cannot be the parent of the new node.
   */
  static @Nonnull <T> Node<T> combine(@Nonnull Node<T> newNode, @Nullable Node<T> oldNode) {
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
    Node<T> parent = new Node<>(lcp);

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
   * Interface of fold operations. A fold applies the same operation at each node of the trie,
   * bottom-up. The operation's inputs are the return values of the recursive calls on the subtries,
   * plus the prefix and values at that node.
   */
  public interface FoldOperator<T, R> {
    @Nonnull
    R fold(Prefix prefix, Set<T> elems, @Nullable R leftResult, @Nullable R rightResult);
  }

  private static final class Node<T> implements Serializable {

    @Nonnull private final Prefix _prefix;
    @Nonnull private Set<T> _elements;

    @Nullable private Node<T> _left;
    @Nullable private Node<T> _right;

    Node(Prefix prefix) {
      this(prefix, ImmutableSet.of());
    }

    Node(Prefix prefix, Collection<T> elements) {
      _prefix = prefix;
      _elements = ImmutableSet.copyOf(elements);
    }

    private @Nonnull Node<T> createChild(Prefix prefix) {
      assert _prefix.containsPrefix(prefix);
      boolean currentBit =
          Ip.getBitAtPosition(prefix.getStartIp().asLong(), _prefix.getPrefixLength());

      Node<T> node = new Node<>(prefix);
      if (currentBit) {
        _right = combine(node, _right);
      } else {
        _left = combine(node, _left);
      }
      return node;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Node<?> that = (Node<?>) o;
      return _prefix.equals(that._prefix)
          && _elements.equals(that._elements)
          && Objects.equals(_left, that._left)
          && Objects.equals(_right, that._right);
    }

    /** Find or create a node for a given prefix (must be an exact match) */
    @Nonnull
    private Node<T> findOrCreateNode(Prefix prefix) {
      assert _prefix.containsPrefix(prefix);

      Node<T> node = findLongestPrefixMatchNode(prefix);
      return node._prefix.equals(prefix) ? node : node.createChild(prefix);
    }

    @Nonnull
    <R> R fold(FoldOperator<T, R> operator) {
      R leftResult = _left == null ? null : _left.fold(operator);
      R rightResult = _right == null ? null : _right.fold(operator);
      return operator.fold(_prefix, _elements, leftResult, rightResult);
    }

    /** Returns the list of non-null children for this node */
    @Nonnull
    List<Node<T>> getChildren() {
      if (_left == null && _right == null) {
        return ImmutableList.of();
      } else if (_left == null) {
        return ImmutableList.of(_right);
      } else if (_right == null) {
        return ImmutableList.of(_left);
      } else {
        return ImmutableList.of(_left, _right);
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(_prefix, _elements, _left, _right);
    }

    /** Returns the node with the longest prefix match for a given prefix. */
    @Nonnull
    Node<T> findLongestPrefixMatchNode(Prefix prefix) {
      assert _prefix.containsPrefix(prefix);

      Node<T> node = this;
      while (true) {
        // Choose which child might have a longer match
        Node<T> child = node.matchingChild(prefix);
        if (child == null) {
          return node;
        }
        node = child;
      }
    }

    @Nullable
    Node<T> findLongestPrefixMatchNonEmptyNode(Prefix prefix) {
      assert _prefix.containsPrefix(prefix);

      Node<T> longestNonEmpty = null;
      Node<T> node = this;
      while (node != null) {
        if (!node._elements.isEmpty()) {
          longestNonEmpty = node;
        }

        // Choose which child might have a longer match
        node = node.matchingChild(prefix);
      }

      return longestNonEmpty;
    }

    @Nullable
    Node<T> matchingChild(Prefix prefix) {
      if (_prefix.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
        return null;
      }
      Node<T> child =
          Ip.getBitAtPosition(prefix.getStartIp().asLong(), _prefix.getPrefixLength())
              ? _right
              : _left;
      return child == null || !child._prefix.containsPrefix(prefix) ? null : child;
    }

    private void setLeft(@Nullable Node<T> left) {
      assert left == null || legalLeftChildPrefix(_prefix, left._prefix);
      _left = left;
    }

    private void setRight(@Nullable Node<T> right) {
      assert right == null || legalRightChildPrefix(_prefix, right._prefix);
      _right = right;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("prefix", _prefix).toString();
    }

    /**
     * Returns true iff there is a {@link Prefix} key in this subtree included in {@code
     * prefixRange}.
     */
    private boolean intersectsPrefixRange(PrefixRange prefixRange) {
      // Overview:
      // - If this prefix's length is greater than prefixRange's max length, return false.
      // - If this prefix is contained in prefixRange and this node has any elements (making this
      //   prefix a key), return true.
      // - If either of this prefix or prefixRange's match prefix contains the other, then check
      //   this node's children.
      // - Else return false.

      int currentLength = _prefix.getPrefixLength();
      int maxLength = prefixRange.getLengthRange().getEnd();
      if (currentLength > maxLength) {
        return false;
      }

      if (prefixRange.includesPrefixRange(PrefixRange.fromPrefix(_prefix))
          && !_elements.isEmpty()) {
        return true;
      }

      Prefix rangePrefix = prefixRange.getPrefix();
      return (_prefix.containsPrefix(rangePrefix) || rangePrefix.containsPrefix(_prefix))
          && ((_left != null && _left.intersectsPrefixRange(prefixRange))
              || (_right != null && _right.intersectsPrefixRange(prefixRange)));
    }
  }

  private @Nullable Node<T> _root;

  public PrefixTrieMultiMap(Prefix prefix) {
    _root = new Node<T>(prefix);
  }

  public PrefixTrieMultiMap() {
    _root = null;
  }

  /**
   * Post-order traversal over the entries. Entries will always contain non-null keys and values.
   * The traversal may not mutate the entries (the values are immutable sets).
   */
  public void traverseEntries(BiConsumer<Prefix, Set<T>> consumer) {
    // Chose null instead of something like BiPredicates.alwaysTrue() because:
    // - it doesn't exist
    // - could not get custom version to type-check
    traverseEntriesImpl(consumer, null);
  }

  /**
   * Post-order traversal over the entries. Entries will always contain non-null keys and values.
   * The traversal may not mutate the entries (the values are immutable sets).
   *
   * <p>A node will only be visited if {@code visitNode} returns {@code true} for its prefix and
   * elements.
   */
  public void traverseEntries(
      BiConsumer<Prefix, Set<T>> consumer, BiPredicate<Prefix, Set<T>> visitChild) {
    traverseEntriesImpl(consumer, visitChild);
  }

  private void traverseEntriesImpl(
      BiConsumer<Prefix, Set<T>> consumer, @Nullable BiPredicate<Prefix, Set<T>> visitNode) {
    Consumer<Node<T>> nodeConsumer =
        node -> consumer.accept(node._prefix, ImmutableSet.copyOf(node._elements));
    if (visitNode == null) {
      traverseNodes(nodeConsumer);
    } else {
      traverseNodes(nodeConsumer, node -> visitNode.test(node._prefix, node._elements));
    }
  }

  /**
   * Perform a fold over the trie. The fold applies the same operation at each node of the trie,
   * bottom-up. The operation's inputs are the return values of the recursive calls on the subtries,
   * plus the prefix and values at that node.
   */
  public <R> R fold(FoldOperator<T, R> operator) {
    if (_root == null) {
      return null;
    }
    return _root.fold(operator);
  }

  private void traverseNodes(Consumer<Node<T>> consumer) {
    traverseNodes(consumer, Predicates.alwaysTrue());
  }

  private void traverseNodes(Consumer<Node<T>> consumer, Predicate<Node<T>> visitNode) {
    if (_root == null || !visitNode.test(_root)) {
      return;
    }
    Traverser.<Node<T>>forTree(
            node ->
                node.getChildren().stream()
                    .filter(visitNode)
                    .collect(ImmutableList.toImmutableList()))
        .depthFirstPostOrder(_root)
        .forEach(consumer);
  }

  private @Nullable Node<T> exactMatchNode(Prefix p) {
    Node<T> node = longestMatchNode(p);
    return node == null || !node._prefix.equals(p) ? null : node;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PrefixTrieMultiMap<?>)) {
      return false;
    }
    PrefixTrieMultiMap<?> map = (PrefixTrieMultiMap<?>) o;
    return Objects.equals(_root, map._root);
  }

  /**
   * Retrieve an immutable copy of elements for the given prefix (anywhere in the subtree). Returns
   * empty set if the prefix is not in the subtree.
   *
   * @return null if the prefix is not contained in the trie.
   */
  @Nonnull
  public Set<T> get(Prefix p) {
    Node<T> node = exactMatchNode(p);
    return node == null ? ImmutableSet.of() : ImmutableSet.copyOf(node._elements);
  }

  /** @return all elements in the trie. */
  @Nonnull
  public Set<T> getAllElements() {
    Builder<T> b = ImmutableSet.builder();
    traverseNodes(node -> b.addAll(node._elements));
    return b.build();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_root);
  }

  private @Nullable Node<T> longestMatchNode(Prefix p) {
    return _root == null || !_root._prefix.containsPrefix(p)
        ? null
        : _root.findLongestPrefixMatchNode(p);
  }

  private @Nullable Node<T> longestMatchNonEmptyNode(Prefix p) {
    return _root == null || !_root._prefix.containsPrefix(p)
        ? null
        : _root.findLongestPrefixMatchNonEmptyNode(p);
  }

  /** Find the elements associated with the longest matching prefix of a given IP address. */
  @Nonnull
  public Set<T> longestPrefixMatch(Ip address) {
    return longestPrefixMatch(address, Prefix.MAX_PREFIX_LENGTH);
  }

  /**
   * Find the elements associated with the longest matching prefix of a given IP address, up to the
   * given maximum length.
   */
  @Nonnull
  public Set<T> longestPrefixMatch(Ip address, int maxPrefixLength) {
    Node<T> node = longestMatchNonEmptyNode(Prefix.create(address, maxPrefixLength));
    assert node == null || !node._elements.isEmpty();
    return node == null ? ImmutableSet.of() : ImmutableSet.copyOf(node._elements);
  }

  /**
   * Stores a key-value pair in the multimap.
   *
   * @return whether the multimap was modified.
   */
  public boolean put(Prefix p, T e) {
    return putAll(p, ImmutableList.of(e));
  }

  /**
   * Stores multiple key-value pairs for a single key in the multimap.
   *
   * @return whether the multimap was modified.
   */
  public boolean putAll(Prefix p, Collection<T> elements) {
    if (_root == null || !_root._prefix.containsPrefix(p)) {
      _root = combine(new Node<T>(p, elements), _root);
      return true;
    }
    Node<T> node = _root.findOrCreateNode(p);
    if (node._elements.containsAll(elements)) {
      return false;
    }
    if (node._elements.isEmpty()) {
      node._elements = ImmutableSet.copyOf(elements);
    } else {
      node._elements =
          ImmutableSet.<T>builderWithExpectedSize(node._elements.size() + elements.size())
              .addAll(node._elements)
              .addAll(elements)
              .build();
    }
    return true;
  }

  /**
   * Remove a key-value pair from the multimap.
   *
   * @return whether the multimap was modified.
   */
  public boolean remove(Prefix p, T e) {
    Node<T> node = exactMatchNode(p);
    if (node == null || !node._elements.contains(e)) {
      return false;
    }
    if (node._elements.size() == 1) {
      node._elements = ImmutableSet.of();
    } else {
      node._elements =
          node._elements.stream()
              .filter(el -> !el.equals(e))
              .collect(ImmutableSet.toImmutableSet());
    }
    return true;
  }

  /**
   * Replace any elements associated with prefix {@code p} with a given element.
   *
   * @return whether the multimap was modified
   */
  public boolean replaceAll(Prefix p, T e) {
    Node<T> node = _root == null || !_root._prefix.containsPrefix(p) ? null : exactMatchNode(p);
    if (node == null) {
      return put(p, e);
    }
    if (node._elements.size() == 1 && node._elements.contains(e)) {
      return false;
    }
    node._elements = ImmutableSet.of(e);
    return true;
  }

  /** Remove all elements from the multimap. */
  public void clear() {
    _root = null;
  }

  /**
   * Returns {@code true} iff there is any intersection between the prefixes that are keys of this
   * trie and the provided {@code prefixSpace}.
   */
  public boolean intersectsPrefixSpace(PrefixSpace prefixSpace) {
    return prefixSpace.getPrefixRanges().stream().anyMatch(_root::intersectsPrefixRange);
  }
}
