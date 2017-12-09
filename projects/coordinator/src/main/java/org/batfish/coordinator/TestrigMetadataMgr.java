package org.batfish.coordinator;

// make sure that WorkQueueMgr is never called from this class directly or indirectly
// otherwise, we risk a deadlock, since WorkQueueMgr calls into this class
// at the time of writing this comment, this invariant was ensured by this class never calling out

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Path;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.TestrigMetadata;

public class TestrigMetadataMgr {

  public synchronized void initializeMetadata(TestrigMetadata metadata, Path testrigDir)
      throws JsonProcessingException {
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    Path metadataPath = testrigDir.resolve(BfConsts.RELPATH_METADATA_FILE);
    CommonUtil.writeFile(metadataPath, mapper.writeValueAsString(metadata));
  }
}
