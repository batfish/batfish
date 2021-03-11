package org.batfish.representation.cisco_asa;

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

  @Nonnull
  public List<CryptoMapEntry> getCryptoMapEntries() {
    return _cryptoMapEntries;
  }

  public void setCryptoMapEntries(@Nonnull List<CryptoMapEntry> cryptoMapEntries) {
    _cryptoMapEntries = cryptoMapEntries;
  }
}
