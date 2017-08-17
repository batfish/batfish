package org.batfish.storage;

import java.nio.file.Path;
import org.batfish.datamodel.pojo.Analysis;

/** Common storage APIs */
public interface Storage {

  //deprecated
  Path getContainerPath(String containerName);

  //Storage APIs for Configure Analysis

  Analysis getAnalysis(String containerName, String analysisName);

  Analysis createAnalysis(String containerName, String analysisName);

  Analysis updateAnalysis(String containerName, Analysis analysis);

  Analysis saveOrUpdateAnalysis(String containerName, Analysis analysis);

  boolean deleteAnalysis(String containerName, String analysisName, boolean force);
}
