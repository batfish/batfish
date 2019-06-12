package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link EnumSetSpecifier} the returns the structure types specified */
@ParametersAreNonnullByDefault
public final class IdentityEnumSetSpecifier implements EnumSetSpecifier {

  @Nonnull private final Set<String> _structureTypes;

  public IdentityEnumSetSpecifier(Set<String> structureTypes) {
    _structureTypes = ImmutableSet.copyOf(structureTypes);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof IdentityEnumSetSpecifier)) {
      return false;
    }
    return Objects.equals(_structureTypes, ((IdentityEnumSetSpecifier) obj)._structureTypes);
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
