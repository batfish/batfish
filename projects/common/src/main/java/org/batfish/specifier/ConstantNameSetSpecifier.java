package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NameSetSpecifier} the returns the structure types specified */
@ParametersAreNonnullByDefault
public final class ConstantNameSetSpecifier implements NameSetSpecifier {

  private final @Nonnull Set<String> _names;

  public ConstantNameSetSpecifier(Set<String> names) {
    _names = ImmutableSet.copyOf(names);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConstantNameSetSpecifier)) {
      return false;
    }
    return _names.equals(((ConstantNameSetSpecifier) obj)._names);
  }

  @Override
  public int hashCode() {
    return _names.hashCode();
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return _names;
  }
}
