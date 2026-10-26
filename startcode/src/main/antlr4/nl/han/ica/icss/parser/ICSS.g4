grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: (variable_assignment | css_rule)* EOF;

css_rule: selector OPEN_BRACE (declaration | statement)* CLOSE_BRACE;

selector: LOWER_IDENT
        | ID_IDENT
        | CLASS_IDENT;

declaration: LOWER_IDENT COLON (expression | operation) SEMICOLON;

expression: value
            | variable_reference;

value: COLOR
     | PIXELSIZE
     | PERCENTAGE
     | SCALAR
     | LOWER_IDENT
     | TRUE
     |  FALSE;

variable_reference: CAPITAL_IDENT;
conditional: IF | ELSE;
statement: conditional (BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE | ) OPEN_BRACE (declaration | statement)* CLOSE_BRACE;

variable_assignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR value SEMICOLON;

operation: operation MUL operation
        | operation (PLUS | MIN) operation
        | expression;
