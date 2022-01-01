/****************************************************/
/* File: parse.c                                    */
/* The parser implementation for the TINY compiler  */
/* Compiler Construction: Principles and Practice   */
/* Kenneth C. Louden                                */
/****************************************************/

#include "globals.h"
#include "util.h"
#include "scan.h"
#include "parse.h"

static TokenType token; /* holds current token */

/* function prototypes for recursive calls */
static TreeNode * stmt_sequence(void);
static TreeNode * statement(void);
static TreeNode * if_stmt(void);
static TreeNode * repeat_stmt(void);
static TreeNode * assign_stmt(void);
static TreeNode * read_stmt(void);
static TreeNode * write_stmt(void);
static TreeNode * exp(void);
static TreeNode * simple_exp(void);
static TreeNode * term(void);
static TreeNode * factor(void);

//扩充语法规则
static TreeNode* dowhile_stmt(void);
static TreeNode* for_stmt(void);
static TreeNode* power(void);

static TreeNode* logic_exp(void);
static TreeNode* logic_term(void);
static TreeNode* logic_factor(void);

static TreeNode* reg_exp(void);
static TreeNode* reg_term(void);
static TreeNode* reg_factor(void);
static TreeNode* reg_letter(void);

static void syntaxError(char * message)
{ fprintf(listing,"\n>>> ");
  fprintf(listing,"Syntax error at line %d: %s",lineno,message);
  Error = TRUE;
}

static void match(TokenType expected)
{ if (token == expected) token = getToken();
  else {
    syntaxError("unexpected token -> ");
    printToken(token,tokenString);
    fprintf(listing,"      ");
  }
}

TreeNode * stmt_sequence(void)
{ TreeNode * t = statement();
  TreeNode * p = t;
  while ((token!=ENDFILE) && (token!=END) &&
         (token!=ELSE) && (token!=UNTIL) && (token != WHILE) && (token != ENDDO))
  { TreeNode * q;
    match(SEMI);
    q = statement();
    if (q!=NULL) {
      if (t==NULL) t = p = q;
      else /* now p cannot be NULL either */
      { p->sibling = q;
        p = q;
      }
    }
  }
  return t;
}


//P394 
//lineno: 961
TreeNode * statement(void)
{ TreeNode * t = NULL;
  switch (token) {
    case IF : t = if_stmt(); break;
    case REPEAT : t = repeat_stmt(); break;
    case ID : t = assign_stmt(); break;
    case READ : t = read_stmt(); break;
    case WRITE : t = write_stmt(); break;
    case DO: t = dowhile_stmt(); break;
    case FOR: t = for_stmt(); break;
    default : syntaxError("unexpected token -> ");
              printToken(token,tokenString);
              token = getToken();
              break;
  } /* end case */
  return t;
}


//P394 
//lineno: 977
TreeNode * if_stmt(void)
{ TreeNode * t = newStmtNode(IfK);
  match(IF);
  if (token == LPAREN) {
      match(LPAREN);
      if (t != NULL) t->child[0] = logic_exp();
      match(RPAREN);
  }
  else if (t != NULL) t->child[0] = logic_exp();
  match(THEN);
  if (t!=NULL) t->child[1] = stmt_sequence();
  if (token==ELSE) {
    match(ELSE);
    if (t!=NULL) t->child[2] = stmt_sequence();
  }
  match(END);
  return t;
}



//P394 
//lineno:991
TreeNode * repeat_stmt(void)
{ TreeNode * t = newStmtNode(RepeatK);
  match(REPEAT);
  if (t!=NULL) t->child[0] = stmt_sequence();
  match(UNTIL);
  if (t!=NULL) t->child[1] = logic_exp();
  return t;
}

TreeNode * assign_stmt(void)
{ TreeNode * t = newStmtNode(AssignK);
  if ((t!=NULL) && (token==ID))
    t->attr.name = copyString(tokenString);
  match(ID);
  if (token == MINUSEQ) {   //处理-=逻辑:就是 x -= 1 转变为跟以前语法树上展示x = x - 1 是一样的效果
      match(MINUSEQ);
      TreeNode* opNode = newExpNode(OpK);
      if (opNode != NULL) {
          TreeNode* idNode = newExpNode(IdK);
          if(t != NULL)
            idNode->attr.name = t->attr.name;
          if (opNode != NULL) {
              opNode->child[0] = idNode;
              opNode->attr.op = MINUS;
              opNode->child[1] = logic_exp();
          }
      }
      if (t != NULL) {
          t->child[0] = opNode;
      }
  }
  else if (token == ASSIGN) { //处理=
      match(ASSIGN);
      if(t != NULL) t->child[0] = logic_exp();
  }
  else if (token == REG_ASSIGN) {   //处理:=
      match(REG_ASSIGN);
      if (t != NULL) t->child[0] = reg_exp();
  }
  return t;
}

TreeNode * read_stmt(void)
{ TreeNode * t = newStmtNode(ReadK);
  match(READ);
  if ((t!=NULL) && (token==ID))
    t->attr.name = copyString(tokenString);
  match(ID);
  return t;
}

TreeNode * write_stmt(void)
{ TreeNode * t = newStmtNode(WriteK);
  match(WRITE);
  if (t!=NULL) t->child[0] = logic_exp();
  return t;
}

TreeNode * exp(void)
{ TreeNode * t = simple_exp();
  if ((token==LT)||(token==EQ) || (token==GT) || (token==LTEQ) || (token==GTEQ) || (token==NEQ)) {
    TreeNode * p = newExpNode(OpK);
    if (p!=NULL) {
      p->child[0] = t;
      p->attr.op = token;
      t = p;
    }
    match(token);
    if (t!=NULL)
      t->child[1] = simple_exp();
  }
  return t;
}

TreeNode * simple_exp(void)
{ TreeNode * t = term();
  while ((token==PLUS)||(token==MINUS))
  { TreeNode * p = newExpNode(OpK);
    if (p!=NULL) {
      p->child[0] = t;
      p->attr.op = token;
      t = p;
      match(token);
      t->child[1] = term();
    }
  }
  return t;
}

TreeNode * term(void)
{ TreeNode * t = power();
  while ((token==TIMES)||(token==OVER) || token == MOD)
  { TreeNode * p = newExpNode(OpK);
    if (p!=NULL) {
      p->child[0] = t;
      p->attr.op = token;
      t = p;
      match(token);
      p->child[1] = power();
    }
  }
  return t;
}

TreeNode* power(void)
{
    TreeNode* t = factor();
    while (token == POWER) {
        TreeNode* p = newExpNode(OpK);
        if (p != NULL) {
            p->child[0] = t;
            p->attr.op = POWER;
            t = p;
            match(POWER);
            p->child[1] = factor();
        }
    }
    return t;
}

TreeNode * factor(void)
{ TreeNode * t = NULL;
  switch (token) {
    case NUM :
      t = newExpNode(ConstK);
      if ((t!=NULL) && (token==NUM))
        t->attr.val = atoi(tokenString);
      match(NUM);
      break;
    case ID :
      t = newExpNode(IdK);
      if ((t!=NULL) && (token==ID))
        t->attr.name = copyString(tokenString);
      match(ID);
      break;
    case LPAREN :
      match(LPAREN);
      t = exp();
      match(RPAREN);
      break;
    default:
      syntaxError("unexpected token -> ");
      printToken(token,tokenString);
      token = getToken();
      break;
    }
  return t;
}

//(1) Dowhile-stmt-->do  stmt-sequence  while(exp); 
TreeNode* dowhile_stmt(void)
{
    TreeNode* t = newStmtNode(DoWhileK);
    match(DO);
    if (t != NULL)
        t->child[0] = stmt_sequence();
    match(WHILE);
    match(LPAREN);
    if(t != NULL)
        t->child[1] = logic_exp();
    match(RPAREN);
    //match(SEMI);
    return t;
}

//(2) for-stmt-->for identifier:=simple-exp  to  simple-exp  do  stmt-sequence enddo    步长递增1
//(3) for-stmt-->for identifier:=simple-exp  downto  simple-exp  do  stmt-sequence enddo    步长递减1
//   合并得 for-stmt-->for identifier:=simple-exp  (to | downto)  simple-exp  do  stmt-sequence enddo
TreeNode* for_stmt(void)
{
    TreeNode* t = newStmtNode(ForK);
    match(FOR);
    if ((t != NULL) && (token == ID))
        t->attr.name = copyString(tokenString);
    match(ID);
    match(ASSIGN);
    if (t != NULL)
        t->child[0] = simple_exp();
    if (token == TO || token == DOWNTO) {
        match(token);
        if (t != NULL)
            t->child[1] = simple_exp();
    }
    match(DO);
    if (t != NULL)
        t->child[2] = stmt_sequence();
    match(ENDDO);
    return t;
}

TreeNode* logic_exp(void) {
    TreeNode* t = logic_term();
    while (token == OR) {
        TreeNode* p = newExpNode(OpK);
        if (p != NULL) {
            p->attr.op = OR;
            p->child[0] = t;
            t = p;
            match(OR);
            p->child[1] = logic_term();
        }
    }
    return t;
}
TreeNode* logic_term(void) {
    TreeNode* t = logic_factor();
    while (token == AND) {
        TreeNode* p = newExpNode(OpK);
        if (p != NULL) {
            p->attr.op = AND;
            p->child[0] = t;
            t = p;
            match(AND);
            p->child[1] = logic_factor();
        }
    }
    return t;
}
TreeNode* logic_factor(void) {
    TreeNode* t;
    if (token == NOT) {
        TreeNode* p = newExpNode(OpK);
        if (p != NULL) {
            p->attr.op = NOT;
            p->type = Integer;
            t = p;
            match(NOT);
            p->child[0] = logic_factor();
        }
    }
    else {
        t = exp();
    }
    return t;
}

TreeNode* reg_exp(void)
{
    TreeNode* t = reg_term();
    while (token == REG_OR) {
        TreeNode* p = newExpNode(OpK);
        if (p != NULL) {
            p->attr.op = REG_OR;
            p->child[0] = t;
            t = p;
            match(REG_OR);
            p->child[1] = reg_term();
        }
    }
    return t;
}

TreeNode* reg_term(void)
{
    TreeNode* t = reg_factor();
    while (token == REG_AND) {
        TreeNode* p = newExpNode(OpK);
        if (p != NULL) {
            p->attr.op = REG_AND;
            p->child[0] = t;
            t = p;
            match(REG_AND);
            p->child[1] = reg_factor();
        }
    }
    return t;
}

TreeNode* reg_factor(void)
{
    TreeNode* t = reg_letter();
    if (token == CLOSURE) {
        TreeNode* p = newExpNode(OpK);
        if (p != NULL) {
            p->attr.op = CLOSURE;
            t = p;
            match(CLOSURE);
            p->child[0] = t;
        }
    }
    return t;
}

TreeNode* reg_letter(void)
{
    TreeNode* t;
    switch (token) {
    case LPAREN:
        match(LPAREN);
        t = reg_exp();
        match(RPAREN);
        break;
    case ID:
        t = newExpNode(IdK);
        if (t != NULL) {
            t->attr.name = copyString(tokenString);
        }
        match(ID);
        break;
    case EPSILON:
        t = newExpNode(IdK);
        if (t != NULL) {
            t->attr.name = copyString(tokenString);
        }
        match(EPSILON);
        break;
    default:
        syntaxError("unexpected token -> ");
        printToken(token, tokenString);
        token = getToken();
        break;
    }
    return t;
}

/****************************************/
/* the primary function of the parser   */
/****************************************/
/* Function parse returns the newly 
 * constructed syntax tree
 */
TreeNode * parse(void)
{ TreeNode * t;
  token = getToken();
  t = stmt_sequence();
  if (token!=ENDFILE)
    syntaxError("Code ends before file\n");
  return t;
}
