package org.batfish.datamodel.applications;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;

/**
 * An abstract class that represents an application, which is an IP protocol and
 * application-speficic details covered in child classes
 */
@ParametersAreNonnullByDefault
public abstract class Application {

  @Nonnull private final IpProtocol _ipProtocol;

  protected Application(IpProtocol ipProtocol) {
    _ipProtocol = ipProtocol;
  }

  @Nonnull
  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }
}
