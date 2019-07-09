package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;

/** An {@link IpAccessListLine} providing a comment. */
public final class RemarkIpAccessListLine extends IpAccessListLine {

  private final @Nonnull String _text;

  public RemarkIpAccessListLine(long line, String text) {
    super(line);
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
