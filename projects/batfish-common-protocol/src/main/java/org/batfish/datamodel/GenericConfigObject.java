package org.batfish.datamodel;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;

public interface GenericConfigObject extends Serializable {

  /**
   * Returns the list of border interfaces for this config object. A return value of null implies
   * that the subclass does not provide meaningful information.
   *
   * <p>Subclasses whose border interfaces are not expected to be covered by the user-supplied ISP
   * config file (e.g., AWS) should override this method.
   */
  @Nullable
  default List<BorderInterfaceInfo> getBorderInterfaces() {
    throw new UnsupportedOperationException("Get border interfaces method has not been overriden");
  }
}
