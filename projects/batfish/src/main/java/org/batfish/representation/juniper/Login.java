package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;

/**
 * Vendor-specific model of the Junos {@code [edit system login]} block.
 *
 * <p>Aggregates the login sub-objects, each modeled in its own class.
 */
public class Login implements Serializable {

  private final Map<String, LoginClass> _classes;

  private final Map<String, LoginUser> _users;

  private @Nullable LoginRetryOptions _retryOptions;

  private @Nullable LoginPassword _password;

  public Login() {
    _classes = new TreeMap<>();
    _users = new TreeMap<>();
  }

  /** Login classes, keyed by class name. */
  public Map<String, LoginClass> getClasses() {
    return _classes;
  }

  /** Login users, keyed by username. */
  public Map<String, LoginUser> getUsers() {
    return _users;
  }

  /** The {@code retry-options} block, or {@code null} if not configured. */
  public @Nullable LoginRetryOptions getRetryOptions() {
    return _retryOptions;
  }

  public void setRetryOptions(@Nullable LoginRetryOptions retryOptions) {
    _retryOptions = retryOptions;
  }

  /** The {@code password} policy block, or {@code null} if not configured. */
  public @Nullable LoginPassword getPassword() {
    return _password;
  }

  public void setPassword(@Nullable LoginPassword password) {
    _password = password;
  }
}
