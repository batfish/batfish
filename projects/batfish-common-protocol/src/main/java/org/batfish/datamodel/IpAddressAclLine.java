package org.batfish.datamodel;

import javax.annotation.Nonnull;

public class IpAddressAclLine {

  public static final IpAddressAclLine PERMIT_ALL =
      IpAddressAclLine.builder().setIpSpace(IpSpace.UNIVERSE).build();

  public static class Builder {

    private LineAction _action;

    private IpSpace _ipSpace;

    private boolean _matchComplement;

    private Builder() {
      _action = LineAction.ACCEPT;
      _matchComplement = false;
    }

    public IpAddressAclLine build() {
      return new IpAddressAclLine(_ipSpace, _action, _matchComplement);
    }

    public Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public Builder setIpSpace(IpSpace ipSpace) {
      _ipSpace = ipSpace;
      return this;
    }

    public Builder setMatchComplement(boolean matchComplement) {
      _matchComplement = matchComplement;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final LineAction _action;

  private final IpSpace _ipSpace;

  private final boolean _matchComplement;

  private IpAddressAclLine(
      @Nonnull IpSpace ipSpace, @Nonnull LineAction action, boolean matchComplement) {
    _ipSpace = ipSpace;
    _action = action;
    _matchComplement = matchComplement;
  }

  public LineAction getAction() {
    return _action;
  }

  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  /** Match the complement of the {@code IpSpace} of this line. */
  public boolean getMatchComplement() {
    return _matchComplement;
  }
}
