package org.batfish.minesweeper.abstraction;

import java.util.Objects;
import javax.annotation.Nullable;

class EdgePolicy {

  private InterfacePolicy _importPol;

  private InterfacePolicy _exportPol;

  private int _hcode = 0;

  EdgePolicy(@Nullable InterfacePolicy importPol, @Nullable InterfacePolicy exportPol) {
    _importPol = importPol;
    _exportPol = exportPol;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EdgePolicy)) {
      return false;
    }
    EdgePolicy other = (EdgePolicy) o;
    return Objects.equals(_importPol, other._importPol)
        && Objects.equals(_exportPol, other._exportPol);
  }

  @Override
  public int hashCode() {
    if (_hcode == 0) {
      int result = _importPol != null ? _importPol.hashCode() : 0;
      result = 31 * result + (_exportPol != null ? _exportPol.hashCode() : 0);
      _hcode = result;
    }
    return _hcode;
  }
}
