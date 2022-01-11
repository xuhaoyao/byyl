package com.oyyy.cfg;

import java.util.*;

/**
 * @description:求文法各非终结符号的first集合与follow集合
 * @author: oyyy
 * @date: 2021/11/18 10:46
 */
public class CfgFirstAndFollow {

    CfgEliminateBack comLeft = new CfgEliminateBack();

    /**
     * description:求非终结符x对应的产生式
     * @param: cfg
     * @param: x
     * @return com.oyyy.cfg.Rules
     * @author: oyyy
     * @date: 2021/11/19 23:27
     */
    public Rules getRulesPro(CFG cfg,char x){
        ArrayList<Rules> production = cfg.production;
        for (int i = 0; i < production.size(); i++) {
            Rules rules = cfg.production.get(i);
            if(rules.left==x)
                return rules;
        }
        return null;
    }

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
            StringBuffer sb = rules.right.get(i);	//方案Xi
            if(sb.length() == 1) {
                first.addAll(singleFirst(cfg,sb.charAt(0)));	//单个字符，直接求这个字符对应的first集合
                continue;
            }
            for(int j = 0;j < sb.length();j++){
                char c = sb.charAt(j);
                Set<Character> cFirst = singleFirst(cfg,c);
                if(!cFirst.contains('@')){
                    first.addAll(cFirst);	//不包含@的话，可以退出了
                    break;
                }
                else{
                    if(j != sb.length() - 1){
                        cFirst.remove('@');
                        first.addAll(cFirst);
                    }
                    else{
                        first.addAll(cFirst);
                    }
                }
            }
        }
        return first;
    }

    /**
     * description:递归的计算某字符的first
     * @param: x
     * @return java.util.Set<java.lang.Character>
     * @author: oyyy
     * @date: 2021/11/18 11:12
     */
    public Set<Character> singleFirst1(CFG cfg,Character x){
        Set<Character>first=new HashSet<>();
        //如果是终结符，直接返回first集合
        if(cfg.Vt.contains(x) || x=='@'){
            first.add(x);
            return first;
        }else{
            //否则，等于其右边各个符号的first集合相加
            //先求出该字符所在的产生式
            Rules rules =getRulesPro(cfg,x);
            if(rules!=null)
            {
                for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                    StringBuffer str = rules.right.get(j);//每个方案
                    if(str.length()==1){
                        //如果右部的产生式是单个符号的，直接加入到first集合
                        first.addAll(singleFirst1(cfg,str.charAt(0)));
                        continue;
                    }
                    for (int k = 0; k < str.length(); k++) {  //每个字符
                        char curch=str.charAt(k);
                        //如果右部是多个符号，就看串首符号
                        //如果是终结符，直接把它加入first集合
                        if(cfg.Vt.contains(curch)){
                            first.add(curch);
                            break;
                        }else{
                            //如果是非终结符，就计算它的first集
                            Set<Character>cFirst=singleFirst1(cfg,curch);
                            //如果包含空串，就要继续计算后一个符号的first集
                            if(cFirst.contains('@')){
                                //如果此时是最后一个符号，就要把空串也加入first集合
                                if(k==str.length()-1){
                                    first.addAll(cFirst);
                                }else{
                                    cFirst.remove('@');
                                    first.addAll(cFirst);
                                }
                            }else{
                                //如果不包含空串，就不需要继续计算了
                                first.addAll(cFirst);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return first;
    }

    /**
     * description:求文法各非终结符号的first集合
     * @param: cfg,文法
     * @return void
     * @author: oyyy
     * @date: 2021/11/18 11:13
     */
    public void getFirst(CFG cfg){
        //对于每个终结符
        Set<Character> vt = cfg.Vt;
        for(Character ch:vt){
            Set<Character>first=new HashSet<>();
            first.add(ch);
            cfg.first.put(ch,first);
        }

        //对于每个非终结符
        System.out.println("First集合如下：");
        ArrayList<Rules> production = cfg.production;
        for (int i = 0; i < production.size(); i++) {
            Rules rules = cfg.production.get(i);
            cfg.first.put(rules.left,singleFirst(cfg,rules.left));
            //test
            Set<Character> characters = cfg.first.get(rules.left);
            System.out.println(rules.left+" = " + characters);
        }
    }

    /**
     * description:将follow集合加入key为x的value中
     * @param: cfg
     * @param: x,key
     * @param: follow,要加入的值
     * @return void
     * @author: oyyy
     * @date: 2021/11/20 23:35
     */
    public void addCharToFollow(CFG cfg,Character x,Set<Character>follow){
        Set<Character> xFollow = cfg.follow.get(x);
        //如果直接将follow加入，地址相同，所以要获取它的值
        Set<Character> ch = new HashSet<>(follow);

        if(xFollow!=null)
            xFollow.addAll(ch);
        else
            cfg.follow.put(x,ch);
    }

    public Set<Character> singleFollow(CFG cfg,char x){
        Set<Character> follow = new HashSet<>();
        for(int i = 0;i < cfg.production.size();i++){
            Rules rules = cfg.production.get(i);
            for(int j = 0;j < rules.right.size();j++){
                StringBuffer plan = rules.right.get(j);
                boolean canSkip = false;  //若扫到的first集合没有@,说明可以跳出
                for(int k = 0;k < plan.length();k++){
                    if(canSkip) break;
                    char c = plan.charAt(k);
                    if(c != x) continue;
                    //代码走到这,说明找到了非终结符x
                    if(k == plan.length() - 1){
                        //若处于方案的最右边
                        if(rules.left != x){
                            Set<Character> curRulesFollow = cfg.follow.get(rules.left);
                            if(curRulesFollow == null){
                                curRulesFollow = singleFollow(cfg,rules.left);
                                cfg.follow.put(rules.left,curRulesFollow);
                            }
                            follow.addAll(curRulesFollow); //将left的follow集合加入到x的follow集合
                        }
                        continue;
                    }
                    //代码走到这,说明x不在方案最右边,那么我们就去迭代的找下一个字符的first集合
                    for(int p = k + 1;p < plan.length();p++){
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
                                    cfg.follow.put(rules.left,curRulesFollow);
                                }
                                follow.addAll(curRulesFollow); //将left的follow集合加入到x的follow集合
                            }
                        }
                        //由于扫到了@,因此还不能break,要继续迭代找下一个字符的first集合
                    }
                }//for k:扫描完一个方案
            }//for j:跑每个产生式的全部方案,找非终结符x
        }//for i:暴力跑完所有产生式
        return follow;
    }

    /**
     * description:递归的计算某字符的follow
     * @param: cfg
     * @param: x
     * @return java.util.Set<java.lang.Character>
     * @author: oyyy
     * @date: 2021/11/20 21:13
     */
    public Set<Character> singleFollow1(CFG cfg,Character x){
        Set<Character>follow=new HashSet<>();
        //在产生式中搜索所有非终结符出现的位置
        for (int i = 0; i < cfg.production.size(); i++) {   //对于每一条产生式
            Rules rules = cfg.production.get(i);
            for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                StringBuffer str = rules.right.get(j);//每个方案
                int nextSch=0;
                for (int k = 0; k < str.length(); k++) {  //每个字符
                    if(nextSch==1)break;
                    //搜索当前查找的非终结符的位置
                    if(str.charAt(k)==x){
                        //判断是否处于最右边的位置
                        if(k<str.length()-1){
                            //如果没在最右边的位置
                            //判断下一个符号是否为非终结符
                            for (int l = k+1; l < str.length(); l++) {
                                char nextChar = str.charAt(l);
                                if (cfg.Vn.contains(nextChar)) {  //如果是非终结符
                                    //查看其first集是否包含空串
                                    Set<Character> nextFirst = singleFirst(cfg, nextChar);
                                    if(nextFirst.contains('@')){
                                        //如果包含空串，并且此时这个符号是最后一个符号
                                        //就要将其FIRST除去空串的集合加入follow集，且左部的follow集加入follow集
                                        if(l==str.length()-1){
                                            if(rules.left!=x) {
                                                Set<Character> leftFollow = cfg.follow.get(rules.left);
                                                if (leftFollow == null)
                                                    leftFollow = singleFollow1(cfg, rules.left);
                                                addCharToFollow(cfg, x, leftFollow);
                                            }
                                                Set<Character> nextFirstExcept = new HashSet<>(nextFirst);
                                                nextFirstExcept.remove('@');
                                                addCharToFollow(cfg,x,nextFirstExcept);
                                        }else{
                                            Set<Character> nextFirstExcept = new HashSet<>(nextFirst);
                                            nextFirstExcept.remove('@');
                                            addCharToFollow(cfg,x,nextFirstExcept);
                                        }
                                    }else{
                                        //如果不包含空串加入FIRST之后跳出循环
                                        addCharToFollow(cfg,x,nextFirst);
                                        nextSch=1;
                                        break;
                                    }
                                }else{  //如果是终结符
                                    //把此符号加入到当前查找的非终结符的follow集中
                                    follow.add(nextChar);
                                    addCharToFollow(cfg,x,follow);
                                    nextSch=1;
                                    break;
                                }
                            }
                        }
                        else{
                            //在最右的位置，将左部的follow集加入follow集
                            if(rules.left!=x) {
                                Set<Character> leftFollow = cfg.follow.get(rules.left);
                                if (leftFollow == null)
                                    leftFollow = singleFollow1(cfg, rules.left);
                                addCharToFollow(cfg, x, leftFollow);
                            }
                        }
                    }
                }
            }
        }
        return cfg.follow.get(x);
    }

    /**
     * description:求文法各非终结符号的follow集合
     * @param: cfg，文法
     * @return void
     * @author: oyyy
     * @date: 2021/11/19 23:40
     */
    public void getFollow(CFG cfg){
        //对于每个非终结符
        System.out.println("Follow集合如下：");
        ArrayList<Rules> production = cfg.production;
        for (int i = 0; i < production.size(); i++) {
            Rules rules = cfg.production.get(i);
            Set<Character> follow = singleFollow(cfg,rules.left);
            if(rules.left == cfg.startSymbol){
                follow.add('$');  //文法开始符号要额外加一个串结束符号
            }
            cfg.follow.put(rules.left,follow);
            //test
            Set<Character> characters = cfg.follow.get(rules.left);
            System.out.println(rules.left+" = " + characters);
        }
    }

    /**
     * description:求first和follow集
     * @param: cfg
     * @return CFG
     * @author: oyyy
     * @date: 2021/11/21 19:40
     */
    public CFG getFirstAndFollow(CFG cfg){
        cfg=comLeft.EliminteBack(cfg);
        getFirst(cfg);   //求first
        getFollow(cfg);  //求follow
        return cfg;
    }
}
