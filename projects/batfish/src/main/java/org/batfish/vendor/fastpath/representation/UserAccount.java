package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A FastPath local user account, from {@code username ...}. */
public final class UserAccount implements Serializable {

  private final @Nonnull String _name;
  private @Nullable Integer _level;
  private boolean _hasPassword;
  private boolean _encrypted;
  private @Nullable String _userGroup;

  public UserAccount(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getLevel() {
    return _level;
  }

  public void setLevel(@Nullable Integer level) {
    _level = level;
  }

  public boolean getHasPassword() {
    return _hasPassword;
  }

  public void setHasPassword(boolean hasPassword) {
    _hasPassword = hasPassword;
  }

  public boolean getEncrypted() {
    return _encrypted;
  }

  public void setEncrypted(boolean encrypted) {
    _encrypted = encrypted;
  }

  public @Nullable String getUserGroup() {
    return _userGroup;
  }

  public void setUserGroup(@Nullable String userGroup) {
    _userGroup = userGroup;
  }
}
