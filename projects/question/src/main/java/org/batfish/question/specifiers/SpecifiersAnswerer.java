package org.batfish.question.specifiers;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;

public final class SpecifiersAnswerer extends Answerer {

  public SpecifiersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    SpecifiersQuestion question = (SpecifiersQuestion) _question;
    SpecifierContext context = _batfish.specifierContext();

    switch (question.getQueryType()) {
      case IP_SPACE:
        return resolveIpSpace(question, context);
      default:
        throw new IllegalArgumentException("Unhandled query type: " + question.getQueryType());
    }
  }

  private static TableAnswerElement resolveIpSpace(
      SpecifiersQuestion question, SpecifierContext context) {

    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("Locations", Schema.STRING, "Resolution", false, false),
            new ColumnMetadata("IpSpace", Schema.STRING, "IpSpace", false, false));
    TableAnswerElement table = new TableAnswerElement(new TableMetadata(columns));
    Map<String, ColumnMetadata> columnMap = table.getMetadata().toColumnMap();

    Set<Location> locations = question.getLocationSpecifier().resolve(context);
    IpSpaceAssignment ipSpaceAssignment =
        question.getIpSpaceSpecifier().resolve(locations, context);

    for (IpSpaceAssignment.Entry entry : ipSpaceAssignment.getEntries()) {
      table.addRow(
          Row.of(
              columnMap,
              "Locations",
              entry.getLocations().toString(),
              "IpSpace",
              Objects.toString(entry.getIpSpace())));
    }
    return table;
  }
}
