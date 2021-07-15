///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0

import static java.util.stream.Collectors.toSet;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "jpwgen",
         mixinStandardHelpOptions = true,
         version = "jpwgen 0.1",
         showDefaultValues = true,
         description = "jpwgen made with jbang")
class jpwgen implements Callable<Integer> {

  @Parameters(index = "0",
              paramLabel = "pw_len",
              description = "Length of the created passwords",
              defaultValue = "12")
  private int pwlength;

  @Parameters(index = "1",
              paramLabel = "num_pw",
              description = "Number of passwords generated",
              defaultValue = "12")
  private int numpw;

  @Option(names = {"-c", "--capitalize"},
          description = "Include at least one capital letter in the password",
          negatable = true)
  private boolean capitalize;

  @Option(names = {"-n", "--numerals"},
          description = "Include at least one number in the password",
          negatable = true)
  private boolean numerals;

  @Option(names = { "-y", "--symbols"},
          description = "Include at least one special symbol in the password",
          negatable = true)
  private boolean symbols;

  @Option(names = {"-B", "--ambiguous"},
          description = "Don't include ambiguous characters in the password",
          negatable = true)
  private boolean ambiguous;

  @Option(names = {"-v", "--no-vowels"},
          description = "Do not use any vowels so as to avoid accidental nasty words",
          negatable = true)
  private boolean noVowels;

  // fields

  private String charPool;

  public static void main(String... args) {
    int exitCode = new CommandLine(new jpwgen()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    generateCharPool();
    SecureRandom random = new SecureRandom();

    List<String> passwords = new ArrayList<>(numpw);
    while (passwords.size() < numpw) {
      final String pw = getPw(random);
      if (isValid(pw)) {
        passwords.add(pw);
      }

    }

    for (String password : passwords) {
      System.out.println(password);
    }

    return 0;
  }

  private String getPw(SecureRandom random) {
    StringBuilder out = new StringBuilder();

    while (out.length() < pwlength) {
      String nextChar = String.valueOf(charPool.charAt(random.nextInt(charPool.length())));
      out.append(nextChar);
    }

    return out.toString();
  }

  private boolean isValid(String pw) {
    if (capitalize && containsNoneOf(pw, Constants.UPPERCASE_CHARS)) {
      return false;
    }

    if (symbols && containsNoneOf(pw, Constants.SYMBOL_CHARS)) {
      return false;
    }

    if (numerals && containsNoneOf(pw, Constants.NUMERAL_CHARS)) {
      return false;
    }

    return true;
  }

  private boolean containsNoneOf(String text, String anyOfMustMatch) {
    return anyOfMustMatch.codePoints()
        .noneMatch(thechar -> text.contains(String.valueOf((char) thechar)));
  }

  private void generateCharPool() {
    Set<String> buffergen = new HashSet<>();
    stream(Constants.LOWERCASE_CHARS).forEach(buffergen::add);

    if (capitalize) {
      stream(Constants.UPPERCASE_CHARS).forEach(buffergen::add);
    }

    if (numerals) {
      stream(Constants.NUMERAL_CHARS).forEach(buffergen::add);
    }

    if (symbols) {
      stream(Constants.SYMBOL_CHARS).forEach(buffergen::add);
    }

    if (!ambiguous) {
      buffergen = buffergen.stream()
          .filter(theChar -> !Constants.AMBIGUOUS_CHARS.contains(theChar))
          .collect(toSet());
    }

    if (noVowels) {
      buffergen = buffergen.stream()
          .filter(theChar -> !Constants.VOWEL_CHARS.contains(theChar))
          .collect(toSet());
    }


    this.charPool = String.join("", buffergen);
  }

  Stream<String> stream(String toStream) {
    return toStream.codePoints()
        .mapToObj(c -> String.valueOf((char) c));
  }

  static class Constants {

    static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";

    static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static final String VOWEL_CHARS = "AEIOUaeiou";

    static final String NUMERAL_CHARS = "0123456789";

    static final String SYMBOL_CHARS = "_-.,;:#*+~?!§$%&/()[]{}";

    static final String AMBIGUOUS_CHARS = "1lI0O";
  }
}