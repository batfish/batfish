package org.batfish.common.plugin;

import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public abstract class AbstractCoordinator extends PluginConsumer implements ICoordinator {

  protected final Map<String, SyncTestrigsPlugin> _snapshotsSyncers;

  protected final Map<String, String> _versions;

  public AbstractCoordinator() {
    _snapshotsSyncers = new HashMap<>();
    _versions = new HashMap<>();
  }

  @Override
  public final PluginClientType getType() {
    return PluginClientType.COORDINATOR;
  }

  public final Map<String, String> getVersions() {
    return _versions;
  }

  @Override
  public final void registerTestrigSyncer(String name, SyncTestrigsPlugin plugin) {
    if (_snapshotsSyncers.containsKey(name)) {
      throw new BatfishException("Multiple SyncTestrigs plugins are registering for " + name);
    }
    _snapshotsSyncers.put(name, plugin);
  }

  @Override
  public final void registerVersion(String name, String version) {
    String previous = _versions.putIfAbsent(name, version);
    if (previous != null) {
      throw new IllegalArgumentException(
          String.format("%s already has a registered version.", name));
    }
  }
}
