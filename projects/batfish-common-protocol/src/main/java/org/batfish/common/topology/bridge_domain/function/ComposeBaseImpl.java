package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;

/**
 * Base implementation for {@link StateFunction} composition.
 *
 * <p>Due to desired type invariants and the Java type system, it is not possible to make a single
 * {@code Compose} class whose result is of the correct {@link StateFunction} subtype {@code E}.
 */
public abstract class ComposeBaseImpl<E extends StateFunction> implements StateFunction {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitCompose(this, arg);
  }

  /** The first function to apply in order to the state. */
  public @Nonnull E getFunc1() {
    return _func1;
  }

  /** The function to apply to the result of applying {@link #getFunc1()} to the state. */
  public @Nonnull E getFunc2() {
    return _func2;
  }

  protected ComposeBaseImpl(E func1, E func2) {
    _func1 = func1;
    _func2 = func2;
  }

  private final @Nonnull E _func1;
  private final @Nonnull E _func2;
}
