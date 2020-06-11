package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Num;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ExpressionParser implements ProdParser<Expression> {
  private ParserContext ctx;
  private static final String LOG_NAME = "parse expression";

  public ExpressionParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Expression parse(TokenList tokens) {
    List<Token> exprTokens = new ArrayList<>();
    boolean negative = false;
    if (tokens.curr().eq(op(MINUS))) {
      negative = true;
      tokens.next();
      if (tokens.curr() instanceof Num) {
        Num number = (Num) tokens.curr();
        if (negative) {
          number.changeSign();
        }
        exprTokens.add(tokens.curr());
        tokens.next();
      } else if (tokens.curr() instanceof Identifier) {
        exprTokens.add(sym(LPAREN));
        exprTokens.add(integer(-1));
        exprTokens.add(sym(RPAREN));
        exprTokens.add(op(TIMES));
        exprTokens.add(tokens.curr());
      } else {
        ctx.errors.addError(
            LOG_NAME,
            "unexpected symbol "
                + tokens.curr()
                + " after starting \"-\" in expression (expected number literal or identifier)");
        return null;
      }
    }
    int parenthesis = 0;
    while (tokens.curr() instanceof Num
        || tokens.curr() instanceof Str
        || tokens.curr() instanceof Chr
        || tokens.curr() instanceof Identifier
        || tokens.curr() instanceof Operator
        || tokens.curr().eq(sym(LPAREN))
        || tokens.curr().eq(sym(RPAREN))) {
      int idx = tokens.idx();
      if (tokens.curr() instanceof Identifier) {
        Identifier id = ((Identifier) tokens.curr());
        tokens.next();
        ArraySelector sel = ctx.arraySelectorParser.parse(tokens, true);
        if (sel != null) {
          id.arrSel = sel;
        } else {
          tokens.prev();
        }
      }
      MethodCall methodCall = ctx.methodCallParser.parse(tokens, true);
      if (methodCall != null) {
        exprTokens.add(methodCall);
      } else {
        tokens.setIdx(idx);
        if (tokens.curr().eq(sym(LPAREN))) {
          parenthesis--;
        } else if (tokens.curr().eq(sym(RPAREN))) {
          parenthesis++;
        }
        if (parenthesis > 0) {
          break;
        }
        exprTokens.add(tokens.curr());
        tokens.next();
      }
    }
    return (exprTokens.size() > 0) ? new Expression(postfix(exprTokens)) : null;
  }

  private boolean isHigerPrec(Operator op, Token sub) {
    return (sub instanceof Operator && ((Operator) sub).precedence() >= op.precedence());
  }

  // shunting yard algorithm to transform the token list to postfix notation
  private List<Token> postfix(List<Token> tokens) {
    List<Token> output = new ArrayList<>();
    Deque<Token> stack = new LinkedList<>();
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (token instanceof Operator) {
        while (!stack.isEmpty() && isHigerPrec((Operator) token, stack.peek())) {
          output.add(stack.pop());
        }
        stack.push(token);
      } else if (token.eq(sym(LPAREN))) {
        stack.push(token);
      } else if (token.eq(sym(RPAREN))) {
        while (!stack.peek().eq(sym(LPAREN))) {
          output.add(stack.pop());
        }
        stack.pop();
      } else {
        output.add(token);
      }
    }
    while (!stack.isEmpty()) {
      output.add(stack.pop());
    }
    return output;
  }
}
