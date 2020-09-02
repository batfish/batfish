package org.batfish.common.plugin;

public abstract class AbstractCoordinator extends PluginConsumer implements ICoordinator {

  @Override
  public final PluginClientType getType() {
    return PluginClientType.COORDINATOR;
  }
}
