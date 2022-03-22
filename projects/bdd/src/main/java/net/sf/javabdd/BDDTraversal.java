package net.sf.javabdd;

public interface BDDTraversal {
  /** Called upon reaching the one node. */
  void one();

  /** Called upon reaching the zero node. */
  void zero();

  /** Called upon backtracking. */
  void backtrack();

  /**
   * Called when taking the high (true) branch of the variable {@code var}.
   *
   * @return whether to continue the traversal. If false, will backtrack.
   */
  boolean high(int var);

  /**
   * Called when taking the low (false) branch of the variable {@code var}.
   *
   * @return whether to continue the traversal. If false, will backtrack.
   */
  boolean low(int var);
}
