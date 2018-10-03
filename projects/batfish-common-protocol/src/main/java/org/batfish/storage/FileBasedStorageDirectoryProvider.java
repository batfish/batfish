package org.batfish.storage;

import java.nio.file.Path;
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

  public @Nonnull Path getCompressedConfigDir(NetworkId network, SnapshotId snapshot) {
    return getSnapshotDir(network, snapshot).resolve(BfConsts.RELPATH_COMPRESSED_CONFIG_DIR);
  }

  public @Nonnull Path getDeltaAnswerDir(
      NetworkId network,
      SnapshotId snapshot,
      QuestionId question,
      SnapshotId referenceSnapshot,
      @Nullable AnalysisId analysis) {
    Path snapshotDir = getSnapshotDir(network, snapshot);
    return analysis != null
        ? snapshotDir
            .resolve(BfConsts.RELPATH_ANALYSES_DIR)
            .resolve(analysis.getId())
            .resolve(BfConsts.RELPATH_QUESTIONS_DIR)
            .resolve(question.getId())
            .resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR)
            .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
            .resolve(BfConsts.RELPATH_DELTA)
            .resolve(referenceSnapshot.getId())
            .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
        : snapshotDir
            .resolve(BfConsts.RELPATH_ANSWERS_DIR)
            .resolve(question.getId())
            .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
            .resolve(BfConsts.RELPATH_DIFF_DIR)
            .resolve(referenceSnapshot.getId())
            .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME);
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

  public @Nonnull Path getQuestionDir(
      NetworkId network, QuestionId question, @Nullable AnalysisId analysis) {
    return analysis != null
        ? getAnalysisQuestionDir(network, question, analysis)
        : getAdHocQuestionDir(network, question);
  }

  public @Nonnull Path getSnapshotDir(NetworkId network, SnapshotId snapshot) {
    return getNetworkDir(network).resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve(snapshot.getId());
  }

  public @Nonnull Path getStandardAnswerDir(
      NetworkId network, SnapshotId snapshot, QuestionId question, @Nullable AnalysisId analysis) {
    Path snapshotDir = getSnapshotDir(network, snapshot);
    return analysis != null
        ? snapshotDir
            .resolve(BfConsts.RELPATH_ANALYSES_DIR)
            .resolve(analysis.getId())
            .resolve(BfConsts.RELPATH_QUESTIONS_DIR)
            .resolve(question.getId())
            .resolve(BfConsts.RELPATH_ENVIRONMENTS_DIR)
            .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
        : snapshotDir
            .resolve(BfConsts.RELPATH_ANSWERS_DIR)
            .resolve(question.getId())
            .resolve(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
            .resolve(BfConsts.RELPATH_STANDARD_DIR);
  }

  public @Nonnull Path getStorageBase() {
    return _baseDir;
  }

  public @Nonnull Path getVendorIndependentConfigDir(NetworkId network, SnapshotId snapshot) {
    return getSnapshotDir(network, snapshot)
        .resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR);
  }

  public @Nonnull Path getVendorSpecificConfigDir(NetworkId network, SnapshotId snapshot) {
    return getSnapshotDir(network, snapshot).resolve(BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR);
  }
}
