package org.batfish.main.annotate;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.answers.ParseStatus.FAILED;
import static org.batfish.main.CliUtils.readAllFiles;
import static org.batfish.main.CliUtils.relativize;
import static org.batfish.main.CliUtils.resolve;
import static org.batfish.main.CliUtils.writeAllFiles;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BfConsts;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection.SilentSyntaxElem;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationJob.VendorFile;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.main.preprocess.Preprocessor;

/** Tool to annotate configurations with silent syntax and warnings */
public final class Annotate {

  public static void main(String[] args) throws IOException {
    checkArgument(args.length == 2, "Expected arguments: <input_dir> <output_dir>");
    Path inputPath = Paths.get(args[0]);
    Path outputPath = Paths.get(args[1]);

    // Bazel: resolve relative to current working directory. No-op if paths are already absolute.
    String wd = System.getenv("BUILD_WORKING_DIRECTORY");
    if (wd != null) {
      inputPath = Paths.get(wd).resolve(inputPath);
      outputPath = Paths.get(wd).resolve(outputPath);
    }

    Settings settings = new Settings(new String[] {"-storagebase", "/"});
    settings.setPrintParseTree(true);

    annotate(inputPath, outputPath, settings);
  }

  private static void annotate(Path inputPath, Path outputPath, Settings settings)
      throws IOException {
    // Get annotated text for all files in inputPath, then write them under outputPath to the same
    // relative location.
    writeAllFiles(
        resolve(
            outputPath,
            relativize(
                inputPath,
                annotate(
                    readAllFiles(inputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR)),
                    settings))));
  }

  @Nonnull
  private static Map<Path, String> annotate(Map<Path, String> inputData, Settings settings) {
    // For each (path, text) in inputData, return an entry (path, annotated text).
    Map<Path, String> outputData = new ConcurrentHashMap<>(inputData.size());
    inputData.entrySet().parallelStream()
        .forEach(
            inputDataEntry -> {
              Path inputFile = inputDataEntry.getKey();
              String annotatedText = annotateText(inputFile, inputDataEntry.getValue(), settings);
              if (annotatedText != null) {
                outputData.put(inputFile, annotatedText);
              }
            });
    return ImmutableMap.copyOf(outputData);
  }

  /** Return annotated input text, or {@code null} if there is an error. */
  @Nullable
  private static String annotateText(Path inputFile, String inputText, Settings settings) {
    LOGGER.debug("Preprocessing: {}", inputFile);
    // preprocess the input text
    String preprocessedText;
    try {
      preprocessedText =
          Preprocessor.preprocess(
              settings, inputText, inputFile, new Warnings(false, false, false));
    } catch (IOException e) {
      LOGGER.warn("Skipping {} because of preprocessing error: {}", inputText, e);
      return null;
    }
    Warnings warnings = new Warnings(true, true, true);
    LOGGER.debug("Parsing: {}", inputFile);
    // parse the preprocessed text
    ParseVendorConfigurationResult parseResult =
        new ParseVendorConfigurationJob(
                settings,
                new NetworkSnapshot(new NetworkId("dummyNetwork"), new SnapshotId("dummySnapshot")),
                ImmutableList.of(new VendorFile(inputFile.toString(), preprocessedText)),
                null,
                warnings,
                ConfigurationFormat.UNKNOWN,
                ImmutableMultimap.of(),
                null)
            .call();
    if (parseResult.getStatus() == FAILED) {
      LOGGER.error("Failed to parse: {}", inputFile);
      return null;
    }
    // annotate the preprocessed text based on warnings and silent syntax in parse result
    LOGGER.debug("Annotating: {}", inputFile);
    return annotatePreprocessedFile(
        preprocessedText,
        parseResult.getSilentSyntax(),
        warnings,
        getCommentHeader(parseResult.getConfigurationFormat()));
  }

  @Nonnull
  private static String annotatePreprocessedFile(
      String inputText,
      SilentSyntaxCollection silentSyntax,
      Warnings warnings,
      String commentHeader) {
    LinkedHashMultimap<Integer, SilentSyntaxElem> silentSyntaxByLine = LinkedHashMultimap.create();
    silentSyntax.getElements().forEach(elem -> silentSyntaxByLine.put(elem.getLine(), elem));
    LinkedHashMultimap<Integer, ParseWarning> parseWarningsByLine = LinkedHashMultimap.create();
    warnings
        .getParseWarnings()
        .forEach(parseWarning -> parseWarningsByLine.put(parseWarning.getLine(), parseWarning));
    StringBuilder sb = new StringBuilder();
    String[] lines = inputText.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
      // Silent syntax and warning line indices start at 1.
      // Annotate silent syntax for this line.
      silentSyntaxByLine.get(i + 1).stream()
          .map(elem -> printElem(commentHeader, elem))
          .forEach(sb::append);
      // Annotate all warnings for this line.
      parseWarningsByLine.get(i + 1).stream()
          .map(pw -> printParseWarning(commentHeader, pw))
          .forEach(sb::append);
      sb.append(lines[i]).append('\n');
    }
    return sb.toString();
  }

  @VisibleForTesting
  @Nonnull
  static String printElem(String commentHeader, SilentSyntaxElem silentSyntaxElem) {
    // TODO: optional extra debug information
    return String.format(
        "%s SILENTLY IGNORED: %s\n", commentHeader, silentSyntaxElem.getText().trim());
  }

  @VisibleForTesting
  static @Nonnull String printParseWarning(String commentHeader, ParseWarning parseWarning) {
    String comment = parseWarning.getComment();
    switch (comment) {
      case "This syntax is unrecognized":
        return String.format("%s UNRECOGNIZED SYNTAX: %s\n", commentHeader, parseWarning.getText());
      case "This feature is not currently supported":
        return String.format(
            "%s PARTIALLY UNSUPPORTED: %s\n", commentHeader, parseWarning.getText());
      default:
        return String.format(
            "%s WARNING: %s: %s\n", commentHeader, comment, parseWarning.getText());
    }
  }

  private static final Logger LOGGER = LogManager.getLogger(Annotate.class);

  @Nonnull
  private static String getCommentHeader(ConfigurationFormat format) {
    switch (format) {
      case F5:
      case F5_BIGIP_STRUCTURED:
      case FLAT_JUNIPER:
      case FLAT_VYOS:
      case FORTIOS:
      case IPTABLES:
      case JUNIPER:
      case JUNIPER_SWITCH:
      case PALO_ALTO:
      case PALO_ALTO_NESTED:
      case VYOS:
        return "#";
      default:
        return "!";
    }
  }
}
