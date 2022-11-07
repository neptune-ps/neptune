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
TYPE_ARRAY  : IDENTIFIER 'array' ;
DEF_TYPE    : 'def_' IDENTIFIER ;
SWITCH_TYPE : 'switch_' IDENTIFIER ;

// literals
INTEGER_LITERAL : '-'? Digit+ ;
HEX_LITERAL     : '0' [xX] [0-9a-fA-F]+ ;
COORD_LITERAL   : Digit+ '_' Digit+ '_' Digit+ '_' Digit+ '_' Digit+ ;
BOOLEAN_LITERAL : 'true' | 'false' ;
CHAR_LITERAL    : '\'' (CharEscapeSequence | ~['\\\r\n]) '\'' ;
NULL_LITERAL    : 'null' ;

// comments
LINE_COMMENT    : '//' .*? ('\n' | EOF) -> channel(HIDDEN) ;
BLOCK_COMMENT   : '/*' .*? '*/' -> channel(HIDDEN) ;

// a basic digit rule
fragment Digit
    : [0-9]
    ;

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
