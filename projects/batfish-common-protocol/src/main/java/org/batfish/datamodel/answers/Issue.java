package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private static final String COL_MAJOR = "major";
    private static final String COL_MINOR = "minor";

    private String _major;
    private String _minor;

    @JsonCreator
    private static Type createType(
        @JsonProperty(COL_MAJOR) String major, @JsonProperty(COL_MINOR) String minor) {
      return new Type(firstNonNull(major, ""), firstNonNull(minor, ""));
    }

    public Type(String major, String minor) {
      _major = major;
      _minor = minor;
    }

    @JsonProperty(COL_MAJOR)
    public String getMajor() {
      return _major;
    }

    @JsonProperty(COL_MINOR)
    public String getMinor() {
      return _minor;
    }
  }

  private static final String COL_EXPLANATION = "explanation";
  private static final String COL_SEVERITY = "severity";
  private static final String COL_TYPE = "type";

  private String _explanation;
  private int _severity;
  private Type _type;

  @JsonCreator
  private static Issue getIssue(
      @Nullable @JsonProperty(COL_EXPLANATION) String explanation,
      @Nullable @JsonProperty(COL_SEVERITY) Integer severity,
      @Nullable @JsonProperty(COL_TYPE) Type type) {
    return new Issue(
        firstNonNull(explanation, ""),
        requireNonNull(severity, COL_SEVERITY + " cannot be null"),
        requireNonNull(type, COL_TYPE + " cannot be null"));
  }

  public Issue(String explanation, Integer severity, Type type) {
    _explanation = explanation;
    _severity = severity;
    _type = type;
  }

  @JsonProperty(COL_EXPLANATION)
  public String getExplanation() {
    return _explanation;
  }

  @JsonProperty(COL_SEVERITY)
  public int getSeverity() {
    return _severity;
  }

  @JsonProperty(COL_TYPE)
  public Type getType() {
    return _type;
  }
}
