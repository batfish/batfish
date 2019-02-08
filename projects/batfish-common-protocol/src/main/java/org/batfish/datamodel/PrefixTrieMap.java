package org.batfish.datamodel;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
public final class PrefixTrieMap<DataT> implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nonnull private final Prefix _prefix;
  @Nonnull private final Set<DataT> _elements;

  @Nullable private PrefixTrieMap<DataT> _left;
  @Nullable private PrefixTrieMap<DataT> _right;

  public PrefixTrieMap(Prefix prefix) {
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
  public Set<DataT> getElements() {
    return ImmutableSet.copyOf(_elements);
  }

  /** Return left subtree */
  @Nullable
  public PrefixTrieMap<DataT> getLeft() {
    return _left;
  }

  private void setLeft(@Nullable PrefixTrieMap<DataT> left) {
    _left = left;
  }

  /** Return right subtree */
  @Nullable
  public PrefixTrieMap<DataT> getRight() {
    return _right;
  }

  private void setRight(@Nullable PrefixTrieMap<DataT> right) {
    _right = right;
  }

  /** Returns the list of non-null children for this node */
  @Nonnull
  public List<PrefixTrieMap<DataT>> getChildren() {
    return Stream.of(_left, _right)
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Retrieve an immutable copy of elements for the given prefix (anywhere in the subtree). Returns
   * empty set if the prefix is not in the subtree.
   */
  @Nonnull
  public Set<DataT> getElements(Prefix p) {
    PrefixTrieMap<DataT> node = findNode(p);
    return node == null ? ImmutableSet.of() : ImmutableSet.copyOf(node._elements);
  }

  /** Add an element to <i>this</i> node */
  public boolean add(DataT e) {
    return _elements.add(e);
  }

  /** Add a set of elements to <i>this</i> node */
  public boolean addAll(Collection<DataT> elements) {
    return _elements.addAll(elements);
  }

  /** Remove an element from <i>this</i> node */
  public boolean remove(DataT e) {
    return _elements.remove(e);
  }

  /** Clear any elements stored at <i>this</i> node */
  public void clear() {
    _elements.clear();
  }

  /** Replace all elements stored at <i>this</i> node with a given element */
  public boolean replaceAll(DataT e) {
    _elements.clear();
    return add(e);
  }

  /** Replace all elements stored at <i>this</i> node with the given elements */
  public boolean replaceAll(Collection<DataT> e) {
    _elements.clear();
    return addAll(e);
  }

  /** Find or create a node for a given prefix (must be an exact match) */
  @Nonnull
  public PrefixTrieMap<DataT> findOrCreateNode(Prefix prefix) {
    return findOrCreateNode(prefix, prefix.getStartIp().asLong(), 0);
  }

  /** Find the node for a given prefix (must be an exact match) */
  @Nullable
  public PrefixTrieMap<DataT> findNode(Prefix prefix) {
    return findNode(prefix.getStartIp().asLong(), prefix.getPrefixLength(), 0);
  }

  /** Find a node longest prefix match for a given IP address. */
  @Nullable
  public PrefixTrieMap<DataT> longestPrefixMatch(Ip address) {
    return longestPrefixMatch(address, Prefix.MAX_PREFIX_LENGTH);
  }

  /**
   * Find a node longest prefix match for a given IP address, where the length of the match is
   * bounded by {@code maxPrefixLength}
   */
  @Nullable
  public PrefixTrieMap<DataT> longestPrefixMatch(Ip address, int maxPrefixLength) {
    return findLongestPrefixMatchNode(address, address.asLong(), maxPrefixLength);
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

  @Nonnull
  private PrefixTrieMap<DataT> findOrCreateNode(
      Prefix prefix, long bits, int firstUnmatchedBitIndex) {
    /*
     * We know we've reached the node at which to insert the element because
     * this node's prefix equals our target prefix.
     */
    if (prefix.getPrefixLength() == _prefix.getPrefixLength()) {
      return this;
    }

    /*
     * The prefix match is not exact, do some extra insertion logic.
     * Current bit determines which side of the tree to go down (1 = right, 0 = left)
     */
    boolean currentBit = Ip.getBitAtPosition(bits, firstUnmatchedBitIndex);
    return findHelper(this, prefix, bits, firstUnmatchedBitIndex, currentBit);
  }

  @Nullable
  private PrefixTrieMap<DataT> findNode(long bits, int prefixLength, int firstUnmatchedBitIndex) {
    // If prefix lengths match, this is the node where such element would be stored.
    if (prefixLength == _prefix.getPrefixLength()) {
      return this;
    }

    boolean currentBit = Ip.getBitAtPosition(bits, firstUnmatchedBitIndex);
    /*
     * If prefixes don't match exactly, look at the current bit. That determines whether we look
     * left or right. As long as the child is not null, recurse.
     *
     * Note that:
     * 1) elements are stored in the nodes where lengths of the node prefix and the desired prefix
     *    match exactly; and
     * 2) prefix matches only get more specific (longer) the deeper we go in the tree
     *
     * Therefore, we can fast-forward the firstUnmatchedBitIndex to the prefix length of the
     * child node
     */
    if (currentBit) {
      return (_right != null)
          ? _right.findNode(bits, prefixLength, _right._prefix.getPrefixLength())
          : null;
    } else {
      return (_left != null)
          ? _left.findNode(bits, prefixLength, _left._prefix.getPrefixLength())
          : null;
    }
  }

  /**
   * Returns a node with the longest prefix match for a given IP address.
   *
   * @param address IP address
   * @param bits IP address represented as a set of bits
   * @param maxPrefixLength only return routes with prefix length less than or equal to given value
   * @return a set of routes
   */
  @Nullable
  private PrefixTrieMap<DataT> findLongestPrefixMatchNode(
      Ip address, long bits, int maxPrefixLength) {
    // If the current subtree only contains routes that are too long, no matches.
    int index = _prefix.getPrefixLength();
    if (index > maxPrefixLength) {
      return null;
    }

    // If the current subtree does not contain the destination IP, no matches.
    if (!_prefix.containsIp(address)) {
      return null;
    }

    // If the network of the current node is exactly the desired maximum length, stop here.
    if (index == maxPrefixLength) {
      return this;
    }

    // Examine the bit at the given index
    boolean currentBit = Ip.getBitAtPosition(bits, index);
    // If the current bit is 1, go right recursively
    PrefixTrieMap<DataT> child = currentBit ? _right : _left;
    if (child == null) {
      return this;
    }

    // Represents any potentially longer route matches (than ones stored at this node)
    PrefixTrieMap<DataT> candidateNode =
        child.findLongestPrefixMatchNode(address, bits, maxPrefixLength);

    // If we found no better matches, return the ones from this node
    if (candidateNode == null) {
      return this;
    } else { // otherwise return longer matches
      return candidateNode;
    }
  }

  /**
   * Takes care of adding new nodes to the tree and maintaining correct pointers.
   *
   * @param parent node that we are trying to merge an element into
   * @param prefix the desired prefix the element should be mapped to
   * @param bits the {@code long} representation of the desired prefix
   * @param firstUnmatchedBitIndex the index of the first bit in the desired prefix that we haven't
   *     checked yet
   * @param rightBranch whether we should recurse down the right side of the tree
   * @return the node into which an element can be added
   */
  @Nonnull
  private static <DataT> PrefixTrieMap<DataT> findHelper(
      PrefixTrieMap<DataT> parent,
      Prefix prefix,
      long bits,
      int firstUnmatchedBitIndex,
      boolean rightBranch) {
    PrefixTrieMap<DataT> node;

    // Get our node from one of the tree sides
    if (rightBranch) {
      node = parent._right;
    } else {
      node = parent._left;
    }

    // Node doesn't exist, so create one. By construction, it will be the best match
    // for the given elementToMerge
    if (node == null) {
      node = new PrefixTrieMap<>(prefix);
      // don't forget to assign new node element to parent node
      assignChild(parent, node, rightBranch);
      return node;
    }

    // Node exists, get some helper data out of the current node we are examining
    Prefix nodePrefix = node._prefix;
    int nodePrefixLength = nodePrefix.getPrefixLength();
    Ip nodeAddress = nodePrefix.getStartIp();
    long nodeAddressBits = nodeAddress.asLong();
    int nextUnmatchedBit;
    // Set up two "pointers" as we scan through the bits and the node's prefixes
    boolean currentAddressBit = false;
    boolean currentNodeAddressBit;

    /*
     * We know we matched up to firstUnmatchedBitIndex. Continue going forward in the bits
     * to find a longer match.
     * At the end of this loop nextUnmatchedBit will be the first place where the elementToMerge prefix
     * and this node's prefix diverge.
     * Note that nextUnmatchedBit can be outside of the node's or the elementToMerge's prefix.
     */
    for (nextUnmatchedBit = firstUnmatchedBitIndex + 1;
        nextUnmatchedBit < nodePrefixLength && nextUnmatchedBit < prefix.getPrefixLength();
        nextUnmatchedBit++) {
      currentAddressBit = Ip.getBitAtPosition(bits, nextUnmatchedBit);
      currentNodeAddressBit = Ip.getBitAtPosition(nodeAddressBits, nextUnmatchedBit);
      if (currentNodeAddressBit != currentAddressBit) {
        break;
      }
    }

    /*
     * If the next unmatched bit is the same as node prefix length, we "ran off" the node prefix.
     * Recursively find the appropriate node that's a child of this node.
     */
    if (nextUnmatchedBit == nodePrefixLength) {
      return node.findOrCreateNode(prefix, bits, nextUnmatchedBit);
    }

    /*
     * If we reached the desired prefix length (but have not exhausted the nodes) we need to create a new node
     * above the current node that matches the prefix and re-attach the current node to
     * the newly created node.
     */
    if (nextUnmatchedBit == prefix.getPrefixLength()) {
      currentNodeAddressBit = Ip.getBitAtPosition(nodeAddressBits, nextUnmatchedBit);
      PrefixTrieMap<DataT> oldNode = node;
      node = new PrefixTrieMap<>(prefix);
      // Keep track of pointers
      assignChild(parent, node, rightBranch);
      assignChild(node, oldNode, currentNodeAddressBit);
      return node;
    }

    /*
     * If we are here, there is a bit difference between the node's prefix and the desired prefix,
     * before we reach the end of either prefix. This requires the following:
     * - Compute the max prefix match (up to nextUnmatchedBit)
     * - Create a new node with this new prefix above the current node
     * - Create a new node with the desired prefix and assign the newly created node.
     * - Existing node becomes a sibling of the node with full elementToMerge prefix
     */
    PrefixTrieMap<DataT> oldNode = node;

    // newNetwork has the max prefix match up to nextUnmatchedBit
    Prefix newNetwork = Prefix.create(prefix.getStartIp(), nextUnmatchedBit);
    node = new PrefixTrieMap<>(newNetwork); // this is the node we are inserting in the middle
    PrefixTrieMap<DataT> child = new PrefixTrieMap<>(prefix);

    assignChild(parent, node, rightBranch);
    // child and old node become siblings, children of the newly inserted node
    assignChild(node, child, currentAddressBit);
    assignChild(node, oldNode, !currentAddressBit);
    return child;
  }

  private static <DataT> void assignChild(
      PrefixTrieMap<DataT> parent, PrefixTrieMap<DataT> child, boolean branchRight) {
    if (branchRight) {
      parent.setRight(child);
    } else {
      parent.setLeft(child);
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
    PrefixTrieMap<?> that = (PrefixTrieMap<?>) o;
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
