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
public class JsonPathToTableComposition {

  private static final String PROP_SCHEMA = "schema";

  private static final String PROP_DICTIONARY = "dictionary";

  @Nonnull private Map<String, String> _dictionary;

  @Nonnull private Schema _schema;

  @JsonCreator
  public JsonPathToTableComposition(
      @JsonProperty(PROP_SCHEMA) Schema schema,
      @JsonProperty(PROP_DICTIONARY) Map<String, String> dictionary) {

    if (schema == null) {
      throw new IllegalArgumentException("Schema not specified in the composition");
    }
    if (dictionary == null || dictionary.isEmpty()) {
      throw new IllegalArgumentException("Dictionary not specified or empty in the composition");
    }

    _schema = schema;
    _dictionary = dictionary;
  }

  @JsonProperty(PROP_DICTIONARY)
  public Map<String, String> getDictionary() {
    return _dictionary;
  }

  @JsonProperty(PROP_SCHEMA)
  public Schema getSchema() {
    return _schema;
  }

  /**
   * Returns all variable names that are used in the composition.
   *
   * @return A {@link Set} of variable name strings.
   */
  @JsonIgnore
  public Set<String> getVars() {
    Set<String> retSet = new HashSet<>();
    retSet.addAll(_dictionary.values());
    return retSet;
  }
}
