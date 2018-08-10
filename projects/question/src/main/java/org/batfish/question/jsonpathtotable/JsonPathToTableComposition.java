package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Schema;

/**
 * Describes how to compose a column in {@link JsonPathToTableQuery} using values extracted per
 * {@link JsonPathToTableExtraction}.
 */
public class JsonPathToTableComposition extends JsonPathToTableColumn {

  private static final String PROP_DICTIONARY = "dictionary";

  @Nonnull private Map<String, String> _dictionary;

  @JsonCreator
  public JsonPathToTableComposition(
      @JsonProperty(PROP_SCHEMA) Schema schema,
      @JsonProperty(PROP_DICTIONARY) Map<String, String> dictionary,
      @JsonProperty(PROP_DESCRIPTION) String description,
      @JsonProperty(PROP_INCLUDE) Boolean include,
      @JsonProperty(PROP_IS_KEY) Boolean isKey,
      @JsonProperty(PROP_IS_VALUE) Boolean isValue) {
    super(schema, description, include, isKey, isValue);

    if (dictionary == null || dictionary.isEmpty()) {
      throw new IllegalArgumentException("Dictionary not specified or empty in the composition");
    }
    _dictionary = dictionary;
  }

  @JsonProperty(PROP_DICTIONARY)
  public Map<String, String> getDictionary() {
    return _dictionary;
  }

  /**
   * Returns all variable names that are used in the composition.
   *
   * @return A {@link Set} of variable name strings.
   */
  @JsonIgnore
  public Set<String> getVars() {
    Set<String> retSet = new HashSet<>(_dictionary.values());
    return retSet;
  }
}
