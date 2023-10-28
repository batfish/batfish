package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.AclLineEvaluator;

/** An access-list used to filter IPV4 packets */
public class IpAccessList implements Serializable {

  public static class Builder {

    private List<AclLine> _lines;
    private @Nullable String _name;
    private @Nullable Supplier<String> _nameGenerator;
    private Configuration _owner;
    private String _sourceName;
    private String _sourceType;

    private Builder(@Nullable Supplier<String> nameGenerator) {
      _lines = ImmutableList.of();
      _nameGenerator = nameGenerator;
    }

    public IpAccessList build() {
      checkArgument(_name != null || _nameGenerator != null, "Must set name before building");
      String name = _name != null ? _name : _nameGenerator.get();
      IpAccessList ipAccessList = new IpAccessList(name, _lines, _sourceName, _sourceType);
      if (_owner != null) {
        _owner.getIpAccessLists().put(name, ipAccessList);
      }
      return ipAccessList;
    }

    public Builder setLines(List<AclLine> lines) {
      _lines = lines;
      return this;
    }

    public Builder setLines(AclLine... lines) {
      _lines = ImmutableList.copyOf(lines);
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }

    public Builder setOwner(Configuration owner) {
      _owner = owner;
      return this;
    }

    public Builder setSourceName(@Nullable String sourceName) {
      _sourceName = sourceName;
      return this;
    }

    public Builder setSourceType(@Nullable String sourceType) {
      _sourceType = sourceType;
      return this;
    }
  }

  private static final String PROP_LINES = "lines";
  private static final String PROP_NAME = "name";
  private static final String PROP_SOURCE_NAME = "sourceName";
  private static final String PROP_SOURCE_TYPE = "sourceType";

  static boolean bothNullOrSameName(IpAccessList a, IpAccessList b) {
    if (a == null && b == null) {
      return true;
    } else if (a != null && b != null) {
      return a.getName().equals(b.getName());
    } else {
      return false;
    }
  }

  public static Builder builder() {
    return new Builder(null);
  }

  public static Builder builder(@Nullable Supplier<String> nameGenerator) {
    return new Builder(nameGenerator);
  }

  private final @Nonnull List<AclLine> _lines;
  private final @Nonnull String _name;
  private final String _sourceName;
  private final String _sourceType;

  @JsonCreator
  private static IpAccessList jsonCreator(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_LINES) @Nullable List<AclLine> lines,
      @JsonProperty(PROP_SOURCE_NAME) @Nullable String sourceName,
      @JsonProperty(PROP_SOURCE_TYPE) @Nullable String sourceType) {
    checkArgument(name != null, "IpAccessList missing %s", PROP_NAME);
    return new IpAccessList(name, firstNonNull(lines, ImmutableList.of()), sourceName, sourceType);
  }

  private IpAccessList(
      @Nonnull String name,
      @Nonnull List<AclLine> lines,
      @Nullable String sourceName,
      @Nullable String sourceType) {
    _name = name;
    _lines = ImmutableList.copyOf(lines);
    _sourceName = sourceName;
    _sourceType = sourceType;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof IpAccessList)) {
      return false;
    }
    IpAccessList other = (IpAccessList) o;
    return _name.equals(other._name) && other._lines.equals(_lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _lines);
  }

  public FilterResult filter(
      Flow flow,
      @Nullable String srcInterface,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces) {
    AclLineEvaluator lineEvaluator =
        new AclLineEvaluator(flow, srcInterface, availableAcls, namedIpSpaces);
    for (int i = 0; i < _lines.size(); i++) {
      LineAction action = lineEvaluator.visit(_lines.get(i));
      if (action != null) {
        return new FilterResult(i, action);
      }
    }
    return new FilterResult(null, LineAction.DENY);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  /** The lines against which to check an IPV4 packet. */
  @JsonProperty(PROP_LINES)
  public @Nonnull List<AclLine> getLines() {
    return _lines;
  }

  @JsonProperty(PROP_SOURCE_NAME)
  public @Nullable String getSourceName() {
    return _sourceName;
  }

  @JsonProperty(PROP_SOURCE_TYPE)
  public @Nullable String getSourceType() {
    return _sourceType;
  }

  @JsonIgnore
  public boolean isComposite() {
    return getName().startsWith("~");
  }

  @Override
  public String toString() {
    StringBuilder output =
        new StringBuilder().append(getClass().getSimpleName()).append(":").append(_name);
    for (AclLine line : _lines) {
      output.append("\n");
      output.append(line);
    }
    return output.toString();
  }

  public Builder toBuilder() {
    return builder()
        .setName(_name)
        .setLines(ImmutableList.copyOf(_lines))
        .setSourceName(_sourceName)
        .setSourceType(_sourceType);
  }
}
