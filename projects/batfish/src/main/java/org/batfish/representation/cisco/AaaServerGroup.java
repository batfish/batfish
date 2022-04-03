package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An abstract class for a AAA group */
@ParametersAreNonnullByDefault
public abstract class AaaServerGroup implements Serializable {

  private @Nonnull final String _name;
  private @Nonnull List<String> _privateServers;
  private @Nonnull List<String> _servers;

  public AaaServerGroup(String name) {
    _name = name;
    _privateServers = ImmutableList.of();
    _servers = ImmutableList.of();
  }

  public void addServer(String server) {
    _servers = ImmutableList.<String>builder().addAll(_servers).add(server).build();
  }

  public void addPrivateServer(String server) {
    _privateServers = ImmutableList.<String>builder().addAll(_privateServers).add(server).build();
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public List<String> getPrivateServers() {
    return _privateServers;
  }

  @Nonnull
  public List<String> getServers() {
    return _servers;
  }
}
