package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class InitializationMetadata {

  public enum ProcessingStatus {
    UNINITIALIZED,
    PARSING,
    PARSED,
    PARSING_FAIL,
    DATAPLANING,
    DATAPLANED,
    DATAPLANING_FAIL
  }

  private static final String PROP_CURRENT_STATUS = "currentStatus";
  private static final String PROP_ERR_MESSAGE = "errMessage";
  private static final String PROP_STATUS_HISTORY = "statusHistory";

  private final ProcessingStatus _currentStatus;

  private final String _errMessage;

  private final List<String> _statusHistory;

  @JsonCreator
  private static @Nonnull InitializationMetadata create(
      @JsonProperty(PROP_CURRENT_STATUS) @Nullable ProcessingStatus status,
      @JsonProperty(PROP_ERR_MESSAGE) @Nullable String errMessage,
      @JsonProperty(PROP_STATUS_HISTORY) @Nullable List<String> statusHistory) {
    return new InitializationMetadata(
        requireNonNull(status),
        errMessage,
        ImmutableList.copyOf(firstNonNull(statusHistory, ImmutableList.of())));
  }

  public InitializationMetadata(
      ProcessingStatus status, @Nullable String errMessage, List<String> statusHistory) {
    _currentStatus = status;
    _errMessage = errMessage;
    _statusHistory = statusHistory;
  }

  @JsonProperty(PROP_CURRENT_STATUS)
  public ProcessingStatus getProcessingStatus() {
    return _currentStatus;
  }

  @JsonProperty(PROP_ERR_MESSAGE)
  public String getErrMessage() {
    return _errMessage;
  }

  @JsonProperty(PROP_STATUS_HISTORY)
  public List<String> getStatusHistory() {
    return _statusHistory;
  }

  public @Nonnull InitializationMetadata updateStatus(
      @Nonnull ProcessingStatus status, @Nullable String errMessage) {
    return new InitializationMetadata(
        status,
        errMessage != null ? errMessage : _errMessage,
        ImmutableList.<String>builder()
            .addAll(_statusHistory)
            .add("Status changed to " + status + " at " + Instant.now())
            .build());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InitializationMetadata)) {
      return false;
    }
    InitializationMetadata rhs = (InitializationMetadata) obj;
    return _currentStatus == rhs._currentStatus
        && Objects.equals(_errMessage, rhs._errMessage)
        && _statusHistory.equals(rhs._statusHistory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_currentStatus.ordinal(), _errMessage, _statusHistory);
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_CURRENT_STATUS, _currentStatus)
        .add(PROP_ERR_MESSAGE, _errMessage)
        .add(PROP_STATUS_HISTORY, _statusHistory)
        .toString();
  }
}
