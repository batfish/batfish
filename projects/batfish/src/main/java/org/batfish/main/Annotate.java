package org.batfish.main;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.Warnings.forLogger;
import static org.batfish.main.CliUtils.readAllFiles;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.grammar.SilentSyntax;
import org.batfish.grammar.SilentSyntaxElem;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.job.BatfishJobExecutor;
import org.batfish.job.ParseVendorConfigurationJob;

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
    settings.setLogger(new BatfishLogger(BatfishLogger.LEVELSTR_WARN, false, System.out));
    settings.setPrintParseTree(true);

    Preprocess.main(new String[] {inputPath.toString(), outputPath.toString()});
    // Overwrite output in-place
    annotate(outputPath, outputPath, settings);
  }

  private static void annotate(Path inputPath, Path outputPath, Settings settings)
      throws IOException {
    BatfishLogger logger = settings.getLogger();
    logger.info("\n*** READING INPUT FILES ***\n");
    Map<Path, String> configurationData =
        readAllFiles(inputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR), logger);

    logger.info("\n*** COMPUTING OUTPUT FILES ***\n");
    logger.resetTimer();
    List<ParseVendorConfigurationJob> parseJobs = new ArrayList<>();
    for (Entry<Path, String> configFile : configurationData.entrySet()) {
      Path inputFile = configFile.getKey();
      System.out.println("loop1 inputFile: " + inputFile);
      String fileText = configFile.getValue();
      Warnings warnings = forLogger(logger);
      String name = inputPath.relativize(inputFile).toString();
      System.out.println("loop1 name: " + name);
      parseJobs.add(
          new ParseVendorConfigurationJob(
              settings,
              new NetworkSnapshot(new NetworkId("dummyNetwork"), new SnapshotId("dummySnapshot")),
              fileText,
              name,
              warnings,
              ConfigurationFormat.UNKNOWN,
              ImmutableMultimap.of(),
              null));
    }
    ParseVendorConfigurationAnswerElement answer = new ParseVendorConfigurationAnswerElement();
    BatfishJobExecutor.runJobsInExecutor(
        settings,
        logger,
        parseJobs,
        new TreeMap<>(),
        answer,
        settings.getFlatten() || settings.getHaltOnParseError(),
        "Annotate silent syntax");
    logger.printElapsedTime();
    System.out.println("keys: " + configurationData.keySet());
    answer
        .getSilentSyntax()
        .forEach(
            (relativizedInputFile, silentSyntax) -> {
              System.out.println("loop2 relativized: " + relativizedInputFile);
              Path outputFile = outputPath.resolve(relativizedInputFile);
              System.out.println("loop2 outputFile: " + outputFile);
              String textToAnnotate = configurationData.get(outputFile);
              logger.debugf("Writing config to \"%s\"...", relativizedInputFile);
              CommonUtil.writeFile(
                  outputFile,
                  annotateFile(
                      textToAnnotate,
                      silentSyntax,
                      answer.getWarnings().get(relativizedInputFile),
                      getCommentHeader(answer.getFileFormats().get(relativizedInputFile))));
              logger.debug("OK\n");
            });
  }

  @Nonnull
  private static String annotateFile(
      String inputText, SilentSyntax silentSyntax, Warnings warnings, String commentHeader) {
    LinkedHashMultimap<Integer, SilentSyntaxElem> silentSyntaxByLine = LinkedHashMultimap.create();
    silentSyntax.getElements().forEach(elem -> silentSyntaxByLine.put(elem.getLine(), elem));
    LinkedHashMultimap<Integer, ParseWarning> parseWarningsByLine = LinkedHashMultimap.create();
    warnings
        .getParseWarnings()
        .forEach(parseWarning -> parseWarningsByLine.put(parseWarning.getLine(), parseWarning));
    StringBuilder sb = new StringBuilder();
    String[] lines = inputText.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
      // silent syntax line indices start at 1
      silentSyntaxByLine.get(i + 1).stream()
          .map(elem -> printElem(commentHeader, elem))
          .forEach(sb::append);
      parseWarningsByLine.get(i + 1).stream()
          .map(pw -> printParseWarning(commentHeader, pw))
          .filter(Objects::nonNull)
          .forEach(sb::append);
      sb.append(lines[i] + "\n");
    }
    return sb.toString();
  }

  @VisibleForTesting
  @Nonnull
  static String printElem(String commentHeader, SilentSyntaxElem elem) {
    // TODO: optional extra debug information
    return String.format("%s SILENTLY IGNORED: %s\n", commentHeader, elem.getText().trim());
  }

  @VisibleForTesting
  @Nullable
  static String printParseWarning(String commentHeader, ParseWarning parseWarning) {
    switch (parseWarning.getComment()) {
      case "This syntax is unrecognized":
        return String.format("%s UNRECOGNIZED SYNTAX\n", commentHeader);
      case "This feature is not currently supported":
        return String.format("%s PARTIALLY UNSUPPORTED\n", commentHeader);
      default:
        return null;
    }
  }

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
