package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An abstract class for a AAA group */
@ParametersAreNonnullByDefault
public abstract class AaaServerGroup implements Serializable {

  private final @Nonnull String _name;
  private @Nonnull List<String> _privateServers;
  private @Nonnull List<String> _servers;

  public AaaServerGroup(String name) {
    _name = name;
    _privateServers = ImmutableList.of();
    _servers = ImmutableList.of();
  }

  public void addServer(String server) {
    _servers =
        ImmutableList.<String>builderWithExpectedSize(_servers.size() + 1)
            .addAll(_servers)
            .add(server)
            .build();
  }

  public void addPrivateServer(String server) {
    _privateServers =
        ImmutableList.<String>builderWithExpectedSize(_privateServers.size() + 1)
            .addAll(_privateServers)
            .add(server)
            .build();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<String> getPrivateServers() {
    return _privateServers;
  }

  public @Nonnull List<String> getServers() {
    return _servers;
  }
}
