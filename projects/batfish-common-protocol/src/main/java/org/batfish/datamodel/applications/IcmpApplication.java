package org.batfish.datamodel.applications;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;

/** An abstract class that represents an ICMP application */
@ParametersAreNonnullByDefault
public abstract class IcmpApplication extends Application {

  public static int MAX_TYPE = 255;

  public static int MAX_TYPE_CODE = 255;

  protected IcmpApplication() {
    super(IpProtocol.ICMP);
  }
}
