package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Issue {

  public static int SEVERITY_ERROR_0 = 100; // clearly an error right here (on this row)
  public static int SEVERITY_ERROR_1 = 90; // an error but maybe someplace else
  public static int SEVERITY_ERROR_2 = 80;
  public static int SEVERITY_WARN_0 = 70; // very likely an error
  public static int SEVERITY_WARN_1 = 60;
  public static int SEVERITY_WARN_2 = 50; // violation of a best practice
  public static int SEVERITY_FYI_0 = 40;
  public static int SEVERITY_FYI_1 = 30;
  public static int SEVERITY_FYI_2 = 20; // something to be aware of, not likely a problem
  public static int SEVERITY_OK = 0;

  /**
   * Analysis classes that flag an issue can label it with this class, using a major and minor type.
   * For example, the major type could be the analysis type and the minor type could be sub-type
   * within it. This labeling will help sift through the flagged issues to focus on or ignore issues
   * of certain (sub) type.
   */
  public static class Type {
    private static final String PROP_MAJOR = "major";
    private static final String PROP_MINOR = "minor";

    private @Nonnull String _major;
    private @Nonnull String _minor;

    @JsonCreator
    private static Type createType(
        @JsonProperty(PROP_MAJOR) String major, @JsonProperty(PROP_MINOR) String minor) {
      return new Type(firstNonNull(major, ""), firstNonNull(minor, ""));
    }

    public Type(String major, String minor) {
      _major = major;
      _minor = minor;
    }

    @JsonProperty(PROP_MAJOR)
    public @Nonnull String getMajor() {
      return _major;
    }

    @JsonProperty(PROP_MINOR)
    public @Nonnull String getMinor() {
      return _minor;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Type)) {
        return false;
      }
      Type rhs = (Type) obj;
      return Objects.equals(_major, rhs._major) && Objects.equals(_minor, rhs._minor);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_major, _minor);
    }

    @Override
    public String toString() {
      return toStringHelper(getClass()).add(PROP_MAJOR, _major).add(PROP_MINOR, _minor).toString();
    }
  }

  private static final String PROP_EXPLANATION = "explanation";
  private static final String PROP_SEVERITY = "severity";
  private static final String PROP_TYPE = "type";
  private static final String PROP_URL = "url";

  private @Nullable String _explanation;
  private @Nonnull int _severity;
  private @Nonnull Type _type;
  private @Nullable String _url;

  @JsonCreator
  private static Issue getIssue(
      @JsonProperty(PROP_EXPLANATION) String explanation,
      @JsonProperty(PROP_SEVERITY) Integer severity,
      @JsonProperty(PROP_TYPE) Type type,
      @JsonProperty(PROP_URL) String url) {
    return new Issue(
        firstNonNull(explanation, ""),
        requireNonNull(severity, PROP_SEVERITY + " cannot be null"),
        requireNonNull(type, PROP_TYPE + " cannot be null"),
        url);
  }

  public Issue(String explanation, Integer severity, Type type) {
    this(explanation, severity, type, null);
  }

  public Issue(String explanation, Integer severity, Type type, String url) {
    _explanation = explanation;
    _severity = severity;
    _type = type;
    _url = url;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Issue)) {
      return false;
    }
    Issue rhs = (Issue) obj;
    return Objects.equals(_explanation, rhs._explanation)
        && Objects.equals(_severity, rhs._severity)
        && Objects.equals(_type, rhs._type)
        && Objects.equals(_url, rhs._url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_explanation, _severity, _type, _url);
  }

  @JsonProperty(PROP_EXPLANATION)
  public @Nullable String getExplanation() {
    return _explanation;
  }

  @JsonProperty(PROP_SEVERITY)
  public int getSeverity() {
    return _severity;
  }

  @JsonProperty(PROP_TYPE)
  public @Nonnull Type getType() {
    return _type;
  }

  @JsonProperty(PROP_URL)
  public @Nullable String getUrl() {
    return _url;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_EXPLANATION, _explanation)
        .add(PROP_SEVERITY, _severity)
        .add(PROP_TYPE, _type)
        .add(PROP_URL, _url)
        .toString();
  }
}
