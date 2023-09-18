package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Resolution RIB settings for a routing instance. */
@ParametersAreNonnullByDefault
public final class ResolutionRib implements Serializable {

  public ResolutionRib(String name) {
    _name = name;
    _importPolicies = ImmutableList.of();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void addImportPolicy(String policy) {
    _importPolicies = ImmutableList.<String>builder().addAll(_importPolicies).add(policy).build();
  }

  /** Returns the policy(ies) routes in this RIB must match to be usable for next-hop resolution. */
  public @Nonnull List<String> getImportPolicies() {
    return _importPolicies;
  }

  private @Nonnull List<String> _importPolicies;
  private final @Nonnull String _name;
}
