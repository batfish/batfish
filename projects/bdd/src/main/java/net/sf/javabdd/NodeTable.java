package net.sf.javabdd;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

/** Node table for {@link JFactory}. */
final class NodeTable implements Serializable {
  private static final int REF_MASK = 0xFFC00000;
  private static final int MARK_MASK = 0x00200000;
  private static final int LEV_MASK = 0x001FFFFF;
  private static final int MAXVAR = LEV_MASK;
  private static final int INVALID_BDD = -1;

  private static final int REF_INC = 0x00400000;

  // nodes are represented as 5 integers
  private static final int NODE_SIZE = 5;

  private static final int OFFSET__REFCOUNT_MARK_AND_LEVEL = 0;
  private static final int OFFSET__LOW = 1;
  private static final int OFFSET__HIGH = 2;
  private static final int OFFSET__HASH = 3;
  private static final int OFFSET__NEXT = 4;

  private static final VarHandle AA = MethodHandles.arrayElementVarHandle(int[].class);
  private int[] array;

  NodeTable(int initialSize) {
    array = new int[initialSize * NODE_SIZE];
  }

  /** No other threads may access this concurrently with this method. */
  void resize(int newSize) {
    array = Arrays.copyOf(array, newSize * NODE_SIZE);
  }

  /** Get the current value of the reference count. */
  int getRef(int node) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    return (int) AA.getVolatile(array, idx) >>> 22;
  }

  /** Saturate the input node's reference counter. */
  void setMaxRef(int node) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    AA.getAndBitwiseOr(array, idx, REF_MASK);
  }

  /** Increment the reference count of the input node (if it has not become saturated). */
  void incRef(int node) {
    incOrDecRef(node, true);
  }

  /** Increment the reference count of the input node (if it has not become saturated). */
  void decRef(int node) {
    incOrDecRef(node, false);
  }

  private void incOrDecRef(int node, boolean increment) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    int delta = increment ? REF_INC : -REF_INC;
    while (true) {
      int ref = (int) AA.getVolatile(array, idx);
      if ((ref & REF_MASK) == REF_MASK || (!increment && ref == 0)) {
        // either the ref count is saturated, or we're trying to decrement from 0
        return;
      }
      if (AA.weakCompareAndSet(array, idx, ref, ref + delta)) {
        // write succeeded
        return;
      }
    }
  }

  /** Returns whether the input node has any references. */
  boolean hasRef(int node) {
    return getRef(node) != 0;
  }

  /** Clear any references of the input node. */
  void clearRef(int node) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    AA.getAndBitwiseAnd(array, idx, ~REF_MASK);
  }

  /** Mark the input node and return the previous value. */
  boolean setMark(int node, boolean value) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    int prev;
    if (value) {
      prev = (int) AA.getAndBitwiseOr(array, idx, MARK_MASK);
    } else {
      prev = (int) AA.getAndBitwiseAnd(array, idx, ~MARK_MASK);
    }
    return (prev & MARK_MASK) != 0;
  }

  /** Mark the input node and return the previous value. */
  void setMarkNonVolatile(int node, boolean value) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    if (value) {
      array[idx] |= MARK_MASK;
    } else {
      array[idx] &= ~MARK_MASK;
    }
  }

  /** Return whether the input node is marked. */
  boolean getMark(int node) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    return ((int) AA.getVolatile(array, idx) & MARK_MASK) != 0;
  }

  /** Return whether the input node is marked. */
  boolean getMarkNonVolatile(int node) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    return (array[idx] & MARK_MASK) != 0;
  }

  void setLevel(int node, int level) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;

    while (true) {
      int prev = (int) AA.getVolatile(array, idx);
      int next = (prev & ~LEV_MASK) | level;
      if (AA.weakCompareAndSet(array, idx, prev, next)) {
        // write succeeded
        return;
      }
    }
  }

  void setLevelAndMark(int node, int val) {
    assert val == (val & (LEV_MASK | MARK_MASK));
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;

    while (true) {
      int prev = (int) AA.getVolatile(array, idx);
      int next = (prev & ~(LEV_MASK | MARK_MASK)) | val;
      if (AA.weakCompareAndSet(array, idx, prev, next)) {
        // write succeeded
        return;
      }
    }
  }

  void setRefcountLevelAndMark(int node, int val) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    AA.setVolatile(array, idx, val);
  }

  /** Invariant: node is fully built. */
  int getLevel(int node) {
    // We don't need to use getVolatile here, because the level cannot change during the node's
    // lifetime.
    // other bits of this index can change (e.g. refcount), but the level part is constant. Java's
    // memory
    // model guarantees no thin-air reads, so level will always be correct
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    return array[idx] & LEV_MASK;
  }

  int getLevelVolatile(int node) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    return (int) AA.getVolatile(array, idx) & LEV_MASK;
  }

  int getLevelAndMark(int node) {
    int idx = node * NODE_SIZE + OFFSET__REFCOUNT_MARK_AND_LEVEL;
    return (int) AA.getVolatile(array, idx) & (LEV_MASK | MARK_MASK);
  }

  /** Precondition: node is fully built. */
  int getLowNonVolatile(int node) {
    // Don't need to use getVolatile, because LOW is constant for fully built nodes.
    int idx = node * NODE_SIZE + OFFSET__LOW;
    return array[idx];
  }

  int getLowVolatile(int node) {
    int idx = node * NODE_SIZE + OFFSET__LOW;
    return (int) AA.getVolatile(array, idx);
  }

  void setLow(int node, int low) {
    int idx = node * NODE_SIZE + OFFSET__LOW;
    AA.setVolatile(array, idx, low);
  }

  void setLowNonVolatile(int node, int low) {
    int idx = node * NODE_SIZE + OFFSET__LOW;
    array[idx] = low;
  }

  int getHigh(int node) {
    int idx = node * NODE_SIZE + OFFSET__HIGH;
    return array[idx];
  }

  int getHighVolatile(int node) {
    int idx = node * NODE_SIZE + OFFSET__HIGH;
    return (int) AA.getVolatile(array, idx);
  }

  void setHigh(int node, int high) {
    int idx = node * NODE_SIZE + OFFSET__HIGH;
    AA.setVolatile(array, idx, high);
  }

  /** Set the hash bucket index (i.e. the ID of the first node in bucket) for the input node. */
  void setHash(int node, int value) {
    int idx = node * NODE_SIZE + OFFSET__HASH;
    AA.setVolatile(array, idx, value);
  }

  void setHashNonVolatile(int node, int value) {
    int idx = node * NODE_SIZE + OFFSET__HASH;
    array[idx] = value;
  }

  int getHash(int node) {
    // have to use getVolatile, because bdd_makenode can set hash in another thread
    int idx = node * NODE_SIZE + OFFSET__HASH;
    return (int) AA.getVolatile(array, idx);
  }

  int getHashNonVolatile(int node) {
    // have to use getVolatile, because bdd_makenode can set hash in another thread
    int idx = node * NODE_SIZE + OFFSET__HASH;
    return array[idx];
  }

  int getNext(int node) {
    // have to use getVolatile, because bdd_makenode may have just inserted this node in another
    // thread
    int idx = node * NODE_SIZE + OFFSET__NEXT;
    return (int) AA.getVolatile(array, idx);
  }

  void setNext(int node, int next) {
    int idx = node * NODE_SIZE + OFFSET__NEXT;
    AA.setVolatile(array, idx, next);
  }

  void setNextNonVolatile(int node, int next) {
    int idx = node * NODE_SIZE + OFFSET__NEXT;
    array[idx] = next;
  }

  boolean trySetInitializing(int node) {
    int idx = node * NODE_SIZE + OFFSET__LOW;
    // LOW == -1 means it's free. set it to any other value to prevent
    // another thread from claiming it
    return AA.compareAndSet(array, idx, -1, 0);
  }

  /**
   * Insert the input {@code node} with the input {@code hashcode} into the first position of it's
   * hash bucket.
   */
  int insertNode(int node, int hashcode, int prevBucket, int level, int low, int high) {
    int next_idx = node * NODE_SIZE + OFFSET__NEXT;
    int bucket_idx = hashcode * NODE_SIZE + OFFSET__HASH;
    int next = prevBucket;
    while (true) {
      // set node's next pointer first, so that if the insert succeeds
      // the chain is immediately correct
      AA.setVolatile(array, next_idx, next);

      // make sure no other thread has inserted a different node
      if (AA.compareAndSet(array, bucket_idx, next, node)) {
        return node;
      }

      // bucket changed since last read. another thread may have inserted this bdd.
      next = (int) AA.getVolatile(array, bucket_idx);

      int res = next;
      while (res != 0) {
        if (getLevel(res) == level && getLowNonVolatile(res) == low && getHigh(res) == high) {
          // another thread created a copy of node. Return that copy, since it's already in the
          // table
          return res;
        }

        res = getNext(res);
      }

      // didn't find a copy, so retry the insert
    }
  }
}
