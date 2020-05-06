package cc.mschneider.kommpeiler.scanner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import cc.mschneider.kommpeiler.error.CompilerErrors;
import cc.mschneider.kommpeiler.error.ErrorType;
import cc.mschneider.kommpeiler.scanner.tokens.DoubleNum;
import cc.mschneider.kommpeiler.scanner.tokens.Identifier;
import cc.mschneider.kommpeiler.scanner.tokens.IntNum;
import cc.mschneider.kommpeiler.scanner.tokens.Keyword;
import cc.mschneider.kommpeiler.scanner.tokens.Operator;
import cc.mschneider.kommpeiler.scanner.tokens.Str;
import cc.mschneider.kommpeiler.scanner.tokens.Sym;
import cc.mschneider.kommpeiler.scanner.tokens.SymbolType;
import cc.mschneider.kommpeiler.scanner.tokens.Token;
import cc.mschneider.kommpeiler.scanner.tokens.TokenType;


/**
 * Scanner class for Kommpeiler
 * @author Martin Schneider
 */
public class Scanner
{

    // a PushbackReader is used to be able to jump forward and backward in the input stream
    private PushbackReader inputReader;
    private StringBuffer buffer;
    private List<Token> tokenList;
    private char character;
    private final String[] keywords =
    {"if", "else", "do", "while", "return", "public", "private", "protected", "class"};
    private final String[] basicTypes =
    {"void", "int", "double", "String"};
    private CompilerErrors errors = new CompilerErrors();

    /**
     * @param string input stream
     * @return a list of tokens
     * @throws IOException I/O-error
     */
    public List<Token> getTokens(final String string) throws IOException
    {
        errors.clear();
        Reader reader = new StringReader(string);
        inputReader = new PushbackReader(reader);
        return getTokens(inputReader);
    }

    /**
     * @param file input stream
     * @return a list of tokens
     * @throws IOException I/O-error
     */
    public List<Token> getTokens(final File file) throws IOException
    {
        errors.clear();
        Reader reader = new FileReader(file);
        inputReader = new PushbackReader(reader);
        return getTokens(inputReader);
    }

    /**
     * @param fileReader reader for the input
     * @return a list of tokens
     * @throws IOException I/O-error
     */
    public List<Token> getTokens(final PushbackReader fileReader) throws IOException
    {
        errors.clear();
        tokenList = new ArrayList<Token>();
        buffer = new StringBuffer();
        int tokenCount;
        int c;
        // scan each char of the input
        while ((c = fileReader.read()) != -1)
        {
            character = (char) c;
            tokenCount = tokenList.size();
            // parentheses
            scanParen();
            // identifiers & keywords
            if (tokenList.size() == tokenCount)
            {
                scanId();
            }
            // symbols
            if (tokenList.size() == tokenCount)
            {
                scanSym();
            }
            // operators
            if (tokenList.size() == tokenCount)
            {
                scanOps();
            }
            // Strings
            if (tokenList.size() == tokenCount)
            {
                scanStr();
            }
            // numbers
            if (tokenList.size() == tokenCount)
            {
                scanNum();
            }
            // comments
            if (tokenList.size() == tokenCount)
            {
                scanComment();
            }
        }
        return tokenList;
    }

    /**
     * Scans for identifiers, primitive types and keywords. identifier = letter {letter|digit},
     * keyword = "if" | "else" |" while" | "do" | "return" | basicType | scope, scope = "public" |
     * "private" | "protected", basicType = "int" | "double" | "void", "String"
     */
    private void scanId() throws IOException
    {
        if (Character.isLetter(character))
        {
            buffer.append(character);
            while (Character.isLetter(character = (char) inputReader.read()))
            {
                buffer.append(character);
            }
            inputReader.unread((int) character);
            String str = buffer.toString();

            // keywords
            for (int i = 0; i < keywords.length; i++)
            {
                if (str.equals(keywords[i]))
                {
                    tokenList.add(new Keyword(str));
                    buffer.setLength(0);
                }
            }
            
            // basic types
            for (int i = 0; i < basicTypes.length; i++)
            {
                if (str.equals(basicTypes[i]))
                {
                    tokenList.add(new SymbolType(str));
                    buffer.setLength(0);
                }
            }
            
            if (buffer.length() > 0)
            {
                tokenList.add(new Identifier(buffer.toString()));
            }
            buffer.setLength(0);
        }
    }

    /**
     * Scans for numerics. integer = digit {digit}, double = {digit} [.] digit {digit}
     * @throws IOException
     */
    private void scanNum() throws IOException
    {
        if (Character.isDigit(character))
        {
            buffer.append(character);
            while (Character.isDigit(character = (char) inputReader.read()))
            {
                buffer.append(character);
            }
            if (character == '.')
            {
                buffer.append(character);
                scanDouble();
            }
            else
            {
                inputReader.unread((int) character);
                tokenList.add(new IntNum(buffer.toString()));
                buffer.setLength(0);
            }
        }
    }

    /**
     * Scans for double after '.' was found.
     * @throws IOException
     */
    private void scanDouble() throws IOException
    {
        while (Character.isDigit(character = (char) inputReader.read()))
        {
            buffer.append(character);
        }
        inputReader.unread((int) character);
        tokenList.add(new DoubleNum(buffer.toString()));
        buffer.setLength(0);
    }

    /**
     * Scans for different kinds of parentheses. parenthesis = [ "(" | ")" | "{" | "}" ]
     */
    private void scanParen()
    {
        if (character == '(')
        {
            tokenList.add(new Sym(TokenType.LPAREN));
        }
        else if (character == ')')
        {
            tokenList.add(new Sym(TokenType.RPAREN));
        }
        else if (character == '{')
        {
            tokenList.add(new Sym(TokenType.LBRACE));
        }
        else if (character == '}')
        {
            tokenList.add(new Sym(TokenType.RBRACE));
        }
        else if (character == '[')
        {
            tokenList.add(new Sym(TokenType.LBRAK));
        }
        else if (character == ']')
        {
            tokenList.add(new Sym(TokenType.RBRAK));
        }
    }

    /**
     * Scans for symbols. symbol = "," | "." | ";"
     * @throws IOException
     */
    private void scanSym() throws IOException
    {
        if (character == ',')
        {
            tokenList.add(new Sym(TokenType.COMMA));
        }
        else if (character == ';')
        {
            tokenList.add(new Sym(TokenType.SEMICOLON));
        }
        else if (character == '.')
        {
            if (Character.isDigit(character = (char) inputReader.read()))
            {
                buffer.append('.');
                buffer.append(character);
                scanDouble();
            }
            else
            {
                inputReader.unread(character);
                tokenList.add(new Sym(TokenType.DOT));
            }
        }
    }

    /**
     * Scans for operators. operator = "-" | "+" | "*" | "/" | "%" | ">" | "<" | "<=" | ">=" | "!="
     * | "=" | "==" ]
     * @throws IOException
     * @throws ScannerException
     */
    // CHECKSTYLE:OFF
    // don't care ;)
    private void scanOps() throws IOException
    // CHECKSTYLE:ON
    {
        if (character == '-')
        {
            tokenList.add(new Operator(TokenType.MINUS));
        }
        else if (character == '/')
        {
            // look-ahead to check for comment
            char character;
            if ((character = (char) inputReader.read()) == '*')
            {
                scanComment1();
            }
            else if (character == '/')
            {
                scanComment2();
            }
            else
            {
                inputReader.unread(character);
                tokenList.add(new Operator(TokenType.DIV));
            }
        }
        else if (character == '%')
        {
            tokenList.add(new Operator(TokenType.MOD));
        }
        else if (character == '*')
        {
            tokenList.add(new Operator(TokenType.TIMES));
        }
        else if (character == '+')
        {
            tokenList.add(new Operator(TokenType.PLUS));
        }
        else if (character == '>')
        {
            if ((character = (char) inputReader.read()) == '=')
            {
                tokenList.add(new Operator(TokenType.GREATEREQ));
            }
            else
            {
                tokenList.add(new Operator(TokenType.GREATER));
                inputReader.unread(character);
            }
        }
        else if (character == '<')
        {
            if ((character = (char) inputReader.read()) == '=')
            {
                tokenList.add(new Operator(TokenType.SMALLEREQ));
            }
            else
            {
                tokenList.add(new Operator(TokenType.SMALLER));
                inputReader.unread(character);
            }
        }
        else if (character == '=')
        {
            if ((character = (char) inputReader.read()) == '=')
            {
                tokenList.add(new Operator(TokenType.EQUAL));
            }
            else
            {
                tokenList.add(new Operator(TokenType.ASSIGN));
                inputReader.unread(character);
            }
        }
        else if (character == '!')
        {
            if ((character = (char) inputReader.read()) == '=')
            {
                tokenList.add(new Operator(TokenType.NOTEQUAL));
            }
            else
            {
                inputReader.unread(character);
                errors.addError("! must be followed by =", ErrorType.SCANNER);
            }
        }
    }

    /**
     * Scans for comments.
     * @throws IOException
     */
    private void scanComment() throws IOException
    {
        if ((character == '/'))
        {
            if ((character = (char) inputReader.read()) == '*')
            {
                scanComment1();
            }
            else if (character == '/')
            {
                scanComment2();
            }
        }
    }

    /**
     * Scans comment text until closing tag is found. Can handle nested comments.
     * @throws IOException
     */
    private void scanComment1() throws IOException
    {
        int c;
        int nested = 1;
        while ((c = inputReader.read()) != -1)
        {
            if ((character = (char) c) == '*')
            {
                if (((character = (char) inputReader.read()) == '/'))
                {
                    nested--;
                    if (nested == 0)
                    {
                        break;
                    }
                }
            }
            else if (character == '/')
            {
                if (((character = (char) inputReader.read()) == '*'))
                {
                    nested++;
                }
            }
        }
        if (c == -1)
        {
            errors.addError("No closing */ for comment found.", ErrorType.SCANNER);
        }
    }

    /**
     * Scans for a comment until the end of the line.
     * @throws IOException
     */
    private void scanComment2() throws IOException
    {
        int c;
        do
        {
            c = inputReader.read();
        } while (c != 10 && c != 13 && c != -1);
    }

    /**
     * Scans for strings. string = """ { char } """
     * @throws IOException
     */
    private void scanStr() throws IOException
    {
        if ((character == '"'))
        {
            int c;
            while ((c = inputReader.read()) != -1)
            {
                if ((character = (char) c) == '"')
                {
                    break;
                }
                buffer.append(character);
            }
            if (c == -1)
            {
                errors.addError("No closing \" for input string found.", ErrorType.SCANNER);
            }
            else
            {
                tokenList.add(new Str(buffer.toString()));
            }
        }
    }

    public CompilerErrors getErrors()
    {
        return errors;
    }
}
