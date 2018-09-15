package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;

@ParametersAreNonnullByDefault
public class AnswerMetadata {

  public static class Builder {
    private Set<String> _majorIssueTypes;

    private Metrics _metrics;

    private AnswerStatus _status;

    private Builder() {
      _majorIssueTypes = ImmutableSet.of();
    }

    public @Nonnull AnswerMetadata build() {
      return new AnswerMetadata(
          firstNonNull(_majorIssueTypes, ImmutableSet.of()), _metrics, requireNonNull(_status));
    }

    public @Nonnull Builder setMajorIssueTypes(@Nonnull Set<String> majorIssueTypes) {
      _majorIssueTypes = ImmutableSet.copyOf(majorIssueTypes);
      return this;
    }

    public @Nonnull Builder setMetrics(@Nullable Metrics metrics) {
      _metrics = metrics;
      return this;
    }

    public @Nonnull Builder setStatus(@Nonnull AnswerStatus status) {
      _status = status;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull AnswerMetadata create(
      @JsonProperty(BfConsts.PROP_MAJOR_ISSUE_TYPES) @Nullable Set<String> majorIssueTypes,
      @JsonProperty(BfConsts.PROP_METRICS) @Nullable Metrics metrics,
      @JsonProperty(BfConsts.PROP_STATUS) @Nullable AnswerStatus status) {
    return new AnswerMetadata(
        firstNonNull(majorIssueTypes, ImmutableSet.of()), metrics, requireNonNull(status));
  }

  private final Set<String> _majorIssueTypes;

  private final Metrics _metrics;

  private final AnswerStatus _status;

  private AnswerMetadata(
      @Nonnull Set<String> majorIssueTypes,
      @Nullable Metrics metrics,
      @Nonnull AnswerStatus status) {
    _majorIssueTypes = majorIssueTypes;
    _metrics = metrics;
    _status = status;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AnswerMetadata)) {
      return false;
    }
    AnswerMetadata rhs = (AnswerMetadata) obj;
    return _majorIssueTypes.equals(rhs._majorIssueTypes)
        && Objects.equals(_metrics, rhs._metrics)
        && _status == rhs._status;
  }

  @JsonProperty(BfConsts.PROP_MAJOR_ISSUE_TYPES)
  public Set<String> getMajorIssueTypes() {
    return _majorIssueTypes;
  }

  @JsonProperty(BfConsts.PROP_METRICS)
  public @Nullable Metrics getMetrics() {
    return _metrics;
  }

  @JsonProperty(BfConsts.PROP_STATUS)
  public @Nonnull AnswerStatus getStatus() {
    return _status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_majorIssueTypes, _metrics, _status.ordinal());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(BfConsts.PROP_MAJOR_ISSUE_TYPES, !_majorIssueTypes.isEmpty() ? _majorIssueTypes : null)
        .add(BfConsts.PROP_METRICS, _metrics)
        .add(BfConsts.PROP_STATUS, _status)
        .toString();
  }
}
