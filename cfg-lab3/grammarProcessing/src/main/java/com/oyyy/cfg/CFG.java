package com.oyyy.cfg;

import java.util.*;

/**
 * @description:文法类
 * @author: oyyy
 * @date: 2021/11/14 16:49
 */
public class CFG {
    char startSymbol;  //文法开始符
    Set<Character> Vn;  //非终结符
    Set<Character> Vt;  //终结符
    ArrayList<Rules> production;  //文法产生式存储入口
    Map<Character, Set<Character>> first;   //first集合
    Map<Character, Set<Character>> follow;   //follow集合
    Map<StringBuffer,Set<Character>>select;  //select集合
    Map<Node,StringBuffer>predict;  //构造预测分析表
    ArrayList<StringBuffer> result; //推导过程
    boolean flag;  //是否推导成功
    boolean isLL1;  //是否符合LL1文法

    public CFG() {
        this.Vn=new HashSet<>();
        this.Vt=new HashSet<>();
        this.production=new ArrayList<>();
        this.first=new HashMap<Character, Set<Character>>();
        this.follow=new HashMap<Character, Set<Character>>();
        this.select=new HashMap<StringBuffer,Set<Character>>();
        this.predict=new HashMap<Node,StringBuffer>();
        this.result=new ArrayList<StringBuffer>();
        this.flag=true;
        this.isLL1=true;
    }

    /**
     * description:获取预测分析表中结点Node对应的方案
     * @param: cfg
     * @param: Nonter
     * @param: ter
     * @return java.lang.StringBuffer
     * @author: oyyy
     * @date: 2021/11/24 10:51
     */
    public StringBuffer getPre(CFG cfg,Character Nonter,Character ter){
        for (Node node : cfg.predict.keySet()) {
            if(node.nonter==Nonter && node.ter==ter)
                return cfg.predict.get(node);
        }
        return null;
    }

    /**
     * description:文法存储
     * @param: rule
     * @return com.oyyy.wenfa.Grammar
     * @author: oyyy
     * @date: 2021/11/14 17:20
     */
    public CFG storeGrammar(String []rule){
        CFG cfg = new CFG(); //新建一个文法对象
        StringBuffer str = new StringBuffer();
        //处理每一条产生式
        for (int i = 0; i < rule.length; i++) {
            int same=0;
            String inRule=rule[i];
            char[] curRule=inRule.toCharArray();

            if(i==0){   //如果是第一条产生式，设置文法开始符
                cfg.startSymbol=curRule[0];
            }
            ArrayList<Rules> pro=cfg.production;
            for(Rules rulew:pro) {
                if (rulew.left == curRule[0]) {
                    same=1;
                    break;
                }
            }
            cfg.Vn.add(curRule[0]);   //左边第一个:文法中的非终结符

            //构建数据结构
            int ortag=0;  //用于判断由于扫描到“|”
            /*
                A->x1 | x2
                sr存储x1和x2这样子
             */
            ArrayList<StringBuffer> sr=new ArrayList<>();
            str = new StringBuffer();
            for (int j = 3; j < curRule.length; j++) {  //从产生式的第四个字符开始
                if(curRule[j]!='|'){
                    str.append(curRule[j]);
                }else{
                    sr.add(str);
                    str = new StringBuffer();
                }
                //构建终结符和非终结符
                if(curRule[j]=='@' || curRule[j]=='|'){ }
                else if(curRule[j]>='A' && curRule[j]<='Z')
                    cfg.Vn.add(curRule[j]); //大写字母是非终结符,加入到Vn
                else
                    cfg.Vt.add(curRule[j]); //小写字母是终结符,加入到Vt
            }
            sr.add(str);
            if(same==1){
                for(Rules rulew:pro){
                    if(rulew.left==curRule[0]){
                        rulew.right.addAll(sr);
                        break;
                    }
                }
            }else{
                Rules rul=new Rules(curRule[0],sr);
                cfg.production.add(rul);
            }
        }//for结束
        return cfg;
    }
}
