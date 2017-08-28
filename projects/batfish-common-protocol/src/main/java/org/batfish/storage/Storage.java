package org.batfish.storage;

import org.batfish.datamodel.pojo.Analysis;
import org.batfish.datamodel.pojo.Environment;

/** Common storage APIs */
public interface Storage {
  //Storage APIs for Configure Analysis

  Analysis getAnalysis(String containerName, String analysisName);

  Analysis saveAnalysis(String containerName, Analysis analysis);

  Analysis updateAnalysis(String containerName, Analysis analysis);

  boolean deleteAnalysis(String containerName, String analysisName, boolean force);

  Environment getEnvironment(String containerName, String testrigName, String environmentName);

  Environment saveEnvironment(String containerName, String testrigName, Environment environment);

  Environment updateEnvironment(String containerName, String testrigName, Environment environment);

  boolean deleteEnvironment(String containerName, String testrigName, boolean force);
}
