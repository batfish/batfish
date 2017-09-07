package org.batfish.storage;

import java.util.List;
import org.batfish.datamodel.pojo.Analysis;

/** Common storage APIs */
public interface Storage {
  //Storage APIs for Configure Analysis

  Analysis getAnalysis(String containerName, String analysisName);

  Analysis saveAnalysis(String containerName, Analysis analysis);

  Analysis updateAnalysis(String containerName, Analysis analysis);

  boolean deleteAnalysis(String containerName, String analysisName, boolean force);

  List<String> listAnalyses(String containerName);
}
