package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Policy for matching/transforming routes. */
@ParametersAreNonnullByDefault
public final class RoutePolicy implements Serializable {

  public RoutePolicy(String name) {
    _name = name;
    _statements = ImmutableList.of();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<RoutePolicyStatement> getStatements() {
    return _statements;
  }

  public void addStatement(RoutePolicyStatement statement) {
    _statements =
        ImmutableList.<RoutePolicyStatement>builderWithExpectedSize(_statements.size() + 1)
            .addAll(_statements)
            .add(statement)
            .build();
  }

  private final @Nonnull String _name;
  private @Nonnull List<RoutePolicyStatement> _statements;
}
