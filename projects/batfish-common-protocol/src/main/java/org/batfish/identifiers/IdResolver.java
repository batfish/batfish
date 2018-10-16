package org.batfish.identifiers;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Reader for persistent mappings between object names and their IDs. */
@ParametersAreNonnullByDefault
public interface IdResolver {

  /**
   * Retrieve the {@link AnalysisId} assigned to {@code analysis} under {@code networkId}.
   *
   * @throws IllegalArgumentException if none assigned
   */
  @Nonnull
  AnalysisId getAnalysisId(String analysis, NetworkId networkId);

  /** Retrieve the {@link AnswerId} corresponding to the provided input IDs. */
  @Nonnull
  AnswerId getBaseAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      QuestionSettingsId questionSettingsId,
      NodeRolesId networkNodeRolesId,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId);

  /** Retrieve the {@link AnswerId} of the final answer corresponding to the provided input IDs. */
  @Nonnull
  AnswerId getFinalAnswerId(AnswerId baseAnswerId, Set<IssueSettingsId> issueSettingsIds);

  /**
   * Retrieve the {@link IssueSettingsId} assigned to {@code majorIssueType} under {@code
   * networkId}.
   *
   * @throws IllegalArgumentException if none assigned
   */
  @Nonnull
  IssueSettingsId getIssueSettingsId(String majorIssueType, NetworkId networkId);

  /**
   * Retrieve the {@link NetworkId} assigned to {@code network}.
   *
   * @throws IllegalArgumentException if none assigned
   */
  @Nonnull
  NetworkId getNetworkId(String network);

  /** Retrieve the current {@link NodeRolesId} for {@code networkId}. */
  @Nonnull
  NodeRolesId getNetworkNodeRolesId(NetworkId networkId);

  /**
   * Retrieve the {@link QuestionId} assigned to {@code question} under {@code networkId} and {@code
   * analysisId}. If {@code analysisId} is {@code null}, returns the mapping for an ad-hoc question.
   *
   * @throws IllegalArgumentException if none assigned
   */
  @Nonnull
  QuestionId getQuestionId(String question, NetworkId networkId, @Nullable AnalysisId analysisId);

  /**
   * Retrieve the {@link QuestionSettingsId} assigned to {@code questionClassId} under {@code
   * networkId}.
   *
   * @throws IllegalArgumentException if none assigned
   */
  @Nonnull
  QuestionSettingsId getQuestionSettingsId(String questionClassId, NetworkId networkId);

  /**
   * Retrieve the {@link SnapshotId} assigned to {@code snapshot} under {@code networkId}.
   *
   * @throws IllegalArgumentException if none assigned
   */
  @Nonnull
  SnapshotId getSnapshotId(String snapshot, NetworkId networkId);

  /** Retrieve the {@link NodeRolesId} corresponding to the provided input IDs. */
  @Nonnull
  NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId);

  /**
   * Return {@code true} iff some {@link AnalysisId} is assigned to {@code analysis} under {@code
   * networkId}.
   */
  boolean hasAnalysisId(String analysis, NetworkId networkId);

  /**
   * Return {@code true} iff some {@link IssueSettingsId} is assigned to {@code majorIssueType}
   * under {@code networkId}.
   */
  boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId);

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
   * Return {@code true} iff some {@link QuestionSettingsId} is assigned to {@code questionClassId}
   * under {@code networkId}.
   */
  boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId);

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
