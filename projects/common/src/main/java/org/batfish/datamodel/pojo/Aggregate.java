package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Aggregate extends BfObject {

  public enum AggregateType {
    CLOUD,
    REGION,
    SUBNET,
    VNET, // "VPC" in AWS
    UNKNOWN
  }

  private static final String PROP_CONTENTS = "contents";
  private static final String PROP_NAME = "name";
  private static final String PROP_TYPE = "type";

  private Set<String> _contents;

  private final String _name;

  private AggregateType _type;

  public Aggregate(String name, AggregateType type) {
    super(makeId(name));
    _name = name;
    _type = type;
    _contents = new HashSet<>();
  }

  @JsonCreator
  private static Aggregate jsonCreator(
      @JsonProperty(PROP_ID) @Nullable String id,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_CONTENTS) @Nullable Set<String> contents,
      @JsonProperty(PROP_TYPE) @Nullable AggregateType type) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(id != null, "Missing %s", PROP_ID);
    checkArgument(type != null, "Missing %s", PROP_TYPE);
    Aggregate aggregate = new Aggregate(name, type);
    aggregate.setType(type);
    aggregate.setContents(ImmutableSet.copyOf(firstNonNull(contents, ImmutableSet.of())));
    return aggregate;
  }

  @JsonProperty(PROP_CONTENTS)
  public Set<String> getContents() {
    return _contents;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_TYPE)
  public AggregateType getType() {
    return _type;
  }

  public void setContents(Set<String> contents) {
    _contents = contents;
  }

  public void setType(AggregateType type) {
    _type = type;
  }

  static @Nonnull String makeId(@Nonnull String name) {
    return "aggregate-" + name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Aggregate)) {
      return false;
    }
    Aggregate a = (Aggregate) o;
    return _type == a._type
        && _name.equals(a._name)
        && _contents.equals(a._contents)
        && Objects.equals(getId(), a.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type.ordinal(), _name, _contents, getId());
  }
}
