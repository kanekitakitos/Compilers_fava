grammar Cal;
prog    : stmt+ EOF ;
stmt    : ID ASSIGN expr
        | READ ID
        | WRITE expr
        ;
expr    : expr (TIMES|DIV) expr
        | expr (PLUS|MINUS) expr
        | NUMBER
        | ID
        | LPAREN expr RPAREN
        ;


ASSIGN  : '=' ;
PLUS    : '+' ;
MINUS   : '-' ;
TIMES   : '*' ;
DIV     : '/' ;
LPAREN  : '(' ;
RPAREN  : ')' ;
READ    : 'read' ;
WRITE   : 'write' ;
ID      : LETTER (LETTER | DIGIT)* ;
NUMBER  : DIGIT+ | DIGIT* ('.' DIGIT | DIGIT '.') DIGIT* ;
WS      : [ \r\t\n]+ -> skip ;


fragment
DIGIT   : [0-9] ;

fragment
LETTER  : [a-zA-Z] ;