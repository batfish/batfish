package org.batfish.minesweeper.abstraction;

import java.util.Objects;
import javax.annotation.Nullable;

public class EquivalenceEdge {

  private Integer _abstractId;

  private InterfacePolicy _importPol;

  private InterfacePolicy _exportPol;

  private int _hcode = 0;

  public EquivalenceEdge(
      Integer abstractId,
      @Nullable InterfacePolicy importPol,
      @Nullable InterfacePolicy exportPol) {
    _abstractId = abstractId;
    _importPol = importPol;
    _exportPol = exportPol;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EquivalenceEdge)) {
      return false;
    }
    EquivalenceEdge other = (EquivalenceEdge) o;
    return Objects.equals(_abstractId, other._abstractId)
        && Objects.equals(_importPol, other._importPol)
        && Objects.equals(_exportPol, other._exportPol);
  }

  @Override
  public int hashCode() {
    if (_hcode == 0) {
      int result = _abstractId != null ? _abstractId.hashCode() : 0;
      result = 31 * result + (_importPol != null ? _importPol.hashCode() : 0);
      result = 31 * result + (_exportPol != null ? _exportPol.hashCode() : 0);
      _hcode = result;
    }
    return _hcode;
  }

  public Integer getAbstractId() {
    return _abstractId;
  }

  public InterfacePolicy getImportPol() {
    return _importPol;
  }

  public InterfacePolicy getExportPol() {
    return _exportPol;
  }
}
