package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IssueConfig {

  public static class MinorIssueConfig {
    private static final String PROP_MAJOR = "major";
    private static final String PROP_MINOR = "minor";
    private static final String PROP_SEVERITY = "severity";
    private static final String PROP_URL = "url";

    @Nonnull private String _major;
    @Nonnull private String _minor;
    @Nullable private Integer _severity;
    @Nullable private String _url;

    @JsonCreator
    public MinorIssueConfig(
        @JsonProperty(PROP_MAJOR) String major,
        @JsonProperty(PROP_MINOR) String minor,
        @JsonProperty(PROP_SEVERITY) Integer severity,
        @JsonProperty(PROP_URL) String url) {
      checkArgument(major != null, "'major for an Issue cannot be null");
      checkArgument(major != null, "'minor for an Issue cannot be null");
      _major = major;
      _minor = minor;
      _severity = severity;
      _url = url;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof MinorIssueConfig)) {
        return false;
      }
      return Objects.equals(_major, ((MinorIssueConfig) o)._major)
          && Objects.equals(_minor, ((MinorIssueConfig) o)._minor)
          && Objects.equals(_severity, ((MinorIssueConfig) o)._severity)
          && Objects.equals(_url, ((MinorIssueConfig) o)._url);
    }

    @JsonProperty(PROP_MAJOR)
    @Nonnull
    public String getMajor() {
      return _major;
    }

    @JsonProperty(PROP_MINOR)
    @Nonnull
    public String getMinor() {
      return _minor;
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
      return Objects.hash(_major, _minor, _severity, _url);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(MinorIssueConfig.class)
          .add(PROP_MAJOR, _major)
          .add(PROP_MINOR, _minor)
          .add(PROP_SEVERITY, _severity)
          .add(PROP_URL, _url)
          .toString();
    }
  }

  private static final String PROP_MINOR_ISSUE_CONFIGS = "minorIssueConfigs";

  @Nonnull private List<MinorIssueConfig> _minorIssueConfigs;

  @JsonCreator
  public IssueConfig(
      @JsonProperty(PROP_MINOR_ISSUE_CONFIGS) List<MinorIssueConfig> minorIssueConfigs) {
    _minorIssueConfigs = firstNonNull(minorIssueConfigs, ImmutableList.of());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IssueConfig)) {
      return false;
    }
    return Objects.equals(_minorIssueConfigs, ((IssueConfig) o)._minorIssueConfigs);
  }

  @JsonProperty(PROP_MINOR_ISSUE_CONFIGS)
  @Nonnull
  public List<MinorIssueConfig> getMinorIssueConfigs() {
    return _minorIssueConfigs;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_minorIssueConfigs);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(IssueConfig.class)
        .add(PROP_MINOR_ISSUE_CONFIGS, _minorIssueConfigs)
        .toString();
  }
}
