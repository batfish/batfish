package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.MinorIssueConfig;

@ParametersAreNonnullByDefault
public class IssueConfigBean {
  /** The major type of the issue */
  public @Nonnull String major;

  /** The minor type of the issue */
  public @Nonnull String minor;

  /** The severity of the issue */
  public @Nullable Integer severity;

  /** The URL of the issue */
  public @Nullable String url;

  @JsonCreator
  private IssueConfigBean() {}

  public IssueConfigBean(String majorIssue, MinorIssueConfig minorIssueConfig) {
    major = majorIssue;
    minor = minorIssueConfig.getMinor();
    severity = minorIssueConfig.getSeverity();
    url = minorIssueConfig.getUrl();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IssueConfigBean)) {
      return false;
    }
    IssueConfigBean other = (IssueConfigBean) o;
    return major.equals(other.major)
        && minor.equals(other.minor)
        && Objects.equals(severity, other.severity)
        && Objects.equals(url, other.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, severity, url);
  }

  /** Creates {@link MinorIssueConfig} from this bean */
  public MinorIssueConfig toMinorIssueConfig() {
    return new MinorIssueConfig(minor, severity, url);
  }
}
