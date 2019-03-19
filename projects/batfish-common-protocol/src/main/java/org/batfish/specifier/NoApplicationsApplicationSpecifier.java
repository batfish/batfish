package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.Protocol;

/** A {@link ApplicationSpecifier} the specifies the empty set of protocols */
public final class NoApplicationsApplicationSpecifier implements ApplicationSpecifier {
  public static final NoApplicationsApplicationSpecifier INSTANCE =
      new NoApplicationsApplicationSpecifier();

  private NoApplicationsApplicationSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof NoApplicationsApplicationSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<Protocol> resolve() {
    return ImmutableSet.of();
  }
}
