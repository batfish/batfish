package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
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

    @Nonnull private String _major;
    @Nonnull private String _minor;

    @JsonCreator
    public Type(@JsonProperty(COL_MAJOR) String major, @JsonProperty(COL_MINOR) String minor) {
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

  @Nonnull private String _explanation;
  @Nonnull private int _severity;
  @Nonnull private Type _type;

  public Issue(
      @JsonProperty(COL_EXPLANATION) String explanation,
      @JsonProperty(COL_SEVERITY) Integer severity,
      @JsonProperty(COL_TYPE) Type type) {
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
