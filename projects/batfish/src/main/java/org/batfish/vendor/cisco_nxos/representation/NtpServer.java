package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for an NTP server. */
public final class NtpServer implements Serializable {

  public NtpServer(String host) {
    _host = host;
  }

  public @Nonnull String getHost() {
    return _host;
  }

  public boolean getPrefer() {
    return _prefer;
  }

  public void setPrefer(boolean prefer) {
    _prefer = prefer;
  }

  public String getUseVrf() {
    return _useVrf;
  }

  public void setUseVrf(String useVrf) {
    _useVrf = useVrf;
  }

  private final @Nonnull String _host;
  private boolean _prefer;
  private @Nullable String _useVrf;
}
