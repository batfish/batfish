package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class CryptoMapSet implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _dynamic;

  private List<CryptoMapEntry> _cryptoMapEntries;

  private final String _name;

  public CryptoMapSet(String name) {
    _cryptoMapEntries = new ArrayList<>();
    _name = name;
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
