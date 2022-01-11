package com.oyyy.cfg;

import java.util.*;

/**
 * @description:最左推导
 * @author: oyyy
 * @date: 2021/11/23 10:38
 */
public class LL1 {
    /**
     * description:计算select集
     * @param: cfg
     * @return void
     * @author: oyyy
     * @date: 2021/11/23 10:39
     */
    public void getSelect(CFG cfg){
        int canNull=0;  //为1表示该方案可以推导出空串
        for (int i = 0; i < cfg.production.size(); i++) {   //对于每一条产生式
            Rules rules = cfg.production.get(i);
            char left = rules.left;  //产生式左部
            for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                StringBuffer str = rules.right.get(j);//每个方案
                Set<Character> allFirst = new HashSet<>();  //该方案对应的first集合
                canNull=0;
                for (int k = 0; k < str.length(); k++) {  //每个字符
                    Set<Character> currentFirst = new HashSet<>();
                    CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
                    char ch=str.charAt(k);
                    currentFirst=cfgF.singleFirst(cfg,ch);
                    if(currentFirst.contains('@')){
                        //如果该方案最后一个符号也能推导出空串，说明该方案可以推导出空串
                        if(k==str.length()-1)
                            canNull=1;
                        allFirst.addAll(currentFirst);
                        allFirst.remove('@');
                    }else{
                        allFirst.addAll(currentFirst);
                        break;  //进入下一方案
                    }
                }
                //对该方案进行处理
                StringBuffer rule=new StringBuffer();
                rule.append(left).append("->").append(str);
                if(canNull==1){
                    Set<Character> follow = cfg.follow.get(left);//产生式左侧对应的follow集合
                    allFirst.addAll(follow);
                }
                cfg.select.put(rule,allFirst);
            }
        }
        System.out.println("cfg.select = " + cfg.select);
    }

    /**
     * description:构造预测分析表
     * @param: cfg
     * @return void
     * @author: oyyy
     * @date: 2021/11/23 11:14
     */
    public void getPredict(CFG cfg){
        for (StringBuffer rule : cfg.select.keySet()) {
            for(Character ch:cfg.select.get(rule)){
                Node node=new Node(rule.charAt(0),ch);
                StringBuffer rs=new StringBuffer(rule);
                rs.delete(0,3);
                cfg.predict.put(node,rs);
            }
        }
    }

    /**
     * description:判断两个集合是否有相同元素
     * @param: set1
     * @param: set2
     * @return boolean，有返回true，否则返回false
     * @author: oyyy
     * @date: 2021/11/23 13:18
     */
    public boolean hasCommon(Set<Character>set1,Set<Character>set2){
        for(Character ch1:set1){
            for(Character ch2:set2){
                if(ch1==ch2)
                    return true;
            }
        }
        return false;
    }

    /**
     * description:判断是否符合LL1文法
     * @param: cfg
     * @return:CFG
     * @author: oyyy
     * @date: 2021/11/23 11:34
     */
    public CFG isLL1(CFG cfg){
        //根据select集合判断，只要保证左部相同的不同产生式的可选集不相交就说明是LL1文法
        for(StringBuffer p1:cfg.select.keySet()){
            Set<Character> set1 = cfg.select.get(p1);
            Set<StringBuffer> leftKeySet = new HashSet<>(cfg.select.keySet());
            leftKeySet.remove(p1);
            for(StringBuffer p2:leftKeySet){
                if(p1.charAt(0)==p2.charAt(0)){
                    //如果两条产生式左部相等
                    Set<Character> set2 = cfg.select.get(p2);
                    if(hasCommon(set1,set2)){
                        //判断是否相交
                        cfg.isLL1=false;
                        return cfg;
                    }
                }
            }
        }
        return cfg;
    }

    /**
     * description:预测分析程序
     * @param: cfg
     * @param: str
     * @return com.oyyy.cfg.CFG
     * @author: oyyy
     * @date: 2021/11/26 11:26
     */
    public CFG Derivation(CFG cfg,StringBuffer str){
        ArrayList<StringBuffer> result = new ArrayList<>();
        Stack<Character> chSt=new Stack<Character>();  //符号栈
        chSt.push('#');
        chSt.push(cfg.startSymbol);
        char x=chSt.peek();
        int index=0;  //记录输入串的位置
        int lineIndex=1;
        while(x!='#' && index<str.length()){
            char a=str.charAt(index);
            StringBuffer line = new StringBuffer();
            StringBuffer us=new StringBuffer(str.subSequence(index,str.length()));
            line.append(chSt.toString()).append("\t").append("\t").append(us).append("\t");
            if(cfg.Vt.contains(x)){  //如果栈顶元素是终结符
                if(x==a){   //匹配成功
                    chSt.pop();  //栈顶元素出栈
                    index++;
                    line.append("匹配"+a+"\n");
                    x=chSt.peek();
                    result.add(line);
                    continue;
                }else{  //匹配失败
                    line.append("无法匹配"+"\n");
                    result.add(line);
                    cfg.flag=false;
                    break;
                }
            }else{  //如果是非终结符
                CFG CF = new CFG(); //新建一个文法对象
                StringBuffer rule = CF.getPre(cfg,x,a);
                if(rule!=null){ //如果有对应规则
                    chSt.pop();  //栈顶元素出栈
                    if(rule.charAt(0)!='@'){
                        for (int i = rule.length()-1; i>=0; i--) {
                            chSt.push(rule.charAt(i));  //对应规则倒序入栈
                        }
                    }
                    line.append(x+"->"+rule+"\n");
                    x=chSt.peek();
                    result.add(line);
                }else{
                    line.append("非终结符"+x+"没有可选规则"+"\n");
                    result.add(line);
                    cfg.flag=false;
                    break;
                }
            }
        }
        cfg.result=result;
        return cfg;
    }

    /**
     * description:总控
     * @param: cfg
     * @return void
     * @author: oyyy
     * @date: 2021/11/23 21:21
     */
    public CFG analyse(CFG cfg,StringBuffer str) {
        getSelect(cfg);
        getPredict(cfg);
        cfg=isLL1(cfg);
        if (cfg.isLL1==true) {  //符号LL1文法，开始推导
            cfg= Derivation(cfg,str);
        }
        return cfg;
    }
}
