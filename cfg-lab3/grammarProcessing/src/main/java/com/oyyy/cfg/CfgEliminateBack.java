package com.oyyy.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @description:消除回溯，即消除左公共因子
 * @author: oyyy
 * @date: 2021/11/21 20:18
 */
public class CfgEliminateBack {

    /**
     * description:求产生式rules中每个方案first集合中含有ch的方案下标
     * @param: cfg
     * @param: rules
     * @param: ch
     * @return 返回方案的下标
     * @author: oyyy
     * @date: 2021/11/21 20:48
     */
    public Set<Integer> getComIndex(CFG cfg,Rules rules,Character ch){
        HashSet<Integer> getIndex = new HashSet<>();
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        for (int j = 0; j < rules.right.size(); j++) {
            StringBuffer str = rules.right.get(j);
            if(cfgF.singleFirst(cfg, str.charAt(0)).contains(ch)){
                getIndex.add(j);
            }
        }
        return getIndex;
    }

    /**
     * description:消除文法的左公因子
     * @param: cfg
     * @return com.oyyy.cfg.CFG
     * @author: oyyy
     * @date: 2021/11/21 20:45
     */
    public CFG eliLeftCommon(CFG cfg){
        /*  例：
            "A->af|Bd|c|@";"B->ak|e"
            hasFirstOnce={a,c,e}
            hasAllFirst={a,a,c,e}
            改造为：A->ed|aQ|c|@;Q->f|kd;
         */
        for (int i = 0; i < cfg.production.size(); i++) {   //对于每一条产生式
            Rules rules = cfg.production.get(i);
            CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
            HashSet<Character> hasFirstOnce = new HashSet<>(); //方案出现的first，只记录一次
            ArrayList<Character> hasAllFirst=new ArrayList<>(); //方案出现的first，每次都记录

            for (int j = 0; j < rules.right.size(); j++) {  //产生式右部的每个方案
                StringBuffer str = rules.right.get(j);
                //求每个方案的first集合
                if(str.charAt(0)!='@'){  //方案不为空时
                    Set<Character> first = cfgF.singleFirst(cfg, str.charAt(0));
                    hasFirstOnce.addAll(first);
                    hasAllFirst.addAll(first);
                }
            }

            for(Character ch:hasFirstOnce){
                if(ch=='@')
                    continue;
                Set<Integer> comIndex = getComIndex(cfg, rules, ch); //用一个set记录拥有左公因子的方案下标
                //需要引入新的产生式，先产生一个新的非终结符
                char newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
                while(cfg.Vn.contains(newLeft)){
                    newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
                }
                ArrayList<StringBuffer> leftCommon = new ArrayList<>(); //方案中除公共因子的剩余字符串
                cfg.Vn.add(newLeft); //将该字符加入文法的非终结符
                if(comIndex.size()>1){  //说明有公共前缀,则需要将有该前缀的产生式右部更改
                    for(Integer index:comIndex){
                        StringBuffer nh = rules.right.get(index);
                        if(cfg.Vt.contains(nh.charAt(0))){
                            //如果是终结符
                            StringBuffer temp=new StringBuffer(nh.substring(1,nh.length()));
                            if(temp.length()==0)
                                leftCommon.add(new StringBuffer("@"));
                            else
                                leftCommon.add(temp);
                        }else{
                            //如果是非终结符，不断替换非终结符直到方案的第一个元素为终结符
                            cfg=replaceFirst(ch,rules.left,cfg,nh,leftCommon);
                        }
                    }
                    //更新产生式
                    //将产生式中以ch开头的产生式和index下标且开头为非终结符的删除
                    for (int j = 0; j < rules.right.size(); j++) {
                        StringBuffer ff = rules.right.get(j);
                        if(ff.charAt(0)==ch)
                        {
                            rules.right.remove(j);
                            j=j-1;
                        }
                        if(comIndex.contains(j)&&cfg.Vn.contains(ff.charAt(0))){
                            rules.right.remove(j);
                            j=j-1;
                        }
                    }
                    //在原有产生式添加新的方案
                    StringBuffer newPlan = new StringBuffer();
                    newPlan.append(ch).append(newLeft);
                    rules.right.add(newPlan);
                    //新的产生式
                    Rules rul2 = new Rules(newLeft,leftCommon);
                    cfg.production.add(rul2);
                }

            }
        }
        return cfg;
    }

    /**
     * description:对首字符为非终结符的方案不断替换直到首字符为终结符
     * @param: 公因子
     * @param: left,产生式左部
     * @param: cfg,文法
     * @param: nh,当前要操作的字符串
     * @param: leftCommon,存储公因子后面字符的列表。如aA,aB，则leftCommon为A和B
     * @return com.oyyy.cfg.CFG
     * @author: oyyy
     * @date: 2021/11/24 12:51
     */
    private CFG replaceFirst(Character ch, char left,CFG cfg, StringBuffer nh, ArrayList<StringBuffer> leftCommon) {
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        if(cfg.Vt.contains(nh.charAt(0))){
            if(nh.charAt(0)==ch){
                if(nh.length()==1)
                    leftCommon.add(new StringBuffer("@"));  //后面没有字符了，将空加入
                else
                    leftCommon.add(new StringBuffer(nh.substring(1,nh.length())));  //如果以ch开头，则将后面的内容加入
            }
            else{
                //将对应的产生式加入
                Rules rulesPro = cfgF.getRulesPro(cfg, left);
                rulesPro.right.add(nh);
            }
            return cfg;
        }
        else{
            //找nh首字符对应的产生式
            Rules nhRules = cfgF.getRulesPro(cfg, nh.charAt(0));
            for(StringBuffer su:nhRules.right){  //对它右部的每个方案
                StringBuffer newSu=new StringBuffer();
                //A->af|Bd|c|@ ; B->ak|e
                //这里nh是Bd,nhRules = {B, [ak,e] }
                if(su.charAt(0)=='@')
                    newSu.append(nh.substring(1,nh.length()));
                else
                    newSu.append(su).append(nh.substring(1,nh.length())); //akd  ed
                cfg=replaceFirst(ch,left,cfg,newSu,leftCommon);
            }
        }
        return cfg;
    }

    public CFG EliminteBack(CFG cfg){
        CfgSimplify csgSim=new CfgSimplify();
        CfgLeftRecursion lr = new CfgLeftRecursion();

        cfg=lr.LeftRecursion(cfg);
        cfg=eliLeftCommon(cfg);  //消除左公因子
        cfg=csgSim.simplifyRules(cfg);  //消去多余规则
        return cfg;
    }
}
