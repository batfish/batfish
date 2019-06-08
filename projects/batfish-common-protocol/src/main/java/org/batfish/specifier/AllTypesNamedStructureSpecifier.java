package org.batfish.specifier;

import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;

/** A {@link NamedStructureSpecifier} the specifies all structure types */
public final class AllTypesNamedStructureSpecifier implements NamedStructureSpecifier {
  public static final AllTypesNamedStructureSpecifier INSTANCE =
      new AllTypesNamedStructureSpecifier();

  private AllTypesNamedStructureSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof AllTypesNamedStructureSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<String> resolve() {
    return NamedStructurePropertySpecifier.JAVA_MAP.keySet();
  }
}
