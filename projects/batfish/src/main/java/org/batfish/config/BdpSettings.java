package org.batfish.config;

import org.batfish.common.plugin.DataPlanePluginSettings;

public interface BdpSettings extends DataPlanePluginSettings {

  boolean getBdpDebugAllIterations();

  boolean getBdpDebugIterationsDetailed();

  int getBdpDebugMaxRecordedIterations();

  boolean getBdpDebugRepeatIterations();

  void setBdpDebugAllIterations(boolean bdpDebugAlliterations);

  void setBdpDebugIterationsDetailed(boolean bdpDebugIterationsDetailed);

  void setBdpDebugMaxRecordedIterations(int bdpDebugMaxRecordedIterations);

  void setBdpDebugRepeatIterations(boolean bdpDebugRepeatIterations);
}
