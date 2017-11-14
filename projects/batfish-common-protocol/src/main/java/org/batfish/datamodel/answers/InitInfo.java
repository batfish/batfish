package org.batfish.datamodel.answers;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;

public class InitInfo {

  protected SortedMap<String, List<BatfishStackTrace>> _errors;

  protected SortedMap<String, ParseStatus> _parseStatus;

  protected SortedMap<String, ParseTreeSentences> _parseTrees;

  protected SortedMap<String, Warnings> _warnings;

  public InitInfo() {
    _parseStatus = new TreeMap<>();
    _warnings = new TreeMap<>();
    _errors = new TreeMap<>();
  }

  public SortedMap<String, List<BatfishStackTrace>> getErrors() {
    return _errors;
  }

  public SortedMap<String, ParseStatus> getParseStatus() {
    return _parseStatus;
  }

  public SortedMap<String, ParseTreeSentences> getParseTrees() {
    return _parseTrees;
  }

  public SortedMap<String, Warnings> getWarnings() {
    return _warnings;
  }

  public void setErrors(SortedMap<String, List<BatfishStackTrace>> errors) {
    _errors = errors;
  }

  public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
    _parseStatus = parseStatus;
  }

  public void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees) {
    _parseTrees = parseTrees;
  }

  public void setWarnings(SortedMap<String, Warnings> warnings) {
    _warnings = warnings;
  }

  public String prettyPrint() {
    final StringBuilder sb = new StringBuilder();
    int pedanticCount = 0;
    int redFlagCount = 0;
    int unimplementedCount = 0;
    int emptyCount = 0;
    int failedCount = 0;
    int ignoredCount = 0;
    int orphanedCount = 0;
    int passedCount = 0;
    int unknownCount = 0;
    int unrecognizedCount = 0;
    int unsupportedCount = 0;
    if (!_warnings.isEmpty()) {
      sb.append("DETAILED WARNINGS\n");
      for (String name : _warnings.keySet()) {
        sb.append("  " + name + ":\n");
        for (Warning warning : _warnings.get(name).getRedFlagWarnings()) {
          sb.append("    RedFlag " + warning.getTag() + " : " + warning.getText() + "\n");
          redFlagCount++;
        }
        for (Warning warning : _warnings.get(name).getUnimplementedWarnings()) {
          sb.append("    Unimplemented " + warning.getTag() + " : " + warning.getText() + "\n");
          unimplementedCount++;
        }
        for (Warning warning : _warnings.get(name).getPedanticWarnings()) {
          sb.append("    Pedantic " + warning.getTag() + " : " + warning.getText() + "\n");
          pedanticCount++;
        }
      }
    }
    sb.append("PARSING SUMMARY\n");
    for (Entry<String, ParseStatus> e : _parseStatus.entrySet()) {
      String hostname = e.getKey();
      ParseStatus status = e.getValue();
      switch (status) {
        case FAILED:
          sb.append("  " + hostname + ": failed to parse\n");
          failedCount++;
          break;

        case PARTIALLY_UNRECOGNIZED:
          sb.append("  " + hostname + ": contained at least one unrecognized line\n");
          unrecognizedCount++;
          break;

        case PASSED:
          passedCount++;
          break;

        case EMPTY:
          sb.append("  " + hostname + ": empty file\n");
          emptyCount++;
          break;

        case IGNORED:
          sb.append("  " + hostname + ": explicitly ignored by user\n");
          ignoredCount++;
          break;

        case ORPHANED:
          sb.append("  " + hostname + ": is an orphaned overlay configuration\n");
          orphanedCount++;
          break;

        case UNKNOWN:
          sb.append("  " + hostname + ": unknown configuration format\n");
          unknownCount++;
          break;

        case UNSUPPORTED:
          sb.append("  " + hostname + ": known but unsupported configuration format\n");
          unsupportedCount++;
          break;

        default:
          break;
      }
    }
    if (!_errors.isEmpty()) {
      sb.append("DETAILED ERRORS\n");
      for (String name : _errors.keySet()) {
        sb.append("  Failed to parse " + name + ":\n");
        for (BatfishStackTrace stackTrace : _errors.get(name)) {
          for (String line : stackTrace.getLineMap()) {
            sb.append("    " + line + "\n");
          }
        }
      }
    }
    sb.append("STATISTICS\n");
    if (!_warnings.isEmpty()) {
      sb.append("  Total warnings:\n");
      if (redFlagCount > 0) {
        sb.append("    Red Flag: " + redFlagCount + "\n");
      }
      if (unimplementedCount > 0) {
        sb.append("    Unimplemented: " + unimplementedCount + "\n");
      }
      if (pedanticCount > 0) {
        sb.append("    Pedantic: " + pedanticCount + "\n");
      }
    }
    sb.append("  Parsing results:\n");
    if (passedCount > 0) {
      sb.append("    Parsed successfully: " + passedCount + "\n");
    }
    if (unrecognizedCount > 0) {
      sb.append("    Contained unrecognized line(s): " + unrecognizedCount + "\n");
    }
    if (emptyCount > 0) {
      sb.append("    Empty file: " + emptyCount + "\n");
    }
    if (ignoredCount > 0) {
      sb.append("    Explicitly ignored by user: " + ignoredCount + "\n");
    }
    if (orphanedCount > 0) {
      sb.append("    Is an orphaned overlay configuration: " + orphanedCount + "\n");
    }
    if (failedCount > 0) {
      sb.append("    Failed to parse: " + failedCount + "\n");
    }
    if (unknownCount > 0) {
      sb.append("    Unknown configuration format: " + unknownCount + "\n");
    }
    if (unsupportedCount > 0) {
      sb.append("    Known but unsupported configuration format: " + unsupportedCount + "\n");
    }
    return sb.toString();
  }
}
