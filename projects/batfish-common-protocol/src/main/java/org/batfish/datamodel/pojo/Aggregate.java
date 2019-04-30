package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;

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
    super(getId(name));
    _name = name;
    _type = type;
    _contents = new HashSet<>();
  }

  @JsonCreator
  public Aggregate(@JsonProperty(PROP_NAME) String name) {
    this(name, AggregateType.UNKNOWN);
  }

  @JsonProperty(PROP_CONTENTS)
  public Set<String> getContents() {
    return _contents;
  }

  public static String getId(String name) {
    return "aggregate-" + name;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_TYPE)
  public AggregateType getType() {
    return _type;
  }

  @JsonProperty(PROP_CONTENTS)
  public void setContents(Set<String> contents) {
    _contents = contents;
  }

  @JsonProperty(PROP_TYPE)
  public void setType(AggregateType type) {
    _type = type;
  }
}
