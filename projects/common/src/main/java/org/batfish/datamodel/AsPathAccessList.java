package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.PatternProvider;

/** An AsPathAccessList is used to filter e/iBGP routes according to their AS-path attribute. */
public final class AsPathAccessList implements Serializable {
  private static final String PROP_LINES = "lines";
  private static final String PROP_NAME = "name";

  private final @Nonnull List<AsPathAccessListLine> _lines;

  private final String _name;

  /**
   * Cache for AS path permit/deny decisions. Maps AsPath to Boolean where true = permitted, false =
   * denied, null = not yet evaluated. Bounded to 1024 entries to prevent memory leaks.
   */
  private transient LoadingCache<AsPath, Boolean> _cache;

  @JsonCreator
  public AsPathAccessList(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_LINES) @Nullable List<AsPathAccessListLine> lines) {
    _lines = firstNonNull(lines, ImmutableList.of());
    _name = name;
    initTransientFields();
  }

  private void initTransientFields() {
    _cache = Caffeine.newBuilder().softValues().maximumSize(1024).build(this::evaluatePermits);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof AsPathAccessList)) {
      return false;
    }
    AsPathAccessList other = (AsPathAccessList) o;
    return Objects.equals(_name, other._name) && Objects.equals(_lines, other._lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _lines);
  }

  /** The list of lines against which a route's AS-path will be checked in order. */
  @JsonProperty(PROP_LINES)
  public @Nonnull List<AsPathAccessListLine> getLines() {
    return _lines;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  private boolean evaluatePermits(AsPath asPath) {
    String asPathString = asPath.getAsPathString();
    for (AsPathAccessListLine line : _lines) {
      Pattern p = PatternProvider.fromString(line.getRegex());
      Matcher matcher = p.matcher(asPathString);
      if (matcher.find()) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  public boolean permits(AsPath asPath) {
    return _cache.get(asPath);
  }

  @Serial
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    initTransientFields();
  }
}
