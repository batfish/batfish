package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * The {@link Container Container} is an object representation of the container for BatFish service.
 *
 * <p>Each {@link Container Container} contains a name and a summary of the container {@link
 * #_name}.
 */
public final class Container {
  private static final String PROP_NAME = "name";
  private static final String PROP_CREATED_AT = "createdAt";
  private static final String PROP_TESTRIGS_COUNT = "testrigsCount";
  private static final String PROP_ANALYSES_COUNT = "analysesCount";

  private String _name;
  private String _createdAt;
  private int _testrigsCount;
  private int _analysesCount;

  @JsonCreator
  public Container(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_CREATED_AT) String createdAt,
      @JsonProperty(PROP_TESTRIGS_COUNT) int testrigsCount,
      @JsonProperty(PROP_ANALYSES_COUNT) int analysesCount) {
    this._name = name;
    this._createdAt = createdAt;
    this._testrigsCount = testrigsCount;
    this._analysesCount = analysesCount;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_CREATED_AT)
  public String getCreatedAt() {
    return _createdAt;
  }

  @JsonProperty(PROP_TESTRIGS_COUNT)
  public int getTestrigsCount() {
    return _testrigsCount;
  }

  @JsonProperty(PROP_ANALYSES_COUNT)
  public int getAnalysesCount() {
    return _analysesCount;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Container.class)
        .add(PROP_NAME, _name)
        .add(PROP_CREATED_AT, _createdAt)
        .add(PROP_TESTRIGS_COUNT, _testrigsCount)
        .add(PROP_ANALYSES_COUNT, _analysesCount)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Container)) {
      return false;
    }
    Container other = (Container) o;
    return Objects.equals(_name, other._name)
        && Objects.equals(_createdAt, other._createdAt)
        && Objects.equals(_testrigsCount, other._testrigsCount)
        && Objects.equals(_analysesCount, other._analysesCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _createdAt, _testrigsCount, _analysesCount);
  }
}
