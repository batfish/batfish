package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** BGP neighbor update source ip setting */
public final class UpdateSourceIp implements UpdateSource, Serializable {

  public @Nonnull Ip getIp() {
    return _ip;
  }

  @Override
  public <T> T accept(UpdateSourceVisitor<T> visitor) {
    return visitor.visitUpdateSourceIp(this);
  }

  public UpdateSourceIp(Ip ip) {
    _ip = ip;
  }

  private @Nonnull final Ip _ip;
}
