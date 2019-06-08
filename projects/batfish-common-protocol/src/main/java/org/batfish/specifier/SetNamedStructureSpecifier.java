package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

/** A {@link NamedStructureSpecifier} the specifies all structure types */
public final class SetNamedStructureSpecifier implements NamedStructureSpecifier {

  Set<String> _structureTypes;

  public SetNamedStructureSpecifier(Set<String> structureTypes) {
    _structureTypes = ImmutableSet.copyOf(structureTypes);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof SetNamedStructureSpecifier)) {
      return true;
    }
    return Objects.equals(_structureTypes, ((SetNamedStructureSpecifier) obj)._structureTypes);
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
