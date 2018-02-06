package org.batfish.coordinator;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AnalysisMetadata;

public class AnalysisMetadataMgr {

  public static Instant getAnalysisCreationTimeOrMin(String container, String analysis) {
    try {
      return readMetadata(container, analysis).getCreationTimestamp();
    } catch (IOException e) {
      return Instant.MIN;
    }
  }

  public static AnalysisMetadata readMetadata(String container, String analysis)
      throws IOException {
    return readMetadata(WorkMgr.getpathAnalysisMetadata(container, analysis));
  }

  public static AnalysisMetadata readMetadata(Path metadataPath) throws IOException {
    String jsonStr = CommonUtil.readFile(metadataPath);
    return new BatfishObjectMapper().readValue(jsonStr, AnalysisMetadata.class);
  }

  public static void writeMetadata(AnalysisMetadata metadata, String container, String analysis)
      throws JsonProcessingException {
    writeMetadata(metadata, WorkMgr.getpathAnalysisMetadata(container, analysis));
  }

  public static synchronized void writeMetadata(AnalysisMetadata metadata, Path metadataPath)
      throws JsonProcessingException {
    CommonUtil.writeFile(metadataPath, new BatfishObjectMapper().writeValueAsString(metadata));
  }
}
