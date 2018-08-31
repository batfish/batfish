package org.batfish.datamodel.answers;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents the configuration of a minor issue type */
public class MinorIssueConfig {
  private static final String PROP_MINOR_ISSUE = "minorIssue";
  private static final String PROP_SEVERITY = "severity";
  private static final String PROP_URL = "url";

  @Nonnull private final String _minorIssue;
  @Nullable private final Integer _severity;
  @Nullable private final String _url;

  @JsonCreator
  public MinorIssueConfig(
      @JsonProperty(PROP_MINOR_ISSUE) String minorIssue,
      @JsonProperty(PROP_SEVERITY) Integer severity,
      @JsonProperty(PROP_URL) String url) {
    checkArgument(minorIssue != null, "'minorIssue' cannot be null");
    _minorIssue = minorIssue;
    _severity = severity;
    _url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MinorIssueConfig)) {
      return false;
    }
    return Objects.equals(_minorIssue, ((MinorIssueConfig) o)._minorIssue)
        && Objects.equals(_severity, ((MinorIssueConfig) o)._severity)
        && Objects.equals(_url, ((MinorIssueConfig) o)._url);
  }

  @JsonProperty(PROP_MINOR_ISSUE)
  @Nonnull
  public String getMinor() {
    return _minorIssue;
  }

  @JsonProperty(PROP_SEVERITY)
  @Nullable
  public Integer getSeverity() {
    return _severity;
  }

  @JsonProperty(PROP_URL)
  @Nullable
  public String getUrl() {
    return _url;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_minorIssue, _severity, _url);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(MinorIssueConfig.class)
        .add(PROP_MINOR_ISSUE, _minorIssue)
        .add(PROP_SEVERITY, _severity)
        .add(PROP_URL, _url)
        .toString();
  }
}
