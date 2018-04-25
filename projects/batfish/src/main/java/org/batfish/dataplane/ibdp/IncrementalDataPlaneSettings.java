package org.batfish.dataplane.ibdp;

import static org.batfish.dataplane.ibdp.schedule.IbdpSchedule.Schedule.NODE_COLORED;
import static org.batfish.dataplane.ibdp.schedule.NodeColoredSchedule.Coloring.SATURATION;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.batfish.dataplane.ibdp.schedule.IbdpSchedule.Schedule;
import org.batfish.dataplane.ibdp.schedule.NodeColoredSchedule;
import org.batfish.dataplane.ibdp.schedule.NodeColoredSchedule.Coloring;

/** Settings for {@link IncrementalDataPlanePlugin} */
public class IncrementalDataPlaneSettings {

  private Configuration _config;

  public static final String PROP_COLORING = "coloring";
  public static final String PROP_SCHEDULE = "schedule";
  public static final String PROP_LOG_ROUTES = "logiterationroutes";
  public static final String PROP_CHECK_BGP_REACHABILITY = "checkbgpsessionreachability";

  /**
   * Return the underlying configuration (it will be mutable).
   *
   * @return a {@link Configuration}
   */
  public Configuration getConfig() {
    return _config;
  }

  /** Create new settings object and initialize it with defaults */
  public IncrementalDataPlaneSettings() {
    _config = new PropertiesConfiguration();
    initDefaults();
  }

  /**
   * Create new settings, initialize with defaults, but also copy over settings from {@code
   * configSource}
   *
   * @param configSource the configuration options to override the defaults
   */
  public IncrementalDataPlaneSettings(ImmutableConfiguration configSource) {
    this();
    ConfigurationUtils.copy(configSource, _config);
  }

  /** Initialize defaults for all properties */
  private void initDefaults() {
    _config.setProperty(PROP_COLORING, SATURATION.toString());
    _config.setProperty(PROP_SCHEDULE, NODE_COLORED.toString());
    _config.setProperty(PROP_LOG_ROUTES, true);
    _config.setProperty(PROP_CHECK_BGP_REACHABILITY, true);
  }

  /** Return the dataplane computation {@link Schedule} */
  public Schedule getScheduleName() {
    return Schedule.valueOf(_config.getString(PROP_SCHEDULE));
  }

  /** Whether to perform reachability checks to ensure BGP sessions can be properly established */
  public boolean getCheckBgpSessionReachability() {
    return _config.getBoolean(PROP_CHECK_BGP_REACHABILITY);
  }

  /**
   * Set the dataplane computation {@link Schedule}
   *
   * @param schedule the new schedule
   */
  public void setScheduleName(Schedule schedule) {
    _config.setProperty(PROP_SCHEDULE, schedule.toString());
  }

  /**
   * If the schedule is of type {@link NodeColoredSchedule}, get the type of {@link Coloring} to
   * perform
   */
  public Coloring getColoringType() {
    return Coloring.valueOf(_config.getString(PROP_COLORING));
  }
}
