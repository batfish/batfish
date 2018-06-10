package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.util.ComparableStructure;

public class CryptoMapSet extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private boolean _dynamic;

  private List<CryptoMapEntry> _cryptoMapEntries;

  public CryptoMapSet(String name) {
    super(name);
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
