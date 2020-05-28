package io.github.martinschneider.kommpeiler.parser;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.scanner.TokenList;

public class Parser {
  public ParserContext ctx;

  public Parser(CompilerErrors errors) {
    ctx = ParserContext.build(errors);
  }

  public Clazz parse(TokenList tokens) {
    return new ClassParser(ctx).parse(tokens);
  }
}
