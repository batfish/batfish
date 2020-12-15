package org.batfish.identifiers;

import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Reader for persistent mappings between object names and their IDs. */
@ParametersAreNonnullByDefault
public interface IdResolver {

  /**
   * Retrieve the {@link AnalysisId} assigned to {@code analysis} under {@code networkId}. Returns
   * {@link Optional#empty} if none assigned.
   */
  @Nonnull
  Optional<AnalysisId> getAnalysisId(String analysis, NetworkId networkId);

  /** Retrieve the {@link AnswerId} corresponding to the provided input IDs. */
  @Nonnull
  AnswerId getAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      NodeRolesId networkNodeRolesId,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId);

  /**
   * Retrieve the {@link NetworkId} assigned to {@code network}. Returns {@link Optional#empty} if
   * none assigned.
   */
  @Nonnull
  Optional<NetworkId> getNetworkId(String network);

  /**
   * Retrieve the current {@link NodeRolesId} for {@code networkId}. Returns {@link Optional#empty}
   * if none assigned.
   */
  @Nonnull
  Optional<NodeRolesId> getNetworkNodeRolesId(NetworkId networkId);

  /**
   * Retrieve the {@link QuestionId} assigned to {@code question} under {@code networkId} and {@code
   * analysisId}. If {@code analysisId} is {@code null}, returns the mapping for an ad-hoc question.
   * Returns {@link Optional#empty} if none assigned.
   */
  @Nonnull
  Optional<QuestionId> getQuestionId(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId);

  /**
   * Retrieve the {@link SnapshotId} assigned to {@code snapshot} under {@code networkId}. Returns
   * {@link Optional#empty} if none assigned.
   */
  @Nonnull
  Optional<SnapshotId> getSnapshotId(String snapshot, NetworkId networkId);

  /** Retrieve the {@link NodeRolesId} corresponding to the provided input IDs. */
  @Nonnull
  NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId);

  /**
   * Return {@code true} iff some {@link AnalysisId} is assigned to {@code analysis} under {@code
   * networkId}.
   */
  boolean hasAnalysisId(String analysis, NetworkId networkId);

  /** Return {@code true} iff some {@link NetworkId} is assigned to {@code network}. */
  boolean hasNetworkId(String network);

  /** Return {@code true} iff some {@link NodeRolesId} is associated with {@code networkId} */
  boolean hasNetworkNodeRolesId(NetworkId networkId);

  /**
   * Return {@code true} iff some {@link QuestionId} is assigned to {@code question} under {@code
   * networkId}, {@code analysisId}. If {@code analysisId} is {@code null}, checks for an ad-hoc
   * question.
   */
  boolean hasQuestionId(String question, NetworkId networkId, @Nullable AnalysisId analysisId);

  /**
   * Return {@code true} iff some {@link SnapshotId} is assigned to {@code snapshot} under {@code
   * networkId}.
   */
  boolean hasSnapshotId(String snapshot, NetworkId networkId);

  /** Returns the names of analyses under {@code networkId} */
  @Nonnull
  Set<String> listAnalyses(NetworkId networkId);

  /** Returns the names of available networks */
  @Nonnull
  Set<String> listNetworks();

  /**
   * Returns the names of questions under {@code networkId} and {@code analysisId}. If {@code
   * analysisId} is {@code null}, returns the names of ad-hoc questions in the network.
   */
  Set<String> listQuestions(NetworkId networkId, @Nullable AnalysisId analysisId);

  /** Returns the names of snapshots under {@code networkId} */
  @Nonnull
  Set<String> listSnapshots(NetworkId networkId);
}
