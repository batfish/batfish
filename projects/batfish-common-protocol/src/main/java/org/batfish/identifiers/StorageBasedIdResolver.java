package org.batfish.identifiers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.storage.StorageProvider;

/**
 * Storage-based {@link IdResolver} that reads IDs via {@link org.batfish.storage.StorageProvider}.
 */
public class StorageBasedIdResolver implements IdResolver {

  protected static final String NETWORK_NODE_ROLES = "network_node_roles";

  private static @Nonnull String hash(String input) {
    return Hashing.murmur3_128().hashString(input, UTF_8).toString();
  }

  private @Nonnull Set<String> listResolvableNames(Class<? extends Id> idType, Id... ancestors) {
    try {
      return _s.listResolvableNames(idType, ancestors);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private @Nonnull Optional<String> readId(
      Class<? extends Id> idType, String name, Id... ancestors) {
    try {
      return _s.readId(idType, name, ancestors);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected final StorageProvider _s;

  public StorageBasedIdResolver(StorageProvider s) {
    _s = s;
  }

  @Override
  public @Nonnull Optional<AnalysisId> getAnalysisId(String analysis, NetworkId networkId) {
    return readId(AnalysisId.class, analysis, networkId).map(AnalysisId::new);
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
  public @Nonnull Optional<IssueSettingsId> getIssueSettingsId(
      String majorIssueType, NetworkId networkId) {
    return readId(IssueSettingsId.class, majorIssueType, networkId).map(IssueSettingsId::new);
  }

  @Override
  public @Nonnull Optional<NetworkId> getNetworkId(String network) {
    return readId(NetworkId.class, network).map(NetworkId::new);
  }

  @Override
  public Optional<NodeRolesId> getNetworkNodeRolesId(NetworkId networkId) {
    return readId(NodeRolesId.class, NETWORK_NODE_ROLES, networkId).map(NodeRolesId::new);
  }

  @Override
  public @Nonnull Optional<QuestionId> getQuestionId(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    Id[] ancestors = analysisId != null ? new Id[] {networkId, analysisId} : new Id[] {networkId};
    return readId(QuestionId.class, question, ancestors).map(QuestionId::new);
  }

  @Override
  public @Nonnull Optional<QuestionSettingsId> getQuestionSettingsId(
      String questionClassId, NetworkId networkId) {
    return readId(QuestionSettingsId.class, questionClassId, networkId)
        .map(QuestionSettingsId::new);
  }

  @Override
  public @Nonnull Optional<SnapshotId> getSnapshotId(String snapshot, NetworkId networkId) {
    return readId(SnapshotId.class, snapshot, networkId).map(SnapshotId::new);
  }

  @Override
  public NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId) {
    return new NodeRolesId(hash(ImmutableList.of(networkId, snapshotId).toString()));
  }

  @Override
  public boolean hasAnalysisId(String analysis, NetworkId networkId) {
    return _s.hasId(AnalysisId.class, analysis, networkId);
  }

  @Override
  public boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId) {
    return _s.hasId(IssueSettingsId.class, majorIssueType, networkId);
  }

  @Override
  public boolean hasNetworkId(String network) {
    return _s.hasId(NetworkId.class, network);
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    return _s.hasId(NodeRolesId.class, NETWORK_NODE_ROLES, networkId);
  }

  @Override
  public boolean hasQuestionId(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    Id[] ancestors = analysisId != null ? new Id[] {networkId, analysisId} : new Id[] {networkId};
    return _s.hasId(QuestionId.class, question, ancestors);
  }

  @Override
  public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
    return _s.hasId(QuestionSettingsId.class, questionClassId, networkId);
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    return _s.hasId(SnapshotId.class, snapshot, networkId);
  }

  @Override
  public @Nonnull Set<String> listAnalyses(NetworkId networkId) {
    return listResolvableNames(AnalysisId.class, networkId);
  }

  @Override
  public @Nonnull Set<String> listNetworks() {
    return listResolvableNames(NetworkId.class);
  }

  @Override
  public @Nonnull Set<String> listQuestions(NetworkId networkId, @Nullable AnalysisId analysisId) {
    Id[] ancestors = analysisId != null ? new Id[] {networkId, analysisId} : new Id[] {networkId};
    return listResolvableNames(QuestionId.class, ancestors);
  }

  @Override
  public @Nonnull Set<String> listSnapshots(NetworkId networkId) {
    return listResolvableNames(SnapshotId.class, networkId);
  }
}
