package org.batfish.question.interfaceproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class InterfacePropertiesAnswerer extends Answerer {

  public static final String COL_INTERFACE = "interface";

  public InterfacePropertiesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  static TableMetadata createMetadata(InterfacePropertiesQuestion question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.<ColumnMetadata>builder()
            .add(new ColumnMetadata(COL_INTERFACE, Schema.INTERFACE, "Interface", true, false))
            .addAll(
                question
                    .getPropertySpec()
                    .getMatchingProperties()
                    .stream()
                    .map(
                        prop ->
                            new ColumnMetadata(
                                prop,
                                InterfacePropertySpecifier.JAVA_MAP.get(prop).getSchema(),
                                "Property " + prop,
                                false,
                                true))
                    .collect(Collectors.toList()))
            .build();

    String textDesc = String.format("Properties of interface ${%s}.", COL_INTERFACE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(columnMetadata, textDesc);
  }

  @Override
  public AnswerElement answer() {
    InterfacePropertiesQuestion question = (InterfacePropertiesQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodeRegex().getMatchingNodes(_batfish);

    TableMetadata tableMetadata = createMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    Multiset<Row> propertyRows = rawAnswer(question, configurations, nodes);

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  @VisibleForTesting
  static Multiset<Row> rawAnswer(
      InterfacePropertiesQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes) {
    Multiset<Row> rows = HashMultiset.create();

    for (String nodeName : nodes) {
      for (Interface iface : configurations.get(nodeName).getInterfaces().values()) {
        if (!question.getInterfaceRegex().matches(iface)) {
          continue;
        }
        RowBuilder row =
            Row.builder().put(COL_INTERFACE, new NodeInterfacePair(nodeName, iface.getName()));

        for (String property : question.getPropertySpec().getMatchingProperties()) {
          PropertyDescriptor<Interface> propertyDescriptor =
              InterfacePropertySpecifier.JAVA_MAP.get(property);
          try {
            PropertySpecifier.fillProperty(propertyDescriptor, iface, property, row);
          } catch (ClassCastException e) {
            throw new BatfishException(
                String.format(
                    "Type mismatch between property value ('%s') and Schema ('%s') for property '%s' for interface '%s': %s",
                    propertyDescriptor.getGetter().apply(iface),
                    propertyDescriptor.getSchema(),
                    property,
                    iface,
                    e.getMessage()),
                e);
          }
        }

        rows.add(row.build());
      }
    }

    return rows;
  }
}
