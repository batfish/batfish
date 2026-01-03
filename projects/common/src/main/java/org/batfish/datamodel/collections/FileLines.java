package org.batfish.datamodel.collections;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FileLines {
  private static final String PROP_FILENAME = "filename";
  private static final String PROP_LINES = "lines";

  private final @Nonnull String _filename;

  private @Nonnull SortedSet<Integer> _lines;

  @JsonCreator
  public FileLines(
      @JsonProperty(PROP_FILENAME) String filename,
      @JsonProperty(PROP_LINES) @Nullable SortedSet<Integer> lines) {
    _filename = filename;
    _lines = firstNonNull(lines, ImmutableSortedSet.of());
  }

  @JsonProperty(PROP_FILENAME)
  public String getFilename() {
    return _filename;
  }

  @JsonProperty(PROP_LINES)
  public SortedSet<Integer> getLines() {
    return _lines;
  }

  @Override
  public String toString() {
    return _filename + ":" + _lines;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof FileLines)) {
      return false;
    }
    FileLines o = (FileLines) other;
    return _filename.equals(o._filename) && _lines.equals(o._lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_filename, _lines);
  }
}
