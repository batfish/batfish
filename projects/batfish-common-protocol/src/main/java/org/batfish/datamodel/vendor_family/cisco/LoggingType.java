package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;

public class LoggingType implements Serializable {

  private static final long serialVersionUID = 1L;

  private String _severity;

  private Integer _severityNum;

  public String getSeverity() {
    return _severity;
  }

  public Integer getSeverityNum() {
    return _severityNum;
  }

  public void setSeverity(String severity) {
    _severity = severity;
  }

  public void setSeverityNum(Integer severityNum) {
    _severityNum = severityNum;
  }
}
