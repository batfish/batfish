package org.batfish.coordinator.id;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.Id;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
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

  private boolean deleteNameIdMapping(Class<? extends Id> type, String name, Id... ancestors) {
    try {
      return _s.deleteNameIdMapping(type, name, ancestors);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeId(Id id, String name, Id... ancestors) {
    try {
      _s.writeId(id, name, ancestors);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void assignAnalysis(String analysis, NetworkId networkId, AnalysisId analysisId) {
    writeId(analysisId, analysis, networkId);
  }

  @Override
  public void assignNetwork(String network, NetworkId networkId) {
    writeId(networkId, network);
  }

  @Override
  public void assignNetworkNodeRolesId(NetworkId networkId, NodeRolesId networkNodeRolesId) {
    writeId(networkNodeRolesId, NETWORK_NODE_ROLES, networkId);
  }

  @Override
  public void assignQuestion(
      String question,
      NetworkId networkId,
      QuestionId questionId,
      @Nullable AnalysisId analysisId) {
    Id[] ancestors = analysisId != null ? new Id[] {networkId, analysisId} : new Id[] {networkId};
    writeId(questionId, question, ancestors);
  }

  @Override
  public void assignSnapshot(String snapshot, NetworkId networkId, SnapshotId snapshotId) {
    writeId(snapshotId, snapshot, networkId);
  }

  @Override
  public boolean deleteAnalysis(String analysis, NetworkId networkId) {
    return deleteNameIdMapping(AnalysisId.class, analysis, networkId);
  }

  @Override
  public boolean deleteNetwork(String network) {
    return deleteNameIdMapping(NetworkId.class, network);
  }

  @Override
  public boolean deleteQuestion(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    Id[] ancestors = analysisId != null ? new Id[] {networkId, analysisId} : new Id[] {networkId};
    return deleteNameIdMapping(QuestionId.class, question, ancestors);
  }

  @Override
  public boolean deleteSnapshot(String snapshot, NetworkId networkId) {
    return deleteNameIdMapping(SnapshotId.class, snapshot, networkId);
  }

  @Override
  public @Nonnull AnalysisId generateAnalysisId() {
    return new AnalysisId(uuid());
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
  public @Nonnull SnapshotId generateSnapshotId() {
    return new SnapshotId(uuid());
  }
}
