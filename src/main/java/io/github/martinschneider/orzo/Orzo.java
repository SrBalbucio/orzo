package io.github.martinschneider.orzo;

import io.github.martinschneider.orzo.codegen.CodeGenerator;
import io.github.martinschneider.orzo.codegen.Output;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.Parser;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// main entry point
public class Orzo {
  private static Output fileOutput(File outputFile) {
    if (!outputFile.getParentFile().exists()) {
      outputFile.getParentFile().mkdirs();
    }
    PrintStream fileOutput;
    Output out = null;
    try {
      fileOutput = new PrintStream(new FileOutputStream(outputFile));
      out = new Output(fileOutput);
      return out;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String args[]) throws IOException {
    if (args.length == 0) {
      System.err.println("Syntax: orzo inputFiles -d outputPath");
    } else {
      List<File> inputs = new ArrayList<>();
      String outputPath = null;
      for (int i = 0; i < args.length; i++) {
        if (!args[i].equals("-d")) {
          inputs.add(new File(args[i]));
        } else {
          if (args.length < i + 2) {
            System.err.println("Missing argument for -d option");
            return;
          }
          outputPath = args[i + 1];
          break;
        }
      }
      new Orzo(inputs, outputPath).compile();
    }
  }

  private List<File> inputs;
  private String outputPath;

  public Orzo(List<File> inputs, String outputPath) {
    this.inputs = inputs;
    this.outputPath = outputPath;
  }

  public void compile() throws IOException {
    compile(null);
  }

  public void compile(Output output) throws IOException {
    for (File input : inputs) {
      System.out.println("Reading from: " + input.getAbsolutePath());
      Lexer scanner = new Lexer();
      TokenList tokens = scanner.getTokens(input);
      System.out.println(
          "Scanner output: "
              + tokens.list().stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
      Parser parser = new Parser(scanner.getErrors());
      Clazz clazz = parser.parse(tokens);
      System.out.println("Parser output: " + clazz);
      File outputFile = new File(classPath(outputPath, clazz));
      System.out.println("Writing to: " + outputFile.getAbsolutePath());
      if (output == null) {
        output = fileOutput(outputFile);
      }
      CodeGenerator codeGen = new CodeGenerator(clazz, output, parser.ctx);
      codeGen.generate();
      if (!codeGen.getErrors().getErrors().isEmpty()) {
        StringBuilder errors = new StringBuilder("\n");
        int errCount = codeGen.getErrors().getErrors().size();
        errors.append(errCount);
        errors.append(" error");
        if (errCount > 1) {
          errors.append("s");
        }
        errors.append("\n");
        errors.append(codeGen.getErrors());
        System.out.println(errors.toString());
      }
    }
    System.out.println("Ok bye!\n");
  }

  private String classPath(String dir, Clazz clazz) {
    if (dir == null) {
      dir = System.getProperty("user.dir");
    }
    StringBuilder strBuilder = new StringBuilder(dir);
    strBuilder.append(File.separator);
    strBuilder.append(clazz.packageName.replaceAll("\\.", File.separator));
    strBuilder.append(File.separator);
    strBuilder.append(clazz.name.val);
    strBuilder.append(".class");
    return strBuilder.toString();
  }
}
