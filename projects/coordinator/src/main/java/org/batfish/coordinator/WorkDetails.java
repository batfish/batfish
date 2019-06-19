package org.batfish.coordinator;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;

/** Resolved details about a {@link org.batfish.common.WorkItem} */
@ParametersAreNonnullByDefault
public final class WorkDetails {

  public static final class Builder {

    private @Nullable AnalysisId _analysisId;
    private boolean _isDifferential;
    private @Nullable NetworkId _networkId;
    private @Nullable QuestionId _questionId;
    private @Nullable SnapshotId _referenceSnapshotId;
    private @Nullable SnapshotId _snapshotId;
    private @Nullable WorkType _workType;

    private Builder() {}

    public @Nonnull WorkDetails build() {
      checkState(_networkId != null, "Missing networkId");
      checkState(_snapshotId != null, "Missing snapshotId");
      checkState(_workType != null, "Missing workType");
      return new WorkDetails(
          _networkId,
          _snapshotId,
          _isDifferential,
          _workType,
          _referenceSnapshotId,
          _analysisId,
          _questionId);
    }

    public @Nonnull Builder setAnalysisId(@Nullable AnalysisId analysisId) {
      _analysisId = analysisId;
      return this;
    }

    public @Nonnull Builder setIsDifferential(boolean isDifferential) {
      _isDifferential = isDifferential;
      return this;
    }

    public @Nonnull Builder setNetworkId(NetworkId networkId) {
      _networkId = networkId;
      return this;
    }

    public @Nonnull Builder setQuestionId(@Nullable QuestionId questionId) {
      _questionId = questionId;
      return this;
    }

    public @Nonnull Builder setReferenceSnapshotId(@Nullable SnapshotId referenceSnapshotId) {
      _referenceSnapshotId = referenceSnapshotId;
      return this;
    }

    public @Nonnull Builder setSnapshotId(SnapshotId snapshotId) {
      _snapshotId = snapshotId;
      return this;
    }

    public @Nonnull Builder setWorkType(WorkType workType) {
      _workType = workType;
      return this;
    }
  }

  public enum WorkType {
    DATAPLANE_DEPENDENT_ANSWERING,
    DATAPLANING,
    INDEPENDENT_ANSWERING, // answering includes analyzing
    PARSING,
    PARSING_DEPENDENT_ANSWERING,
    UNKNOWN
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nullable AnalysisId _analysisId;
  private final boolean _isDifferential;
  private final @Nonnull NetworkId _networkId;
  private final @Nullable QuestionId _questionId;
  private final @Nullable SnapshotId _referenceSnapshotId;
  private final @Nonnull SnapshotId _snapshotId;
  private final @Nonnull WorkType _workType;

  public WorkDetails(
      NetworkId networkId,
      SnapshotId snapshotId,
      boolean isDifferential,
      WorkType workType,
      @Nullable SnapshotId referenceSnapshotId,
      @Nullable AnalysisId analysisId,
      @Nullable QuestionId questionId) {
    _networkId = networkId;
    _snapshotId = snapshotId;
    _isDifferential = isDifferential;
    _workType = workType;
    _referenceSnapshotId = referenceSnapshotId;
    _analysisId = analysisId;
    _questionId = questionId;
  }

  public @Nullable AnalysisId getAnalysisId() {
    return _analysisId;
  }

  public boolean isDifferential() {
    return _isDifferential;
  }

  public @Nonnull NetworkId getNetworkId() {
    return _networkId;
  }

  public @Nullable QuestionId getQuestionId() {
    return _questionId;
  }

  public @Nullable SnapshotId getReferenceSnapshotId() {
    return _referenceSnapshotId;
  }

  public @Nonnull SnapshotId getSnapshotId() {
    return _snapshotId;
  }

  public @Nonnull WorkType getWorkType() {
    return _workType;
  }

  public boolean isOverlappingInput(WorkDetails o) {
    return _snapshotId.equals(o._snapshotId)
        || _snapshotId.equals(o._referenceSnapshotId)
        || _isDifferential
            && (_referenceSnapshotId.equals(o._snapshotId)
                || _referenceSnapshotId.equals(o._referenceSnapshotId));
  }
}
