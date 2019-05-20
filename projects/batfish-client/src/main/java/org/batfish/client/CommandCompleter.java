package org.batfish.client;

import com.google.common.base.Strings;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

public class CommandCompleter implements Completer {

  private final SortedSet<String> _commandStrs;

  public CommandCompleter() {
    _commandStrs = new TreeSet<>(Command.getNameMap().keySet());
  }

  @Override
  public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> candidates) {
    String buffer = parsedLine.word().trim();

    if (Strings.isNullOrEmpty(buffer)) {
      candidates.addAll(_commandStrs.stream().map(Candidate::new).collect(Collectors.toList()));
    } else {
      for (String match : _commandStrs.tailSet(buffer)) {
        if (!match.startsWith(buffer)) {
          break;
        }

        candidates.add(new Candidate(match));
      }
    }

    // if the match was unique and the complete command was specified, print the command usage
    if (candidates.size() == 1 && candidates.get(0).displ().equals(buffer)) {
      candidates.clear();
      candidates.add(
          new Candidate(
              " " + Command.getUsageMap().get(Command.getNameMap().get(buffer)).getUsage()));
    }
  }
}
