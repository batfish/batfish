package org.batfish.coordinator;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.time.Instant;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AnalysisMetadata;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.NetworkId;
import org.batfish.storage.StorageProvider;

public class AnalysisMetadataMgr {
  /** Analyses that can be requested for listing */
  public enum AnalysisType {
    /** Selects the suggested analyses */
    SUGGESTED,

    /** Selects the user analyses */
    USER,

    /** Selects both suggested and user analyses */
    ALL
  }

  public static Instant getAnalysisCreationTimeOrMin(NetworkId networkId, AnalysisId analysisId) {
    try {
      return readMetadata(networkId, analysisId).getCreationTimestamp();
    } catch (IOException e) {
      return Instant.MIN;
    }
  }

  private static StorageProvider storage() {
    return Main.getWorkMgr().getStorage();
  }

  /**
   * Returns suggested property of given analysis's metadata, or false if no metadata exists.
   *
   * @param networkId Container in which to find analysis
   * @param analysisId Analysis whose suggested property to return
   * @return suggested property of given analysis's metadata, or false if no metadata exists
   */
  public static boolean getAnalysisSuggestedOrFalse(NetworkId networkId, AnalysisId analysisId) {
    // If metadata file doesn't exist, assume analysis is not suggested
    if (!storage().hasAnalysisMetadata(networkId, analysisId)) {
      return false;
    }
    try {
      return readMetadata(networkId, analysisId).getSuggested();
    } catch (IOException e) {
      throw new BatfishException("Unable to read metadata for analysis '" + analysisId + "'", e);
    }
  }

  public static AnalysisMetadata readMetadata(NetworkId networkId, AnalysisId analysisId)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(
            storage().loadAnalysisMetadata(networkId, analysisId),
            new TypeReference<AnalysisMetadata>() {});
  }

  public static synchronized void writeMetadata(
      AnalysisMetadata metadata, NetworkId networkId, AnalysisId analysisId) throws IOException {
    storage().storeAnalysisMetadata(metadata, networkId, analysisId);
  }
}
