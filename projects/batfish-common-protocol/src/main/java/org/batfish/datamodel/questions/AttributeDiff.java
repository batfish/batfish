package org.batfish.datamodel.questions;

import static java.util.Comparator.comparing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

// TODO; There is some relationship between this class and Schema, which we can consider
// reconciling.

/** A shared interface for things we pack in as attribute differences */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = StringDiff.class, name = "String"),
  @JsonSubTypes.Type(value = StringListDiff.class, name = "StringList"),
})
@ParametersAreNonnullByDefault
public abstract class AttributeDiff implements Comparable<AttributeDiff> {

  protected static final String PROP_FIELD_NAME = "fieldName";

  @Nonnull protected final String _fieldName;

  public AttributeDiff(String fieldName) {
    _fieldName = fieldName;
  }

  @JsonProperty(PROP_FIELD_NAME)
  @Nonnull
  public String getFieldName() {
    return _fieldName;
  }

  @Override
  public int compareTo(AttributeDiff that) {
    return comparing(AttributeDiff::getFieldName).compare(this, that);
  }
}
