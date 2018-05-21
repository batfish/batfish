package org.batfish.specifier;

import org.batfish.common.BatfishException;

public abstract class TypedLocationSpecifierFactory<T> implements LocationSpecifierFactory {
  protected abstract Class<T> getInputClass();

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    Class<T> inputClass = getInputClass();
    if (!getInputClass().isInstance(input)) {
      throw new BatfishException(
          "TypedLocationSpecifierFactory expected input of type " + inputClass.getSimpleName());
    }
    return specifierTyped(inputClass.cast(input));
  }

  public abstract LocationSpecifier specifierTyped(T input);
}
