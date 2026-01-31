package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class CryptoMapSet implements Serializable {

  private boolean _dynamic;

  private List<CryptoMapEntry> _cryptoMapEntries;

  public CryptoMapSet() {
    _cryptoMapEntries = new ArrayList<>();
  }

  public boolean getDynamic() {
    return _dynamic;
  }

  public void setDynamic(boolean dynamic) {
    _dynamic = dynamic;
  }

  public @Nonnull List<CryptoMapEntry> getCryptoMapEntries() {
    return _cryptoMapEntries;
  }

  public void setCryptoMapEntries(@Nonnull List<CryptoMapEntry> cryptoMapEntries) {
    _cryptoMapEntries = cryptoMapEntries;
  }
}
