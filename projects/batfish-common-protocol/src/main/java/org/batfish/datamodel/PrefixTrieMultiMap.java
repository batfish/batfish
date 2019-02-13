package org.batfish.datamodel;

import static org.batfish.datamodel.Prefix.longestCommonPrefix;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
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
import java.util.function.Consumer;
import java.util.stream.Stream;
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

  private static final long serialVersionUID = 1L;

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

  private static final class Node<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull private final Prefix _prefix;
    @Nonnull private final Set<T> _elements;

    @Nullable private Node<T> _left;
    @Nullable private Node<T> _right;

    Node(Prefix prefix) {
      _prefix = prefix;
      _elements = new HashSet<>();
    }

    Node(Prefix prefix, T elem) {
      this(prefix);
      _elements.add(elem);
    }

    Node(Prefix prefix, Collection<T> elements) {
      this(prefix);
      _elements.addAll(elements);
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
      return Objects.equals(_prefix, that._prefix)
          && Objects.equals(_elements, that._elements)
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

    /** Returns the list of non-null children for this node */
    @Nonnull
    List<Node<T>> getChildren() {
      return Stream.of(_left, _right)
          .filter(Objects::nonNull)
          .collect(ImmutableList.toImmutableList());
    }

    @Override
    public int hashCode() {
      return Objects.hash(_prefix, _elements, _left, _right);
    }

    /** Returns the node with the longest prefix match for a given prefix. */
    @Nonnull
    Node<T> findLongestPrefixMatchNode(Prefix prefix) {
      assert this._prefix.containsPrefix(prefix);

      Node<T> node = this;
      long prefixAsLong = prefix.getStartIp().asLong();
      while (true) {
        if (node._prefix.equals(prefix)) {
          // found an exact match
          return node;
        }

        // Choose which child might have a longer match
        Node<T> child =
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
    traverseNodes(node -> consumer.accept(node._prefix, ImmutableSet.copyOf(node._elements)));
  }

  private void traverseNodes(Consumer<Node<T>> consumer) {
    if (_root == null) {
      return;
    }
    Traverser.<Node<T>>forTree(Node::getChildren).depthFirstPostOrder(_root).forEach(consumer);
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
    return Objects.hash(_root);
  }

  private @Nullable Node<T> longestMatchNode(Prefix p) {
    return _root == null || !_root._prefix.containsPrefix(p)
        ? null
        : _root.findLongestPrefixMatchNode(p);
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
    Node<T> node = longestMatchNode(Prefix.create(address, maxPrefixLength));
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
    return node._elements.addAll(elements);
  }

  /**
   * Remove a key-value pair from the multimap.
   *
   * @return whether the multimap was modified.
   */
  public boolean remove(Prefix p, T e) {
    Node<T> node = exactMatchNode(p);
    return node != null && node._elements.remove(e);
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
    Set<T> elems = node._elements;
    if (elems.size() == 1 && elems.contains(e)) {
      return false;
    }
    elems.clear();
    elems.add(e);
    return true;
  }
}
