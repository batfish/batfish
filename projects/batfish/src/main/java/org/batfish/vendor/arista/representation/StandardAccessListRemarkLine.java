package org.batfish.vendor.arista.representation;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StandardAccessListRemarkLine implements StandardAccessListLine {

  private final long _seq;
  private final @Nonnull String _remark;

  public StandardAccessListRemarkLine(long seq, String remark) {
    _seq = seq;
    _remark = remark;
  }

  @Override
  public long getSeq() {
    return _seq;
  }

  public @Nonnull String getRemark() {
    return _remark;
  }

  @Override
  public Optional<ExtendedAccessListLine> toExtendedAccessListLine() {
    return Optional.empty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof StandardAccessListRemarkLine)) {
      return false;
    }
    StandardAccessListRemarkLine that = (StandardAccessListRemarkLine) o;
    return _seq == that._seq && _remark.equals(that._remark);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_seq, _remark);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("seq", _seq)
        .add("remark", _remark)
        .toString();
  }
}
