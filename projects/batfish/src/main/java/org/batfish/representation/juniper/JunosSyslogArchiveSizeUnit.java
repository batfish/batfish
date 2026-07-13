package org.batfish.representation.juniper;

/**
 * The unit of a {@code system syslog file <name> archive size} value.
 *
 * <p>{@link #BYTES} represents a bare numeric value with no {@code k}/{@code m}/{@code g} suffix.
 */
public enum JunosSyslogArchiveSizeUnit {
  BYTES,
  KILOBYTES,
  MEGABYTES,
  GIGABYTES
}
