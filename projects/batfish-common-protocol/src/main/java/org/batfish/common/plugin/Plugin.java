package org.batfish.common.plugin;

public abstract class Plugin implements Comparable<Plugin> {

  protected PluginConsumer _pluginConsumer;

  @Override
  public final int compareTo(Plugin o) {
    return getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
  }

  @Override
  public final boolean equals(Object obj) {
    return obj != null && getClass().equals(obj.getClass());
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode();
  }

  public final void initialize(PluginConsumer pluginConsumer) {
    _pluginConsumer = pluginConsumer;
    pluginInitialize();
  }

  protected abstract void pluginInitialize();
}
