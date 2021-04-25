lexer grammar RuneScriptLexer;

COLON   : ':' ;
COMMA   : ',' ;
LBRACK  : '[' ;
RBRACK  : ']' ;

INTEGER_VALUE
    : DIGIT+
    ;

IDENTIFIER
    : (LOWERCASE_LETTER | DIGIT | '_')+
    ;

fragment LOWERCASE_LETTER
    : [a-z]
    ;

fragment DIGIT
    : [0-9]
    ;

WHITESPACE
    : [ \t\n\r]+ -> channel(HIDDEN)
    ;

UNKNOWN
	: . -> channel(HIDDEN)
	;
