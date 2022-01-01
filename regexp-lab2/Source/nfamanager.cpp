#include "nfamanager.h"
#include "graph.h"
#include <QDir>
#include <QFileInfo>
#include<QCoreApplication>
#include <QDebug>
#include <map>

nfaManager::nfaManager()
{
    int NumOfChar=0;
}

//正则表达式插入连接符“.”
string nfaManager::insert_concat(string regexp){
    string ret="";
    char c,c2;
    for(unsigned int i=0; i<regexp.size(); i++){
        c=regexp[i];
        if(i+1<regexp.size()){
            c2=regexp[i+1];
            ret+=c;
            if(c!='('&&c2!=')'&&c!='|'&&c2!='|'&&c2!='*'){
                ret+='.';
            }
        }
    }
    ret+=regexp[regexp.size()-1];//处理原正则表达式最后一个字符
    cout <<"after insert . -->  " << ret << endl;
    return ret;
}

int nfaManager::priority(char c){
    if(c=='*')
        return 3;
    else if(c=='.')
        return 2;
    else if(c=='|')
        return 1;
    else return 0;
}

string nfaManager::regexp_to_postfix(string regexp)
{
    string postfix="";
    stack<char> op;
    char c;
    for(unsigned int i=0; i<regexp.size(); i++)
    {
        if((regexp[i]>=48 && regexp[i]<=57)||(regexp[i]>=65 && regexp[i]<=90)
                ||(regexp[i]>=97 && regexp[i]<=122))
            postfix+=regexp[i];
        else
             switch(regexp[i])
            {
            case '(':
                op.push(regexp[i]); break;
            case ')':
                while(op.top()!='('){
                    postfix+=op.top();
                    op.pop();
                }
                op.pop();
                break;
            default:
                while(!op.empty()){
                    c=op.top();
                    if(priority(c)>=priority(regexp[i])){
                        postfix+=op.top();
                        op.pop();
                    }
                    else break;
                    }
                op.push(regexp[i]);
            }
    }
    while(!op.empty())
    {
        postfix += op.top();
        op.pop();
    }
    cout<<"postfix:" << postfix<<endl;
    return postfix;
}

void nfaManager::character(char c)
{
    st.push(nfa.numVertices);
    nfa.insertVertex();
    st.push(nfa.numVertices);
    nfa.insertVertex();
    nfa.insertEdge(nfa.numVertices-2,nfa.numVertices-1,c);
}

void nfaManager::union_()//处理选择
{
    nfa.insertVertex();
    nfa.insertVertex();
    int d = st.top(); st.pop();
    int c = st.top(); st.pop();
    int b = st.top(); st.pop();
    int a = st.top(); st.pop();
    nfa.insertEdge(nfa.numVertices-2,a,'e');
    nfa.insertEdge(nfa.numVertices-2,c,'e');
    nfa.insertEdge(b,nfa.numVertices-1,'e');
    nfa.insertEdge(d,nfa.numVertices-1,'e');
    st.push(nfa.numVertices-2);
    st.push(nfa.numVertices-1);

}

void nfaManager::concatenation()//处理连接
{
    int d = st.top(); st.pop();
    int c = st.top(); st.pop();
    int b = st.top(); st.pop();
    int a = st.top(); st.pop();
    nfa.insertEdge(b,c,'e');
    st.push(a);
    st.push(d);
}

void nfaManager::kleene_star()//闭包
{
    nfa.insertVertex();
    nfa.insertVertex();
    int b = st.top();
    st.pop();
    int a = st.top();
    st.pop();
    nfa.insertEdge(nfa.numVertices-2,nfa.numVertices-1,'e');
    nfa.insertEdge(b,nfa.numVertices-1,'e');
    nfa.insertEdge(nfa.numVertices-2,a,'e');
    nfa.insertEdge(b,a,'e');
    st.push(nfa.numVertices-2);
    st.push(nfa.numVertices-1);
}

void nfaManager::postfix_to_nfa(string postfix)
{
    map<char,bool> vis;
    for(unsigned int i=0; i<postfix.size(); i++)
    {

        if((postfix[i]>=48 && postfix[i]<=57)||(postfix[i]>=65 && postfix[i]<=90)
                ||(postfix[i]>=97 && postfix[i]<=122))
        {
            character(postfix[i]);
            if(vis.count(postfix[i]) == 0){
                NumOfChar++;
                chars.push_back(postfix[i]);
                vis[postfix[i]] = true;
            }
        }
        else
            switch(postfix[i])
            {
            case '*': kleene_star(); break;
            case '.': concatenation(); break;
            case '|': union_();
        }
    }

   int w = st.top();
   nfa.NodeTable[w].final=1;//标识最后一个结点

   int m=st.top();
   st.pop();
   int n=st.top();
   st.pop();
   st.push(n);
   st.push(m);
   start_state = n;//记录起始结点的位置

}

void nfaManager::show_nfa(string zzz)
{
    string s = zzz;
    s =s+ "\\graph\\nfa.txt";
    cout<<s;
    ofstream out(s);
    out<<"digraph abc{"<<endl;

    for(int i=0;i<nfa.numVertices; i++)
    {
        //不显示结点的名称
        out<<nfa.NodeTable[i].data<<"[fontcolor=white][shape=circle];"<<endl;
    }

    //开始结点显示“begin”，这里发现画图命令后面的会覆盖前面的
    out<<nfa.NodeTable[start_state].data<<"[fontcolor=black][label=begin];"<<endl;

    for(int i=0;i<nfa.numVertices; i++)
    {
        //接受结点显示“acc”
        if(nfa.NodeTable[i].final==1)
            out<<nfa.NodeTable[i].data<<"[fontcolor=black][label=acc];"<<endl;

        Edge *p=nfa.NodeTable[i].adj;
        while(p!=NULL)
        {         
            out<<nfa.NodeTable[i].data<<"->"<<p->nextVertex<<"[label="<<p->dest<<"];"<<endl;
            p=p->link;
        }
    }

    out<<"}";
    out.close();
}

//nfa图中,一个状态v接受到c到达的所有状态di
void nfaManager::getNeighbor(int v, char c,set<int>&di)
{
    Edge *p=nfa.NodeTable[v].adj;
    while(p!=NULL)
    {
        if(p->dest==c)
            di.insert(p->nextVertex);
        p=p->link;
    }
}

void nfaManager::epsilon_closure(int v,set<int>&si)
{
    Edge *p=nfa.NodeTable[v].adj;
    while(p!=NULL)
    {
        if(p->dest=='e')
        {
             si.insert(p->nextVertex);
             epsilon_closure(p->nextVertex,si);
        }
        p=p->link;
    }
}

void nfaManager::nfa_to_dfa(set<int> & states){
    map<set<int>,int> stateMap;  //存储每一个状态集合
    stateMap[states] = -1;
    queue<set<int>> nfa_stateQ;  //存储每一个状态集合的队列
    states.clear();

    //以下是对于初始状态的操作:子集构造
    states.insert(start_state);
    epsilon_closure(start_state,states);  //找初始状态的状态集合(ε能到达的)
    int stateId = 0;  //状态集合的编号
    queue<int> dfa_stateQ;
    if(stateMap.count(states) == 0){
        //每出现一个新的状态集合,都是一个新的dfa状态结点
        stateMap[states] = stateId++;
        nfa_stateQ.push(states);
        dfa.insertVertex();
        dfa_stateQ.push(dfa.numVertices - 1);
    }
    //看dfa图的初始状态是不是接受状态
    for(set<int>::iterator it=states.begin(); it != states.end(); it++){
        if(nfa.NodeTable[*it].final){
            dfa.NodeTable[0].final = true;
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
                stateMap[allState] = stateId++;
                nfa_stateQ.push(allState);  //此状态应继续去接受字符,看看能否得到新的状态集合
                dfa.insertVertex();
                dfa_stateQ.push(dfa.numVertices - 1);
                //此轮迭代是从dfa_state开始的,接受了chars[i],生成了状态编号为stateId - 1的状态,因此插入一条边
                dfa.insertEdge(dfa_state,stateId - 1,chars[i]);
                //判断此次生成的dfa状态是不是接受状态
                bool end = false;
                for(set<int>::iterator it = allState.begin(); it != allState.end(); it++){
                    if(nfa.NodeTable[*it].final){
                        end = true;
                        break;
                    }
                }
                dfa.NodeTable[dfa.numVertices - 1].final = end;
            }
            else{
                dfa.insertEdge(dfa_state,stateMap[allState],chars[i]);
            }
        }
    }

}

//void nfaManager::nfa_to_dfa(set<int>&si,int k)
//{
//    map<set<int>, int> mp;
//    mp[si]=-1;
//    queue<set<int> > que;

//    si.clear();
//    si.insert(start_state);
//    int ct=0;

//    queue<int> s;

//    epsilon_closure(start_state,si);
//    if(mp.count(si)==0){
//        mp[si]=ct++;
//        que.push(si);
//        dfa.insertVertex();
//        s.push(dfa.numVertices-1);
//    }

//    for (set<int>::iterator it=si.begin(); it!=si.end(); ++it)
//    {
//        if(nfa.NodeTable[*it].final==1)
//            dfa.NodeTable[0].final=1;
//    }
//    //si是状态集合
//    int h;
//    while(que.size()!=0)
//    {
//        h=s.front();
//        s.pop();

//        si.empty();
//        si=que.front();
//        que.pop();

//        for(int j=0;j<chars.size();j++)
//        {
//            set<int> di;
//            //先走一步
//            for (set<int>::iterator it=si.begin(); it!=si.end(); ++it)
//                getNeighbor(*it,chars[j],di);


//            set<int> gi;//传递：si -"字母"-> di -"e"-> gi
//            //再走一步
//            for (set<int>::iterator ite=di.begin(); ite!=di.end(); ++ite)
//                 {
//                    epsilon_closure(*ite,gi);
//                    gi.insert(*ite);
//                 }

//                if(mp.count(gi)==0) //看gi这个状态集合有没有出现过
//                {
//                    mp[gi]=ct++;
//                    que.push(gi);
//                    dfa.insertVertex();
//                    s.push(dfa.numVertices-1);
//                    dfa.insertEdge(h,ct-1,chars[j]);
//                    int f1=0;
//                    for (set<int>::iterator it=gi.begin(); it!=gi.end(); ++it)
//                    {
//                        if(nfa.NodeTable[*it].final==1)
//                            f1=1;
//                    }
//                    dfa.NodeTable[dfa.numVertices-1].final=f1;
//                }
//                else
//                {
//                    dfa.insertEdge(h,mp[gi],chars[j]);
//                }
//                di.empty();
//                gi.empty();

//        }
//    }
//}

void nfaManager::show_dfa(string zzz)
{
    string s = zzz;
    s =s+ "\\graph\\dfa.txt";
    ofstream out(s);
    out<<"digraph abc{";

    for(int i=0;i<dfa.numVertices; i++)
    {
        //不显示结点的名称
        out<<dfa.NodeTable[i].data<<"[fontcolor=white][shape=circle];"<<endl;//
    }

    //开始结点显示“begin”，这里发现画图命令后面的会覆盖前面的
    out<<dfa.NodeTable[0].data<<"[fontcolor=black][label=begin];"<<endl;

    for(int i=0;i<dfa.numVertices; i++)
    {
        //接受结点显示“acc”
        if(dfa.NodeTable[i].final==1)
            out<<dfa.NodeTable[i].data<<"[fontcolor=black][label=acc];"<<endl;

        Edge *p=dfa.NodeTable[i].adj;
        while(p!=NULL)
        {
            out<<dfa.NodeTable[i].data<<"->"<<p->nextVertex<<"[label="<<p->dest<<"];"<<endl;
            p=p->link;
        }
    }
    out<<"}";
    out.close();
}

int nfaManager::dfa_transform(int v, char c, map<int,vector<int>> mp_1)
{
    Edge *p=dfa.NodeTable[v].adj;
    int node;
    //遍历查看该结点是否有该边
     while(p!=NULL)
    {
        if(p->dest==c)
        {
            node=p->nextVertex;
            break;
        }
        p=p->link;
    }
    //假如该结点没有该边，这里我是这样处理的：该结点在该符号下指向的自己的集合
    //因此让node为本身，这样在接下里的处理，让它返回自身集合
    if(p==NULL){
        //node= v;
        if(dfa.NodeTable[v].final == 1){
            return -2;  //-2表接受状态
        }
        else{
            return -1; //-1表拒绝状态
        }
    }
    //经过以上工作，确定该结点，接下里返回该结点所在的集合（即指向某个vector<int>的指针）
    int q; //所在集合在临时表中的序号
    for(int i=0;i<mp_1.size();i++)
    {
        for(int j=0;j<mp_1[i].size();j++)
        {
            if(mp_1[i][j]==node)
                q = i;
        }
    }
    return q;
}

bool nfaManager::if_equal(int v1, int v2, vector<char> chars, map<int,vector<int>> mp_1)
{
    for(int i=0;i<chars.size();i++)
    {
        if(dfa_transform(v1,chars[i],mp_1)!=dfa_transform(v2,chars[i],mp_1))
            return false;
    }
    return true;
}

void nfaManager::minimize_dfa()
{
    vector<int> s1,s2;//先把所有的dfa结点划分为两个子集：终态（s1）和非终态（s2）
    for(int i=0;i<dfa.numVertices; i++)
    {
        if(dfa.NodeTable[i].final==1)
            s1.push_back(dfa.NodeTable[i].data);
        else
            s2.push_back(dfa.NodeTable[i].data);
    }

    queue<vector<int> > states;  // 等待划分的状态集合队列
    states.push(s2);             //先处理非终态集
    states.push(s1);

    //这张映射表是用来在临时划分的结点和dfa结点集建立映射关系的，临时的
    map<int,vector<int>> mp_1;
    int ct_1=0;
    mp_1[ct_1++]=s1;
    mp_1[ct_1++]=s2;

    //这张映射表是用来在mini_dfa结点和dfa结点集建立映射关系的，最终结果的
    map<int,vector<int>> mp;
    int ct=0;

    int pop_num=-1;    //这里是为了记录此时处理的集合在临时表中的序号，一旦该集合需要划分，则要在临时表里删除掉该集合

    //开始划分状态集合
    while(!states.empty())
    {

        vector<int> s= states.front();
        states.pop();
        pop_num++;

        if(s.empty()) continue;
        int sz= s.size();
        if(sz==1)
        {
            mp[ct++]= s;    //若集合(终态或非终态)只有一个元素了,那么不必再划分,直接加入到最终映射表
            continue;
        }

        //用来收集dfa结点序号
        vector<vector<int>> sub_states;

        int node= s[0];
        bool ok = true;

        for(int i=1;i<sz;i++)
        {
            //如果两个结点的转换结果不一样，则需要划分
            if(if_equal(s[i],node,chars,mp_1)==false)
            {
                int cur_node =s[i];
                vector<int> cur_states; //cur_states是划分出来的新集合
                for(vector<int>::iterator it = s.begin()+1; it != s.end();)
                {
                    if(if_equal(*it,cur_node,chars,mp_1))
                    {
                        cur_states.push_back(*it);  // 加入新集合
                        it =s.erase(it);            // 在s中删除该结点, 更新it为原it的下一个位置
                    }
                    else
                        ++it;
                }
                sub_states.push_back(cur_states);
                // 因为有删除s中的元素，要更新一下s的大小
                sz = s.size();
                i = i - 1;
                ok = false;
            }
        }
        if(ok)
        {
            mp[ct++]=s;
            //输出日志
            cout<<"This is a test:"<<endl;
            cout<<ct-1;
            for(int r=0;r<mp[ct-1].size();r++)
            {
                cout<<mp[ct-1][r]<<" ";
            }
            cout<<endl;
        }
        else
        {
            sub_states.push_back(s);
            for(int l=0;l<sub_states.size();l++)
            {
                states.push(sub_states[l]);
                mp_1[ct_1++]=sub_states[l];
            }
            mp_1[pop_num].clear();
        }
    }

    //获得最终结果的mini_dfa结点和dfa结点集的映射表mp后，下一步生成最小化dfa

    //首先在mini_dfa中插入结点
    for(int i=0;i<mp.size();i++)
    {
        mini_dfa.insertVertex();
    }

    //接着在mini_dfa中插入边
    for(int i=0;i<mp.size();i++)
    {
        //遍历每个mini_dfa结点所对应的dfa结点集中的结点
        for(int j=0;j<mp[i].size();j++)
        {
            //遍历每个dfa结点的边
            Edge *p=dfa.NodeTable[mp[i][j]].adj;
            while(p!=NULL)
            {
                //然后又要遍历每个mini_dfa结点所对应的dfa结点集中的结点，以找出该结点所连接的结点所在的mini_dfa
                for(int k=0;k<mp.size();k++)
                {
                    vector<int>::iterator it;
                    it=std::find(mp[k].begin(),mp[k].end(),p->nextVertex);
                    if(it!=mp[k].end())
                    {
                        mini_dfa.insertEdge(i,k,p->dest);
                        //break;
                    }
                }
                p=p->link;
            }
        }
    }

    //再次遍历mini_dfa的结点，这次是为了确定开始结点和接受结点
    for(int i=0;i<mp.size();i++)
    {
        for(int j=0;j<mp[i].size();j++)
        {
            if(dfa.NodeTable[mp[i][j]].data==0)
                start_state_dfa = i;
            if(dfa.NodeTable[mp[i][j]].final==1)
                mini_dfa.NodeTable[i].final = 1;
        }
    }

}

void nfaManager::show_mini_dfa(string zzz)
{
    string s = zzz;
    s =s+ "\\graph\\mini_dfa.txt";
    ofstream out(s);
    out<<"digraph abc{";

    for(int i=0;i<mini_dfa.numVertices; i++)
    {
        //不显示结点的名称
        out<<mini_dfa.NodeTable[i].data<<"[fontcolor=white][shape=circle];"<<endl;
    }

    //开始结点显示“begin”，这里发现画图命令后面的会覆盖前面的
    out<<dfa.NodeTable[start_state_dfa].data<<"[fontcolor=black][label=begin];"<<endl;

    for(int i=0;i<mini_dfa.numVertices; i++)
    {
        //接受结点显示“acc”
        if(mini_dfa.NodeTable[i].final==1)
            out<<mini_dfa.NodeTable[i].data<<"[fontcolor=black][label=acc];"<<endl;

        Edge *p=mini_dfa.NodeTable[i].adj;
        while(p!=NULL)
        {
            out<<mini_dfa.NodeTable[i].data<<"->"<<p->nextVertex<<"[label="<<p->dest<<"];"<<endl;
            p=p->link;
        }
    }
    out<<"}";
    out.close();
}

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

    //接下来是main函数
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


void nfaManager::show_code(string zzz)
{
    vector<string> lines;
    getCcode(mini_dfa.NodeTable[start_state_dfa].data,lines);

    string s = zzz;
   // s =s+ "\\Ccode.txt";
    ofstream out("Ccode.txt");
    for(int i=0;i<lines.size();i++)
    {
       out<<lines[i]<<endl;
    }
    out.close();

}
