package org.batfish.question.filtertable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** {@link Answerer} to answer the {@link FilterTableQuestion} */
@ParametersAreNonnullByDefault
public class FilterTableAnswerer extends Answerer {

  public FilterTableAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Returns a {@link TableMetadata} object based on the (outer) question and the metadata for the
   * innerquestion, by selecting only columns mentioned in the question.
   *
   * @param question The outer question
   * @param innerMetadata The inner metadata
   * @return The final metadata
   */
  static TableMetadata createMetadata(FilterTableQuestion question, TableMetadata innerMetadata) {
    if (question.getColumns() == null) {
      return innerMetadata;
    } else {
      List<ColumnMetadata> innerColumns = innerMetadata.getColumnMetadata();

      // check if columns mentions names that are not in the innermetadata
      Sets.SetView<String> unknownColumns =
          Sets.difference(
              question.getColumns(),
              innerColumns.stream().map(ColumnMetadata::getName).collect(Collectors.toSet()));
      if (!unknownColumns.isEmpty()) {
        throw new IllegalArgumentException("Unknown columns: " + unknownColumns);
      }

      List<ColumnMetadata> columnMetadata =
          innerColumns.stream()
              .filter(cm -> question.getColumns().contains(cm.getName()))
              .collect(Collectors.toList());

      return new TableMetadata(columnMetadata, innerMetadata.getTextDesc());
    }
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    FilterTableQuestion question = (FilterTableQuestion) _question;
    Question innerQuestion = question.getInnerQuestion();
    AnswerElement innerAnswer = _batfish.createAnswerer(innerQuestion).answer(snapshot);
    if (!(innerAnswer instanceof TableAnswerElement)) {
      throw new IllegalArgumentException("The inner question does not produce table answers");
    }
    TableAnswerElement innerTable = (TableAnswerElement) innerAnswer;

    TableAnswerElement answer =
        new TableAnswerElement(createMetadata(question, innerTable.getMetadata()));

    Multiset<Row> answerRows = innerTable.getRows().getData();
    if (question.getFilter() != null) {
      answerRows = filterRows(question.getFilter(), answerRows);
    }
    if (question.getColumns() != null) {
      answerRows = selectColumns(question.getColumns(), answerRows);
    }

    answer.postProcessAnswer(question, answerRows);
    return answer;
  }

  /**
   * Given a filter and a set of rows, returns the subset of rows that match the filter
   *
   * @param filter The filter that should be matched
   * @param inputRows The input set of rows
   * @return A new set with matching rows
   */
  @VisibleForTesting
  static Multiset<Row> filterRows(Filter filter, Multiset<Row> inputRows) {
    return inputRows.stream()
        .filter(filter::matches)
        .collect(ImmutableMultiset.toImmutableMultiset());
  }

  /**
   * Selects specified columns from the Multiset of rows that is provided as input. A new set is
   * created and returned, and the input is not modified.
   *
   * @param columns The columns to select
   * @param inputRows The input set.
   * @return A new set of rows with specified columns
   */
  @VisibleForTesting
  static Multiset<Row> selectColumns(Set<String> columns, Multiset<Row> inputRows) {
    return inputRows.stream()
        .map(row -> Row.builder().putAll(row, columns).build())
        .collect(ImmutableMultiset.toImmutableMultiset());
  }
}
