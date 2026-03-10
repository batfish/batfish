package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

public class Line extends ComparableStructure<String> {
  private static final String PROP_EXEC_TIMEOUT_MINUTES = "execTimeoutMinutes";
  private static final String PROP_EXEC_TIMEOUT_SECONDS = "execTimeoutSeconds";
  private static final String PROP_INPUT_ACCESS_LIST = "inputAccessList";
  private static final String PROP_INPUT_IPV6_ACCESS_LIST = "inputIpv6AccessList";
  private static final String PROP_LINE_TYPE = "lineType";
  private static final String PROP_LOGIN_AUTHENTICATION = "loginAuthentication";
  private static final String PROP_OUTPUT_ACCESS_LIST = "outputAccessList";
  private static final String PROP_OUTPUT_IPV6_ACCESS_LIST = "outputIpv6AccessList";
  private static final String PROP_TRANSPORT_INPUT = "transportInput";
  private static final String PROP_TRANSPORT_OUTPUT = "transportOutput";
  private static final String PROP_TRANSPORT_PREFERRED = "transportPreferred";

  private AaaAuthenticationLoginList _aaaAuthenticationLoginList;

  private int _execTimeoutMinutes;

  private int _execTimeoutSeconds;

  private String _inputAccessList;

  private String _inputIpv6AccessList;

  private String _loginAuthentication;

  private String _outputAccessList;

  private String _outputIpv6AccessList;

  private SortedSet<String> _transportInput;

  private SortedSet<String> _transportOutput;

  private SortedSet<String> _transportPreferred;

  @JsonCreator
  private static Line jsonCreator(@JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(name != null, "%s must be provided", PROP_NAME);
    return new Line(name);
  }

  public Line(@Nonnull String name) {
    super(name);
    _transportInput = new TreeSet<>();
    _transportOutput = new TreeSet<>();
    _transportPreferred = new TreeSet<>();
  }

  public AaaAuthenticationLoginList getAaaAuthenticationLoginList() {
    return _aaaAuthenticationLoginList;
  }

  @JsonProperty(PROP_EXEC_TIMEOUT_MINUTES)
  public Integer getExecTimeoutMinutes() {
    return _execTimeoutMinutes;
  }

  @JsonProperty(PROP_EXEC_TIMEOUT_SECONDS)
  public Integer getExecTimeoutSeconds() {
    return _execTimeoutSeconds;
  }

  @JsonProperty(PROP_INPUT_ACCESS_LIST)
  public String getInputAccessList() {
    return _inputAccessList;
  }

  @JsonProperty(PROP_INPUT_IPV6_ACCESS_LIST)
  public String getInputIpv6AccessList() {
    return _inputIpv6AccessList;
  }

  @JsonProperty(PROP_LINE_TYPE)
  public LineType getLineType() {
    return LineType.toLineType(getName());
  }

  @JsonProperty(PROP_LOGIN_AUTHENTICATION)
  public String getLoginAuthentication() {
    return _loginAuthentication;
  }

  @JsonProperty(PROP_OUTPUT_ACCESS_LIST)
  public String getOutputAccessList() {
    return _outputAccessList;
  }

  @JsonProperty(PROP_OUTPUT_IPV6_ACCESS_LIST)
  public String getOutputIpv6AccessList() {
    return _outputIpv6AccessList;
  }

  @JsonProperty(PROP_TRANSPORT_INPUT)
  public SortedSet<String> getTransportInput() {
    return _transportInput;
  }

  @JsonProperty(PROP_TRANSPORT_OUTPUT)
  public SortedSet<String> getTransportOutput() {
    return _transportOutput;
  }

  @JsonProperty(PROP_TRANSPORT_PREFERRED)
  public SortedSet<String> getTransportPreferred() {
    return _transportPreferred;
  }

  /**
   * Check whether the line requires authentication. A line requires authentication if it has a
   * login list with at least one method and does not contain the 'none' method
   *
   * @return true if line requires authentication, false if otherwise
   */
  public boolean requiresAuthentication() {
    boolean requiresAuthentication = false;

    if (_aaaAuthenticationLoginList != null) {
      for (AuthenticationMethod method : _aaaAuthenticationLoginList.getMethods()) {
        if (method == AuthenticationMethod.NONE || method == AuthenticationMethod.UNKNOWN) {
          return false;
        }
        requiresAuthentication = true;
      }
    }

    return requiresAuthentication;
  }

  public void setAaaAuthenticationLoginList(AaaAuthenticationLoginList aaaAuthenticationLoginList) {
    _aaaAuthenticationLoginList = aaaAuthenticationLoginList;
  }

  @JsonProperty(PROP_EXEC_TIMEOUT_MINUTES)
  public void setExecTimeoutMinutes(Integer execTimeoutMinutes) {
    _execTimeoutMinutes = execTimeoutMinutes;
  }

  @JsonProperty(PROP_EXEC_TIMEOUT_SECONDS)
  public void setExecTimeoutSeconds(Integer execTimeoutSeconds) {
    _execTimeoutSeconds = execTimeoutSeconds;
  }

  @JsonProperty(PROP_INPUT_ACCESS_LIST)
  public void setInputAccessList(String inputAccessList) {
    _inputAccessList = inputAccessList;
  }

  @JsonProperty(PROP_INPUT_IPV6_ACCESS_LIST)
  public void setInputIpv6AccessList(String inputIpv6AccessList) {
    _inputIpv6AccessList = inputIpv6AccessList;
  }

  @JsonProperty(PROP_LINE_TYPE)
  private void setLineType(String ignoredLineType) {
    /* Ignore this -- it's only used by Jackson to ignore fields. */
  }

  @JsonProperty(PROP_LOGIN_AUTHENTICATION)
  public void setLoginAuthentication(@Nullable String loginAuthentication) {
    _loginAuthentication = loginAuthentication;
  }

  @JsonProperty(PROP_OUTPUT_ACCESS_LIST)
  public void setOutputAccessList(String outputAccessList) {
    _outputAccessList = outputAccessList;
  }

  @JsonProperty(PROP_OUTPUT_IPV6_ACCESS_LIST)
  public void setOutputIpv6AccessList(String outputIpv6AccessList) {
    _outputIpv6AccessList = outputIpv6AccessList;
  }

  @JsonProperty(PROP_TRANSPORT_INPUT)
  public void setTransportInput(SortedSet<String> transportInput) {
    _transportInput = transportInput;
  }

  @JsonProperty(PROP_TRANSPORT_OUTPUT)
  public void setTransportOutput(SortedSet<String> transportOutput) {
    _transportOutput = transportOutput;
  }

  @JsonProperty(PROP_TRANSPORT_PREFERRED)
  public void setTransportPreferred(SortedSet<String> transportPreferred) {
    _transportPreferred = transportPreferred;
  }
}
