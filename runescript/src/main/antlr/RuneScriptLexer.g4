lexer grammar RuneScriptLexer;

COLON   : ':' ;
COMMA   : ',' ;
LBRACK  : '[' ;
RBRACK  : ']' ;

INTEGER_VALUE
    : DIGIT+
    ;

fragment DIGIT
    : [0-9]
    ;

IDENTIFIER
    : [a-zA-Z0-9_+]+
    ;

WHITESPACE
    : [ \t\n\r]+ -> channel(HIDDEN)
    ;

UNKNOWN
	: . -> channel(HIDDEN)
	;
