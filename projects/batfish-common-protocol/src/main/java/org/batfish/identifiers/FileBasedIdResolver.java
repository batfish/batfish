package org.batfish.identifiers;

import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;
import org.batfish.storage.FileBasedStorageDirectoryProvider;

/**
 * Filesystem-based {@link IdResolver} that reads IDs from directories compatible with {@link
 * FileBasedStorageDirectoryProvider}. Intended to be used together with {@link
 * org.batfish.storage.FileBasedStorage}.
 */
public class FileBasedIdResolver implements IdResolver {

  private static final String ID_EXTENSION = ".id";

  private static final String RELPATH_ANALYSIS_IDS = "analysis_ids";

  private static final String RELPATH_ISSUE_SETTINGS_IDS = "analysis_ids";

  private static final String RELPATH_NETWORK_IDS = "network_ids";

  private static final String RELPATH_NETWORK_NODE_ROLES_ID = "network_node_roles.id";

  private static final String RELPATH_QUESTION_IDS = "question_ids";

  private static final String RELPATH_QUESTION_SETTINGS_IDS = "question_settings_ids";

  private static final String RELPATH_SNAPSHOT_IDS = "snapshot_ids";

  private static @Nonnull String hash(String input) {
    return Hashing.murmur3_128().hashString(input, StandardCharsets.UTF_8).toString();
  }

  private static @Nonnull Set<String> listResolvableNames(Path idsDir) {
    if (!Files.exists(idsDir)) {
      return ImmutableSet.of();
    }
    try (Stream<Path> files = CommonUtil.list(idsDir)) {
      return files
          .filter(path -> path.toString().endsWith(ID_EXTENSION))
          .map(Path::getFileName)
          .map(Path::toString)
          .map(
              nameWithExtension ->
                  nameWithExtension.substring(
                      0, nameWithExtension.length() - ID_EXTENSION.length()))
          .collect(ImmutableSet.toImmutableSet());
    }
  }

  protected final FileBasedStorageDirectoryProvider _d;

  public FileBasedIdResolver(Path storageBase) {
    _d = new FileBasedStorageDirectoryProvider(storageBase);
  }

  @Override
  public @Nonnull AnalysisId getAnalysisId(String analysis, NetworkId networkId) {
    if (!hasAnalysisId(analysis, networkId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent analysis %s", analysis));
    }
    return new AnalysisId(CommonUtil.readFile(getAnalysisIdPath(analysis, networkId)));
  }

  protected @Nonnull Path getAnalysisIdPath(String analysis, NetworkId networkId) {
    return getAnalysisIdsDir(networkId).resolve(String.format("%s%s", analysis, ID_EXTENSION));
  }

  protected @Nonnull Path getAnalysisIdsDir(NetworkId networkId) {
    return _d.getNetworkDir(networkId).resolve(RELPATH_ANALYSIS_IDS);
  }

  @Override
  public @Nonnull AnswerId getBaseAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      QuestionSettingsId questionSettingsId,
      NodeRolesId networkNodeRolesId,
      SnapshotId referenceSnapshotId,
      AnalysisId analysisId) {
    return new AnswerId(
        hash(
            ImmutableList.of(
                    networkId,
                    snapshotId,
                    questionId,
                    questionSettingsId,
                    networkNodeRolesId,
                    ofNullable(referenceSnapshotId),
                    ofNullable(analysisId))
                .toString()));
  }

  @Override
  public @Nonnull AnswerId getFinalAnswerId(
      AnswerId baseAnswerId, Set<IssueSettingsId> issueSettingsIds) {
    return new AnswerId(
        hash(
            ImmutableList.of(
                    baseAnswerId,
                    ImmutableSortedSet.copyOf(
                        Comparator.comparing(IssueSettingsId::getId), issueSettingsIds))
                .toString()));
  }

  @Override
  public @Nonnull IssueSettingsId getIssueSettingsId(String majorIssueType, NetworkId networkId) {
    if (!hasIssueSettingsId(majorIssueType, networkId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-configured majorIssueType %s", majorIssueType));
    }
    return new IssueSettingsId(
        CommonUtil.readFile(getIssueSettingsIdPath(majorIssueType, networkId)));
  }

  protected @Nonnull Path getIssueSettingsIdPath(String majorIssueType, NetworkId networkId) {
    return getIssueSettingsIdsDir(networkId)
        .resolve(String.format("%s%s", majorIssueType, ID_EXTENSION));
  }

  protected @Nonnull Path getIssueSettingsIdsDir(NetworkId networkId) {
    return _d.getNetworkDir(networkId).resolve(RELPATH_ISSUE_SETTINGS_IDS);
  }

  @Override
  public @Nonnull NetworkId getNetworkId(String network) {
    if (!hasNetworkId(network)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent network %s", network));
    }
    return new NetworkId(CommonUtil.readFile(getNetworkIdPath(network)));
  }

  protected @Nonnull Path getNetworkIdPath(String network) {
    return getNetworkIdsDir().resolve(String.format("%s%s", network, ID_EXTENSION));
  }

  protected @Nonnull Path getNetworkIdsDir() {
    return _d.getStorageBase().resolve(RELPATH_NETWORK_IDS);
  }

  @Override
  public NodeRolesId getNetworkNodeRolesId(NetworkId networkId) {
    if (!hasNetworkNodeRolesId(networkId)) {
      throw new IllegalArgumentException("No assigned node-roles ID");
    }
    return new NodeRolesId(CommonUtil.readFile(getNetworkNodeRolesIdPath(networkId)));
  }

  protected @Nonnull Path getNetworkNodeRolesIdPath(NetworkId networkId) {
    return _d.getNetworkDir(networkId).resolve(RELPATH_NETWORK_NODE_ROLES_ID);
  }

  @Override
  public @Nonnull QuestionId getQuestionId(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    if (!hasQuestionId(question, networkId, analysisId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent question '%s'", question));
    }
    return new QuestionId(CommonUtil.readFile(getQuestionIdPath(question, networkId, analysisId)));
  }

  protected @Nonnull Path getQuestionIdPath(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    return getQuestionIdsDir(networkId, analysisId)
        .resolve(String.format("%s%s", question, ID_EXTENSION));
  }

  protected @Nonnull Path getQuestionIdsDir(NetworkId networkId, @Nullable AnalysisId analysisId) {
    if (analysisId != null) {
      return _d.getAnalysisQuestionsDir(networkId, analysisId)
          .getParent()
          .resolve(RELPATH_QUESTION_IDS);
    } else {
      return _d.getNetworkDir(networkId).resolve(RELPATH_QUESTION_IDS);
    }
  }

  @Override
  public @Nonnull QuestionSettingsId getQuestionSettingsId(
      String questionClassId, NetworkId networkId) {
    if (!hasQuestionSettingsId(questionClassId, networkId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-configured questionClassId '%s'", questionClassId));
    }
    return new QuestionSettingsId(
        CommonUtil.readFile(getQuestionSettingsIdPath(questionClassId, networkId)));
  }

  protected @Nonnull Path getQuestionSettingsIdPath(String questionClassId, NetworkId networkId) {
    return getQuestionSettingsIdsDir(networkId)
        .resolve(String.format("%s%s", questionClassId, ID_EXTENSION));
  }

  protected @Nonnull Path getQuestionSettingsIdsDir(NetworkId networkId) {
    return _d.getNetworkDir(networkId).resolve(RELPATH_QUESTION_SETTINGS_IDS);
  }

  @Override
  public @Nonnull SnapshotId getSnapshotId(String snapshot, NetworkId networkId) {
    if (!hasSnapshotId(snapshot, networkId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent snapshot '%s'", snapshot));
    }
    return new SnapshotId(CommonUtil.readFile(getSnapshotIdPath(snapshot, networkId)));
  }

  protected @Nonnull Path getSnapshotIdPath(String snapshot, NetworkId networkId) {
    return getSnapshotIdsDir(networkId).resolve(String.format("%s%s", snapshot, ID_EXTENSION));
  }

  protected @Nonnull Path getSnapshotIdsDir(NetworkId networkId) {
    return _d.getNetworkDir(networkId).resolve(RELPATH_SNAPSHOT_IDS);
  }

  @Override
  public NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId) {
    return new NodeRolesId(hash(ImmutableList.of(networkId, snapshotId).toString()));
  }

  @Override
  public boolean hasAnalysisId(String analysis, NetworkId networkId) {
    return Files.exists(getAnalysisIdPath(analysis, networkId));
  }

  @Override
  public boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId) {
    return Files.exists(getIssueSettingsIdPath(majorIssueType, networkId));
  }

  @Override
  public boolean hasNetworkId(String network) {
    return Files.exists(getNetworkIdPath(network));
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    return Files.exists(getNetworkNodeRolesIdPath(networkId));
  }

  @Override
  public boolean hasQuestionId(String question, NetworkId networkId, AnalysisId analysisId) {
    return Files.exists(getQuestionIdPath(question, networkId, analysisId));
  }

  @Override
  public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
    return Files.exists(getQuestionSettingsIdPath(questionClassId, networkId));
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    return Files.exists(getSnapshotIdPath(snapshot, networkId));
  }

  @Override
  public @Nonnull Set<String> listAnalyses(NetworkId networkId) {
    return listResolvableNames(getAnalysisIdsDir(networkId));
  }

  @Override
  public @Nonnull Set<String> listNetworks() {
    return listResolvableNames(getNetworkIdsDir());
  }

  @Override
  public @Nonnull Set<String> listQuestions(NetworkId networkId, @Nullable AnalysisId analysisId) {
    return listResolvableNames(getQuestionIdsDir(networkId, analysisId));
  }

  @Override
  public @Nonnull Set<String> listSnapshots(NetworkId networkId) {
    return listResolvableNames(getSnapshotIdsDir(networkId));
  }
}
