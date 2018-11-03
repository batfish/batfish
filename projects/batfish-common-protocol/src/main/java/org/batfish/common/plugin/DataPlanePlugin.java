package org.batfish.common.plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;
import org.batfish.datamodel.flow.Trace;

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

  /**
   * Builds the {@link Trace}s for a {@link Set} of {@link Flow}s
   *
   * @param flows {@link Set} of {@link Flow} for which {@link Trace}s are to be found
   * @param dataPlane {@link DataPlane} for this network snapshot
   * @param ignoreFilters if true, will ignore filters/ACLs encountered in the network
   * @return {@link SortedMap} of {@link Flow} to {@link List} of {@link Trace}s
   */
  public abstract SortedMap<Flow, List<Trace>> buildFlows(
      Set<Flow> flows, DataPlane dataPlane, boolean ignoreFilters);

  public abstract ComputeDataPlaneResult computeDataPlane(boolean differentialContext);

  public abstract ComputeDataPlaneResult computeDataPlane(
      boolean differentialContext, Map<String, Configuration> configurations, Topology topology);

  protected void dataPlanePluginInitialize() {}

  public abstract List<Flow> getHistoryFlows(DataPlane dataPlane);

  public abstract List<FlowTrace> getHistoryFlowTraces(DataPlane dataPlane);

  public abstract ITracerouteEngine getTracerouteEngine();

  public abstract SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getRoutes(
      DataPlane dataPlane);

  public abstract void processFlows(Set<Flow> flows, DataPlane dataPlane, boolean ignoreFilters);

  /** Return the name of this plugin */
  public abstract String getName();
}
