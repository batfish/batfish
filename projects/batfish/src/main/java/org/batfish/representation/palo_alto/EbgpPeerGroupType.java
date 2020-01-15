package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;

public class EbgpPeerGroupType implements BgpPeerGroupTypeAndOptions, Serializable {
  public enum ImportNexthopMode {
    ORIGINAL,
    USE_PEER
  }

  public enum ExportNexthopMode {
    RESOLVE,
    USE_SELF
  }

  public @Nullable Boolean getRemotePrivateAs() {
    return _remotePrivateAs;
  }

  public void setRemotePrivateAs(@Nullable Boolean remotePrivateAs) {
    _remotePrivateAs = remotePrivateAs;
  }

  public @Nullable ExportNexthopMode getExportNexthop() {
    return _exportNexthop;
  }

  public void setExportNexthop(@Nullable ExportNexthopMode exportNexthop) {
    _exportNexthop = exportNexthop;
  }

  public @Nullable ImportNexthopMode getImportNexthop() {
    return _importNexthop;
  }

  public void setImportNexthop(@Nullable ImportNexthopMode importNexthop) {
    _importNexthop = importNexthop;
  }

  private @Nullable Boolean _remotePrivateAs;
  private @Nullable ExportNexthopMode _exportNexthop;
  private @Nullable ImportNexthopMode _importNexthop;
}
