package org.batfish.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;

/** Provides consistent directory structure for client classes that provide file-based storage. */
@ParametersAreNonnullByDefault
public class FileBasedStorageDirectoryProvider {

  private static final String RELPATH_BLOBS = "blobs";
  private static final String RELPATH_EXTENDED = "extended";
  private static final String RELPATH_NODE_ROLES_DIR = "node_roles";

  private final Path _baseDir;

  public FileBasedStorageDirectoryProvider(Path baseDir) {
    _baseDir = baseDir;
  }

  public @Nonnull Path getAdHocQuestionDir(NetworkId network, QuestionId question) {
    return getAdHocQuestionsDir(network).resolve(question.getId());
  }

  public @Nonnull Path getAdHocQuestionsDir(NetworkId networkId) {
    return getNetworkDir(networkId).resolve(BfConsts.RELPATH_QUESTIONS_DIR);
  }

  public @Nonnull Path getAnalysisQuestionDir(
      NetworkId network, QuestionId question, AnalysisId analysis) {
    return getAnalysisQuestionsDir(network, analysis).resolve(question.getId());
  }

  public @Nonnull Path getAnalysisQuestionsDir(NetworkId network, AnalysisId analysis) {
    return getNetworkAnalysisDir(network, analysis).resolve(BfConsts.RELPATH_QUESTIONS_DIR);
  }

  public @Nonnull Path getAnswerDir(AnswerId answerId) {
    return _baseDir.resolve(BfConsts.RELPATH_ANSWERS_DIR).resolve(answerId.getId());
  }

  public @Nonnull Path getMajorIssueConfigDir(NetworkId network, IssueSettingsId majorIssueType) {
    return getNetworkSettingsDir(network)
        .resolve(BfConsts.RELPATH_CONTAINER_SETTINGS_ISSUES)
        .resolve(majorIssueType + ".json");
  }

  public @Nonnull Path getNetworkAnalysisDir(NetworkId network, AnalysisId analysis) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_ANALYSES_DIR).resolve(analysis.getId());
  }

  public @Nonnull Path getNetworkDir(NetworkId network) {
    return _baseDir.resolve(network.getId());
  }

  public @Nonnull Path getNetworkSettingsDir(NetworkId network) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_CONTAINER_SETTINGS);
  }

  public Path getNodeRolesDir() {
    return _baseDir.resolve(RELPATH_NODE_ROLES_DIR);
  }

  public @Nonnull Path getQuestionDir(
      NetworkId network, QuestionId question, @Nullable AnalysisId analysis) {
    return analysis != null
        ? getAnalysisQuestionDir(network, question, analysis)
        : getAdHocQuestionDir(network, question);
  }

  public @Nonnull Path getSnapshotDir(NetworkId network, SnapshotId snapshot) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_SNAPSHOTS_DIR).resolve(snapshot.getId());
  }

  public @Nonnull Path getStorageBase() {
    return _baseDir;
  }

  public @Nonnull Path getVendorIndependentConfigDir(NetworkId network, SnapshotId snapshot) {
    return getSnapshotDir(network, snapshot)
        .resolve(
            Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR));
  }

  public @Nonnull Path getVendorSpecificConfigDir(NetworkId network, SnapshotId snapshot) {
    return getSnapshotDir(network, snapshot)
        .resolve(Paths.get(BfConsts.RELPATH_OUTPUT, BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR));
  }

  public Path getNetworkBlobsDir(NetworkId networkId) {
    return getNetworkDir(networkId).resolve(RELPATH_BLOBS);
  }

  public Path getNetworkObjectsDir(NetworkId networkId) {
    return getNetworkDir(networkId).resolve(RELPATH_EXTENDED);
  }

  public Path getSnapshotObjectsDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotDir(networkId, snapshotId).resolve(RELPATH_EXTENDED);
  }

  public Path getSnapshotInputObjectsDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotDir(networkId, snapshotId).resolve(BfConsts.RELPATH_INPUT);
  }

  public Path getSnapshotOutputDir(NetworkId networkId, SnapshotId snapshotId) {
    return getSnapshotDir(networkId, snapshotId).resolve(BfConsts.RELPATH_OUTPUT);
  }
}
