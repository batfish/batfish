package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A syslog file configured under {@code system syslog file <name>}.
 *
 * <p>Captures the archive settings Batfish extracts: the archive size (normalized to bytes) and the
 * archive file count.
 */
@ParametersAreNonnullByDefault
public class JunosSyslogFile implements Serializable {

  private final @Nonnull String _name;

  /**
   * Archive size in bytes. Junos {@code k}/{@code m}/{@code g} suffixes are 1024-based and are
   * normalized to bytes at extraction; a bare value is already in bytes.
   */
  private @Nullable Long _archiveSizeBytes;

  private @Nullable Integer _archiveFileCount;

  public JunosSyslogFile(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Long getArchiveSizeBytes() {
    return _archiveSizeBytes;
  }

  public void setArchiveSizeBytes(@Nullable Long archiveSizeBytes) {
    _archiveSizeBytes = archiveSizeBytes;
  }

  public @Nullable Integer getArchiveFileCount() {
    return _archiveFileCount;
  }

  public void setArchiveFileCount(@Nullable Integer archiveFileCount) {
    _archiveFileCount = archiveFileCount;
  }
}
