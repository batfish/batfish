package org.batfish.common.plugin;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;

/**
 * Abstract class that defines the behavior expected for a Batfish plugin that implements data plane
 * capabilities.
 */
public abstract class DataPlanePlugin extends BatfishPlugin implements IDataPlanePlugin {

  private static DataPlanePlugin DATA_PLANE_PLUGIN;

  private static Class<? extends DataPlanePlugin> DATA_PLANE_PLUGIN_CLASS;

  public static synchronized DataPlanePlugin getDataPlanePlugin() {
    return DATA_PLANE_PLUGIN;
  }

  public static synchronized Class<? extends DataPlanePlugin> getDataPlanePluginClass() {
    return DATA_PLANE_PLUGIN_CLASS;
  }

  public static synchronized void setDataPlanePlugin(DataPlanePlugin dataPlanePlugin) {
    DATA_PLANE_PLUGIN = dataPlanePlugin;
  }

  public static synchronized void setDataPlanePluginClass(
      Class<? extends DataPlanePlugin> dataPlanePlugin) {
    DATA_PLANE_PLUGIN_CLASS = dataPlanePlugin;
  }

  /**
   * Result of computing the dataplane. Combines a {@link DataPlane} with a {@link
   * DataPlaneAnswerElement}
   */
  public static final class ComputeDataPlaneResult {
    public final DataPlaneAnswerElement _answerElement;
    public final DataPlane _dataPlane;

    public ComputeDataPlaneResult(DataPlaneAnswerElement answerElement, DataPlane dataPlane) {
      _answerElement = answerElement;
      _dataPlane = dataPlane;
    }
  }

  @Override
  protected final void batfishPluginInitialize() {
    _batfish.registerDataPlanePlugin(this, getName());
    dataPlanePluginInitialize();
  }

  public abstract ComputeDataPlaneResult computeDataPlane();

  /** @deprecated in favor of {@link #computeDataPlane()} */
  @Deprecated
  public abstract ComputeDataPlaneResult computeDataPlane(boolean differentialContext);

  public abstract ComputeDataPlaneResult computeDataPlane(
      Map<String, Configuration> configurations, Topology topology);

  /** @deprecated in favor of {@link #computeDataPlane(Map, Topology)} */
  @Deprecated
  public abstract ComputeDataPlaneResult computeDataPlane(
      boolean differentialContext, Map<String, Configuration> configurations, Topology topology);

  protected void dataPlanePluginInitialize() {}

  public abstract SortedMap<String, SortedMap<String, SortedSet<AnnotatedRoute<AbstractRoute>>>>
      getRoutes(DataPlane dataPlane);

  /** Return the name of this plugin */
  public abstract String getName();
}
