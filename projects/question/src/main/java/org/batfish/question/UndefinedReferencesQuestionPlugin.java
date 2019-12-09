package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

@AutoService(Plugin.class)
public class UndefinedReferencesQuestionPlugin extends QuestionPlugin {

  public static class UndefinedReferencesAnswerer extends Answerer {

    static final String COL_CONTEXT = "Context";
    static final String COL_FILENAME = "File_Name";
    static final String COL_LINES = "Lines";
    static final String COL_REF_NAME = "Ref_Name";
    static final String COL_STRUCT_TYPE = "Struct_Type";

    public UndefinedReferencesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public TableAnswerElement answer(NetworkSnapshot snapshot) {
      UndefinedReferencesQuestion question = (UndefinedReferencesQuestion) _question;

      // Find all the filenames that produced the queried nodes. This might have false positives if
      // a file produced multiple nodes, but that was already mis-handled before. Need to rewrite
      // this question as a TableAnswerElement.
      Set<String> includeNodes =
          question.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot));
      Multimap<String, String> hostnameFilenameMap =
          _batfish.loadParseVendorConfigurationAnswerElement(snapshot).getFileMap();
      Set<String> includeFiles =
          hostnameFilenameMap.entries().stream()
              .filter(e -> includeNodes.contains(e.getKey()))
              .map(Entry::getValue)
              .collect(Collectors.toSet());

      Multiset<Row> rows = LinkedHashMultiset.create();
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          undefinedReferences =
              _batfish
                  .loadConvertConfigurationAnswerElementOrReparse(snapshot)
                  .getUndefinedReferences();
      undefinedReferences.entrySet().stream()
          .filter(e -> includeFiles.contains(e.getKey()))
          .forEach(e -> rows.addAll(processEntryToRows(e)));

      TableAnswerElement table = new TableAnswerElement(createMetadata());
      table.postProcessAnswer(_question, rows);
      return table;
    }

    @VisibleForTesting
    // Entry is: filename -> struct type -> struct name -> context -> line nums
    public static List<Row> processEntryToRows(
        Entry<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
            e) {
      List<Row> rows = new ArrayList<>();
      String filename = e.getKey();
      for (Entry<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> e1 :
          e.getValue().entrySet()) {
        String structType = e1.getKey();
        for (Entry<String, SortedMap<String, SortedSet<Integer>>> e2 : e1.getValue().entrySet()) {
          String name = e2.getKey();
          for (Entry<String, SortedSet<Integer>> e3 : e2.getValue().entrySet()) {
            String context = e3.getKey();
            SortedSet<Integer> lineNums = e3.getValue();
            rows.add(
                Row.of(
                    COL_FILENAME,
                    filename,
                    COL_STRUCT_TYPE,
                    structType,
                    COL_REF_NAME,
                    name,
                    COL_CONTEXT,
                    context,
                    COL_LINES,
                    new FileLines(filename, lineNums)));
          }
        }
      }
      return rows;
    }

    public static TableMetadata createMetadata() {
      List<ColumnMetadata> columnMetadata =
          ImmutableList.of(
              new ColumnMetadata(
                  COL_FILENAME, Schema.STRING, "File containing reference", true, false),
              new ColumnMetadata(
                  COL_STRUCT_TYPE,
                  Schema.STRING,
                  "Type of struct reference is supposed to be",
                  false,
                  false),
              new ColumnMetadata(
                  COL_REF_NAME, Schema.STRING, "The undefined reference", true, false),
              new ColumnMetadata(
                  COL_CONTEXT, Schema.STRING, "Context of undefined reference", true, false),
              new ColumnMetadata(
                  COL_LINES, Schema.FILE_LINES, "Lines where reference appears", false, false));

      return new TableMetadata(
          columnMetadata,
          String.format(
              "'${%s}' has undefined '${%s}' called '${%s}' being used as '${%s}' at line(s) ${%s}",
              COL_FILENAME, COL_STRUCT_TYPE, COL_REF_NAME, COL_CONTEXT, COL_LINES));
    }
  }

  /**
   * Outputs cases where undefined structures (e.g., ACL, routemaps) are referenced.
   *
   * <p>Such occurrences indicate configuration errors and can have serious consequences with some
   * vendors.
   */
  public static class UndefinedReferencesQuestion extends Question {
    private static final String PROP_NODES = "nodes";

    @Nullable private final String _nodes;

    @JsonCreator
    private static UndefinedReferencesQuestion create(@JsonProperty(PROP_NODES) String nodes) {
      return new UndefinedReferencesQuestion(nodes);
    }

    UndefinedReferencesQuestion() {
      this(null);
    }

    UndefinedReferencesQuestion(@Nullable String nodes) {
      _nodes = nodes;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "undefinedReferences";
    }

    @Nullable
    @JsonProperty(PROP_NODES)
    public String getNodes() {
      return _nodes;
    }

    @Nonnull
    @JsonIgnore
    public NodeSpecifier getNodeSpecifier() {
      return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new UndefinedReferencesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new UndefinedReferencesQuestion();
  }
}
