package org.batfish.coordinator.id_manager;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.BaseAnswerId;
import org.batfish.identifiers.FinalAnswerId;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;

/** Reader and write for persistent mappings between object names and their IDs. */
public interface IdManager {

  /** Assign {@code network} to {@code networkId}. */
  void assignNetwork(String network, NetworkId networkId);

  /**
   * Assign {@code question} under ({@code networkId}, {@code analysisId}) to {@code questionId}. If
   * {@code analysisId} is {@code null}, the mapping is for an ad-hoc question.
   */
  void assignQuestion(
      String question, NetworkId networkId, QuestionId questionId, @Nullable AnalysisId analyisId);

  /** Assign {@code snapshot} under {@code networkId} to {@code snapshotId}. */
  void assignSnapshot(String snapshot, NetworkId networkId, SnapshotId snapshotId);

  /** Delete any mapping for {@code network} */
  void deleteNetwork(String network);

  /** Delete any mapping for {@code question} under {@code networkId} */
  void deleteQuestion(String question, NetworkId networkId);

  /** Delete any mapping for {@code snapshot} under {@code networkId} */
  void deleteSnapshot(String snapshot, NetworkId networkId);

  /** Generate a new {@link NetworkId} suitable for assignment */
  @Nonnull
  NetworkId generateNetworkId();

  /** Generate a new {@link QuestionId} suitable for assignment */
  @Nonnull
  QuestionId generateQuestionId();

  /** Generate a new {@link QuestionSettingsId} suitable for assignment */
  @Nonnull
  QuestionSettingsId generateQuestionSettingsId();

  /** Generate a new {@link SnapshotId} suitable for assignment */
  @Nonnull
  SnapshotId generateSnapshotId();

  /** Retrieve the {@link BaseAnswerId} corresponding to the provided input IDs. */
  @Nonnull
  BaseAnswerId getBaseAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      QuestionSettingsId questionSettingsId,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId);

  /** Retrieve the {@link FinalAnswerId} corresponding to the provided input IDs. */
  @Nonnull
  FinalAnswerId getFinalAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      QuestionSettingsId questionSettingsId,
      Set<IssueSettingsId> issueSettingsIds,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId);

  /**
   * Retrieve the {@link NetworkId} assigned to {@code network}. Packs {@code network} in the
   * returned {@link NetworkId} for context. Returns {@code null} if absent.
   */
  @Nullable
  NetworkId getNetworkId(String network);

  /**
   * Retrieve the {@link QuestionId} assigned to {@code question} under {@code networkId} and {@code
   * analysisId}. If {@code analysisId} is {@code null}, returns the mapping for an ad-hoc question.
   * Packs {@code question} in the returned {@link QuestionId} for context. Returns {@code null} if
   * absent.
   */
  @Nullable
  QuestionId getQuestionId(String question, NetworkId networkId, @Nullable AnalysisId analysisId);

  /**
   * Retrieve the {@link QuestionSettingsId} assigned to {@code questionClassId} under {@code
   * networkId}. Packs {@code questionClassId} in the returned {@link QuestionSettingsId} for
   * context. Returns {@code null} if absent.
   */
  @Nullable
  QuestionSettingsId getQuestionSettingsId(String questionClassId, NetworkId networkId);

  /**
   * Retrieve the {@link SnapshotId} assigned to {@code snapshot} under {@code networkId}. Packs
   * {@code snapshot} in the returned {@link SnapshotId} for context. Returns {@code null} if
   * absent.
   */
  @Nullable
  SnapshotId getSnapshotId(String snapshot, NetworkId networkId);
}
