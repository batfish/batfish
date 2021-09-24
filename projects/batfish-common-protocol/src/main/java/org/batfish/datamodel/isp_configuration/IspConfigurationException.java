package org.batfish.datamodel.isp_configuration;

import org.batfish.common.BatfishException;

/** Indicates a problem with the ISP configuration provided by the user */
public class IspConfigurationException extends BatfishException {

  public IspConfigurationException(String msg) {
    super(msg);
  }

  public IspConfigurationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
