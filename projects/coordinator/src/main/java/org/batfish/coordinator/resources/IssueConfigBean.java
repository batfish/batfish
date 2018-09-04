package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.MinorIssueConfig;

@ParametersAreNonnullByDefault
public class IssueConfigBean {
  /** The major type of the issue */
  public String major;

  /** The minor type of the issue */
  public String minor;

  /** The severity of the issue */
  public int severity;

  /** The URL of the issue */
  public String url;

  @JsonCreator
  private IssueConfigBean() {}

  public IssueConfigBean(String majorIssue, MinorIssueConfig minorIssueConfig) {
    major = majorIssue;
    minor = minorIssueConfig.getMinor();
    severity = minorIssueConfig.getSeverity();
    url = minorIssueConfig.getUrl();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IssueConfigBean)) {
      return false;
    }
    IssueConfigBean other = (IssueConfigBean) o;
    return Objects.equals(major, other.major)
        && Objects.equals(minor, other.minor)
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
