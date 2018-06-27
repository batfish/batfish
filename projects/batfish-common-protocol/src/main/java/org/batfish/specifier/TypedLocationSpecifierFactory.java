package org.batfish.specifier;

import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

/**
 * Helper class for implementing type-safe subclasses of {@link LocationSpecifierFactory}. It
 * validates that the input is of the appropriate type and then delegates to the subclass for
 * further validation and to build the {@link LocationSpecifier}.
 *
 * @param <T> The input type of the {@link LocationSpecifier}.
 */
public abstract class TypedLocationSpecifierFactory<T> implements LocationSpecifierFactory {
  /** @return The input type expected by this {@link LocationSpecifierFactory}. */
  protected abstract Class<T> getInputClass();

  @Override
  public final LocationSpecifier buildLocationSpecifier(@Nullable Object input) {
    Class<T> inputClass = getInputClass();
    if (!getInputClass().isInstance(input)) {
      throw new BatfishException(
          "TypedLocationSpecifierFactory expected input of type " + inputClass.getSimpleName());
    }
    return buildLocationSpecifierTyped(inputClass.cast(input));
  }

  /** Construct a {@link LocationSpecifier} object for the supplied input. */
  public abstract LocationSpecifier buildLocationSpecifierTyped(T input);
}
