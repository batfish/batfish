package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;

public final class RemarkIpAccessListLine implements IpAccessListLine {

  private final @Nonnull String _text;

  public RemarkIpAccessListLine(String text) {
    _text = text;
  }

  @Override
  public <T> T accept(IpAccessListLineVisitor<T> visitor) {
    return visitor.visitRemarkIpAccessListLine(this);
  }

  public @Nonnull String getText() {
    return _text;
  }
}
