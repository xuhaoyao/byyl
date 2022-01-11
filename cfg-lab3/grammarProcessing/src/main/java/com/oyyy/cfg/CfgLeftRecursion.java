package com.oyyy.cfg;

import java.util.ArrayList;

/**
 * @description:消除直接左递归和间接左递归
 * @author: oyyy
 * @date: 2021/11/21 9:25
 */
public class CfgLeftRecursion {
    /**
     * description:消除直接左递归
     * @param: cfg
     * @return com.oyyy.cfg.CFG
     * @author: oyyy
     * @date: 2021/11/21 9:26
     */
    public CFG DirectLeftRecursion(CFG cfg){
//      例：将P->Pab|Pc|ef|g
//         nonLeftRecursion=ef,g
//         leftRecursion=ab,c
//      转成:
//      P -> efP'|gP'
//      p'-> abP'|cP'|@
        for (int i = 0; i < cfg.production.size(); i++) {   //对于每一条产生式
            ArrayList<StringBuffer> nonLeftRecursion = new ArrayList<>(); //不包含左递归的方案集合
            ArrayList<StringBuffer> leftRecursion = new ArrayList<>(); //包含左递归的方案集合
            Rules rules = cfg.production.get(i);
            char left = rules.left;  //产生式左部
            for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                StringBuffer str = rules.right.get(j);//每个方案
                if(left==str.charAt(0))  //首字符等于左部
                    leftRecursion.add(str.delete(0,1));   //ab,c
                else
                    nonLeftRecursion.add((str));  //ef,g
            }

            if(leftRecursion.size()==0){  //表示该产生式没有直接左递归，不用做任何修改
                continue;
            }else{
                //需要进行转换，先产生一个新的非终结符
                char newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
                while(cfg.Vn.contains(newLeft)){
                    newLeft = (char) ('A' + Math.random() * ('Z' - 'A' + 1));
                }
                cfg.Vn.add(newLeft); //将该字符加入文法的非终结符
                //将原来的产生式删除
                cfg.production.remove(rules);
                //构建P -> efP'|gP'
                for (int j = 0; j < nonLeftRecursion.size(); j++) {
                    if(nonLeftRecursion.get(j).charAt(0)=='@')
                        nonLeftRecursion.get(j).delete(0,1);
                    nonLeftRecursion.get(j).append(newLeft);
                }
                Rules rul = new Rules(left,nonLeftRecursion);
                cfg.production.add(rul);
                //p'-> abP'|cP'|@
                for (int j = 0; j < leftRecursion.size(); j++) {
                    leftRecursion.get(j).append(newLeft);
                }
                leftRecursion.add(new StringBuffer("@"));
                Rules rul2 = new Rules(newLeft,leftRecursion);
                i=i-1;
                cfg.production.add(rul2);
            }
        }
        System.out.println("消除直接左递归后：");
        System.out.println("cfg.pro = " + cfg.production);
        return cfg;
    }

    /**
     * description:消除间接左递归
     * @param: cfg
     * @return com.oyyy.cfg.CFG
     * @author: oyyy
     * @date: 2021/11/21 10:23
     */
    public CFG inDirectLeftRecursion(CFG cfg){
        for (int i = 0; i < cfg.production.size(); i++) {   //对于每一条产生式
            Rules rules = cfg.production.get(i);
            int rightSize=rules.right.size();
            for (int j = 0; j < rightSize; j++) {  //产生式右部
                CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
                StringBuffer str = rules.right.get(j);//每个方案
                if(cfg.Vn.contains(str.charAt(0)) && cfgF.getRulesPro(cfg, str.charAt(0)).isAppear){
                    //如果字符为非终结符且之前遍历过，对该字符进行替换
                    ArrayList<StringBuffer> right = cfgF.getRulesPro(cfg, str.charAt(0)).right;//找到该字符对应的产生式
                    StringBuffer strw = new StringBuffer(str.deleteCharAt(0));  //str去掉当前字符
                    rules.right.remove(j);  //删除该方案
                    j=j-1;
                    for(StringBuffer rg:right){
                        StringBuffer newRg = new StringBuffer(rg);
                        if(newRg.charAt(0)=='@')
                            newRg.delete(0,1);
                        newRg.append(strw);
                        rules.right.add(newRg);  //增加新方案
                    }
                }else{
                    continue;
                }
            }
            rules.isAppear=true;
        }
        System.out.println("消除间接左递归后：");
        System.out.println("cfg.pro = " + cfg.production);
        return cfg;
    }

    /**
     * description:消除左递归
     * @param: cfg
     * @return com.oyyy.cfg.CFG
     * @author: oyyy
     * @date: 2021/11/21 19:36
     */
    public CFG LeftRecursion(CFG cfg){
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.simplifyRules(cfg);  //消去多余规则
//        CfgEliminateBack eliminateBack = new CfgEliminateBack();
//        cfg=eliminateBack.EliminteBack(cfg);
        cfg=DirectLeftRecursion(cfg);  //先消除直接左递归
        cfg=inDirectLeftRecursion(cfg); //消除间接左递归
        cfg=DirectLeftRecursion(cfg); //间接左递归后消除直接左递归
        cfg=csgSim.simplifyRules(cfg);  //消去多余规则
        return cfg;
    }
}
