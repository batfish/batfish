package org.batfish.question.referencedstructures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** An {@link Answerer} for {@link ReferencedStructuresQuestion}. */
public class ReferencedStructuresAnswerer extends Answerer {

  static final String COL_CONTEXT = "Context";
  static final String COL_SOURCE_LINES = "Source_Lines";
  static final String COL_STRUCTURE_NAME = "Structure_Name";
  static final String COL_STRUCTURE_TYPE = "Structure_Type";

  public ReferencedStructuresAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    ReferencedStructuresQuestion question = (ReferencedStructuresQuestion) _question;
    Set<String> includeNodes = question.getNodes().getMatchingNodes(_batfish, snapshot);
    Multimap<String, String> hostnameFilenameMap =
        _batfish.loadParseVendorConfigurationAnswerElement(snapshot).getFileMap();
    Set<String> includeFiles =
        hostnameFilenameMap.entries().stream()
            .filter(e -> includeNodes.contains(e.getKey()))
            .map(Entry::getValue)
            .collect(Collectors.toSet());

    Pattern includeStructureNames = Pattern.compile(question.getNames(), Pattern.CASE_INSENSITIVE);
    Pattern includeStructureTypes = Pattern.compile(question.getTypes(), Pattern.CASE_INSENSITIVE);

    Multiset<Row> rows = LinkedHashMultiset.create();
    _batfish
        .loadConvertConfigurationAnswerElementOrReparse(snapshot)
        .getReferencedStructures()
        .forEach(
            (filename, value) -> {
              if (!includeFiles.contains(filename)) {
                return;
              }
              List<Row> rows1 = new ArrayList<>();
              value.forEach(
                  (structType, byName) -> {
                    if (!includeStructureTypes.matcher(structType).matches()) {
                      return;
                    }
                    byName.forEach(
                        (name, byContext) -> {
                          if (!includeStructureNames.matcher(name).matches()) {
                            return;
                          }
                          byContext.forEach(
                              (context, lineNums) -> {
                                rows1.add(
                                    Row.of(
                                        COL_STRUCTURE_TYPE,
                                        structType,
                                        COL_STRUCTURE_NAME,
                                        name,
                                        COL_CONTEXT,
                                        context,
                                        COL_SOURCE_LINES,
                                        new FileLines(filename, lineNums)));
                              });
                        });
                  });
              rows.addAll(rows1);
            });

    TableAnswerElement table = new TableAnswerElement(createMetadata());
    table.postProcessAnswer(_question, rows);
    return table;
  }

  public static TableMetadata createMetadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(
                COL_STRUCTURE_TYPE, Schema.STRING, "Type of structure referenced", false, false),
            new ColumnMetadata(
                COL_STRUCTURE_NAME, Schema.STRING, "The referenced structure", true, false),
            new ColumnMetadata(
                COL_CONTEXT,
                Schema.STRING,
                "Configuration context in which the reference appears",
                true,
                false),
            new ColumnMetadata(
                COL_SOURCE_LINES,
                Schema.FILE_LINES,
                "Lines where reference appears",
                false,
                false));

    return new TableMetadata(
        columnMetadata,
        String.format(
            "A structure of type ${%s} named ${%s} is referred to in the context ${%s} at line(s)"
                + " ${%s}",
            COL_STRUCTURE_TYPE, COL_STRUCTURE_NAME, COL_CONTEXT, COL_SOURCE_LINES));
  }
}
