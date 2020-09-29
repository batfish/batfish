package org.batfish.coordinator.id;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.IdResolver;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;

/** Reader and writer for persistent mappings between object names and their IDs. */
@ParametersAreNonnullByDefault
public interface IdManager extends IdResolver {

  /** Assign {@code analysis} to {@code analysisId} under {@code networkId}. */
  void assignAnalysis(String analysis, NetworkId networkId, AnalysisId analysisId);

  /** Assign {@code network} to {@code networkId}. */
  void assignNetwork(String network, NetworkId networkId);

  /** Assign network-wide node roles ID for {@code networkId} to {@code networkNodeRolesId}. */
  void assignNetworkNodeRolesId(NetworkId networkId, NodeRolesId networkNodeRolesId);

  /**
   * Assign {@code question} under {@code networkId}, {@code analysisId}) to {@code questionId}. If
   * {@code analysisId} is {@code null}, the mapping is for an ad-hoc question.
   */
  void assignQuestion(
      String question, NetworkId networkId, QuestionId questionId, @Nullable AnalysisId analysisId);

  /** Assign {@code snapshot} under {@code networkId} to {@code snapshotId}. */
  void assignSnapshot(String snapshot, NetworkId networkId, SnapshotId snapshotId);

  /**
   * Delete any mapping for {@code analysis} under {@code network}. Returns {@code true} iff a
   * mapping for the provided name was successfully deleted.
   */
  boolean deleteAnalysis(String analysis, NetworkId networkId);

  /**
   * Delete any mapping for {@code network}. Returns {@code true} iff a mapping for the provided
   * name was successfully deleted.
   */
  boolean deleteNetwork(String network);

  /**
   * Delete any mapping for {@code question} under {@code networkId}, {@code analysisId}. If {@code
   * analysisId} is {@code null}, the mapping to remove is for an ad-hoc question. Returns {@code
   * true} iff a mapping for the provided name was successfully deleted.
   */
  boolean deleteQuestion(String question, NetworkId networkId, @Nullable AnalysisId analysisId);

  /**
   * Delete any mapping for {@code snapshot} under {@code networkId}. Returns {@code true} iff a
   * mapping for the provided name was successfully deleted.
   */
  boolean deleteSnapshot(String snapshot, NetworkId networkId);

  /** Generate a new {@link AnalysisId} suitable for assignment */
  @Nonnull
  AnalysisId generateAnalysisId();

  /** Generate a new {@link NetworkId} suitable for assignment */
  @Nonnull
  NetworkId generateNetworkId();

  /** Generate a new network-level {@link NodeRolesId} suitable for assignment */
  @Nonnull
  NodeRolesId generateNetworkNodeRolesId();

  /** Generate a new {@link QuestionId} suitable for assignment */
  @Nonnull
  QuestionId generateQuestionId();

  /** Generate a new {@link SnapshotId} suitable for assignment */
  @Nonnull
  SnapshotId generateSnapshotId();
}
