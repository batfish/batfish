package org.batfish.engine;

import javax.annotation.Nullable;

/**
 * The device's configuration language, pinned by the caller rather than guessed by Batfish's format
 * detector. Each dialect knows how its config text is presented to Batfish and how proposed config
 * lines are appended to it.
 */
public enum Dialect {
  ARISTA_EOS("arista"),
  CISCO_IOS("ios"),
  CISCO_NXOS("cisco-nx"),
  /** Junos in {@code display set} form, so proposed {@code set}/{@code delete} lines append. */
  JUNOS("juniper"),
  /**
   * FRR on a Linux host. Batfish does not parse a standalone FRR config: it reads FRR only inside a
   * Cumulus-style concatenated file, whose interfaces section carries the Linux addresses.
   */
  FRR_LINUX(null);

  private final @Nullable String _rancidType;

  Dialect(@Nullable String rancidType) {
    _rancidType = rancidType;
  }

  /** The RANCID content type that pins Batfish's format, or null if the format is self-evident. */
  public @Nullable String rancidType() {
    return _rancidType;
  }
}
