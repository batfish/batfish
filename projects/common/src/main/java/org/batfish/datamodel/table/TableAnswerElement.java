package org.batfish.datamodel.table;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.questions.Assertion;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.datamodel.questions.Question;

/** Holds tabular answers. */
public final class TableAnswerElement extends AnswerElement {
  private static final String PROP_EXCLUDED_ROWS = "excludedRows";
  private static final String PROP_METADATA = "metadata";
  private static final String PROP_ROWS = "rows";
  private static final String PROP_WARNINGS = "warnings";

  private List<ExcludedRows> _excludedRows;
  private Set<String> _columnNames;
  private Rows _rows;
  private TableMetadata _tableMetadata;
  private List<String> _warnings;

  @JsonCreator
  public TableAnswerElement(@JsonProperty(PROP_METADATA) @Nonnull TableMetadata tableMetadata) {
    _tableMetadata = tableMetadata;
    _columnNames =
        tableMetadata.getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableSet.toImmutableSet());
    _rows = new Rows();
    _excludedRows = new LinkedList<>();
    _warnings = new LinkedList<>();
  }

  /**
   * Adds a new row to data rows
   *
   * @param row The row to add
   */
  public @Nonnull TableAnswerElement addRow(Row row) {
    checkArgument(
        row.getColumnNames().equals(_columnNames),
        "Row columns %s do not match metadata columns metadata %s",
        row.getColumnNames(),
        _columnNames);
    _rows.add(row);
    return this;
  }

  /**
   * Adds a new row to excluded data rows
   *
   * @param row The row to add
   */
  public @Nonnull TableAnswerElement addExcludedRow(Row row, String exclusionName) {
    checkArgument(
        row.getColumnNames().equals(_columnNames),
        "Row columns %s do not match metadata columns metadata %s",
        row.getColumnNames(),
        _columnNames);
    for (ExcludedRows exRows : _excludedRows) {
      if (exRows.getExclusionName().equals(exclusionName)) {
        exRows.addRow(row);
        return this;
      }
    }
    // no matching exclusionName found; create a new one
    ExcludedRows rows = new ExcludedRows(exclusionName);
    rows.addRow(row);
    _excludedRows.add(rows);
    return this;
  }

  /** Adds a warning to the answer. */
  public @Nonnull TableAnswerElement addWarning(String warning) {
    _warnings.add(warning);
    return this;
  }

  /** Computes the summary of this table, given the assertion */
  public AnswerSummary computeSummary(Assertion assertion) {
    String notes = "Found " + _rows.size() + " results";
    return computeSummary(assertion, notes);
  }

  /** Computes the summary of this table, given the assertion and notes */
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

  /**
   * Evaluates the assertion over the rows
   *
   * @return The results of the evaluation
   */
  public boolean evaluateAssertion(Assertion assertion) {
    if (assertion == null) {
      throw new IllegalArgumentException("Provided assertion object cannot be null");
    }
    return switch (assertion.getType()) {
      case countequals -> _rows.size() == assertion.getExpect().asInt();
      case countlessthan -> _rows.size() < assertion.getExpect().asInt();
      case countmorethan -> _rows.size() > assertion.getExpect().asInt();
      case equals -> {
        Rows expectedEntries;
        try {
          expectedEntries =
              BatfishObjectMapper.mapper().readValue(assertion.getExpect().toString(), Rows.class);
        } catch (IOException e) {
          throw new BatfishException("Could not recover Rows object from expect", e);
        }
        yield _rows.equals(expectedEntries);
      }
    };
  }

  @JsonProperty(PROP_EXCLUDED_ROWS)
  public List<ExcludedRows> getExcludedRows() {
    return _excludedRows;
  }

  @JsonProperty(PROP_METADATA)
  public TableMetadata getMetadata() {
    return _tableMetadata;
  }

  @JsonIgnore
  public Rows getRows() {
    return _rows;
  }

  @JsonProperty(PROP_ROWS)
  public List<Row> getRowsList() {
    return ImmutableList.copyOf(_rows.iterator());
  }

  @JsonProperty(PROP_WARNINGS)
  public List<String> getWarnings() {
    return _warnings;
  }

  /**
   * Given an initial set of rows produced by an {@link org.batfish.common.Answerer}, this procedure
   * processes exclusions, assertions, and summary to update this object.
   *
   * @param question The question that generated the initial set of rows
   * @param initialSet The initial set of rows
   */
  public void postProcessAnswer(Question question, Iterable<Row> initialSet) {
    initialSet.forEach(
        initialRow -> {
          // exclude or not?
          Exclusion exclusion = Exclusion.covered(initialRow, question.getExclusions());
          if (exclusion != null) {
            addExcludedRow(initialRow, exclusion.getName());
          } else {
            addRow(initialRow);
          }
        });

    setSummary(computeSummary(question.getAssertion()));
  }

  @JsonProperty(PROP_EXCLUDED_ROWS)
  private void setExcludedRows(List<ExcludedRows> excludedRows) {
    _excludedRows = excludedRows == null ? new LinkedList<>() : excludedRows;
  }

  @JsonProperty(PROP_ROWS)
  private void setRowsList(List<Row> rows) {
    _rows = new Rows();
    if (rows != null) {
      rows.forEach(_rows::add);
    }
  }
}
