package org.batfish.coordinator.resources;

import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.coordinator.id.IdManager;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;

/**
 * Memory-based {@link IdManager} suitable for testing. Compatible with any {@link StorageProvider}.
 */
@ParametersAreNonnullByDefault
public class LocalIdManager implements IdManager {

  private static @Nonnull String hash(String input) {
    return Hashing.murmur3_128().hashString(input, StandardCharsets.UTF_8).toString();
  }

  private static @Nonnull <T> T illegalIfNull(@Nullable T t) {
    if (t == null) {
      throw new IllegalArgumentException("Invalid ID");
    }
    return t;
  }

  private static @Nonnull String uuid() {
    return UUID.randomUUID().toString();
  }

  private final Map<NetworkId, Map<String, AnalysisId>> _analysisIds;
  private final Map<NetworkId, Map<String, IssueSettingsId>> _issueSettingsIds;
  private final Map<String, NetworkId> _networkIds;
  private final Map<NetworkId, NodeRolesId> _networkNodeRolesIds;
  private final Map<NetworkId, Map<Optional<AnalysisId>, Map<String, QuestionId>>> _questionIds;
  private final Map<NetworkId, Map<String, QuestionSettingsId>> _questionSettingsIds;
  private final Map<NetworkId, Map<String, SnapshotId>> _snapshotIds;

  public LocalIdManager() {
    _analysisIds = new HashMap<>();
    _issueSettingsIds = new HashMap<>();
    _networkIds = new HashMap<>();
    _networkNodeRolesIds = new HashMap<>();
    _questionIds = new HashMap<>();
    _questionSettingsIds = new HashMap<>();
    _snapshotIds = new HashMap<>();
  }

  @Override
  public void assignAnalysis(String analysis, NetworkId networkId, AnalysisId analysisId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    _analysisIds.computeIfAbsent(networkId, n -> new HashMap<>()).put(analysis, analysisId);
  }

  @Override
  public void assignIssueSettingsId(
      String majorIssueType, NetworkId networkId, IssueSettingsId issueSettingsId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    _issueSettingsIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .put(majorIssueType, issueSettingsId);
  }

  @Override
  public void assignNetwork(String network, NetworkId networkId) {
    _networkIds.put(network, networkId);
  }

  @Override
  public void assignNetworkNodeRolesId(NetworkId networkId, NodeRolesId networkNodeRolesId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    _networkNodeRolesIds.put(networkId, networkNodeRolesId);
  }

  @Override
  public void assignQuestion(
      String question,
      NetworkId networkId,
      QuestionId questionId,
      @Nullable AnalysisId analysisId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    if (analysisId != null && !_analysisIds.get(networkId).values().contains(analysisId)) {
      throw new IllegalArgumentException(String.format("No analysis with ID: '%s'", analysisId));
    }
    _questionIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .computeIfAbsent(ofNullable(analysisId), a -> new HashMap<>())
        .put(question, questionId);
  }

  @Override
  public void assignQuestionSettingsId(
      String questionClassId, NetworkId networkId, QuestionSettingsId questionSettingsId) {
    _questionSettingsIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .put(questionClassId, questionSettingsId);
  }

  @Override
  public void assignSnapshot(String snapshot, NetworkId networkId, SnapshotId snapshotId) {
    _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).put(snapshot, snapshotId);
  }

  @Override
  public void deleteAnalysis(String analysis, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    _analysisIds.computeIfAbsent(networkId, n -> new HashMap<>()).remove(analysis);
  }

  @Override
  public void deleteNetwork(String network) {
    _networkIds.remove(network);
  }

  @Override
  public void deleteQuestion(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    if (analysisId != null && !_analysisIds.get(networkId).values().contains(analysisId)) {
      throw new IllegalArgumentException(String.format("No analysis with ID: '%s'", analysisId));
    }
    _questionIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .computeIfAbsent(ofNullable(analysisId), a -> new HashMap<>())
        .remove(question);
  }

  @Override
  public void deleteSnapshot(String snapshot, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).remove(snapshot);
  }

  @Override
  public @Nonnull AnalysisId generateAnalysisId() {
    return new AnalysisId(uuid());
  }

  @Override
  public @Nonnull IssueSettingsId generateIssueSettingsId() {
    return new IssueSettingsId(uuid());
  }

  @Override
  public @Nonnull NetworkId generateNetworkId() {
    return new NetworkId(uuid());
  }

  @Override
  public NodeRolesId generateNetworkNodeRolesId() {
    return new NodeRolesId(uuid());
  }

  @Override
  public @Nonnull QuestionId generateQuestionId() {
    return new QuestionId(uuid());
  }

  @Override
  public @Nonnull QuestionSettingsId generateQuestionSettingsId() {
    return new QuestionSettingsId(uuid());
  }

  @Override
  public @Nonnull SnapshotId generateSnapshotId() {
    return new SnapshotId(uuid());
  }

  @Override
  public AnalysisId getAnalysisId(String analysis, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return illegalIfNull(
        _analysisIds.computeIfAbsent(networkId, n -> new HashMap<>()).get(analysis));
  }

  @Override
  public AnswerId getBaseAnswerId(
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
  public AnswerId getFinalAnswerId(AnswerId baseAnswerId, Set<IssueSettingsId> issueSettingsIds) {
    return new AnswerId(
        hash(
            ImmutableList.of(
                    baseAnswerId,
                    ImmutableSortedSet.copyOf(
                        Comparator.comparing(IssueSettingsId::getId), issueSettingsIds))
                .toString()));
  }

  @Override
  public IssueSettingsId getIssueSettingsId(String majorIssueType, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return illegalIfNull(
        _issueSettingsIds.computeIfAbsent(networkId, n -> new HashMap<>()).get(majorIssueType));
  }

  @Override
  public NetworkId getNetworkId(String network) {
    return illegalIfNull(_networkIds.get(network));
  }

  @Override
  public NodeRolesId getNetworkNodeRolesId(NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return illegalIfNull(_networkNodeRolesIds.get(networkId));
  }

  @Override
  public QuestionId getQuestionId(String question, NetworkId networkId, AnalysisId analysisId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    if (analysisId != null && !_analysisIds.get(networkId).values().contains(analysisId)) {
      throw new IllegalArgumentException(String.format("No analysis with ID: '%s'", analysisId));
    }
    return illegalIfNull(
        _questionIds
            .computeIfAbsent(networkId, n -> new HashMap<>())
            .computeIfAbsent(ofNullable(analysisId), a -> new HashMap<>())
            .get(question));
  }

  @Override
  public QuestionSettingsId getQuestionSettingsId(String questionClassId, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return illegalIfNull(
        _questionSettingsIds.computeIfAbsent(networkId, n -> new HashMap<>()).get(questionClassId));
  }

  @Override
  public SnapshotId getSnapshotId(String snapshot, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return illegalIfNull(
        _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).get(snapshot));
  }

  @Override
  public NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId) {
    return new NodeRolesId(hash(ImmutableList.of(networkId, snapshotId).toString()));
  }

  @Override
  public boolean hasAnalysisId(String analysis, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _analysisIds.computeIfAbsent(networkId, n -> new HashMap<>()).containsKey(analysis);
  }

  @Override
  public boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _issueSettingsIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .containsKey(majorIssueType);
  }

  @Override
  public boolean hasNetworkId(String network) {
    return _networkIds.containsKey(network);
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _networkNodeRolesIds.containsKey(networkId);
  }

  @Override
  public boolean hasQuestionId(String question, NetworkId networkId, AnalysisId analysisId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    if (analysisId != null && !_analysisIds.get(networkId).values().contains(analysisId)) {
      throw new IllegalArgumentException(String.format("No analysis with ID: '%s'", analysisId));
    }
    return _questionIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .computeIfAbsent(ofNullable(analysisId), a -> new HashMap<>())
        .containsKey(question);
  }

  @Override
  public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _questionSettingsIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .containsKey(questionClassId);
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).containsKey(snapshot);
  }

  @Override
  public Set<String> listAnalyses(NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _analysisIds.computeIfAbsent(networkId, n -> new HashMap<>()).keySet();
  }

  @Override
  public Set<String> listNetworks() {
    return _networkIds.keySet();
  }

  @Override
  public Set<String> listQuestions(NetworkId networkId, @Nullable AnalysisId analysisId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    if (analysisId != null && !_analysisIds.get(networkId).values().contains(analysisId)) {
      throw new IllegalArgumentException(String.format("No analysis with ID: '%s'", analysisId));
    }
    return _questionIds
        .computeIfAbsent(networkId, n -> new HashMap<>())
        .computeIfAbsent(ofNullable(analysisId), a -> new HashMap<>())
        .keySet();
  }

  @Override
  public Set<String> listSnapshots(NetworkId networkId) {
    if (!_networkIds.values().contains(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).keySet();
  }
}
