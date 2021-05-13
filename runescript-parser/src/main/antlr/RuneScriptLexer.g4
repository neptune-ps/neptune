lexer grammar RuneScriptLexer;

@members {
private int depth = 0;
}

// symbols
LPAREN      : '(' ;
RPAREN      : ')' ;
COLON       : ':' ;
SEMICOLON   : ';' ;
COMMA       : ',' ;
LBRACK      : '[' ;
RBRACK      : ']' ;
LBRACE      : '{' ;
RBRACE      : '}' ;
PLUS        : '+' ;
MINUS       : '-' ;
MUL         : '*' ;
DIV         : '/' ;
MOD         : '%' ;
AND         : '&' ;
OR          : '|' ;
EQ          : '=' ;
EXCL        : '!' ;
DOLLAR      : '$' ;
CARET       : '^' ;
TILDE       : '~' ;
AT          : '@' ;
GT          : '>' {if (depth > 0) {setType(STRING_EXPR_END); popMode();}} ;
GTE         : '>=' ;
LT          : '<' ;
LTE         : '<=' ;

// keywords
IF          : 'if' ;
ELSE        : 'else' ;
WHILE       : 'while' ;
CASE        : 'case' ;
DEFAULT     : 'default' ;
RETURN      : 'return' ;
CALC        : 'calc' ;
TYPE        : 'int' | 'string' | 'long' | 'enum' ; // TODO allow passing valid types to lexer
TYPE_ARRAY  : TYPE 'array' ; // TODO allow passing valid arrays types to lexer
DEF_TYPE    : 'def_' TYPE ; // TODO allow passing declarable types to lexer
SWITCH_TYPE : 'switch_' TYPE ; // TODO allow passing switchable types to lexer

// literals
INTEGER_LITERAL : [0-9]+ ;
HEX_LITERAL     : '0' [xX] [0-9a-fA-F]+ ;
BOOLEAN_LITERAL : 'true' | 'false' ;
CHAR_LITERAL    : '\'' (CharEscapeSequence | ~['\\\r\n]) '\'' ;
NULL_LITERAL    : 'null' ;

// allows escaping specific characters in a char literal
fragment CharEscapeSequence
    : '\\' ('\\' | '\'')
    ;

// special
QUOTE_OPEN      : '"' {depth++;} -> pushMode(String) ;
IDENTIFIER      : [a-zA-Z0-9_+.]+
                | [a-zA-Z0-9_+.] [a-zA-Z0-9_+.:]+ [a-zA-Z0-9_+.]
                ;
WHITESPACE      : [ \t\n\r]+ -> channel(HIDDEN) ;

// string interpolation support
mode String ;

QUOTE_CLOSE         : '"' {depth--;} -> popMode ;
STRING_TEXT         : StringEscapeSequence | ~('\\' | '"' | '<')+ ;
STRING_TAG          : '<' '/'? Tag ('=' ~'>'+)? '>' ;
STRING_EXPR_START   : '<' -> pushMode(DEFAULT_MODE) ;
STRING_EXPR_END     : '>' ;

// allows escaping specific characters in a string
fragment StringEscapeSequence
    : '\\' ('\\' | '"' | '<')
    ;

// possible tags used in strings
fragment Tag
    : 'br'
    | 'col'
    | 'str'
    | 'shad'
    | 'u'
    | 'img'
    | 'gt'
    | 'lt'
    ;
