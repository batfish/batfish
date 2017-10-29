package org.batfish.symbolic.abstraction;

import java.util.Objects;
import javax.annotation.Nullable;

public class InterfacePolicyPair {

  private InterfacePolicy _importPol;

  private InterfacePolicy _exportPol;

  private int _hcode = 0;

  public InterfacePolicyPair(
      @Nullable InterfacePolicy importPol,
      @Nullable InterfacePolicy exportPol) {
    this._importPol = importPol;
    this._exportPol = exportPol;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfacePolicyPair)) {
      return false;
    }
    InterfacePolicyPair other = (InterfacePolicyPair) o;
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

  public InterfacePolicy getImportPol() {
    return _importPol;
  }

  public InterfacePolicy getExportPol() {
    return _exportPol;
  }
}
