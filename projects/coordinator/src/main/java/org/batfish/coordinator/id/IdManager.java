package org.batfish.coordinator.id;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.IdResolver;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;

/** Reader and writer for persistent mappings between object names and their IDs. */
@ParametersAreNonnullByDefault
public interface IdManager extends IdResolver {

  /** Assign {@code network} to {@code networkId}. */
  void assignNetwork(String network, NetworkId networkId);

  /**
   * Assign {@code question} under {@code networkId}, {@code analysisId}) to {@code questionId}. If
   * {@code analysisId} is {@code null}, the mapping is for an ad-hoc question.
   */
  void assignQuestion(
      String question, NetworkId networkId, QuestionId questionId, @Nullable AnalysisId analyisId);

  /** Assign {@code questionClassId} under {@code networkId} to {@code questionSettingsId}). */
  void assignQuestionSettingsId(
      String questionClassId, NetworkId networkId, QuestionSettingsId questionSettingsId);

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
}
