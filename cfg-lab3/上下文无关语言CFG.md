## 上下文无关语言CFG

![image-20211227141834793](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211227141834793.png)

### 产生式（规则）的作用：

![image-20211227141942368](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211227141942368.png)

### 举一个CFG的例子

![image-20211227142043780](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211227142043780.png)



### 什么是归约和推导

![image-20211227142151779](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211227142151779.png)



## 实验三：

### 文法结构

设计文法类 CFG，上下文无关文法的四个基本要素有：**终结符、非终结符、开始符号和产生式的集合**

终结符和非终结符采用 set 集合存储，可自动去重；文法开始符号是单个的 Char 字符；文法产生式集合采用集合 ArrayList,保存的元素对象类型是 Rules 类（产生式）

如 A->Bc|w，左侧为单个字符，用 char 存储，右侧用字符串列表 right 来存储，right 的每个元素为该产生式的其中一个方案

`left = A` 		`right = {Bc,w}`

```java
public class CFG{
    char startSymbol;  //文法开始符号
    Set<Character> Vn; //非终结符
    Set<Character> Vt; //终结符
    List<Rules> production; //文法产生式存储集合
    Map<Character,Set<Character>> first; //first集合
    Map<Character,Set<Character>> follow; //follow集合
    Map<String,Set<Character>> select; //LL(1)分析法要用到,求每个方案的first集合
    //能不能用LL(1)的方法去分析？对于左公因子问题，它不是LL(1)文法,但是提取一下左公因子就可以用LL(1)分析法去解决
    //但是对于二义性，这里isLL1为false
    boolean isLL1;
    
    Map<LL1Node,String> predict;	//LL1分析表的存储结构	LL1Node的定义在:构造预测分析表中
    boolean flag; //LL1分析输入串的成功标志
}
```

### 文法产生式结构

```java
public class Rules{
    char left; //左部符号
    List<String> right; //右部符号
    boolean isAppear; //去除左递归的时候要用,用来判断此产生式的左部有没有被扫描过,若扫描过那么可以代入
    boolean isClean;  //
}
```



### 文法存储代码

思路：文法G[S] = ( Vn, Vt, P, S )

- 将第一条产生式的左部符号设为S
- 对 P 中的每一个产生式 A->X1 | X2| X3...其中 Xi 为不同的方案
  - 将A加入Vn，对Xi进行遍历的时候，扫描到大写字符加入`cfg.Vn(文法终结符)`,扫描到小写字符加入`cfg.Vt(文法非终结符)`
  - 每一个Xi,用StringBuilder表示，扫描到 | 表示一个方案成立
  - 将全部Xi用一个`ArrayList,即items`存储，最后将整个产生式加入到cfg.production
    - 若A之前出现过，找到A之后追加，`addAll`
    - 否则直接`cfg.production.add(items)`
  - 最终的产生式存储为 A->X1|X2…Xn，即将该非终结符对应的方案都放在一个字符串列表中。用户可输入 `A->aB|c 和 A->Df `两种形式，最终会合并为`A->aB|c|Df`

```java
//rule由外部输入,代表每一条规则,下标为0的那一条是文法开始的规则
public CFG storeGrammar(String[] rule){
    CFG cfg = new CFG();
    for(int i = 0;i < rule.length;i++){
        char[] curRule = rule[i].toCharArray();
        if(i == 0){	//第一条产生式,那么设置文法的开始符号
            cfg.startSymbol = curRule[0];
        }
        
        boolean same = 0;  //判断有没有相同的文法左部符号
        for(Rules rul : cfg.production){
            if(rul.left == curRule[0]){
                same = true;
                break;
            }
        }
        
        cfg.Vn.add(curRule[0]); //往set中加入非终结符号
        StringBuilder sb = new StringBuilder(); //找每一个方案
        List<StringBuilder> items = new ArrayList<>();
        for(int j = 3;j < curRule.length;j++){	//产生式右部从下标3开始
            if(curRule[j] != '|' && j != curRule.length){
                sb.append(curRule[j]);
            }
            else{
                items.add(sb);
                sb = new StringBuilder();  //接着存储下一个方案
            }
            
            if(curRule[j] == '@' || curRule[j] == '|') { //do nothing  }
            else if(curRule[j] >= 'A' && curRule[j] <= 'Z'){
                cfg.Vn.add(curRule[j]);	//保存非终结符号
            }
            else if(curRule[j] >= 'a' && curRule[j] <= 'z'){
                cfg.Vt.add(curRule[j]);	//保存终结符号
            }
         }
                
        if(!same){	//说明是一个新规则
            Rules rul = new Rules(curRule[0],items);
            cfg.production.add(rul);
        }
        else{
            for(Rules rul : cfg.production){	//向已有规则中追加 A -> X1 和 A -> X2 变成 A-> X1 | X2
                if(rul.left == curRule[0]){
                    rul.addAll(items);
                    break;
                }
            }
        }
    }
    return cfg;
}
```



### 文法化简

```java
    public CFG simplifyRules(CFG cfg){
        cfg=deleteUnterminableRules(cfg);  //删除不可终止的
        cfg=deleteUnreachableRules(cfg);   //删除不可到达的
        return cfg;
    }
```

#### 去除不可终止

文法存储之后，有`G[S] = (Vn, Vt, P, S)`

复制一份相同的文法`G1[S] = (Vn1, Vt, P1, S)`

- 更新Vn1：找无害的规则A

  - a.分别清空Vn1和P1

  - b.对 P 中每一个产生式 `A->X1|X2|……Xk`，若存在方案Xi,Xi 中每一个字符都属于 `Vn1∪Vt`，则将A加入到Vn1中，如果存在 A->@的规则，也需要将 A 加入 Vn1，加入之后马上跳出，去下一个产生式

  - c.重复上述b，直到Vn1不再增大

  - ```java
    //为什么要重复做呢？
    B->Bd
    B->c
    文法存储之后 Vn = {B}  Vt = {c,d}
    化简开始：
        Vn1 = {} Vt = {c,d}
    扫描到B->Bd的时候,我们判断它现在是一个有害规则！因为B不属于Vn1∪Vt
    扫描到B->c的时候,发现B是一个无害规则,因为方案c的所有字符都属于  Vn1∪Vt，因此将B加入Vn1
    这个时候Vn1 = {B} Vt = {c,d}
    
    然后我们再次从头开始扫描，这时候B->Bd就是无害规则了,且Vn1不再增大,那么退出循环即可
    ```

- 更新P：在已有的产生式中去掉有害规则

  - 对 P 中每一个产生式 `A->X1|X2|……Xk`，若 A 及方案 Xi 中每一个字符都属于 `Vn1∪Vt`,则将`A->Xi`放入P1
    - 因为我们更新了Vn1(无害规则)，因此我们要将产生式中那些有害的去掉

```java
public CFG deleteUnTerminableRules(CFG cfg){
    CFG newCfg = copyCfg(cfg);
    //将Vn和production清空
    newCfg.Vn.clear();
    newCfg.production.clear();
    boolean change = true; //一直循环,直到newCfg的非终结符号(Vn)不再变大
    while(change){
        change = false;
        for(int i = 0;i < cfg.production.size();i++){	//遍历每一个产生式
            Rules rule = cfg.production.get(i);
            char left = rule.left; //产生式左部,看它能否加入到Vn
            boolean gotoNextPro = false;  //判断能否去往下一个产生式
            for(int j = 0;j < rule.right.size();j++){	//遍历产生式右部,即每个方案
                StringBuilder sb = rule.right.get(j);
                
                //遍历方案的每一个字符，Xi中每一个字符都属于Vn1∪Vt或出现@，则将A放入Vn1中,且马上跳出进入下一个产生式
                for(int k = 0;k < sb.size();k++){	
                    char c = sb.charAt(k);
                    if(c == '@'){
                        newCfg.Vn.add(left);
                        gotoNextPro = true;
                        change = true;
                        break;
                    }
                    else if(newCfg.Vn.contains(c) && newCfg.Vt.contains(c)){
                        if(k == sb.size() - 1){	//此方案结束,字符均满足条件,没有无害,那么加入
                            newCfg.Vn.add(left);
                            gotoNextPro = true;
                            change = true;
                            break;
                        }
                    }
                    else{
                        //方案中有一个字符不属于Vn1∪Vt,则跳出
                        break;
                    }
                }//for k结束
                
                if(gotoNextPro) 
                    break;	//由于已经判定此条规则无害,那么直接跳出,后面的方案就不用判断了
            } //for j 结束
        }//for i 结束
    } //while 结束
    
    //此时我们找到了所有无害的非终结符号,那么我们应该去更新产生式集合
    for(int i = 0;i < cfg.production.size();i++){
        Rules rule = cfg.production.get(i);
        if(!newCfg.Vn.contains(rule.left))	//判断是不是有害规则
            continue;
        //代码走到这,是无害规则,看规则右边有没有有害规则的入口,有的话不要那个方案
        List<StringBuilder> items = new ArrayList<>();
        for(int j = 0;j < rule.right.size();j++){
            StringBuilder sb = rule.right.get(j);	//每个方案
            for(int k = 0;k < sb.size();k++){
                char c = sb.charAt(k);
                if(c == '@'){
                    items.add(new StringBuilder(sb));
                    break;
                }
                else if(newCfg.Vn.contains(c) && newCfg.Vt.contains(c)){
                    if(k == sb.size() - 1){
                        items.add(new StringBuilder(sb));
                        break;
                    }
                }
                else{
                    break; //这个方案出现有害入口,进入下一个方案
                }
            } // for k结束:遍历完一个方案
        } //for j 结束:遍历完一个产生式
        /*
        	可能的情况: oleRule -> X1 | X2 | X3
        	X3是一个有害方案
        			  newRule -> X1 | X2
        */
        Rules newRule = new Rules(rule.left,items);
        newCfg.production.add(newRule);
    }//for i 结束
    return newCfg;
}
```



#### 去除不可到达

```bash
S->Be
B->Ce
B->Af
A->Ae
A->e
C->Cf
D->f
文法存储以及去除不可终止后
S->Be
B->Af
A->Ae|e
D->f
```

- 显然D-f就是不可到达的，我们需要去掉它！
- 我们可以从文法开始符号开始，dfs每一个终结符号，每遍历到一个终结符号，标记它可以到达
- 最后dfs完毕，没有标记的，就是不可达的，去掉即可

```java
//找文法规则左边的符号在文法产生式集合中对应的下标
public findProIndex(char left,List<Rules> production){
    for(int i = 0;i < production.size();i++){
        Rules rules = production.get(i);
        if(rules.left == left){
            return i;
        }
    }
    return -1; //不应该出现的情况
}
```

```java
public CFG deleteUnreachableRules(CFG cfg){
    boolean[] reachable = new boolean[cfg.production.size()];
    dfs(cfg.production.reachable,0);
    List<Rules> newProduction = new ArrayList<>();
    for(int i = 0;i < reachable.length;i++){
        if(reachable[i]){
            newProduction.add(cfg.production.get(i));  //为了简单,不考虑对象引用,垃圾回收的问题了
        }
    }
    cfg.production = newProduction;
    return cfg;
}
```

```java
public void dfs(List<Rules> production,boolean[] reachable,int start){
    if(reachable[start]) return;
    reachable[start] = true;
    Rules rule = production.get(start);
    for(int i = 0;i < rule.right.size();i++){
        StringBuilder sb = rule.right.get(i);
        for(int j = 0;j < sb.length();j++){
            char c = sb.charAt(j);
            if(c >= 'A' && c <= 'Z'){
                dfs(production,reachable,findProIndex(c,production));
            }
        }
    }
}
```



### 消除左递归

```java
public CFG clearLeftRecursion(CFG cfg){
    cfg = simplifyRules(cfg);	//消去多余规则以及有害规则
    cfg = directLeftRecursion(cfg); //首先消除所有的直接左递归
    cfg = inDirectLeftRecursion(cfg); //然后消除间接左递归,即间接转直接
    cfg = directLeftRecursion(cfg);  //消除从间接左递归转移过来的直接左递归
    cfg = simplifyRules(cfg);  //消去多余规则
    return cfg;
}
```



#### 消除直接左递归

![image-20211227224914963](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211227224914963.png)

转为右递归：

- 随机产生一个新的大写非终结符，假设为 W，并将它加入非终结符集合中。
- 将原来的产生式`（P->Pab|Pc|ef|g）`删除，在集合 nonLeftRecursion 中的每个元素后添加字符 W,构造 `P->efW|gW `并将该规则加入文法产生式集合中
- 构造 `W->abW|cW|@`,在集合 leftRecursion 中的每个元素后添加字符 W,**再添加@方案**，将该规则加入文法产生式集合中。

```java
public CFG directLeftRecursion(CFG cfg){
//      例：将P->Pab|Pc|ef|g
//         nonLeftRecursion=ef,g
//         leftRecursion=ab,c
//      转成:
//      P -> efP'|gP'
//      p'-> abP'|cP'|@
    for(int i = 0;i < cfg.production.size();i++){
        List<StringBuilder> nonLeftRecursion = new ArrayList<>(); //不包含左递归的方案集合
        List<StringBuilder> leftRecursion = new ArrayList<>(); //包含左递归的方案集合
        Rules rules = cfg.production.get(i);
        char left = rules.left; //产生式的左部
        for(int j = 0;j < rules.right.size();j++){
            StringBuilder sb = rules.right.get(j);
            if(sb.charAt(0) == left){
                leftRecursion.add(sb.substring(1));
            }
            else{
                nonLeftRecursion.add(new StringBuilder(sb));
            }
        }//for j:遍历产生式右部结束
        
        if(leftRecursion.size() == 0){//表示该产生式没有直接左递归，不用做任何修改
            continue;
        }
        //需要进行转换，先产生一个新的非终结符
        char newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
        while(cfg.Vn.contains(newLeft)){
            newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
        }
        cfg.vn.add(newLeft);	//将该字符加入文法的非终结符
        cfg.production.remove(rules); //将原来的产生式删除,因为产生式发生变化,看下面代码
        i--;
        //构建P -> efP'|gP'
        for(int j = 0;j < nonLeftRecursion.size();j++){
            StringBuilder sb = nonLeftRecursion.get(j);
            if(sb.charAt(0) == '@') sb.deleteCharAt(0);
            sb.append(newLeft);
        }
        Rules newRules = new Rules(left,nonLeftRecursion);
        cfg.production.add(newRules);
        //构建P'->abP' | cP' | @
        for(int j = 0;j < leftRecursion.size();j++){
            leftRecursion.get(j).append(newLeft);
        }
        leftRecursion.add(new StringBuilder('@'));
        newRules = new Rules(newLeft,leftRecursion);
        cfg.production.add(newRules);
    }
}
```

#### 消除间接左递归

```java
public CFG inDirectLeftRecursion(CFG cfg){
    for(int i = 0;i < cfg.production.size();i++){
        Rules rules = cfg.production.get(i);
        rules.isAppear = true; //表示此非终结符访问过了,以后再访问到,可以代入
        for(int j = 0;j < rules.right.size();j++){
            StringBuilder sb = rules.get(j);
            char c = sb.charAt(0);  //只处理一层的左递归
            //如果是非终结符号且之前已经扫描过它了,那么将它代入
            if(cfg.Vn.contains(c) && getRules(cfg,c).isAppear){
                Rules cRules = getRules(cfg,c);
                StringBuilder endStr = new StringBuilder(sb.deleteCharAt(0));
                rules.remove(j);	//删除此方案,因为代入之后产生式发生了变化
                j--;
                for(StringBuilder str : cRules.right){
                    StringBuilder tmp = new StringBuilder(str);
                    if(tmp.charAt(0) == '@') tmp.deleteCharAt(0);
                    tmp.append(endStr); //代入
                    rules.right.add(tmp); //加入新方案
                }
                
            }
        }
    }
    return cfg;
}
```



#### 消除左递归：更改

```java
public void cleanLeftRecursion(CFG cfg){
    for(int i = 0;i < cfg.production.size();i++){ //逐个非终结符号进行解决
        Rules rules = cfg.production.get(i);
        for(int j = 0;j < rules.right.size();j++){
            StringBuilder sb = rules.get(j);
            for(int k = 0;k < sb.length();k++){
                
            }
        }
    }
}
```







### 消除左公因子

怎么判断有没有左公因子？

```java
Set<Character> hasFirstOnce = new HashSet<>();	//方案出现的first,只记录一次
List<Character> hasAllFirst = new ArrayList<>();	//记录方案的所有first
```

遍历文法中非终结符对应的产生式组，求右部每个方案的first集合（@除外）

- 如果出现在`hasFirstOnce`的元素在`hasAllFirst`中出现超过1次，那么存在左公因子（直接或间接）

![image-20211227194440997](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211227194440997.png)

上图存在左公因子a

```java
List<StringBuilder> leftCommon = new ArrayList<>(); 	//方案中除了公共因子的剩余字符
```

求出含有左公因子a的方案下标，遍历含有左公因子的方案。

- 如果方案的首个字符就是a，则将除了a之外的所有字符加入`leftCommon`
  - 如果除了a之外没有多余字符了，那么加入@
- 如果方案的首个字符是非终结符，不断用这个非终结符对应的产生式替换这个非终结符直到方案的第一个字符为终结符（递归）
  - 如果此时第一个元素是 a，不必将方案加入，直接将除 a 外的字符串加入 `leftCommon` 即可.
  - 如果不是a就将该方案加入A的产生式
  - 如上述例子 就 变 成 了 ： `A->af|Bd|c|@|ed 、 B->ak|e `，`leftCommon={f,kd}`

接下来要更新产生式，将产生式中以左公因子开头的方案和index下标且开头为非终结符的方案删除，即删除 af 和 Bd（为什么不在一开始遇到 af 的时候删除呢？因为我用的是 ArrayList 存储，如果一开始就删除下标会发生变化，而我是靠含有左公因子 a 的方案下标查找方案的，所以最后才删除）。

删除后的文法：`A->c|@|ed，B->ak|e。`

- 在原有产生式添加新方案：随机产生一个新的大写非终结符，假设为 W，并将它加入非终结符集合中,构造方案aW(即左公因子和新的非终结符)，加入原有的产生式
- 再生成一条新的产生式，该产生式的左部为新的非终结符，右部为 leftCommon 的各个元素，即` W->f|kd`
- 最终的文法如下：`A->c|@|ed|aW，W->f|kd，B->ak|e`,B不可达可以化简掉

```java
public CFG eliLeftCommon(CFG cfg){
        /*  例：
            "A->af|Bd|c|@";"B->ak|e"
            hasFirstOnce={a,c,e}
            hasAllFirst={a,a,c,e}
            改造为：A->ed|aQ|c|@;Q->f|kd;
         */
    for(int i = 0;i < cfg.production.size();i++){
        Rules rules = cfg.production.get(i);
        Set<Character> first = singleFirst(cfg,rules.left);	//求此条产生式的first集合
        
        for(Character ch : first){
            if(ch == '@') continue;
            Set<Integer> comIndex = getComIndex(cfg,rules,ch); //找到所有含有ch(左公因子)的方案下标
            if(comIndex.size() > 1){	//说明存在左公因子,可能是直接或者间接
                
                //leftCommon的处理需要引入新的产生式，先产生一个新的非终结符
                char newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
                while(cfg.Vn.contains(newLeft)){
                    newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
                }
                cfg.Vn.add(newLeft);
                
                List<StringBuilder> leftCommon = new ArrayList<>(); //方案中除公共因子的剩余字符串
                for(Integer index : comIndex){
                    StringBuilder sb = rules.right.get(index);  //找到有左公因子的方案
                    if(cfg.Vt.contains(sb.charAt(0)){ //只处理一层左公因子
                        //如果是终结符
                        StringBuilder temp = new StringBuilder(sb.substring(1,sb.length()));
                        if(temp.length() == 0){
                            leftCommon.add(new StringBuilder('@'));
                        }
                        else{
                            leftCommon.add(temp);
                        }
                    }
                    else{
                        //如果是非终结符,不断替换非终结符直到方案的第一个元素为终结符
                        //A->af|Bd|c|@";"B->ak|e
                        //即对Bd中的B,让akd中的kd,加入leftCommon,而ed加入A的产生式
                        replaceFirst(ch,rules.left,cfg,sb,leftCommon);
                    }
                }//for index:leftCommon收集完毕
                
                //现在要更新产生式
                //首先将产生式中以ch开头的产生式和index下标且开头为非终结符的删除
                for(int j = 0;j < rules.right.size();j++){
                    StringBuilder sb = rules.right.get(j);
                    if(sb.charAt(0) == ch){	//ch开头的产生式
                        rules.right.remove(j);
                        j--;
                    }
                    else if(comIndex.contains(j) && cfg.Vn.contains(sb.charAt(0))){//index下标且开头为非终结符
                        rules.right.remove(j);
                        j--;
                    }
                }//for j :删除完毕
                //在原有产生式的基础上添加新方案
                StringBuilder newPlan = new StringBuilder();
                newPlan.append(ch).append(newLeft);
                rules.right.add(newPlan);
                //产生新的产生式,规则右部就是leftCommon
                Rules newRules = new Rules(newLeft,leftCommon);
                cfg.production.add(newRules);
            }
        }
    }//for i 结束:遍历完所有产生式
    return cfg;
}
```

```java
//ch:左公因子	left:left对应的规则右部存在ch前缀(左公因子)
//sb:当前方案存在直接或间接的ch
//leftCommon:sb替换后,首部的ch不加入,后面的串加入到leftCommon
//cfg:对于替换后,没有ch的,left对应的规则追加方案
public void replaceFirst(char ch,char left,CFG cfg,StringBuilder sb,List<StringBuilder> leftCommon){
    char c = sb.charAt(0);
    if(cfg.Vt.contains(c)){ //递归终止条件,扫到终结符
        if(ch == c){
            if(sb.length() == 1)
                leftCommon.add(new StringBuilder('@'));
            else
            	leftCommon.add(new StringBuilder( sb.toString().substring(1) ));
        }
        else{
            Rules leftRules = getRules(cfg,left);  //找到left对应的产生式
            leftRules.add(new StringBuilder(sb));
        }
        return;
    }
    //此时c是非终结符,需要不断替换掉这个c,直到没有非终结符为止
    Rules cRules = getRules(cfg,c);  //找到c这个非终结符对应的产生式 c->X1|X2...
    StringBuilder endWiths = new StringBuilder(sb.toString().substring(1)); 
    for(int i = 0;i < cRules.right.size();i++){
        //A->af|Bd|c|@";"B->ak|e
        //将B代入进来,继续递归
        StringBuilder tmp = new StringBuilder(cRules.right.get(i));
        if(tmp.charAt(0) == '@') tmp.deleteCharAt(0);
        tmp.append(endWiths);
        replaceFirst(ch,c,cfg,tmp,leftCommon);
    }
}
```



### 求first集合

```java
//在文法存储结构CFG添加以下代码
Map<Character,Set<Character>> first; //key:非终结符 value:非终结符对应的first集合
```

计算first( X )的集合：

- 如果X是终结符，First(X) = {X}
- 如果X是空串，First( X ) = { 空串 }
- 如果X是非终结符，`X->Y1|Y2|…Yn`, First（X）为右边各个方案的 first集合的并集
  - 每个方案 `Yi=T1T2..Tk`，逐步计算从T1起
    - 如果T1的first集合包含空串，将` {First(T1)- 空 串} `加 入 到 First(X)，继续计算T2
    - 如果计算到某个符号不包含空串，就结束计算
    - 如果计算到最后一个符号也包含空串，也就是说`T1T2T3...->空串 可以成立`，则将空串加入First( X )

对于上述算法，显然采用递归，先写一个方法singleFirst，递归地求某符号Ch的First集合，对于文法的每个非终结符，调用该函数即可

- 递归终止条件：当前字符为终结符或者空集，直接返回first集合
- 否则，First(Ch)为右边各个方案的并集，首先得到Ch对应的产生式（规则），遍历产生式右边的每条方案
  - 如果该方案是单个符号，直接递归计算该符号的 First 集并将返回的结果加入 Frist(Ch)并进入下一方案。
  - 如果该方案是多个符号，就看串首符号：
    - 如果是终结符，直接把它加入 First(Ch)并进入下一方案
    - 如果是非终结符，递归计算该字符的 First集合 cFirst。
      - 如果 cFirst 包含空串，且此时已是最后一个符号，就把cFirst 的元素全都加入 First(Ch)并进入下一方案，如果不是最后一个元素，将{cFirst-空串}加入 First(Ch)并继续计算下一字符的 First 集。
      - 如果cFirst不包含空串，就不需要继续计算了，直接将cFirst的元素全都加入First(Ch)并进入下一个方案

```java
public void getFirst(CFG cfg){
    //对于每个终结符号,它的first集合就是它自己
    for(Character c : cfg.Vt){
        Set<Character> first = new HashSet<>();
        first.add(c);
        cfg.first.put(c,first);
    }
    //对于每个非终结符号，找它的产生式,求右边方案first集合的并集
    for(Character c : cfg.Vn){
        cfg.first.put(c,singleFirst(cfg,c));
    }
}
```

```java
//求x对应的产生式
public Rules getRulesPro(CFG cfg,Character x){
    for(Rules rules : cfg.production){
        if(rules.left == x) return rules;
    }
    return null;
}
```

```java
//求非终结符号x对应的first集合
public Set<Character> singleFirst(CFG cfg,Character x){
    Set<Character> first = new HashSet<>();
    //如果是终结符,那么直接返回
    if(cfg.Vt.contains(x) || x == '@'){
        first.add(x);
        return first;
    }
    //否则，first集合等于右边各个符号的first集合的并集
    //首先拿到x的产生式
    Rules rules = getRulesPro(cfg,x);
    for(int i = 0;i < rules.right.size();i++){	//产生式右部
        String s = rules.right.get(i);	//方案Xi
        if(s.length() == 1) {
            first.addAll(singleFirst(cfg,s.charAt(0)));	//单个字符，直接求这个字符对应的first集合
            continue;
        }
        //对方案的每个字符,从左到右遍历,若求得的first集合不包含@就可以退出,若遍历到最后一个字符还包含@,那么@也加入
        for(int j = 0;j < s.length();j++){
            char c = s.charAt(j);
            Set<Character> cFirst = singleFirst(cfg,c);
            if(!cFirst.contains('@')){
                first.addAll(cFirst);	//不包含@的话，可以退出了
                break;
            }
            if(j != s.length() - 1){
                cFirst.remove('@');
            }
            first.addAll(cFirst);
        }
    }
    return first;
}
```



### 求follow集合

采用 map 存储 Follow 集合，key 为非终结符，value 为 Follow 集，用集合存储。

```java
//CFG增加follow的存储结构
Map<Characher,Set<Character>> follow; //follow集合
```

计算follow集合

- 对于文法开始符号S，那么在其follow集合中加入$
- 对于A->aBp,如果p不能推导出空串，那么将first(p)加入到follow(B)
- 对于A->aBp,如果p能推导出空串,那么将{first(p) - 空串}和follow(A)加入到follow(B)中



```java
public void getFollow(CFG cfg){
    for(Rules rules : cfg.production){
        follow = singleFollow(cfg,rules.left);
        if(rules.left == cfg.startSymbol){
            follow.add('$');  //文法开始符号要额外加一个串结束符号
        }
        cfg.follow.put(rules.left,follow);
    }
}
```

```java
/*
求x(非终结符号)的follow集合
暴力跑所有文法,找x出现的位置i
找到之后,若i位于方案的最右
	若此方案左部的left != x,那么将left的follow集合加入到x的follow集合(调用singleFollow即可)
若不在最右边
	那么求下一个位置的first集合
		若@存在于下一个位置的first集合,那么将{first集合 - @}加入到follow,继续下一个位置
			若一直遍历到最右边还有@,那么把方案左边的left对应的follow集合加入(前提是left != x)
		若@不存在于first集合,把first集合加入到follow集合后就可以退出了.
*/
public Set<Character> singleFollow(CFG cfg,char x){
    Set<Character> follow = new HashSet<>();
    for(Rules rules : cfg.production){	//暴力跑完所有产生式
        for(String plan : rules.right){	////跑每个产生式的全部方案,找非终结符x
            boolean canSkip = false;  //若扫到的first集合没有@,说明可以跳出
            for(int i = 0;i < plan.length();i++){	//扫描一个方案
                if(canSkip) break;
                char c = plan.charAt(i);
                if(c != x) continue;
                //代码走到这,说明找到了非终结符x
                if(i == plan.length() - 1){
                    //若处于方案的最右边
                    if(rules.left != x){
                        Set<Character> curRulesFollow = cfg.follow.get(rules.left);
                        if(curRulesFollow == null){
                            curRulesFollow = singleFollow(cfg,rules.left);
                        }
                        follow.addAll(curRulesFollow); //将left的follow集合加入到x的follow集合
                    }
                    continue;
                }
                //代码走到这,说明x不在方案最右边,那么我们就去迭代的找下一个字符的first集合
                for(int p = i + 1;p < plan.length();p++){
                    char nextC = plan.charAt(p);
                    Set<Character> nextFirst = singleFirst(cfg,nextC);
                    if(!nextFirst.contains('@')){
                        canSkip = true;  //一定会来排队,那么可以跳出
                        follow.addAll(nextFirst);
                        break;
                    }
                    //代码走到这,说明nextFirst含有@,要判断是不是走到了方案走右边
                    nextFirst.remove('@');  //先去除@,方便后面添加
                    follow.addAll(nextFirst);
                    if(p == plan.length() - 1){
                        if(rules.left != x){
                            Set<Character> curRulesFollow = cfg.follow.get(rules.left);
                            if(curRulesFollow == null){
                                curRulesFollow = singleFollow(cfg,rules.left);
                            }
                            follow.addAll(curRulesFollow); //将left的follow集合加入到x的follow集合
                        }
                    }
                    //由于扫到了@,因此还不能break,要继续迭代找下一个字符的first集合
                }
            }
        }
    }
    return follow;
}
```

```c
S->ABC|D
A->aB|@
B->@|cC
C->eC|@
D->i|j
```



### 最左推导：LL（1）分析法

![image-20211229194529844](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211229194529844.png)

对于规则1，只需要消除左公因子即可，对于规则2，这里选择不予分析。

#### 计算select集合：对于一个方案，它能匹配的所有token

`Map<String,Set<Character>> select;`

```java
/*
S -> Aa | Bb | Cc
key -> value,用key这个方案来匹配value里面的元素不会报错
S->bB|Ak、A->aAB|@、B->a|d 
构造的 Select 集为：
S->Ak = [a, k]
S->bB = [b]
A->@ = [a, d, k]
A->aAB = [a]
B->d = [d]
B->a = [a]

一句话：即先求这个方案的first集合,如果含有@,那么将方案左部的follow集合并入,同时去掉@
*/
public void getSelect(CFG cfg){
    for(Rules rules : cfg.production){
        char left = rules.left;  //此条产生式的左部
        for(int j = 0;j < cfg.right.size();j++){
            String s = cfg.right.get(j);
            Set<Character> planFirst = new HashSet<>();
            for(int k = 0;k < s.length();k++){
                char c = sb.charAt(k);
                if(c == '@'){
                    //A->@这种需要特殊处理,因为这种方案下,对于进来的token,需要查看A的follow集合才能决定要不要用A->@归约
                    planFirst.addAll(cfg.follow.get(left));	
                    break;
                }
                //LL(1)分析中，会先消除左公因子，左递归，得到first和follow,因此这里可以直接求字符c对应的first或者follow
                Set<Characher> cFirst = cfg.first.get(c); 
                if(!cFirst.contains('@')){
                    planFirst.addAll(cFirst);
                    break;  //不含@就可以退出了
                }
                //含有@,判断是不是最后一个字符
                cFirst.remove('@');  //首先去掉@
                planFirst.addAll(cFirst);
                if(k == s.length() - 1){
                    planFirst.addAll(cfg.follow.get(left));
                }
            }// for k:遍历完一条方案,这时候要构造一个select元素
            StringBuilder plan = new StringBuilder();
            plan.append(left).append("->").append(s);
            cfg.select.put(plan.toString(),planFirst);
        }//for j :遍历完一条产生式的所有方案
    }//for i:遍历完所有产生式
}
```

#### 判断能否用LL1的方法解决

```java
/*
构造的 Select 集为：
S->Ak = [a, k]
S->bB = [b]
A->@ = [a, d, k]
A->aAB = [a]
B->d = [d]
B->a = [a]

看这个例子，可以看出左部为 A 的产生式的可选集有交集{a}，就会导致再遇到终结符 a 时不知道该选哪条规则的情
况。此时将 isLL1 设为 false，告知用户不符合 LL1 文法，请重新输入文法。
基本思路：在画LL(1)分析表的时候，一个单元格只能有一个方案，若多于一个方案，就不是LL1文法
*/
public void isLL1(CFG cfg){
    Set<String> plans = new HashSet<>(cfg.select.keySet());
    for(String plan : plans){
        Set<Character> matchTokens = cfg.select.get(plan);
        char left = plan.charAt(0);
        for(String otherPlan : plans){
            if(plan.equals(otherPlan)){
                continue;  //相同方案直接跳过
            }
            if(otherPlan.charAt(0) == left){
                Set<Character> tmpMatchTokens = cfg.select.get(otherPlan);
                if(hasCommon(matchTokens,tmoMatchTokens)){
                    //若有交集，说明有二义性，无法用LL1来解决
                    cfg.isLL1 = false;
                    return;
                }
            }
        }
    }
}
```

#### 构造预测分析表

基本思路：回想怎么画表格的？

![image-20211229205202333](https://raw.githubusercontent.com/xuhaoyao/images/master/img/image-20211229205202333.png)

```java
/*
一个非终结符号,与目前的输入串(终结符号)二者对应一个规则
	S				a				->   AbB
*/
public class LL1Node{
    char nonTer;  //非终结符号
    char ter;     //终结符号
    
    public LL1Node(char nonTer,char ter){
        this.nonTer = nonTer;
        this.ter = ter;
    }
    
    //java编程需要考虑的问题：由于要用Map的key来存LL1Node,因此需要重写以下两个方法
    public boolean equals(Object o) {  ... }
    public int hashCode() { ... }
}
```

```java
public void getPredict(CFG cfg){
    for(String rule : cfg.select.keySet()){
        char nonTer = plan.charAt(0);
        String plan = rule.substring(3);  //如S->AbB, AbB从下标3开始
        for(Character ter : cfg.select.get(rule)){	//由key: S->AbB 可以找到value:{a,b}
            LL1Node node = new LL1Node(nonTer,ter);
            cfg.predict.put(node,plan);	//由S和a可以找到这条规则S->AbB的AbB
        }
    }
}
```

#### 预测分析程序

```java
public void predictLL1(CFG cfg,String input){
    Deque<Character> stack = new ArrayDeque<>();
    stack.push(cfg.startSymbol);
    int pos = 0;
    while(!stack.isEmpty() && pos < input.length()){
        char top = stack.pop();
        char curInput = input.charAt(pos);
        if(cfg.Vt.contains(top)){
            //栈顶是终结符
            if(top != curInput){
                cfg.flag = false;
                print(cfg,"匹配失败")；
                return;
            }
            else{
                print(cfg,"匹配成功:" + top);
                pos++;	//匹配成功后拿输入串的下一个字符
                continue;
            }
        }
        //代码走到这，说明栈顶是非终结符，那就去预测分析表找对应的规则
        String rule = cfg.select.get(new Node(top,curInput));
        if(rule == null){
            print(cfg,top + "," + curInput + "没有对应规则,匹配失败");
            cfg.flag = false;
            return;
        }
        //代码走到这,有对应规则可以进行归约
        print(cfg,"用规则:" top + "->" + rule + "归约");
        for(int i = rule.length() - 1;i >= 0;i--){
            stack.push(rule.charAt(i));  //反向入栈
        }
    }//end while
    cfg.flag = true;  //表明分析成功，可以做相应动作
}
```









