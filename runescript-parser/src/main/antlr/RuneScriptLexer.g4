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
PLUS        : '+' ;
MINUS       : '-' ;
MUL         : '*' ;
DIV         : '/' ;
MOD         : '%' ;
AND         : '&' ;
OR          : '|' ;
DOLLAR      : '$' ;
CARET       : '^' ;
TILDE       : '~' ;
AT          : '@' ;
GT          : '>' {if (depth > 0) {setType(STRING_EXPR_END); popMode();}} ;

// keywords
CALC        : 'calc' ;

// literals
INTEGER_LITERAL : [0-9]+ ;
HEX_LITERAL     : '0' [xX] [0-9a-fA-F]+ ;
BOOLEAN_LITERAL : 'true' | 'false' ;
CHAR_LITERAL    : '\'' ~['\\\r\n] '\'' ; // TODO escaping
NULL_LITERAL    : 'null' ;

// special
QUOTE_OPEN      : '"' {depth++;} -> pushMode(String) ;
IDENTIFIER      : [a-zA-Z0-9_+.]+ ;
WHITESPACE      : [ \t\n\r]+ -> channel(HIDDEN) ;

// string interpolation support
mode String ;

QUOTE_CLOSE         : '"' {depth--;} -> popMode ;
STRING_TEXT         : ~('\\' | '"' | '<')+ ;
STRING_TAG          : '<' '/'? Tag ('=' ~'>'+)? '>' ;
STRING_ESCAPED_CHAR : '\\' ('\\' | '"' | '<') ;
STRING_EXPR_START   : '<' -> pushMode(DEFAULT_MODE) ;
STRING_EXPR_END     : '>' ;

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
