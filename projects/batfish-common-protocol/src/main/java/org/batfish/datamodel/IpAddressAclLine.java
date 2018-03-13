package org.batfish.datamodel;

import javax.annotation.Nonnull;

public class IpAddressAclLine {

  public static final IpAddressAclLine PERMIT_ALL =
      IpAddressAclLine.builder().setIpSpace(IpSpace.UNIVERSE).build();

  public static class Builder {

    private LineAction _action;

    private IpSpace _ipSpace;

    private boolean _negate;

    private Builder() {
      _action = LineAction.ACCEPT;
      _negate = false;
    }

    public IpAddressAclLine build() {
      return new IpAddressAclLine(_ipSpace, _action, _negate);
    }

    public Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public Builder setIpSpace(IpSpace ipSpace) {
      _ipSpace = ipSpace;
      return this;
    }

    public Builder setNegate(boolean negate) {
      _negate = negate;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final LineAction _action;

  private final IpSpace _ipSpace;

  private final boolean _negate;

  private IpAddressAclLine(@Nonnull IpSpace ipSpace, @Nonnull LineAction action, boolean negate) {
    _ipSpace = ipSpace;
    _action = action;
    _negate = negate;
  }

  public LineAction getAction() {
    return _action;
  }

  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  public boolean getNegate() {
    return _negate;
  }
}
