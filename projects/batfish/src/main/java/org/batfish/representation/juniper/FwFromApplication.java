package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;

public final class FwFromApplication implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Application _application;

  private final String _applicationName;

  private final Map<String, BaseApplication> _applications;

  public FwFromApplication(Application application) {
    _applicationName = null;
    _application = application;
    _applications = null;
  }

  public FwFromApplication(String applicationName, Map<String, BaseApplication> applications) {
    _applicationName = applicationName;
    _application = null;
    _applications = applications;
  }

  public void applyTo(
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    Application application;
    if (_applicationName != null) {
      application = _applications.get(_applicationName);
    } else {
      application = _application;
    }
    if (application == null) {
      w.redFlag("Reference to undefined application: \"" + _applicationName + "\"");
    } else {
      application.applyTo(srcHeaderSpaceBuilder, action, lines, w);
    }
  }
}
