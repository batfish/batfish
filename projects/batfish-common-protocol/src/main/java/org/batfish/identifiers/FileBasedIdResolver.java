package org.batfish.identifiers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.storage.StorageProvider;

/**
 * Storage-based {@link IdResolver} that reads IDs via {@link org.batfish.storage.StorageProvider}.
 */
public class FileBasedIdResolver implements IdResolver {

  public static final String NETWORK_NODE_ROLES = "network_node_roles";

  private static @Nonnull String hash(String input) {
    return Hashing.murmur3_128().hashString(input, UTF_8).toString();
  }

  private @Nonnull Set<String> listResolvableNames(List<Id> ancestors, IdType type) {
    try {
      return _s.listResolvableNames(ancestors, type);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private @Nonnull String readId(List<Id> ancestors, IdType type, String name) {
    try {
      return _s.readId(ancestors, type, name);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected final StorageProvider _s;

  public FileBasedIdResolver(StorageProvider s) {
    _s = s;
  }

  @Override
  public @Nonnull AnalysisId getAnalysisId(String analysis, NetworkId networkId) {
    if (!hasAnalysisId(analysis, networkId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent analysis %s", analysis));
    }
    return new AnalysisId(readId(ImmutableList.of(networkId), IdType.ANALYSIS, analysis));
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
        readId(ImmutableList.of(networkId), IdType.ISSUE_SETTINGS, majorIssueType));
  }

  @Override
  public @Nonnull NetworkId getNetworkId(String network) {
    if (!hasNetworkId(network)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent network %s", network));
    }
    return new NetworkId(readId(ImmutableList.of(), IdType.NETWORK, network));
  }

  @Override
  public NodeRolesId getNetworkNodeRolesId(NetworkId networkId) {
    if (!hasNetworkNodeRolesId(networkId)) {
      throw new IllegalArgumentException("No assigned node-roles ID");
    }
    return new NodeRolesId(
        readId(ImmutableList.of(networkId), IdType.NODE_ROLES, NETWORK_NODE_ROLES));
  }

  @Override
  public @Nonnull QuestionId getQuestionId(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    if (!hasQuestionId(question, networkId, analysisId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent question '%s'", question));
    }
    List<Id> ancestors =
        analysisId != null ? ImmutableList.of(networkId, analysisId) : ImmutableList.of(networkId);
    return new QuestionId(readId(ancestors, IdType.QUESTION, question));
  }

  @Override
  public @Nonnull QuestionSettingsId getQuestionSettingsId(
      String questionClassId, NetworkId networkId) {
    if (!hasQuestionSettingsId(questionClassId, networkId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-configured questionClassId '%s'", questionClassId));
    }
    return new QuestionSettingsId(
        readId(ImmutableList.of(networkId), IdType.QUESTION_SETTINGS, questionClassId));
  }

  @Override
  public @Nonnull SnapshotId getSnapshotId(String snapshot, NetworkId networkId) {
    if (!hasSnapshotId(snapshot, networkId)) {
      throw new IllegalArgumentException(
          String.format("No ID assigned to non-existent snapshot '%s'", snapshot));
    }
    return new SnapshotId(readId(ImmutableList.of(networkId), IdType.SNAPSHOT, snapshot));
  }

  @Override
  public NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId) {
    return new NodeRolesId(hash(ImmutableList.of(networkId, snapshotId).toString()));
  }

  @Override
  public boolean hasAnalysisId(String analysis, NetworkId networkId) {
    return _s.hasId(ImmutableList.of(networkId), IdType.ANALYSIS, analysis);
  }

  @Override
  public boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId) {
    return _s.hasId(ImmutableList.of(networkId), IdType.ISSUE_SETTINGS, majorIssueType);
  }

  @Override
  public boolean hasNetworkId(String network) {
    return _s.hasId(ImmutableList.of(), IdType.NETWORK, network);
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    return _s.hasId(ImmutableList.of(networkId), IdType.NODE_ROLES, NETWORK_NODE_ROLES);
  }

  @Override
  public boolean hasQuestionId(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    List<Id> ancestors =
        analysisId != null ? ImmutableList.of(networkId, analysisId) : ImmutableList.of(networkId);
    return _s.hasId(ancestors, IdType.QUESTION, question);
  }

  @Override
  public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
    return _s.hasId(ImmutableList.of(networkId), IdType.QUESTION_SETTINGS, questionClassId);
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    return _s.hasId(ImmutableList.of(networkId), IdType.SNAPSHOT, snapshot);
  }

  @Override
  public @Nonnull Set<String> listAnalyses(NetworkId networkId) {
    return listResolvableNames(ImmutableList.of(networkId), IdType.ANALYSIS);
  }

  @Override
  public @Nonnull Set<String> listNetworks() {
    return listResolvableNames(ImmutableList.of(), IdType.NETWORK);
  }

  @Override
  public @Nonnull Set<String> listQuestions(NetworkId networkId, @Nullable AnalysisId analysisId) {
    List<Id> ancestors =
        analysisId != null ? ImmutableList.of(networkId, analysisId) : ImmutableList.of(networkId);
    return listResolvableNames(ancestors, IdType.QUESTION);
  }

  @Override
  public @Nonnull Set<String> listSnapshots(NetworkId networkId) {
    return listResolvableNames(ImmutableList.of(networkId), IdType.SNAPSHOT);
  }
}
