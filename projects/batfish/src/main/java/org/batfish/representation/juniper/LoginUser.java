package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Vendor-specific model of a Junos {@code [edit system login user <name>]} block.
 *
 * <p>Models the user's {@code uid}, assigned login {@code class}, full name, and which password
 * authentication type is used.
 */
public class LoginUser implements Serializable {

  /** How a user's local password is specified. */
  public enum AuthenticationType {
    ENCRYPTED_PASSWORD,
    PLAIN_TEXT_PASSWORD,
  }

  private final String _name;
  private @Nullable Integer _uid;
  private @Nullable String _className;
  private @Nullable String _fullName;
  private @Nullable AuthenticationType _authenticationType;

  public LoginUser(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public @Nullable Integer getUid() {
    return _uid;
  }

  public void setUid(@Nullable Integer uid) {
    _uid = uid;
  }

  /** The name of the login class assigned to this user ({@code user <name> class <class>}). */
  public @Nullable String getClassName() {
    return _className;
  }

  public void setClassName(@Nullable String className) {
    _className = className;
  }

  /** The user's full name ({@code user <name> full-name <complete-name>}). */
  public @Nullable String getFullName() {
    return _fullName;
  }

  public void setFullName(@Nullable String fullName) {
    _fullName = fullName;
  }

  /** Whether the password was configured as encrypted or plain-text. */
  public @Nullable AuthenticationType getAuthenticationType() {
    return _authenticationType;
  }

  public void setAuthenticationType(@Nullable AuthenticationType authenticationType) {
    _authenticationType = authenticationType;
  }
}
