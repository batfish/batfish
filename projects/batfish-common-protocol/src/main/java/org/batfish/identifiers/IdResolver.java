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
   * @throws {@link IllegalArgumentException} if none assigned
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
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId);

  /** Retrieve the {@link FinalAnswerId} corresponding to the provided input IDs. */
  @Nonnull
  AnswerId getFinalAnswerId(
      AnswerId baseAnswerId,
      Set<IssueSettingsId> issueSettingsIds);

  /**
   * Retrieve the {@link IssueSettingsId} assigned to {@code majorIssueType} under {@code
   * networkId}.
   *
   * @throws {@link IllegalArgumentException} if none assigned
   */
  @Nonnull
  IssueSettingsId getIssueSettingsId(String majorIssueType, NetworkId networkId);

  /**
   * Retrieve the {@link NetworkId} assigned to {@code network}.
   *
   * @throws {@link IllegalArgumentException} if none assigned
   */
  @Nonnull
  NetworkId getNetworkId(String network);

  /**
   * Retrieve the {@link QuestionId} assigned to {@code question} under {@code networkId} and {@code
   * analysisId}. If {@code analysisId} is {@code null}, returns the mapping for an ad-hoc question.
   *
   * @throws {@link IllegalArgumentException} if none assigned
   */
  @Nonnull
  QuestionId getQuestionId(String question, NetworkId networkId, @Nullable AnalysisId analysisId);

  /**
   * Retrieve the {@link QuestionSettingsId} assigned to {@code questionClassId} under {@code
   * networkId}.
   *
   * @throws {@link IllegalArgumentException} if none assigned
   */
  @Nonnull
  QuestionSettingsId getQuestionSettingsId(String questionClassId, NetworkId networkId);

  /**
   * Retrieve the {@link SnapshotId} assigned to {@code snapshot} under {@code networkId}.
   *
   * @throws {@link IllegalArgumentException} if none assigned
   */
  @Nonnull
  SnapshotId getSnapshotId(String snapshot, NetworkId networkId);

  /**
   * Return {@code true} iff some {@link IssueSettingsId} is assigned to {@code majorIssueType}
   * under {@code networkId}.
   */
  boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId);

  /**
   * Return {@code true} iff some {@link QuestionSettingsId} is assigned to {@code questionClassId}
   * under {@code networkId}.
   */
  boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId);
}
