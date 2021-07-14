package org.batfish.common.topology.broadcast;

/**
 * A (physical, logical, computational) location in the network that a packet in the process of
 * transmission may reach.
 *
 * <p>Note: Each {@link Node} keeps track of its {@link Edge edges} to other {@link Node nodes}, but
 * these are <strong>not</strong> used in {@link Node#equals(Object)} or {@link Node#hashCode()}.
 */
public abstract class Node<T> {}
