package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

@AutoService(Plugin.class)
public class UnusedStructuresQuestionPlugin extends QuestionPlugin {

  public static class UnusedStructuresAnswerer extends Answerer {

    public static final String COL_SOURCE_LINES = "Source_Lines";
    public static final String COL_STRUCTURE_NAME = "Structure_Name";
    public static final String COL_STRUCTURE_TYPE = "Structure_Type";

    public UnusedStructuresAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public TableAnswerElement answer(NetworkSnapshot snapshot) {
      UnusedStructuresQuestion question = (UnusedStructuresQuestion) _question;

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
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
          definedStructures =
              _batfish
                  .loadConvertConfigurationAnswerElementOrReparse(snapshot)
                  .getDefinedStructures();
      definedStructures.entrySet().stream()
          .filter(e -> includeFiles.contains(e.getKey()))
          .forEach(e -> rows.addAll(processEntryToRows(e)));

      TableAnswerElement table = new TableAnswerElement(createMetadata(question));
      table.postProcessAnswer(_question, rows);
      return table;
    }

    @VisibleForTesting
    // Entry is: filename -> struct type -> struct name -> defined structure info
    static List<Row> processEntryToRows(
        Entry<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> e) {
      List<Row> rows = new ArrayList<>();
      String filename = e.getKey();
      for (Entry<String, SortedMap<String, DefinedStructureInfo>> e1 : e.getValue().entrySet()) {
        String structType = e1.getKey();
        for (Entry<String, DefinedStructureInfo> e2 : e1.getValue().entrySet()) {
          String name = e2.getKey();
          DefinedStructureInfo info = e2.getValue();
          if (info.getNumReferrers() == 0) {
            rows.add(
                Row.builder(COLUMN_METADATA_MAP)
                    .put(COL_STRUCTURE_TYPE, structType)
                    .put(COL_STRUCTURE_NAME, name)
                    .put(
                        COL_SOURCE_LINES,
                        new FileLines(filename, info.getDefinitionLines().enumerate()))
                    .build());
          }
        }
      }
      return rows;
    }

    private static final List<ColumnMetadata> COLUMN_METADATA =
        ImmutableList.of(
            new ColumnMetadata(
                COL_STRUCTURE_TYPE,
                Schema.STRING,
                "Vendor-specific type of the structure",
                true,
                false),
            new ColumnMetadata(
                COL_STRUCTURE_NAME, Schema.STRING, "Name of the structure", true, false),
            new ColumnMetadata(
                COL_SOURCE_LINES,
                Schema.FILE_LINES,
                "File and line numbers where the structure is defined",
                true,
                false));

    private static final Map<String, ColumnMetadata> COLUMN_METADATA_MAP =
        COLUMN_METADATA.stream()
            .collect(ImmutableMap.toImmutableMap(ColumnMetadata::getName, cm -> cm));
    private static final String DEFAULT_TEXT_DESC =
        String.format(
            "An unused structure of type ${%s} named ${%s} is defined at ${%s}.",
            COL_STRUCTURE_TYPE, COL_STRUCTURE_NAME, COL_SOURCE_LINES);

    public static TableMetadata createMetadata(Question question) {
      String textDesc = DEFAULT_TEXT_DESC;
      DisplayHints dhints = question.getDisplayHints();
      if (dhints != null && dhints.getTextDesc() != null) {
        textDesc = dhints.getTextDesc();
      }
      return new TableMetadata(COLUMN_METADATA, textDesc);
    }
  }

  /**
   * Outputs cases where structures (e.g., ACL, routemaps) are defined but not used.
   *
   * <p>Such occurrences could be configuration errors or leftover cruft.
   */
  public static class UnusedStructuresQuestion extends Question {
    private static final String PROP_NODES = "nodes";

    @Nullable private final String _nodes;

    @JsonCreator
    private static UnusedStructuresQuestion create(@JsonProperty(PROP_NODES) String nodes) {
      return new UnusedStructuresQuestion(nodes);
    }

    UnusedStructuresQuestion() {
      this(null);
    }

    public UnusedStructuresQuestion(@Nullable String nodes) {
      _nodes = nodes;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "unusedStructures";
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
    return new UnusedStructuresAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new UnusedStructuresQuestion();
  }
}
