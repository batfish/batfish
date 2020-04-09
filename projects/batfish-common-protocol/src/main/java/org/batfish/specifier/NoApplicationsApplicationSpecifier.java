package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.applications.Application;

/** A {@link ApplicationSpecifier} without any applications. */
@ParametersAreNonnullByDefault
public final class NoApplicationsApplicationSpecifier implements ApplicationSpecifier {
  public static final NoApplicationsApplicationSpecifier INSTANCE =
      new NoApplicationsApplicationSpecifier();

  private NoApplicationsApplicationSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof NoApplicationsApplicationSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<Application> resolve() {
    return ImmutableSet.of();
  }
}
