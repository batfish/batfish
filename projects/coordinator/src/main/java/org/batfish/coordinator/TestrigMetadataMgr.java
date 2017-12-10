package org.batfish.coordinator;

// make sure that WorkQueueMgr is never called from this class directly or indirectly
// otherwise, we risk a deadlock, since WorkQueueMgr calls into this class
// currently, this invariant is ensured by never calling out anywhere

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Path;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.TestrigMetadata;

public class TestrigMetadataMgr {

  public static Path getMetadataPath(String container, String testrig) {
    return Main.getSettings()
        .getContainersLocation()
        .resolve(container)
        .resolve(BfConsts.RELPATH_TESTRIGS_DIR)
        .resolve(testrig)
        .resolve(BfConsts.RELPATH_METADATA_FILE);
  }

  public static synchronized void writeMetadata(TestrigMetadata metadata, Path metadataPath)
      throws JsonProcessingException {
    CommonUtil.writeFile(metadataPath, metadata.toJsonString());
  }

  public static synchronized void initializeEnvironment(
      String container, String testrig, String envName) throws IOException {
    Path metadataPath = getMetadataPath(container, testrig);
    TestrigMetadata metadata = readMetadata(metadataPath);
    metadata.initializeEnvironment(envName);
    writeMetadata(metadata, metadataPath);
  }

  // reading does not need to be synchronized
  public static TestrigMetadata readMetadata(Path metadataPath) throws IOException {
    String jsonStr = CommonUtil.readFile(metadataPath);
    return TestrigMetadata.fromJsonStr(jsonStr);
  }

  public static TestrigMetadata readMetadata(String container, String testrig) throws IOException {
    return readMetadata(getMetadataPath(container, testrig));
  }
}
