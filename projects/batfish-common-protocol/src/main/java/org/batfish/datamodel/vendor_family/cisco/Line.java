package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

public class Line extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

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
  public Line(@JsonProperty(PROP_NAME) String name) {
    super(name);
    _transportInput = new TreeSet<>();
    _transportOutput = new TreeSet<>();
    _transportPreferred = new TreeSet<>();
  }

  public AaaAuthenticationLoginList getAaaAuthenticationLoginList() {
    return _aaaAuthenticationLoginList;
  }

  public Integer getExecTimeoutMinutes() {
    return _execTimeoutMinutes;
  }

  public Integer getExecTimeoutSeconds() {
    return _execTimeoutSeconds;
  }

  public String getInputAccessList() {
    return _inputAccessList;
  }

  public String getInputIpv6AccessList() {
    return _inputIpv6AccessList;
  }

  public String getLoginAuthentication() {
    return _loginAuthentication;
  }

  public String getOutputAccessList() {
    return _outputAccessList;
  }

  public String getOutputIpv6AccessList() {
    return _outputIpv6AccessList;
  }

  public SortedSet<String> getTransportInput() {
    return _transportInput;
  }

  public SortedSet<String> getTransportOutput() {
    return _transportOutput;
  }

  public SortedSet<String> getTransportPreferred() {
    return _transportPreferred;
  }

  public boolean requiresAuthentication() {
    boolean authenticated = false;

    if (_aaaAuthenticationLoginList != null) {
      for (String method : _aaaAuthenticationLoginList.getMethods()) {
        if (method.equals("none")) {
          return false;
        }
        authenticated = true;
      }
    }

    return authenticated;
  }

  public void setAaaAuthenticationLoginList(AaaAuthenticationLoginList aaaAuthenticationLoginList) {
    _aaaAuthenticationLoginList = aaaAuthenticationLoginList;
  }

  public void setExecTimeoutMinutes(Integer execTimeoutMinutes) {
    _execTimeoutMinutes = execTimeoutMinutes;
  }

  public void setExecTimeoutSeconds(Integer execTimeoutSeconds) {
    _execTimeoutSeconds = execTimeoutSeconds;
  }

  public void setInputAccessList(String inputAccessList) {
    _inputAccessList = inputAccessList;
  }

  public void setInputIpv6AccessList(String inputIpv6AccessList) {
    _inputIpv6AccessList = inputIpv6AccessList;
  }

  public void setLoginAuthentication(@Nullable String loginAuthentication) {
    _loginAuthentication = loginAuthentication;
  }

  public void setOutputAccessList(String outputAccessList) {
    _outputAccessList = outputAccessList;
  }

  public void setOutputIpv6AccessList(String outputIpv6AccessList) {
    _outputIpv6AccessList = outputIpv6AccessList;
  }

  public void setTransportInput(SortedSet<String> transportInput) {
    _transportInput = transportInput;
  }

  public void setTransportOutput(SortedSet<String> transportOutput) {
    _transportOutput = transportOutput;
  }

  public void setTransportPreferred(SortedSet<String> transportPreferred) {
    _transportPreferred = transportPreferred;
  }
}
