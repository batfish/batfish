package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Multiset;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.questions.Assertion;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.datamodel.questions.Question;

/** A base class for tabular answers */
public abstract class TableAnswerElement extends AnswerElement {

  protected static final String PROP_EXCLUDED_ROWS = "excludedRows";

  protected static final String PROP_METADATA = "metadata";

  protected static final String PROP_ROWS = "rows";

  List<ExcludedRows> _excludedRows;

  Rows _rows;

  TableMetadata _tableMetadata;

  @JsonCreator
  public TableAnswerElement(@Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    _tableMetadata = tableMetadata;
    _rows = new Rows();
    _excludedRows = new LinkedList<>();
  }

  /**
   * Adds a new row to data rows
   *
   * @param row The row to add
   */
  public void addRow(Row row) {
    _rows.add(row);
  }

  /**
   * Adds a new row to excluded data rows
   *
   * @param row The row to add
   */
  public void addExcludedRow(Row row, String exclusionName) {
    for (ExcludedRows exRows : _excludedRows) {
      if (exRows.getExclusionName().equals(exclusionName)) {
        exRows.addRow(row);
        return;
      }
    }
    // no matching exclusionName found; create a new one
    ExcludedRows rows = new ExcludedRows(exclusionName, new Rows());
    rows.addRow(row);
    _excludedRows.add(rows);
  }

  public AnswerSummary computeSummary(Assertion assertion, String notes) {
    int numPassed = 0;
    int numFailed = 0;
    if (assertion != null) {
      if (evaluateAssertion(assertion)) {
        numPassed = 1;
      } else {
        numFailed = 1;
      }
    }
    return new AnswerSummary(notes, numFailed, numPassed, _rows.size());
  }

  public AnswerSummary computeSummary(Assertion assertion) {
    int numPassed = 0;
    int numFailed = 0;
    if (assertion != null) {
      if (evaluateAssertion(assertion)) {
        numPassed = 1;
      } else {
        numFailed = 1;
      }
    }
    String notes = "Found " + _rows.size() + " results";
    return new AnswerSummary(notes, numFailed, numPassed, _rows.size());
  }

  /**
   * Evaluates the assertion over the rows
   *
   * @return The results of the evaluation
   */
  public boolean evaluateAssertion(Assertion assertion) {
    if (assertion == null) {
      throw new IllegalArgumentException("Provided assertion object cannot be null");
    }
    switch (assertion.getType()) {
      case countequals:
        return _rows.size() == assertion.getExpect().asInt();
      case countlessthan:
        return _rows.size() < assertion.getExpect().asInt();
      case countmorethan:
        return _rows.size() > assertion.getExpect().asInt();
      case equals:
        Rows expectedEntries;
        try {
          expectedEntries =
              BatfishObjectMapper.mapper().readValue(assertion.getExpect().toString(), Rows.class);
        } catch (IOException e) {
          throw new BatfishException("Could not recover Rows object from expect", e);
        }
        return _rows.equals(expectedEntries);
      default:
        throw new BatfishException("Unhandled assertion type: " + assertion.getType());
    }
  }

  /**
   * Converts the {@link Row} object into the actual object that is produced by concrete (child)
   * TableAnswerElement. This method is overriden by the child classes that are aware of what is
   * embedded in the object and provides a standard way to obtain the object from their Row objects.
   *
   * @param row The row to convert
   * @return The converted object
   */
  public abstract Object fromRow(Row row);

  @JsonProperty(PROP_EXCLUDED_ROWS)
  public List<ExcludedRows> getExcludedRows() {
    return _excludedRows;
  }

  @JsonProperty(PROP_METADATA)
  public TableMetadata getMetadata() {
    return _tableMetadata;
  }

  @JsonProperty(PROP_ROWS)
  public Rows getRows() {
    return _rows;
  }

  /**
   * Given all initial answer objects for a question, this function will first process the
   * exclusions and then compute the summary.
   *
   * @param question The question that produced the answer objects
   * @param objects The answer objects to process
   */
  public void postProcessAnswer(Question question, Multiset<?> objects) {
    objects.forEach(
        object -> {
          Row row = toRow(object);

          // exclude or not?
          Exclusion exclusion = Exclusion.covered(row, question.getExclusions());
          if (exclusion != null) {
            addExcludedRow(row, exclusion.getName());
          } else {
            addRow(row);
          }
        });

    setSummary(computeSummary(question.getAssertion()));
  }

  @JsonProperty(PROP_EXCLUDED_ROWS)
  private void setExcludedRows(List<ExcludedRows> excludedRows) {
    _excludedRows = excludedRows == null ? new LinkedList<>() : excludedRows;
  }

  @JsonProperty(PROP_ROWS)
  private void setRows(Rows rows) {
    _rows = rows == null ? new Rows() : rows;
  }

  /**
   * Convert the given object into a {@link Row}. This method, which is overridden by child classes
   * that are aware of what is embedded in the object, allows for a generic {@link
   * TableAnswerElement#postProcessAnswer(Question, Multiset)} method.
   *
   * @param object The object to convert
   * @return The converted {@link Row}
   */
  public abstract Row toRow(Object object);
}
