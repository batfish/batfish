package org.batfish.coordinator.id;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.Id;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.identifiers.StorageBasedIdResolver;
import org.batfish.storage.StorageProvider;

/**
 * Storage-based {@link IdManager} capable of writing mappings used by {@link
 * StorageBasedIdResolver}, from which it inherits. Intended to be used together with {@link
 * org.batfish.storage.StorageProvider}.
 */
@ParametersAreNonnullByDefault
public class StorageBasedIdManager extends StorageBasedIdResolver implements IdManager {

  private static @Nonnull String uuid() {
    return UUID.randomUUID().toString();
  }

  public StorageBasedIdManager(StorageProvider s) {
    super(s);
  }

  private void deleteNameIdMapping(List<Id> ancestors, Class<? extends Id> type, String name) {
    try {
      _s.deleteNameIdMapping(ancestors, type, name);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeId(List<Id> ancestors, Id id, String name) {
    try {
      _s.writeId(ancestors, id, name);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void assignAnalysis(String analysis, NetworkId networkId, AnalysisId analysisId) {
    writeId(ImmutableList.of(networkId), analysisId, analysis);
  }

  @Override
  public void assignIssueSettingsId(
      String majorIssueType, NetworkId networkId, IssueSettingsId issueSettingsId) {
    writeId(ImmutableList.of(networkId), issueSettingsId, majorIssueType);
  }

  @Override
  public void assignNetwork(String network, NetworkId networkId) {
    writeId(ImmutableList.of(), networkId, network);
  }

  @Override
  public void assignNetworkNodeRolesId(NetworkId networkId, NodeRolesId networkNodeRolesId) {
    writeId(ImmutableList.of(networkId), networkNodeRolesId, NETWORK_NODE_ROLES);
  }

  @Override
  public void assignQuestion(
      String question,
      NetworkId networkId,
      QuestionId questionId,
      @Nullable AnalysisId analysisId) {
    List<Id> ancestors =
        analysisId != null ? ImmutableList.of(networkId, analysisId) : ImmutableList.of(networkId);
    writeId(ancestors, questionId, question);
  }

  @Override
  public void assignQuestionSettingsId(
      String questionClassId, NetworkId networkId, QuestionSettingsId questionSettingsId) {
    writeId(ImmutableList.of(networkId), questionSettingsId, questionClassId);
  }

  @Override
  public void assignSnapshot(String snapshot, NetworkId networkId, SnapshotId snapshotId) {
    writeId(ImmutableList.of(networkId), snapshotId, snapshot);
  }

  @Override
  public void deleteAnalysis(String analysis, NetworkId networkId) {
    deleteNameIdMapping(ImmutableList.of(networkId), AnalysisId.class, analysis);
  }

  @Override
  public void deleteNetwork(String network) {
    deleteNameIdMapping(ImmutableList.of(), NetworkId.class, network);
  }

  @Override
  public void deleteQuestion(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    List<Id> ancestors =
        analysisId != null ? ImmutableList.of(networkId, analysisId) : ImmutableList.of(networkId);
    deleteNameIdMapping(ancestors, QuestionId.class, question);
  }

  @Override
  public void deleteSnapshot(String snapshot, NetworkId networkId) {
    deleteNameIdMapping(ImmutableList.of(networkId), SnapshotId.class, snapshot);
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
}
