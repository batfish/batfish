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

  @Nonnull
  public String getName() {
    return _name;
  }

  public void addImportPolicy(String policy) {
    _importPolicies = ImmutableList.<String>builder().addAll(_importPolicies).add(policy).build();
  }

  /** Returns the policy(ies) routes in this RIB must match to be usable for next-hop resolution. */
  @Nonnull
  public List<String> getImportPolicies() {
    return _importPolicies;
  }

  @Nonnull private List<String> _importPolicies;
  @Nonnull private final String _name;
}
