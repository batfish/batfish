package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** FortiOS datamodel component containing ippool configuration */
public final class Ippool implements FortiosRenameableObject, Serializable {
  public enum Type {
    FIXED_PORT_RANGE,
    ONE_TO_ONE,
    OVERLOAD,
    PORT_BLOCK_ALLOCATION,
  }

  private @Nullable String _associatedInterface;
  private @Nullable String _comments;
  private @Nullable Ip _endip;
  private @Nonnull String _name;
  private @Nullable Ip _startip;
  private @Nullable Type _type;
  private final @Nonnull BatfishUUID _uuid;

  public Ippool(String name, BatfishUUID uuid) {
    _name = name;
    _uuid = uuid;
  }

  public @Nullable String getAssociatedInterface() {
    return _associatedInterface;
  }

  public @Nullable String getComments() {
    return _comments;
  }

  public @Nullable Ip getEndip() {
    return _endip;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Ip getStartip() {
    return _startip;
  }

  public @Nullable Type getType() {
    return _type;
  }

  public void setAssociatedInterface(String associatedInterface) {
    _associatedInterface = associatedInterface;
  }

  public void setComments(String comments) {
    _comments = comments;
  }

  public void setEndip(Ip endip) {
    _endip = endip;
  }

  public void setStartip(Ip startip) {
    _startip = startip;
  }

  public void setType(Type type) {
    _type = type;
  }

  @Override
  public BatfishUUID getBatfishUUID() {
    return _uuid;
  }

  @Override
  public void setName(String name) {
    _name = name;
  }
}
