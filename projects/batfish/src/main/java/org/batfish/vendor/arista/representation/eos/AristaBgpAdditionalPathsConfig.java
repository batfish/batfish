package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for BGP "additional paths" setting */
public class AristaBgpAdditionalPathsConfig implements Serializable {
  public enum SendType {
    ANY,
    /** Aka, no [...] additional-paths send any. */
    NONE,
  }

  private @Nullable Boolean _install;
  private @Nullable Boolean _receive;
  private @Nullable SendType _send;

  public AristaBgpAdditionalPathsConfig() {}

  public @Nullable Boolean getInstall() {
    return _install;
  }

  public void setInstall(@Nullable Boolean install) {
    _install = install;
  }

  public @Nullable Boolean getReceive() {
    return _receive;
  }

  public void setReceive(@Nullable Boolean receive) {
    _receive = receive;
  }

  public @Nullable SendType getSend() {
    return _send;
  }

  public void setSend(@Nullable SendType send) {
    _send = send;
  }
}
