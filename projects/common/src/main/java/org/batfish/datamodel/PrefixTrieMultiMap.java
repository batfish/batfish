package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
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
 *
 * <p>Nodes are records in an {@code int[]} rather than objects: prefix bits, prefix length, child
 * offsets and flags take three ints, so a lookup step reads one place in one array and never
 * follows a reference until it has found its answer. A node that has been given elements carries a
 * fourth int, a slot into the element and key arrays, so the branching nodes that path compression
 * creates cost twelve bytes and nothing else. A node with a single element stores the element
 * itself rather than a singleton set.
 */
@ParametersAreNonnullByDefault
public final class PrefixTrieMultiMap<T> implements Serializable {

  /**
   * Interface of fold operations. A fold applies the same operation at each node of the trie,
   * bottom-up. The operation's inputs are the return values of the recursive calls on the subtries,
   * plus the prefix and values at that node.
   */
  public interface FoldOperator<T, R> {
    @Nonnull
    R fold(Prefix prefix, Set<T> elems, @Nullable R leftResult, @Nullable R rightResult);
  }

  /**
   * The elements at one prefix, found (or created) once and then read and modified without
   * searching the trie again. Valid until the trie is {@link #clear() cleared}.
   */
  public final class Handle {
    private final int _node;

    private Handle(int node) {
      _node = node;
    }

    /** The elements at this prefix; empty if there are none. */
    public @Nonnull Set<T> get() {
      return elementsAt(_node);
    }

    /** As {@link PrefixTrieMultiMap#put}. */
    public boolean add(T e) {
      return addElement(_node, e);
    }

    /** As {@link PrefixTrieMultiMap#replaceAll}. */
    public boolean replaceAll(T e) {
      return replaceAllAt(_node, e);
    }

    /** As {@link PrefixTrieMultiMap#remove}. */
    public boolean remove(T e) {
      return removeAt(_node, e);
    }
  }

  @VisibleForTesting
  static boolean legalLeftChildPrefix(Prefix parentPrefix, Prefix childPrefix) {
    return parentPrefix.containsPrefix(childPrefix)
        && parentPrefix.getPrefixLength() < childPrefix.getPrefixLength()
        && !childPrefix.getStartIp().getBitAtPosition(parentPrefix.getPrefixLength());
  }

  @VisibleForTesting
  static boolean legalRightChildPrefix(Prefix parentPrefix, Prefix childPrefix) {
    return parentPrefix.containsPrefix(childPrefix)
        && parentPrefix.getPrefixLength() < childPrefix.getPrefixLength()
        && childPrefix.getStartIp().getBitAtPosition(parentPrefix.getPrefixLength());
  }

  /** Offset used for "no node"; also "no slot". */
  private static final int NONE = -1;

  /*
   * A node is a record in _nodes at offset n:
   *
   *   _nodes[n]     prefix bits
   *   _nodes[n + 1] [left child offset + 1: 26][prefix length: 6]
   *   _nodes[n + 2] [right child offset + 1: 26][DEAD][HAS_SLOT][MULTI][HAS_VALUE][unused: 2]
   *   _nodes[n + 3] slot, present only if HAS_SLOT
   *
   * Child offsets are stored plus one so that zero, the value of a fresh word, means NONE. A node
   * that must gain a slot after creation is copied to a new four-int record and the old record is
   * marked DEAD; iteration over records skips it.
   */
  private static final int LOW_BITS = 6;
  private static final int LEN_MASK = (1 << LOW_BITS) - 1;
  private static final int HAS_VALUE = 1;
  private static final int MULTI = 1 << 1;
  private static final int HAS_SLOT = 1 << 2;
  private static final int DEAD = 1 << 3;
  private static final int FLAGS = HAS_VALUE | MULTI | HAS_SLOT | DEAD;

  /** The largest record offset a child field can address. */
  private static final int MAX_OFFSET = (1 << (Integer.SIZE - LOW_BITS)) - 2;

  private static final int BRANCH_RECORD = 3;
  private static final int SLOTTED_RECORD = 4;
  private static final int INITIAL_CAPACITY = 8 * SLOTTED_RECORD;
  private static final int[] EMPTY_NODES = new int[0];
  private static final Object[] EMPTY_VALUES = new Object[0];
  private static final Prefix[] EMPTY_KEYS = new Prefix[0];

  private int[] _nodes;

  /** The number of ints of {@link #_nodes} in use. */
  private int _used;

  private int _root;

  /** Slot s holds the element ({@code T}) or elements ({@link ImmutableSet}) of one node. */
  private Object[] _values;

  /** Slot s holds the {@link Prefix} of the node, sharing the caller's instance. */
  private Prefix[] _keys;

  private int _numSlots;
  private int _numElements;

  public PrefixTrieMultiMap() {
    clear();
  }

  // Bit arithmetic on prefixes given as (bits, length).

  /** A mask selecting the top {@code len} bits, for {@code 0 <= len <= 32}. */
  private static int prefixMask(int len) {
    return (int) (0xFFFFFFFFL << (Prefix.MAX_PREFIX_LENGTH - len));
  }

  /** Whether the prefix (bits, len) contains the prefix (otherBits, otherLen). */
  private static boolean contains(int bits, int len, int otherBits, int otherLen) {
    return len <= otherLen && ((bits ^ otherBits) & prefixMask(len)) == 0;
  }

  /** The bit of {@code bits} at {@code pos}, where 0 is the most significant bit and pos < 32. */
  private static boolean bitAt(int bits, int pos) {
    return ((bits >>> (Prefix.MAX_PREFIX_LENGTH - 1 - pos)) & 1) != 0;
  }

  private static int bitsOf(Prefix p) {
    return (int) p.getStartIp().asLong();
  }

  // Record field accessors. "a" is the second int of a record and "b" the third.

  private static int lenOf(int a) {
    return a & LEN_MASK;
  }

  private static int leftOf(int a) {
    return (a >>> LOW_BITS) - 1;
  }

  private static int rightOf(int b) {
    return (b >>> LOW_BITS) - 1;
  }

  private static boolean valued(int b) {
    return (b & HAS_VALUE) != 0;
  }

  private static boolean multi(int b) {
    return (b & MULTI) != 0;
  }

  private static boolean slotted(int b) {
    return (b & HAS_SLOT) != 0;
  }

  private static int recordSize(int b) {
    return slotted(b) ? SLOTTED_RECORD : BRANCH_RECORD;
  }

  private int bits(int n) {
    return _nodes[n];
  }

  private int len(int n) {
    return lenOf(_nodes[n + 1]);
  }

  private int left(int n) {
    return leftOf(_nodes[n + 1]);
  }

  private int right(int n) {
    return rightOf(_nodes[n + 2]);
  }

  private boolean hasValue(int n) {
    return valued(_nodes[n + 2]);
  }

  /** The slot of node {@code n}, which must have one. */
  private int slotOf(int n) {
    assert slotted(_nodes[n + 2]);
    return _nodes[n + 3];
  }

  private void setLeft(int n, int child) {
    _nodes[n + 1] = (_nodes[n + 1] & LEN_MASK) | ((child + 1) << LOW_BITS);
  }

  private void setRight(int n, int child) {
    _nodes[n + 2] = (_nodes[n + 2] & FLAGS) | ((child + 1) << LOW_BITS);
  }

  private void setValueFlags(int n, int flags) {
    assert (flags & ~(HAS_VALUE | MULTI)) == 0;
    _nodes[n + 2] = (_nodes[n + 2] & ~(HAS_VALUE | MULTI)) | flags;
  }

  /** The number of elements at node {@code n}. */
  private int countAt(int n) {
    int b = _nodes[n + 2];
    if (!valued(b)) {
      return 0;
    }
    return multi(b) ? ((ImmutableSet<?>) _values[_nodes[n + 3]]).size() : 1;
  }

  @SuppressWarnings("unchecked")
  private @Nonnull ImmutableSet<T> elementsAt(int n) {
    int b = _nodes[n + 2];
    if (!valued(b)) {
      return ImmutableSet.of();
    }
    Object v = _values[_nodes[n + 3]];
    return multi(b) ? (ImmutableSet<T>) v : ImmutableSet.of((T) v);
  }

  private int elementsHashAt(int n) {
    // Set.hashCode is the sum of the element hash codes, so a bare element hashes like its set.
    return hasValue(n) ? _values[slotOf(n)].hashCode() : 0;
  }

  private void setSingle(int n, T element) {
    Objects.requireNonNull(element);
    _numElements += 1 - countAt(n);
    _values[slotOf(n)] = element;
    setValueFlags(n, HAS_VALUE);
  }

  private void setElements(int n, ImmutableSet<T> elements) {
    if (elements.isEmpty()) {
      clearElements(n);
    } else if (elements.size() == 1) {
      setSingle(n, elements.iterator().next());
    } else {
      _numElements += elements.size() - countAt(n);
      _values[slotOf(n)] = elements;
      setValueFlags(n, HAS_VALUE | MULTI);
    }
  }

  private void clearElements(int n) {
    if (!hasValue(n)) {
      return;
    }
    _numElements -= countAt(n);
    _values[slotOf(n)] = null;
    setValueFlags(n, 0);
  }

  /**
   * The prefix of node {@code n}. Nodes that have held elements return the caller's {@link Prefix}
   * instance; branching nodes return a fresh, equal instance that bypasses the {@link Prefix}
   * cache.
   */
  private @Nonnull Prefix keyOf(int n) {
    if (slotted(_nodes[n + 2])) {
      Prefix k = _keys[_nodes[n + 3]];
      if (k != null) {
        return k;
      }
    }
    return Prefix.uncached(bits(n), len(n));
  }

  // Node creation.

  private static int grownCapacity(int oldCapacity, int minimum) {
    return Math.max(
        minimum, oldCapacity == 0 ? INITIAL_CAPACITY : oldCapacity + Math.max(1, oldCapacity >> 1));
  }

  private int allocateSlot() {
    if (_numSlots == _values.length) {
      int newCapacity = grownCapacity(_values.length, _numSlots + 1);
      _values = Arrays.copyOf(_values, newCapacity);
      _keys = Arrays.copyOf(_keys, newCapacity);
    }
    return _numSlots++;
  }

  /** Appends a record for the prefix (bits, len) with no children and returns its offset. */
  private int newNode(int bits, int len, boolean withSlot) {
    int size = withSlot ? SLOTTED_RECORD : BRANCH_RECORD;
    if (_used + size > _nodes.length) {
      _nodes = Arrays.copyOf(_nodes, grownCapacity(_nodes.length, _used + size));
    }
    int n = _used;
    checkState(n <= MAX_OFFSET, "PrefixTrieMultiMap cannot hold more than %s ints", MAX_OFFSET);
    _used += size;
    _nodes[n] = bits;
    _nodes[n + 1] = len;
    if (withSlot) {
      _nodes[n + 2] = HAS_SLOT;
      _nodes[n + 3] = allocateSlot();
    } else {
      _nodes[n + 2] = 0;
    }
    return n;
  }

  /** Create a slotted node for {@code p}, remembering the instance as its key. */
  private int newKeyedNode(Prefix p) {
    int n = newNode(bitsOf(p), p.getPrefixLength(), true);
    _keys[_nodes[n + 3]] = p;
    return n;
  }

  /**
   * Gives branching node {@code n} a slot by copying it to a slotted record, re-pointing its parent
   * (or the root) at the copy. Returns the offset of the copy.
   */
  private int relocateWithSlot(int n, int parent, boolean rightSide) {
    assert !slotted(_nodes[n + 2]);
    int copy = newNode(_nodes[n], lenOf(_nodes[n + 1]), true);
    setLeft(copy, left(n));
    setRight(copy, right(n));
    _nodes[n + 2] |= DEAD;
    if (parent == NONE) {
      assert _root == n;
      _root = copy;
    } else if (rightSide) {
      setRight(parent, copy);
    } else {
      setLeft(parent, copy);
    }
    return copy;
  }

  /** Release the slack in the backing arrays. Call once a trie is no longer being modified. */
  public void trimToSize() {
    if (_used < _nodes.length) {
      _nodes = Arrays.copyOf(_nodes, _used);
    }
    if (_numSlots < _values.length) {
      _values = Arrays.copyOf(_values, _numSlots);
      _keys = Arrays.copyOf(_keys, _numSlots);
    }
  }

  /**
   * Combine two nodes into a tree -- a newly created node, and an existing node (or {@link #NONE}).
   * The existing node's prefix must not contain the new node's prefix. Returns the root of the
   * combined tree.
   */
  private int combine(int newNode, int oldNode) {
    if (oldNode == NONE) {
      return newNode;
    }
    int newBits = bits(newNode);
    int newLen = len(newNode);
    int oldBits = bits(oldNode);
    int oldLen = len(oldNode);
    assert !contains(oldBits, oldLen, newBits, newLen);

    // If the new node's prefix contains the old node's prefix, the old node is its child.
    if (contains(newBits, newLen, oldBits, oldLen)) {
      if (bitAt(oldBits, newLen)) {
        setRight(newNode, oldNode);
      } else {
        setLeft(newNode, oldNode);
      }
      return newNode;
    }

    // Otherwise branch at the longest common prefix. Neither prefix contains the other, so they
    // differ within the shorter length and the common prefix is strictly shorter than both.
    int lcpLen = Integer.numberOfLeadingZeros(newBits ^ oldBits);
    assert lcpLen < Math.min(newLen, oldLen);
    int parent = newNode(newBits & prefixMask(lcpLen), lcpLen, false);
    if (bitAt(newBits, lcpLen)) {
      setRight(parent, newNode);
      setLeft(parent, oldNode);
    } else {
      setLeft(parent, newNode);
      setRight(parent, oldNode);
    }
    return parent;
  }

  // Lookups.

  /**
   * Returns the deepest node whose prefix contains the prefix (bits, len), or {@link #NONE} if the
   * root does not.
   */
  private int longestMatchNode(int bits, int len) {
    int[] nodes = _nodes;
    int node = _root;
    int found = NONE;
    while (node != NONE) {
      int a = nodes[node + 1];
      int nodeLen = lenOf(a);
      if (nodeLen > len || ((nodes[node] ^ bits) & prefixMask(nodeLen)) != 0) {
        break;
      }
      found = node;
      if (nodeLen >= len) {
        break;
      }
      node = bitAt(bits, nodeLen) ? rightOf(nodes[node + 2]) : leftOf(a);
    }
    return found;
  }

  /**
   * Returns the deepest node that has elements and whose prefix contains {@code ip} with length at
   * most {@code maxLen}, or {@link #NONE}.
   */
  private int longestMatchNonEmptyNode(int ip, int maxLen) {
    int[] nodes = _nodes;
    int node = _root;
    int best = NONE;
    while (node != NONE) {
      int a = nodes[node + 1];
      int nodeLen = lenOf(a);
      if (nodeLen > maxLen || ((nodes[node] ^ ip) & prefixMask(nodeLen)) != 0) {
        break;
      }
      int b = nodes[node + 2];
      if (valued(b)) {
        best = node;
      }
      if (nodeLen >= maxLen) {
        break;
      }
      node = bitAt(ip, nodeLen) ? rightOf(b) : leftOf(a);
    }
    return best;
  }

  private int exactMatchNode(Prefix p) {
    int len = p.getPrefixLength();
    int node = longestMatchNode(bitsOf(p), len);
    return node != NONE && len(node) == len ? node : NONE;
  }

  /** Find or create the slotted node for exactly the given prefix. */
  private int findOrCreateNode(Prefix p) {
    int bits = bitsOf(p);
    int len = p.getPrefixLength();
    if (_root == NONE) {
      _root = newKeyedNode(p);
      return _root;
    }
    int[] nodes = _nodes;
    int node = _root;
    if (!contains(nodes[node], lenOf(nodes[node + 1]), bits, len)) {
      int created = newKeyedNode(p);
      _root = combine(created, _root);
      return created;
    }
    int parent = NONE;
    boolean rightSide = false;
    while (true) {
      int a = nodes[node + 1];
      int nodeLen = lenOf(a);
      if (nodeLen == len) {
        if (!slotted(nodes[node + 2])) {
          node = relocateWithSlot(node, parent, rightSide);
        }
        int slot = _nodes[node + 3];
        if (_keys[slot] == null) {
          _keys[slot] = p;
        }
        return node;
      }
      // node's prefix strictly contains p: descend, or hang p off the appropriate child slot.
      boolean side = bitAt(bits, nodeLen);
      int child = side ? rightOf(nodes[node + 2]) : leftOf(a);
      if (child == NONE || !contains(nodes[child], lenOf(nodes[child + 1]), bits, len)) {
        int created = newKeyedNode(p);
        int combined = combine(created, child);
        if (side) {
          setRight(node, combined);
        } else {
          setLeft(node, combined);
        }
        return created;
      }
      parent = node;
      rightSide = side;
      node = child;
    }
  }

  // Element updates at a slotted node.

  @SuppressWarnings("unchecked")
  private boolean addElement(int node, T e) {
    int b = _nodes[node + 2];
    if (!valued(b)) {
      setSingle(node, e);
      return true;
    }
    Object v = _values[_nodes[node + 3]];
    if (!multi(b)) {
      if (v.equals(e)) {
        return false;
      }
      setElements(node, ImmutableSet.of((T) v, e));
      return true;
    }
    ImmutableSet<T> existing = (ImmutableSet<T>) v;
    if (existing.contains(e)) {
      return false;
    }
    setElements(
        node,
        ImmutableSet.<T>builderWithExpectedSize(existing.size() + 1)
            .addAll(existing)
            .add(e)
            .build());
    return true;
  }

  @SuppressWarnings("unchecked")
  private boolean addElements(int node, Collection<T> elements) {
    int b = _nodes[node + 2];
    if (!valued(b)) {
      if (elements.size() == 1) {
        setSingle(node, elements.iterator().next());
        return true;
      }
      ImmutableSet<T> set = ImmutableSet.copyOf(elements);
      if (set.isEmpty()) {
        return false;
      }
      setElements(node, set);
      return true;
    }
    Object v = _values[_nodes[node + 3]];
    if (!multi(b)) {
      boolean allSame = true;
      for (T e : elements) {
        if (!v.equals(e)) {
          allSame = false;
          break;
        }
      }
      if (allSame) {
        return false;
      }
      setElements(
          node,
          ImmutableSet.<T>builderWithExpectedSize(elements.size() + 1)
              .add((T) v)
              .addAll(elements)
              .build());
      return true;
    }
    ImmutableSet<T> existing = (ImmutableSet<T>) v;
    if (existing.containsAll(elements)) {
      return false;
    }
    setElements(
        node,
        ImmutableSet.<T>builderWithExpectedSize(existing.size() + elements.size())
            .addAll(existing)
            .addAll(elements)
            .build());
    return true;
  }

  @SuppressWarnings("unchecked")
  private boolean removeAt(int node, T e) {
    int b = _nodes[node + 2];
    if (!valued(b)) {
      return false;
    }
    Object v = _values[_nodes[node + 3]];
    if (!multi(b)) {
      if (!v.equals(e)) {
        return false;
      }
      clearElements(node);
      return true;
    }
    ImmutableSet<T> existing = (ImmutableSet<T>) v;
    if (!existing.contains(e)) {
      return false;
    }
    setElements(
        node, existing.stream().filter(el -> !el.equals(e)).collect(ImmutableSet.toImmutableSet()));
    return true;
  }

  private boolean replaceAllAt(int node, T e) {
    int b = _nodes[node + 2];
    if (valued(b) && !multi(b) && _values[_nodes[node + 3]].equals(e)) {
      return false;
    }
    setSingle(node, e);
    return true;
  }

  // Public API.

  /**
   * Post-order traversal over the entries. Entries will always contain non-null keys and values.
   * The traversal may not mutate the entries (the values are immutable sets).
   */
  public void traverseEntries(BiConsumer<Prefix, Set<T>> consumer) {
    traverseEntriesImpl(_root, consumer, null);
  }

  /**
   * Post-order traversal over the entries. Entries will always contain non-null keys and values.
   * The traversal may not mutate the entries (the values are immutable sets).
   *
   * <p>A node will only be visited if {@code visitNode} returns {@code true} for its prefix and
   * elements. It is also evaluated at branching nodes that have no elements, whose prefixes are
   * fresh instances that bypass the {@link Prefix} cache.
   */
  public void traverseEntries(
      BiConsumer<Prefix, Set<T>> consumer, BiPredicate<Prefix, Set<T>> visitChild) {
    traverseEntriesImpl(_root, consumer, visitChild);
  }

  private void traverseEntriesImpl(
      int node, BiConsumer<Prefix, Set<T>> consumer, @Nullable BiPredicate<Prefix, Set<T>> visit) {
    if (node == NONE) {
      return;
    }
    if (visit != null && !visit.test(keyOf(node), elementsAt(node))) {
      return;
    }
    traverseEntriesImpl(left(node), consumer, visit);
    traverseEntriesImpl(right(node), consumer, visit);
    if (hasValue(node)) {
      consumer.accept(keyOf(node), elementsAt(node));
    }
  }

  /**
   * Post-order traversal over the entries whose prefix contains {@code prefix} or is contained in
   * it. Below {@code prefix}, a node and its subtree are visited only if {@code descendThrough}
   * accepts the node's elements (empty at branching nodes). Equivalent to {@link
   * #traverseEntries(BiConsumer, BiPredicate)} with the predicate {@code (p, elems) ->
   * p.containsPrefix(prefix) || (prefix.containsPrefix(p) && descendThrough.test(elems))}, but it
   * follows a single path to {@code prefix} and materializes no prefixes for branching nodes.
   */
  public void traverseEntriesAround(
      Prefix prefix, Predicate<Set<T>> descendThrough, BiConsumer<Prefix, Set<T>> consumer) {
    traverseEntriesAroundImpl(
        _root, bitsOf(prefix), prefix.getPrefixLength(), descendThrough, consumer);
  }

  private void traverseEntriesAroundImpl(
      int node,
      int bits,
      int len,
      Predicate<Set<T>> descendThrough,
      BiConsumer<Prefix, Set<T>> consumer) {
    if (node == NONE) {
      return;
    }
    int nodeBits = _nodes[node];
    int nodeLen = len(node);
    if (contains(nodeBits, nodeLen, bits, len)) {
      if (nodeLen < len) {
        // An ancestor: only the child toward the prefix can contain or be contained in it.
        traverseEntriesAroundImpl(
            bitAt(bits, nodeLen) ? right(node) : left(node), bits, len, descendThrough, consumer);
      } else {
        // The prefix itself: everything below is contained in it.
        traverseEntriesBelowImpl(left(node), descendThrough, consumer);
        traverseEntriesBelowImpl(right(node), descendThrough, consumer);
      }
    } else if (contains(bits, len, nodeBits, nodeLen)) {
      // Strictly below the prefix without passing through its node (the path branched above it).
      traverseEntriesBelowImpl(node, descendThrough, consumer);
      return;
    } else {
      return;
    }
    if (hasValue(node)) {
      consumer.accept(keyOf(node), elementsAt(node));
    }
  }

  private void traverseEntriesBelowImpl(
      int node, Predicate<Set<T>> descendThrough, BiConsumer<Prefix, Set<T>> consumer) {
    if (node == NONE) {
      return;
    }
    Set<T> elements = elementsAt(node);
    if (!descendThrough.test(elements)) {
      return;
    }
    traverseEntriesBelowImpl(left(node), descendThrough, consumer);
    traverseEntriesBelowImpl(right(node), descendThrough, consumer);
    if (!elements.isEmpty()) {
      consumer.accept(keyOf(node), elements);
    }
  }

  /**
   * Perform a fold over the trie. The fold applies the same operation at each node of the trie,
   * bottom-up. The operation's inputs are the return values of the recursive calls on the subtries,
   * plus the prefix and values at that node. Branching nodes that have no elements receive prefixes
   * that are fresh instances bypassing the {@link Prefix} cache.
   */
  public <R> R fold(FoldOperator<T, R> operator) {
    return foldImpl(_root, operator);
  }

  private @Nullable <R> R foldImpl(int node, FoldOperator<T, R> operator) {
    if (node == NONE) {
      return null;
    }
    R leftResult = foldImpl(left(node), operator);
    R rightResult = foldImpl(right(node), operator);
    return operator.fold(keyOf(node), elementsAt(node), leftResult, rightResult);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PrefixTrieMultiMap<?>)) {
      return false;
    }
    PrefixTrieMultiMap<?> that = (PrefixTrieMultiMap<?>) o;
    if (_numElements != that._numElements) {
      return false;
    }
    // Every entry here matches an entry there, and the element counts agree, so there are no
    // entries there that are missing here.
    for (int n = 0; n < _used; n += recordSize(_nodes[n + 2])) {
      if (hasValue(n)) {
        int other = that.longestMatchNode(bits(n), len(n));
        if (other == NONE || that.len(other) != len(n)) {
          return false;
        }
        if (!elementsAt(n).equals(that.elementsAt(other))) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    for (int n = 0; n < _used; n += recordSize(_nodes[n + 2])) {
      if (hasValue(n)) {
        hash += 31 * (31 * bits(n) + len(n)) + elementsHashAt(n);
      }
    }
    return hash;
  }

  /**
   * Retrieve an immutable copy of elements for the given prefix (anywhere in the subtree). Returns
   * empty set if the prefix is not in the subtree.
   */
  public @Nonnull Set<T> get(Prefix p) {
    int node = exactMatchNode(p);
    return node == NONE ? ImmutableSet.of() : elementsAt(node);
  }

  /**
   * A {@link Handle} on the elements at {@code p}, creating the prefix's node if needed. Use it to
   * read and then update one prefix with a single search.
   */
  public @Nonnull Handle handle(Prefix p) {
    return new Handle(findOrCreateNode(p));
  }

  /**
   * A {@link Handle} on the elements at {@code p}, or {@code null} if there are none. Unlike {@link
   * #handle}, this never adds to the trie.
   */
  public @Nullable Handle existingHandle(Prefix p) {
    int node = exactMatchNode(p);
    return node == NONE || !hasValue(node) ? null : new Handle(node);
  }

  /**
   * @return all elements in the trie.
   */
  public @Nonnull Set<T> getAllElements() {
    ImmutableSet.Builder<T> b = ImmutableSet.builderWithExpectedSize(_numElements);
    collectElements(_root, b);
    return b.build();
  }

  @SuppressWarnings("unchecked")
  private void collectElements(int node, ImmutableSet.Builder<T> builder) {
    if (node == NONE) {
      return;
    }
    int a = _nodes[node + 1];
    int b = _nodes[node + 2];
    collectElements(leftOf(a), builder);
    collectElements(rightOf(b), builder);
    if (valued(b)) {
      Object v = _values[_nodes[node + 3]];
      if (multi(b)) {
        builder.addAll((ImmutableSet<T>) v);
      } else {
        builder.add((T) v);
      }
    }
  }

  /** Equivalent to {@link #getAllElements()}.{@link Set#size}. */
  public int getNumElements() {
    return _numElements;
  }

  /** Find the elements associated with the longest matching prefix of a given IP address. */
  public @Nonnull Set<T> longestPrefixMatch(Ip address) {
    return longestPrefixMatch(address, Prefix.MAX_PREFIX_LENGTH);
  }

  /**
   * Find the elements associated with the longest matching prefix of a given IP address, up to the
   * given maximum length.
   */
  public @Nonnull Set<T> longestPrefixMatch(Ip address, int maxPrefixLength) {
    int node = longestMatchNonEmptyNode((int) address.asLong(), maxPrefixLength);
    return node == NONE ? ImmutableSet.of() : elementsAt(node);
  }

  /**
   * Return all values whose keys intersect with the input {@link RangeSet}. Values are returned as
   * a {@link Stream} in post-order, so if prefix p1 contains p2, values for p2 will be returned
   * before values for p1.
   */
  public @Nonnull Stream<Map.Entry<Prefix, Set<T>>> getOverlappingEntries(RangeSet<Ip> ips) {
    return getOverlappingEntriesImpl(_root, ips);
  }

  private Stream<Map.Entry<Prefix, Set<T>>> getOverlappingEntriesImpl(int node, RangeSet<Ip> ips) {
    if (node == NONE) {
      return Stream.of();
    }
    Prefix prefix = keyOf(node);
    RangeSet<Ip> matchingIps =
        ips.subRangeSet(Range.closed(prefix.getStartIp(), prefix.getEndIp()));
    if (matchingIps.isEmpty()) {
      return Stream.of();
    }
    Stream<Map.Entry<Prefix, Set<T>>> elementsHere =
        hasValue(node) ? Stream.of(Maps.immutableEntry(prefix, elementsAt(node))) : Stream.of();
    return Stream.concat(
        // recurse lazily
        Stream.of(left(node), right(node))
            .filter(child -> child != NONE)
            .flatMap(child -> getOverlappingEntriesImpl(child, matchingIps)),
        // post-order
        elementsHere);
  }

  /**
   * Stores a key-value pair in the multimap.
   *
   * @return whether the multimap was modified.
   */
  public boolean put(Prefix p, T e) {
    return addElement(findOrCreateNode(p), e);
  }

  /**
   * Stores multiple key-value pairs for a single key in the multimap.
   *
   * @return whether the multimap was modified.
   */
  public boolean putAll(Prefix p, Collection<T> elements) {
    return addElements(findOrCreateNode(p), elements);
  }

  /**
   * Remove a key-value pair from the multimap.
   *
   * @return whether the multimap was modified.
   */
  public boolean remove(Prefix p, T e) {
    int node = exactMatchNode(p);
    return node != NONE && removeAt(node, e);
  }

  /**
   * Replace any elements associated with prefix {@code p} with a given element.
   *
   * @return whether the multimap was modified
   */
  public boolean replaceAll(Prefix p, T e) {
    return replaceAllAt(findOrCreateNode(p), e);
  }

  /** Remove all elements from the multimap. */
  public void clear() {
    _nodes = EMPTY_NODES;
    _used = 0;
    _root = NONE;
    _values = EMPTY_VALUES;
    _keys = EMPTY_KEYS;
    _numSlots = 0;
    _numElements = 0;
  }

  /**
   * Returns {@code true} iff there is any intersection between the prefixes that are keys of this
   * trie and the provided {@code prefixSpace}.
   */
  public boolean intersectsPrefixSpace(PrefixSpace prefixSpace) {
    return _root != NONE
        && prefixSpace.getPrefixRanges().stream()
            .anyMatch(range -> intersectsPrefixRangeImpl(_root, range));
  }

  /**
   * Returns true iff there is a {@link Prefix} key in the subtree rooted at {@code node} included
   * in {@code prefixRange}.
   */
  private boolean intersectsPrefixRangeImpl(int node, PrefixRange prefixRange) {
    // Overview:
    // - If this prefix's length is greater than prefixRange's max length, return false.
    // - If this prefix is contained in prefixRange and this node has any elements (making this
    //   prefix a key), return true.
    // - If either of this prefix or prefixRange's match prefix contains the other, then check
    //   this node's children.
    // - Else return false.
    int len = len(node);
    SubRange lengthRange = prefixRange.getLengthRange();
    if (len > lengthRange.getEnd()) {
      return false;
    }
    int bits = bits(node);
    Prefix rangePrefix = prefixRange.getPrefix();
    int rangeBits = bitsOf(rangePrefix);
    int rangeLen = rangePrefix.getPrefixLength();
    if (hasValue(node)
        && ((rangeBits ^ bits) & prefixMask(rangeLen)) == 0
        && lengthRange.getStart() <= len) {
      return true;
    }
    if (!contains(bits, len, rangeBits, rangeLen) && !contains(rangeBits, rangeLen, bits, len)) {
      return false;
    }
    int left = left(node);
    int right = right(node);
    return (left != NONE && intersectsPrefixRangeImpl(left, prefixRange))
        || (right != NONE && intersectsPrefixRangeImpl(right, prefixRange));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("numElements", _numElements)
        .add("numKeys", _numSlots)
        .toString();
  }

  private static class SerializedForm<T> implements Serializable {
    // Written compactly; see writeObject.
    private transient ImmutableList<Prefix> _keys;
    private transient ImmutableList<Set<T>> _values;

    private SerializedForm(ImmutableList<Prefix> keys, ImmutableList<Set<T>> values) {
      _keys = keys;
      _values = values;
    }

    /*
     * Keys are written as numbers and each value set as its size and elements. Default
     * serialization would write a Prefix, an Ip, and a set with its backing array per key, several
     * times the objects of the elements themselves for a RIB or FIB of single-route prefixes.
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      int size = _keys.size();
      out.writeInt(size);
      for (int i = 0; i < size; i++) {
        Prefix key = _keys.get(i);
        out.writeInt((int) key.getStartIp().asLong());
        out.writeByte(key.getPrefixLength());
        Set<T> value = _values.get(i);
        out.writeInt(value.size());
        for (T t : value) {
          out.writeObject(t);
        }
      }
    }

    @SuppressWarnings("unchecked")
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      int size = in.readInt();
      ImmutableList.Builder<Prefix> keys = ImmutableList.builderWithExpectedSize(size);
      ImmutableList.Builder<Set<T>> values = ImmutableList.builderWithExpectedSize(size);
      for (int i = 0; i < size; i++) {
        keys.add(Prefix.create(Ip.create(in.readInt()), in.readByte()));
        int count = in.readInt();
        ImmutableSet.Builder<T> value = ImmutableSet.builderWithExpectedSize(count);
        for (int j = 0; j < count; j++) {
          value.add((T) in.readObject());
        }
        values.add(value.build());
      }
      _keys = keys.build();
      _values = values.build();
    }

    public static <T> SerializedForm<T> of(PrefixTrieMultiMap<T> map) {
      ImmutableList.Builder<Prefix> keys = ImmutableList.builder();
      ImmutableList.Builder<Set<T>> values = ImmutableList.builder();
      map.traverseEntries(
          (prefix, elements) -> {
            keys.add(prefix);
            values.add(elements);
          });
      return new SerializedForm<>(keys.build(), values.build());
    }

    @Serial
    public Object readResolve() throws ObjectStreamException {
      PrefixTrieMultiMap<T> ret = new PrefixTrieMultiMap<>();
      for (int i = 0; i < _keys.size(); ++i) {
        ret.putAll(_keys.get(i), _values.get(i));
      }
      ret.trimToSize();
      return ret;
    }
  }

  @Serial
  private Object writeReplace() throws ObjectStreamException {
    return SerializedForm.of(this);
  }
}
