package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NamedStructureSpecifier} the returns the structure types specified */
@ParametersAreNonnullByDefault
public final class IdentityNamedStructureSpecifier implements NamedStructureSpecifier {

  @Nonnull private final Set<String> _structureTypes;

  public IdentityNamedStructureSpecifier(Set<String> structureTypes) {
    _structureTypes = ImmutableSet.copyOf(structureTypes);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof IdentityNamedStructureSpecifier)) {
      return false;
    }
    return Objects.equals(_structureTypes, ((IdentityNamedStructureSpecifier) obj)._structureTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_structureTypes);
  }

  @Override
  public Set<String> resolve() {
    return _structureTypes;
  }
}
