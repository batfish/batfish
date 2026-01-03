package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link EnumSetSpecifier} the returns the structure types specified */
@ParametersAreNonnullByDefault
public final class ConstantEnumSetSpecifier<T> implements EnumSetSpecifier<T> {

  private final @Nonnull Set<T> _structureTypes;

  public ConstantEnumSetSpecifier(Set<T> structureTypes) {
    _structureTypes = ImmutableSet.copyOf(structureTypes);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof ConstantEnumSetSpecifier)) {
      return false;
    }
    return _structureTypes.equals(((ConstantEnumSetSpecifier) obj)._structureTypes);
  }

  @Override
  public int hashCode() {
    return _structureTypes.hashCode();
  }

  @Override
  public Set<T> resolve() {
    return _structureTypes;
  }
}
