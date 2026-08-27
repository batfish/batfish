package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Aggregates a FastPath device's AAA ({@code aaa ...}) method-list configuration.
 *
 * <p>Each family (authentication, authorization, accounting) is keyed first by its type and then by
 * list name. FastPath list names are unique only within a type (e.g. the same name may be used for
 * both {@code accounting exec} and {@code accounting commands}), so the type is part of the key.
 *
 * <p>The {@code define*} methods replace any existing list of the same type and name, matching the
 * device: re-issuing a command for an existing list overwrites its record type and method list
 * rather than appending to it.
 */
public final class Aaa implements Serializable {

  private final @Nonnull Map<AuthenticationType, Map<String, Authentication>> _authentication;
  private final @Nonnull Map<AuthorizationType, Map<String, Authorization>> _authorization;
  private final @Nonnull Map<AccountingType, Map<String, Accounting>> _accounting;

  public Aaa() {
    _authentication = new EnumMap<>(AuthenticationType.class);
    _authorization = new EnumMap<>(AuthorizationType.class);
    _accounting = new EnumMap<>(AccountingType.class);
  }

  public @Nonnull Map<AuthenticationType, Map<String, Authentication>> getAuthentication() {
    return _authentication;
  }

  public @Nonnull Map<AuthorizationType, Map<String, Authorization>> getAuthorization() {
    return _authorization;
  }

  public @Nonnull Map<AccountingType, Map<String, Accounting>> getAccounting() {
    return _accounting;
  }

  /** Defines the {@link Authentication} list of the given type and name. */
  public void defineAuthentication(AuthenticationType type, String name, List<AaaMethod> methods) {
    _authentication
        .computeIfAbsent(type, t -> new LinkedHashMap<>())
        .put(name, new Authentication(type, name, methods));
  }

  /** Defines the {@link Authorization} list of the given type and name. */
  public void defineAuthorization(AuthorizationType type, String name, List<AaaMethod> methods) {
    _authorization
        .computeIfAbsent(type, t -> new LinkedHashMap<>())
        .put(name, new Authorization(type, name, methods));
  }

  /** Defines the {@link Accounting} list of the given type and name. */
  public void defineAccounting(
      AccountingType type, String name, Accounting.RecordType recordType, List<AaaMethod> methods) {
    _accounting
        .computeIfAbsent(type, t -> new LinkedHashMap<>())
        .put(name, new Accounting(type, name, recordType, methods));
  }
}
