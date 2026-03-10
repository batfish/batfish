package org.batfish.vendor.cisco_nxos.representation;

/** IGMP options for an {@link ActionIpAccessListLine}. */
public final class IgmpOptions implements Layer4Options {

  private final int _messageType;

  public IgmpOptions(int messageType) {
    _messageType = messageType;
  }

  @Override
  public <T> T accept(Layer4OptionsVisitor<T> visitor) {
    return visitor.visitIgmpOptions(this);
  }

  public int getMessageType() {
    return _messageType;
  }
}
