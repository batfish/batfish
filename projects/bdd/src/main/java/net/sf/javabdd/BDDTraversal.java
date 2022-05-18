package net.sf.javabdd;

public interface BDDTraversal {
  /** Called upon reaching the one node. */
  void one();

  /** Called upon reaching the zero node. */
  void zero();

  /** Called upon backtracking. */
  void backtrack();

  /**
   * Called before taking the high (true) branch of the variable {@code var}.
   *
   * @return whether to continue the traversal. If false, will not take the branch.
   */
  boolean traverse_high(int var);

  /**
   * Called before taking the low (false) branch of the variable {@code var}.
   *
   * @return whether to continue the traversal. If false, will not take the branch.
   */
  boolean traverse_low(int var);
}
