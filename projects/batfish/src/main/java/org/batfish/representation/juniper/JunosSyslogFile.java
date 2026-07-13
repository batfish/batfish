package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A syslog file configured under {@code system syslog file <name>}.
 *
 * <p>Captures the archive settings Batfish extracts: the raw archive size (with its unit) and the
 * archive file count.
 */
@ParametersAreNonnullByDefault
public class JunosSyslogFile implements Serializable {

  private final @Nonnull String _name;

  /** Raw {@code archive size} value, as written in the config (not normalized to bytes). */
  private @Nullable Long _archiveSize;

  /** Unit of {@link #_archiveSize}; {@code null} when no size is configured. */
  private @Nullable JunosSyslogArchiveSizeUnit _archiveSizeUnit;

  private @Nullable Integer _archiveFileCount;

  public JunosSyslogFile(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Long getArchiveSize() {
    return _archiveSize;
  }

  public @Nullable JunosSyslogArchiveSizeUnit getArchiveSizeUnit() {
    return _archiveSizeUnit;
  }

  /** Set the raw archive size and its unit together. */
  public void setArchiveSize(long archiveSize, JunosSyslogArchiveSizeUnit archiveSizeUnit) {
    _archiveSize = archiveSize;
    _archiveSizeUnit = archiveSizeUnit;
  }

  public @Nullable Integer getArchiveFileCount() {
    return _archiveFileCount;
  }

  public void setArchiveFileCount(@Nullable Integer archiveFileCount) {
    _archiveFileCount = archiveFileCount;
  }
}
