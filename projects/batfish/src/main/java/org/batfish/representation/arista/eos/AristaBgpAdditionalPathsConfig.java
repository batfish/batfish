package org.batfish.representation.arista.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for BGP "additional paths" setting */
public class AristaBgpAdditionalPathsConfig implements Serializable {
  public enum SendType {
    ANY,
    /** Aka, no [...] additional-paths send any. */
    NONE,
  }

  @Nullable private Boolean _install;
  @Nullable private Boolean _receive;
  @Nullable private SendType _send;

  public AristaBgpAdditionalPathsConfig() {}

  @Nullable
  public Boolean getInstall() {
    return _install;
  }

  public void setInstall(@Nullable Boolean install) {
    _install = install;
  }

  @Nullable
  public Boolean getReceive() {
    return _receive;
  }

  public void setReceive(@Nullable Boolean receive) {
    _receive = receive;
  }

  @Nullable
  public SendType getSend() {
    return _send;
  }

  public void setSend(@Nullable SendType send) {
    _send = send;
  }
}
