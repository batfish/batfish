package org.batfish.question.specifiers;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
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

    Set<Location> locations = question.getLocationSpecifier().resolve(context);
    IpSpaceAssignment ipSpaceAssignment =
        question.getIpSpaceSpecifier().resolve(locations, context);

    TableAnswerElement table = tableAnswerElement();
    for (IpSpaceAssignment.Entry entry : ipSpaceAssignment.getEntries()) {
      table.addRow(
          Row.of(
              "Locations",
              entry.getLocations().toArray(new Location[0]),
              "IpSpace",
              entry.getIpSpace()));
    }
    return table;
  }

  private static TableAnswerElement tableAnswerElement() {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("Locations", Schema.STRING, "Locations", false, false),
            new ColumnMetadata("IpSpace", Schema.STRING, "IpSpace", false, false));
    DisplayHints displayHints = new DisplayHints();
    return new TableAnswerElement(new TableMetadata(columns, displayHints));
  }
}
