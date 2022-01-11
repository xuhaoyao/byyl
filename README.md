# byyl
## 实验二:XLEX生成器

Qt 5.9.2 ，**代码编译一次之后，将graph复制到debug目录下就可以查看到图片的效果**

本项目代码来自于https://github.com/sureyet/SCNU-CompilerLab2

面向考试复习时根据大佬的代码改写了相关部分，并加上了自己的理解。

设计一个应用软件，以实现将正则表达式-->NFA--->DFA-->DFA最小化-->词法分析程序

 （1）正则表达式应该支持单个字符，运算符号有： 连接 选择 闭包 括号

 （2）要提供一个源程序编辑界面，让用户输入正则表达式（可保存、打开源程序）

 （3）需要提供窗口以便用户可以查看转换得到的NFA（用状态转换表呈现即可）

 （4）需要提供窗口以便用户可以查看转换得到的DFA（用状态转换表呈现即可）

 （5）需要提供窗口以便用户可以查看转换得到的最小化DFA（用状态转换表呈现即可）

 （6）需要提供窗口以便用户可以查看转换得到的词法分析程序（该分析程序需要用C语言描述）

### 预处理

#### 用 . 显示代替连接符号

`ab -> a.b  ||  ab*c -> a.b*.c   l(l|d)* -> l.(l|d)*`

```c++
string nfaManager::insert_concat(string regexp){
    string ans = "";
    char c, c2;
    for(int i = 0;i < regexp.size();i++){
        c = regexp[i];
        ans += c;
        if(i + 1 < regexp.size()){
            c2 = regexp[i + 1];
            if(c != '|' || c != '(' || c2 != '*' || c2 != ')' || c2 != '|'){
                ans += '.';
            }
        }
    }
    return ans;
}
```

#### 将正则表达式转为后缀表达式

```c++
//当符号栈顶的优先级＞=正要进栈字符的优先级,那么栈顶元素出栈
string nfaManager::regexp_to_postfix(string regexp){
    string ans = "";
    stack<char> op;
    char c,t;
    for(int i = 0;i < regexp.size();i++){
        c = regexp[i];
        if(c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')
            ans += c;
        else
            switch(c){
                case '(' : op.push(c); break;
                case ')' :
                    while(op.top() != '('){
                        ans += op.top(); op.pop();
                    }
                    op.pop();
                    break;
                default:
                    while(!op.empty()){
                        t = op.top();
                        if(priority(t) >= priority(c)){
                            ans += t;
                            op.pop();
                        }
                        else break;
                    }
                    op.push(c);
                    break;
            }
    }
    while(!op.empty()){	//优先级比较小的一些元素可能还存留在栈中
        ans += op.top(); op.pop();
    }
    return ans;
}

//闭包 > 连接 > 并置 > 左括号
int nfaManager::priority(char c){
    int ans = 0;
    if(c == '*') ans = 3;
    else if(c == '.') ans = 2;
    else if (c == '|') ans = 1;
    return ans;
}
```



### 存储结构

```c++
class nfaManager
{
private:
    stack<int> st;	//st里面存的是每台nfa机器的start和end
    Graph nfa;
    Graph dfa;
    Graph min_dfa;
    vector<char> chars;
    int start_state_nfa;  //nfa的开始结点
    int start_state_dfa;  //最小化dfa后的开始结点
}
```

```c++
class Graph
{
private:
    int maxVertices;
    int numEdges;
public:
    Vertex1* nodeTable;
    int numVertices;
    
    Graph(){
        maxVertices = 100;
        nodeTable = new Vertex1[maxVertices];
        for(int i = 0;i < maxVertices){
            nodeTable[i].adj = 0;
        }
    }
    
    bool insertVertex();	//插入一个顶点
    bool insertEdge(int v1,int v2,char c);//插入边
}

struct Edge
{
    char dest; //存储该边的值
    int nextVertex;
    Edge* link;
    
    Edge(){}
    Edge(char c,Edge *p,int num):dest(c), link(p),nextVertex(num) {}
}

struct Vertex1
{
    int data;		//存储结点编号
    Edge* adj;
    bool end = false;
}
```

#### insertVertex

```c++
bool Graph::insertVertex(){
    if(numVertices == maxVertices) return false;
    nodeTable[numVertices].data = numVertices;
    numVertices++;
    return true;
}
```

#### insertEdge

```c++
bool Graph::insertEdge(int v1,int v2,char c){
    if(v1 < 0 || v2 < 0 || v1 == numVertices || v2 == numVertices) return false;
    Edge* p = nodeTable[v1].adj;
    while(p != NULL && !(p->dest != c) && (p->nextVertex != v2))	//允许多条边,但边的值要唯一
        p = p->link;
    if(p != NULL) return false;  //说明边已存在
    p = new Edge(c,nodeTable[v1].adj,v2);	//头插入
    nodeTable[v1].adj = p;
    numEdges++;
    return true;
}
```



### 后缀表达式转NFA

```c++
void nfaManager::postfix_to_nfa(string postfix){
    char c;
    map<char,bool> vis;
    for(int i = 0;i < postfix.size();i++){
        c = postfix[i];
        if(c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'){
            character(postfix[i]);
            if(vis.count(c) == 0){	//避免重复放入c
                chars.push_back(c);
                vis[c] = true;
            }
        }
        else
            switch(c){
                case '*' : kleene_star(); break;
                case '.' : connection(); break;
                case '|' : union_(); break;
            }
    }
    
    //st中最后只会留下一台nfa机器,就是开始结点和接受结点
    int end = st.top(); st.pop();
    int start = st.top(); st.pop();
    st.push(start);
    st.push(end);
    start_state_nfa = start; //记录起始结点
    nfa.nodeTable[end].end = true; //标识接受状态
}
```

#### character(char c)

```c++
void nfaManager::character(char c){
    //遇到一个字符，就要开两个状态，一个状态接受到c转换到另一个状态
    //同时可以看出,这是一台nfa机器,要存入st中待用
    st.push(nfa.numVertices);
    nfa.insertVertex();
    st.push(nfa.numVertices);
    nfa.insertVertex();
    nfa.insertEdge(nfa.numVertices - 2,nfa.numVertices - 1,c);
}
```

#### union_()

```c++
//遇到并置| ,用ε连接,新开两个状态
void nfaManager::union_(){
    nfa.insertVertex();
    nfa.insertVertex();			//现在numVertices=6
    //并置要用两台nfa机器，因此弹出4个,即两对(start,end)
    int d = st.top(); st.pop(); //3
    int c = st.top(); st.pop(); //2
    int b = st.top(); st.pop(); //1
    int a = st.top(); st.pop(); //0
    
    int v = nfa.numVertices;
    nfa.insertEdge(v - 2,a,'e');	// (4,0,e)
    nfa.insertEdge(v - 2,c,'e');	// (4,1,e)
    nfa.insertEdge(b,v - 1,'e');	// (1,5,e)
    nfa.insertEdge(d,v - 1,'e');	// (3,5,e)
    
    //看下面的图，之前有两台nfa机器，并置之后只剩下一台了
    //st里面存的就是每台机器的start和end,从图也可以看到,start=4,end=5
    st.push(v - 2);
    st.push(v - 1);
}
```

![$~}UYPU VM%~6AU4{8C7VMF](https://user-images.githubusercontent.com/56396192/147851999-3009df80-3c21-40f1-995d-43633a5ae576.png)


#### connection()

```c++
void nfaManager::connection(){
    //连接要用两台nfa机器,因此弹出两对(start,end)
    int d = st.top(); st.pop(); 
    int c = st.top(); st.pop(); 
    int b = st.top(); st.pop(); 
    int a = st.top(); st.pop(); 
    
    //参考上面那个图,就可以知道选哪个顶点连接
    nfa.insertEdge(b,c,'e');
    //连接之后,两台nfa机器变成一台
    st.push(a);
    st.push(d);
}
```

#### kleene_star()

```c++
void nfaManager::kleene_star(){
    //闭包是一台nfa机器变成另一台nfa机器,但要多开两个状态用ε连接
    nfa.insertVertex();
    nfa.insertVertex();
    
    int b = st.top(); st.pop();	//1
    int a = st.top(); st.pop();	//0
    int v = nfa.numVertices;	
    nfa.insertEdge(v - 2,a,'e');		//(2,0,e)
    nfa.insertEdge(v - 2,v -1 ,'e');	//(2,3,e)
    nfa.insertEdge(b,a,'e');			//(1,0,e)
    nfa.insertEdge(b,v - 1,'e');        //(1,3,e)
    st.push(v - 2);
    st.push(v - 1);
}
```

![image](https://user-images.githubusercontent.com/56396192/147852005-4297f82c-9e5c-4938-b33e-78720807a731.png)



### NFA转DFA：子集构造

```c++
//通过ε可以到达的状态集合,即子集构造算法如下
void nfaManager::epsilon_closure(int v,set<int>& s){
    Edge* p = nodeTable[v].adj;
    while(p != NULL){
        if(p->dest == 'e'){
            s.insert(p->nextVertex);
            epsilon_closure(p->nextVertex,s);
        }
        p = p->link;
    }
}
```

```c++
/*
根据老师上课的手工方法，代码思路如下：
1.构造初始状态集合
2.每一个状态集合对应一个dfa状态,用符号表遍历此集合,
	遍历完后再对集合的每个元素,用ε转换,生成能到达的所有nfa状态，若生成新的集合,就新建一个dfa状态
	若新的集合包含了nfa的接受状态，那么此dfa状态也是一个接受状态，用end字段标识
3.反复步骤2，直到不生成新的dfa状态，此时存储结构dfa就存储了整个dfa图
*/
void nfaManager::nfa_to_dfa(set<int> & states){
    map<set<int>,int> stateMap;  //存储每一个状态集合
    stateMap[states] = -1;
    queue<set<int>> nfa_stateQ;  //存储每一个状态集合的队列
    states.clear();

    //以下是对于初始状态的操作:子集构造
    states.insert(start_state);
    epsilon_closure(start_state,states);  //找初始状态的状态集合(ε能到达的)
    int nfa_stateId = 0;  //状态集合的编号
    queue<int> dfa_stateQ;
    if(stateMap.count(states) == 0){
        //每出现一个新的状态集合,都是一个新的dfa状态结点
        stateMap[states] = nfa_stateId++;
        nfa_stateQ.push(states);
        dfa.insertVertex();
        dfa_stateQ.push(dfa.numVertices - 1);
    }
    //看dfa图的初始状态是不是接受状态
    for(set<int>::iterator it=states.begin(); it != states.end(); it++){
        if(nfa.NodeTable[*it].end){
            dfa.NodeTable[0].end = true;
            break;
        }
    }

    //初始操作结束，开始遍历符号表，直到状态集合不再增加
    while(nfa_stateQ.size() != 0){
        int dfa_state = dfa_stateQ.front();	//拿dfa的状态,这个状态就是对应的nfa子集构造的状态
        dfa_stateQ.pop();

        states = nfa_stateQ.front();	//对应dfa状态的nfa子集
        nfa_stateQ.pop();
        //遍历符号表
        for(int i = 0;i < chars.size();i++){
            set<int> cState;
            for(set<int>::iterator it = states.begin(); it != states.end(); it++)
                getNeighbor(*it,chars[i],cState);  //状态*it接受到字符表中的字符能到达的所有状态

            //对于这个状态集合,再通过ε转换,能到达的所有状态
            set<int> allState;
            for(set<int>::iterator it = cState.begin(); it != cState.end(); it++){
                epsilon_closure(*it,allState);
                allState.insert(*it);
            }

            //判断这个状态allState之前是否出现过,若没有出现过,那么就生成了一个新的dfa状态
            if(stateMap.count(allState) == 0){
                stateMap[allState] = nfa_stateId++;
                nfa_stateQ.push(allState);  //此状态应继续去接受字符,看看能否得到新的状态集合
                dfa.insertVertex();
                dfa_stateQ.push(dfa.numVertices - 1);
                //此轮迭代是从dfa_state开始的,接受了chars[i],生成了状态编号为stateId - 1的状态,因此插入一条边
                dfa.insertEdge(dfa_state,nfa_stateId - 1,chars[i]);
                //判断此次生成的dfa状态是不是接受状态
                bool end = false;
                for(set<int>::iterator it = allState.begin(); it != allState.end(); it++){
                    if(nfa.NodeTable[*it].end){
                        end = true;
                        break;
                    }
                }
                dfa.NodeTable[dfa.numVertices - 1].end = end;
            }
            else{
                dfa.insertEdge(dfa_state,stateMap[allState],chars[i]);
            }
        }
    }

}
```

```c++
//nfa图中,一个状态v接受到c到达的所有状态states
void nfaManager::getNeighbor(int v,char c,set<int> states){
    Edge* p = nfa.nodeTable[v].adj;
    while(p != NULL){
        if(p->dest == c){
            states.insert(p->nextVertex);
        }
        p = p->link;
    }
}
```



### 最小化DFA

#### 例子分析

![image](https://user-images.githubusercontent.com/56396192/147852009-f44f793d-dca0-4f52-9197-1523c171e2a3.png)

首先被分成 A(接受状态)，N（非终止状态）

N：`{q0, q1, q2, q4}`

A：`{q3, q5}`

由于 A 不接受任何字符所以不能再分，由于`字母 e 可以让 q0, q1 指向内部或者不指向(但是仍然在集合{q0,q1,q2,q4},没有跑出去)`，而使 `q2, q4 指向另外一个集合(从N转成A)`，所以 N 被分割成了：`{q0, q1} {q2, q4}`

分割的条件就是在原来的集合`{q0, q1, q2, q4}`中`{q0, q1} 接受字符走到的集合与{q2, q4}接受字符走到的集合不一样`

`对于剩下的{q2, q4},由于q2和q4接受e跑到的集合都是一样的，因此不用分割了`

`对于剩下的{q0, q1}由于q0接受e或者接受f还是待在{q0, q1}, 但是q1接受e或者接受i之后跑到了{q2, q4},这满足了分割条件`

`因此{q0, q1}被分成了{q0} , {q1}`

`最后结果:{q0} {q1} {q2, q4} {q3, q5}`

![image](https://user-images.githubusercontent.com/56396192/147852061-83919fa7-06f1-4fb0-a6f9-ca8852a89841.png)



```c++
void nfaManager::minimize_dfa(){
    vector<int> s1,s2;  //终态集合(s1),非终态(s2)
    //首先划分终态集合和非终态集合
    for(int i = 0;i < dfa.numVertices;i++){
        if(dfa.nodeTable[i].end)
            s1.push_back(dfa.nodeTable[i].data);
        else
            s2.push_back(dfa.nodeTable[i].data);
    }
    queue<vector<int>> stateQ;
    //先从非终态集合开始划分
    stateQ.push(s2);
    stateQ.push(s1);
    
    map<int,vector<int>> minDfa;	//最小化dfa
    int min_cnt = 0;
    map<int,vector<int>> tmpDfa;	//临时的,保存划分后的dfa
    int tmp_cnt = 0;
    tmpDfa[tmp_cnt++] = s1;
    tmpDfa[tmp_cnt++] = s2;
    
    int pop_num = -1; //这里是为了记录此时处理的集合在临时表中的序号，一旦该集合需要划分，则要在临时表里删除掉该集合
    //开始划分状态集合
    while(!stateQ.empty()){
        vector<int> curState = stateQ.front();
        stateQ.pop();
        pop_num++;
        if(curState.empty())
            continue;  
        if(curState.size() == 1){	//若集合中只剩下一个状态,那么不可划分
            minDfa[min_cnt++] = curState;
            continue;
        }
        
        vector<vector<int>> split_states;  //收集划分的集合
        int node = curState[0];	//以状态集合中第一个状态为划分点
        bool split = false;
        for(int i = 1;i < curState.size();i++){
            //如果两个结点在字符表的转换结果不一样,就需要划分
            if(canSplit(node,curState[i],chars,tmpDfa)){
                split = true;
                vector<int> newStates;
                for(vector<int>::iterator it = curState.begin() + 1;it != curState.end();){
                    if(!canSplit(*it,curState[i],chars,tmpDfa)){
                        newStates.push_back(*it);
                        it = it.erase(*it);	// 在curState中删除该结点, 更新it为原it的下一个位置
                    }
                    else
                        it++;
                }
                split_states.push_back(newStates);
                i = i - 1;
            }
        } //end for:划分一个状态集合结束
        if(split == false){
            minDfa[min_cnt++] = curState;	//若curState不用再划分,那么加入到minDfa中保存起来
        }
        else{
            split_states.push_back(curState); //curState也是划分的一部分
            for(int j = 0;j < split_states.size();j++){
                tmpDfa[tmp_cnt++] = split_states[j];
                stateQ.push(split_states[j]);
            }
            tmpDfa[pop_num].clear(); //curState在划分之后没有用了,需要在tmpDfa中清除掉
        }
    }//end while:没有状态需要在划分
    
    //此时获得了最小化dfa的映射表minDfa,根据minDfa生成最小化dfa图
    for(int i = 0;i < minDfa.size();i++){
        min_dfa.insertVertex();
    }
    for(int i = 0;i < minDfa.size();i++){
        for(int j = 0;j < minDfa[i].size();j++){
            //遍历过程中可以顺便确定开始结点和终止结点
            if(dfa.nodeTable[minDfa[i][j]].data == 0)
                start_state_dfa = i;
            min_dfa.nodeTable[i].end = dfa.nodeTable[minDfa[i][j]].end;
            
            //每个minDfa[i]就是一个状态集合,看看里面的状态能去往最小化Dfa的哪些状态点
            Edge* p = dfa.nodeTable[minDfa[i][j]].adj;
            while(p != NULL){
                for(int k = 0;k < minDfa.size();k++){
                    vector<int> iterator:: it;
                    it = std::find(minDfa[k].begin(),minDfa[k].end(),p->nextVertex);
                    if(it != minDfa[k].end()){
                        min_dfa.insertEdge(i,k,p->dest); //若有重复边,则不会插入
                    }
                }
                p = p->link;
            }
        }//end for j:看minDfa[i]能连接哪些minDfa中的其他结点
    }//end for i:生成min_dfa结束
    
}
```

```c++
bool nfaManager::canSplit(int v1,int v2,vector<char> chars,map<int,vector<int>> tmpDfa){
    for(int i = 0;i < chars.size();i++){
        if(dfa_transform(v1,chars[i],tmpDfa) != dfa_transform(v2,chars[i],tmpDfa))
            return false;
    }
    return true;
}
```

```c++
//在最小化dfa中,一个状态v接受到c能变成什么状态
int nfaManager::transform(int v,char c,map<int,vector<int>> tmpDfa){
    Edge* p = dfa.nodeTable[v].adj;
    int state;
    while(p != NULL){
        if(p->dest == c){
            state = p->nextVertex;
            break;
        }
        p = p->link;
    }
    //说明状态v接受了c之后,可能出错可能接受,得看状态v这个结点是不是接受结点
    if(p == NULL){
        if(dfa.nodeTable[v].end)
            state = -2;  //-2表示接受
        else
            state = -1;  //-1表示出错
        return state;
    }
    //代码走到这里,找到了v接受c变成的状态,那么去tmpDfa里面找它对应最小化dfa时候的状态
    for(int i = 0;i < tmpDfa.size();i++){
        for(int j = 0;j < tmpDfa[i].size();j++){
            if(tmpDfa[i][j] == state){
                state = i;
                break;
            }
        }
    }
    return state;
}
```



### 根据最小化DFA生成词法分析程序(c++语言,词法分析程序)

```c++
void f(vector<string>& lines,string s){
    lines.push_back(s);
}


//采用二维转换表的方式生成分析程序
void nfaManager::getCcode(int start, vector<string> &lines)
{
    f(lines,"#include<iostream>");
    f(lines,"using namespace std;");
    string tmp = "'";
    //生成边集
    string s = "char edge[] = {";
    for(int i = 0;i < chars.size();i++){
        s += tmp + chars[i] + "', ";  //多一个逗号编译器能通过,不需要额外处理
    }
    s += "};";
    f(lines,s);

    //生成二维转换表  DFA[state][char_idx], 当前的状态state,收到char_idx,转换成什么,-1表示出错.
    s = "int DFA["+std::to_string(mini_dfa.numVertices)+"]["+std::to_string(chars.size())+"] = {";
    for(int i = 0;i < mini_dfa.numVertices;i++){
        string curLine = "{";
        for(int j = 0;j < chars.size();j++){
            Edge* p = mini_dfa.NodeTable[i].adj;
            while(p != NULL){
                if(p->dest == chars[j]){
                    curLine += std::to_string(p->nextVertex);
                    break;
                }
                p = p->link;
            }
            if(p == NULL) curLine += std::to_string(-1);
            curLine += ", ";
        }
        curLine +="}, ";
        s += curLine;
    }
    s += "};";
    f(lines,s);

    //生成getPos函数:字符c对应edge的下标
    f(lines,"int getPos(char c) {");
    f(lines,"int pos = -1;");
    f(lines,"for(int i = 0; i < "+std::to_string(chars.size())+"; i++){");
    f(lines,"if(c == edge[i]) { pos = i; break; }");
    f(lines,"}"); //for结束
    f(lines,"return pos;");
    f(lines,"}"); //getPos函数结束

    //接下来是main函数，while循环里面套一个switch,接受到什么字符转换到什么状态(不出错的话)
    f(lines,"int main(){");
    f(lines,"int j = 0, start = "+std::to_string(start)+",char_idx;");
    f(lines,"char* str = new char[256];");
    f(lines,"cin >> str;");
    f(lines,"while(str[j] != '\\0') {");
    f(lines,"switch(str[j]){");
    tmp = "case '";
    for(int i = 0;i < chars.size();i++){
        f(lines,tmp + chars[i] + "':");
        f(lines,"char_idx = getPos(str[j]);");
        f(lines,"if(start != -1 && char_idx != -1){");
        f(lines,"start = DFA[start][char_idx];}");
        f(lines,"else { cout <<\"No\"; return 0; }");
        f(lines,"break;");
    }
    f(lines,"default: cout <<\"No\"; return 0;");
    f(lines,"}"); //switch结束
    f(lines,"j++;");
    f(lines,"}");  //while结束
    f(lines,"if(start != -1) cout << \"Yes\";\n else cout << \"No\";");
    f(lines,"return 0;\n}");
}
```



## 实验三

 实验三：文法问题处理器


一、实验内容：
设计一个应用软件，以实现文法的化简及各种问题的处理。

二、实验要求：
 （1）系统需要提供一个文法编辑界面，让用户输入文法规则（可保存、打开存有文法规则的文件）
 （2）化简文法：检查文法是否存在有害规则和多余规则并将其去除。系统应该提供窗口以便用户可以查看文法化简后的结果。
 （3）检查该文法是否存在着左公共因子（可能包含直接和间接的情况）。如果存在，则消除该文法的左公共因子。系统应该提供窗口以便用户可以查看消除左公共因子的结果。
 （4）检查该文法是否存在着左递归（可能包含直接和间接的情况），如果存在，则消除该文法的左递归。系统应该提供窗口以便用户可以查看消除左递归后的结果。
 （5）求出经过前面步骤处理好的文法各非终结符号的first集合与follow集合，并提供窗口以便用户可以查看这些集合结果。【可以采用表格的形式呈现】
 （6）对输入的句子进行最左推导分析，系统应该提供界面让用户可以输入要分析的句子以及方便用户查看最左推导的每一步推导结果。【可以采用表格的形式呈现推导的每一步结果】
 （7）应该书写完善的软件文档

![image-20220111200734601](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20220111200734601.png)



## 实验四

 实验四  TINY扩充语言的语法分析


一、实验内容：

扩充的语法规则有：实现 do while循环，for循环，扩充算术表达式的运算符号：-= 减法赋值运算符号（类似于C语言的-=）、求余%、乘方^，
扩充比较运算符号：==（等于），>(大于)、<=(小于等于)、>=(大于等于)、<>(不等于)等运算符号，
新增支持正则表达式以及用于repeat循环、do while循环、if条件语句作条件判断的逻辑表达式：运算符号有 and（与）、 or（或）、 not（非） 。
具体文法规则自行构造。

可参考：云盘中参考书P97及P136的文法规则。

(1) Dowhile-stmt-->do  stmt-sequence  while(exp); 
(2) for-stmt-->for identifier:=simple-exp  to  simple-exp  do  stmt-sequence enddo    步长递增1
(3) for-stmt-->for identifier:=simple-exp  downto  simple-exp  do  stmt-sequence enddo    步长递减1
(4) -= 减法赋值运算符号、求余%、乘方^、>=(大于等于)、<=(小于等于)、>(大于)、<>(不等于)运算符号的文法规则请自行组织。
(5)把tiny原来的赋值运算符号(:=)改为(=),而等于的比较符号符号（=）则改为（==）
(6)为tiny语言增加一种新的表达式——正则表达式，其支持的运算符号有  或(|)  、连接(&)、闭包(#)、括号( ) 以及基本正则表达式 。
(7)为tiny语言增加一种新的语句，ID:=正则表达式  
(8)为tiny语言增加一种新的表达式——逻辑表达式，其支持的运算符号有  and(与)  、or (或)、非(not)。
(9)为了实现以上的扩充或改写功能，还需要对原tiny语言的文法规则做好相应的改造处理。 


二、要求：
（1）要提供一个源程序编辑界面，以让用户输入源程序（可保存、打开源程序）
（2）可由用户选择是否生成语法树，并可查看所生成的语法树。
（3）应该书写完善的软件文档
（4）要求应用程序应为Windows界面。
