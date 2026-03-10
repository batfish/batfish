package org.batfish.vendor.cisco_nxos.representation;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;

/** An {@link IpAccessListLine} that matches a header and either permits or denies. */
public final class ActionIpAccessListLine extends IpAccessListLine {

  public static final class Builder {

    private @Nullable LineAction _action;
    private @Nullable IpAddressSpec _dstAddressSpec;
    private boolean _fragments;
    private @Nullable Layer3Options _l3Options;
    private @Nullable Layer4Options _l4Options;
    private @Nullable Long _line;
    private boolean _log;
    private @Nullable IpProtocol _protocol;
    private @Nullable IpAddressSpec _srcAddressSpec;
    private @Nullable String _text;

    private Builder() {}

    public @Nonnull ActionIpAccessListLine build() {
      checkArgument(_action != null, "Missing action");
      checkArgument(_dstAddressSpec != null, "Missing dstAddressSpec");
      checkArgument(!_fragments || _l4Options == null, "fragments incompatible with l4Options");
      checkArgument(_line != null, "Missing line");
      checkArgument(_srcAddressSpec != null, "Missing srcAddressSpec");
      checkArgument(_text != null, "Missing text");
      return new ActionIpAccessListLine(
          _line,
          _action,
          _dstAddressSpec,
          _fragments,
          _l3Options,
          _l4Options,
          _log,
          _protocol,
          _srcAddressSpec,
          _text);
    }

    public @Nonnull Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public @Nonnull Builder setDstAddressSpec(IpAddressSpec dstAddressSpec) {
      _dstAddressSpec = dstAddressSpec;
      return this;
    }

    public @Nonnull Builder setFragments(boolean fragments) {
      _fragments = fragments;
      return this;
    }

    public @Nonnull Builder setL3Options(@Nullable Layer3Options l3Options) {
      _l3Options = l3Options;
      return this;
    }

    public @Nonnull Builder setL4Options(@Nullable Layer4Options l4Options) {
      _l4Options = l4Options;
      return this;
    }

    public @Nonnull Builder setLine(long line) {
      _line = line;
      return this;
    }

    public @Nonnull Builder setLog(boolean log) {
      _log = log;
      return this;
    }

    public @Nonnull Builder setProtocol(IpProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public @Nonnull Builder setSrcAddressSpec(IpAddressSpec srcAddressSpec) {
      _srcAddressSpec = srcAddressSpec;
      return this;
    }

    public @Nonnull Builder setText(@Nullable String text) {
      _text = text;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull LineAction _action;
  private final @Nonnull IpAddressSpec _dstAddressSpec;
  private final boolean _fragments;
  private final @Nullable Layer3Options _l3Options;
  private final @Nullable Layer4Options _l4Options;
  private final boolean _log;
  private final @Nullable IpProtocol _protocol;
  private final @Nonnull IpAddressSpec _srcAddressSpec;

  private ActionIpAccessListLine(
      long line,
      LineAction action,
      IpAddressSpec dstAddressSpec,
      boolean fragments,
      Layer3Options l3Options,
      Layer4Options l4Options,
      boolean log,
      IpProtocol protocol,
      IpAddressSpec srcAddressSpec,
      String text) {
    super(line, text);
    _action = action;
    _dstAddressSpec = dstAddressSpec;
    _fragments = fragments;
    _l3Options = l3Options;
    _l4Options = l4Options;
    _log = log;
    _protocol = protocol;
    _srcAddressSpec = srcAddressSpec;
  }

  @Override
  public <T> T accept(IpAccessListLineVisitor<T> visitor) {
    return visitor.visitActionIpAccessListLine(this);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull IpAddressSpec getDstAddressSpec() {
    return _dstAddressSpec;
  }

  public boolean getFragments() {
    return _fragments;
  }

  public @Nonnull Layer3Options getL3Options() {
    return _l3Options;
  }

  public @Nullable Layer4Options getL4Options() {
    return _l4Options;
  }

  public boolean getLog() {
    return _log;
  }

  /**
   * The {@link IpProtocol} matched by this line, or {@code null} if the line matches any protocol.
   */
  public @Nullable IpProtocol getProtocol() {
    return _protocol;
  }

  public @Nonnull IpAddressSpec getSrcAddressSpec() {
    return _srcAddressSpec;
  }
}
