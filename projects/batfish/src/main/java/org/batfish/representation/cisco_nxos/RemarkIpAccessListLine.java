package org.batfish.representation.cisco_nxos;

/** An {@link IpAccessListLine} providing a comment. */
public final class RemarkIpAccessListLine extends IpAccessListLine {

  public RemarkIpAccessListLine(long line, String text) {
    super(line, text);
  }

  @Override
  public <T> T accept(IpAccessListLineVisitor<T> visitor) {
    return visitor.visitRemarkIpAccessListLine(this);
  }
}
