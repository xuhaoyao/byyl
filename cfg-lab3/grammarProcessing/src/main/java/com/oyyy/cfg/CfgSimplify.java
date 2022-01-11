package com.oyyy.cfg;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @description:化简文法
 * @author: oyyy
 * @date: 2021/11/14 19:54
 */
public class CfgSimplify {

    /**
     * description:复制一份相同的cfg
     * @return 复制后的cfg
     * @param: 原cfg
     * @author: oyyy
     * @date: 2021/11/14 21:11
     */
    public CFG copyCfg(CFG originCfg) {
        CFG newCfg = new CFG();
        newCfg.startSymbol = originCfg.startSymbol;
        newCfg.Vt.addAll(originCfg.Vt);
        newCfg.Vn.addAll(originCfg.Vn);
        newCfg.production = (ArrayList<Rules>) originCfg.production.clone();
        return newCfg;
    }

    /**
     * description:执行以下算法可得到一等价文法的G1[S]=(Vn1,Vt,P1,S)，使得对于每一个X∈VN1，都有ω∈Vt*，X=*>ω
     * @return 删除不可终止的产生式和非终结符后的新文法
     * @param: cfg为待化简的文法
     * @author: oyyy
     * @date: 2021/11/14 20:54
     */
    public CFG deleteUnterminableRules(CFG cfg) {
        CFG newCfg = copyCfg(cfg);
        //清空newCfg的非终结符和产生式
        newCfg.Vn.clear();
        newCfg.production.clear();

        //更新newCfg的非终结符
        int change = 1;   //一直循环，直到newCfg的非终结符不再更新
        while (change == 1) {
            change = 0;
            for (int i = 0; i < cfg.production.size(); i++) {   //对于每一条产生式
                Rules rules = cfg.production.get(i);
                char left = rules.left;  //产生式左部
                int nextPro = 0;
                for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                    StringBuffer str = rules.right.get(j);//每个方案
                    for (int k = 0; k < str.length(); k++) {  //每个字符
                        if (str.charAt(k) == '@') { //出现空串,说明它可以终止,那么这个终结符无害
                            if (!newCfg.Vn.contains(left)) {
                                newCfg.Vn.add(left);
                                change = 1;  //表示本轮有更新，需要继续循环
                            }
                            nextPro = 1; //进入下一条产生式
                            break;
                        } else if (cfg.Vt.contains(str.charAt(k)) || newCfg.Vn.contains(str.charAt(k))) {
                            //对P中每一个产生式A→X1|X2|……XK，若存在方案Xi，Xi中每一个字符都属于Vn1∪Vt，则将A放入Vn1中
                            if (k < str.length() - 1)  //进入下一个字符
                                continue;
                            else {   //str.charAt(k)为该产生式该方案的结束字符
                                if (!newCfg.Vn.contains(left)) {
                                    newCfg.Vn.add(left);
                                    change = 1;  //表示本轮有更新，需要继续循环
                                }
                                nextPro = 1; //进入下一条产生式
                                break;  //本条产生式结束
                            }
                        } else {    //方案中有一个字符不属于Vn1∪Vt,则跳出
                            break;
                        }
                    }

                    if (nextPro == 1)   //由于已经判定此条规则无害,那么直接跳出,后面的方案就不用判断了
                        break;
                }
            }
        }

        //test
        Set<Character> vn = newCfg.Vn;
        System.out.println("SimVn1 = " + vn);
        Set<Character> vt = newCfg.Vt;
        System.out.println("SimVt1 = " + vt);

        //更新newCfg的产生式：对于P中的每一个产生式A->X1|X2|……Xk，若A及方案Xi中每一个字符都属于Vn1∪Vt，则将A->Xi放入P1中
        //即这个时候我们需要去除有害规则,代码走到这里,newCfg.Vn里面的规则都是无害的,不在这里面的规则都是有害的,要在A -> Xi中去掉有害的Xi
        for (int i = 0; i < cfg.production.size(); i++) {
            Rules rules = cfg.production.get(i);
            char left = rules.left;  //产生式左部
            if (!newCfg.Vn.contains(left))   //如果产生式左边的字符不属于非终结符，本轮结束
                continue;
            ArrayList<StringBuffer> sr = new ArrayList<>();
            StringBuffer con;

            for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                StringBuffer str = rules.right.get(j);//每个方案
                con = new StringBuffer();
                for (int k = 0; k < str.length(); k++) {  //每个字符
                    if (cfg.Vt.contains(str.charAt(k)) || newCfg.Vn.contains(str.charAt(k)) || str.charAt(k) == '@') {
                        con.append(str.charAt(k));
                        if (k < str.length() - 1) { //进入下一个字符
                            continue;
                        } else {  //将该方案加入产生式中
                            sr.add(new StringBuffer(con));
                            con = new StringBuffer();
                        }
                    } else {
                        break;   //进入下一方案
                    }
                }
            }
            Rules rul = new Rules(left, sr);
            newCfg.production.add(rul);
        }
        ArrayList<Rules> production = newCfg.production;
        System.out.println("SimPro1 = " + production);
        return newCfg;
    }

    //找文法规则左边的符号在文法产生式集合中对应的下标
    public int findProIndex(char left,ArrayList<Rules> production){
        for(int i = 0;i < production.size();i++){
            Rules rule = production.get(i);
            if(rule.left == left){
                return i;
            }
        }
        return -1; //不应该出现的情况
    }

    public void dfs(ArrayList<Rules> production,boolean[] reachable,int start){
        if(reachable[start]) return;
        reachable[start] = true;
        Rules rule = production.get(start);
        for(int i = 0;i < rule.right.size();i++){
            StringBuffer sb = rule.right.get(i);
            for(int j = 0;j < sb.length();j++){
                char c = sb.charAt(j);
                if(c >= 'A' && c <= 'Z'){
                    dfs(production,reachable,findProIndex(c,production));
                }
            }
        }
    }

    public CFG deleteUnreachableRules(CFG cfg){
        boolean[] reachable = new boolean[cfg.production.size()];
        dfs(cfg.production,reachable,0);
        ArrayList<Rules> newProduction = new ArrayList<>();
        for(int i = 0;i < reachable.length;i++){
            if(reachable[i]){
                newProduction.add(cfg.production.get(i));  //为了简单,不考虑对象引用,垃圾回收的问题了
            }
        }
        cfg.production = newProduction;
        return cfg;
    }

    /**
     * description:执行以下算法可得到一等价文法G2[S]=(Vn2，Vt2，P2，S）使得对任一X∈VN’∪VT’都存在α,β∈（VN’∪ VT’）有S=*>αXβ
     * @return 删除不可到达的产生式后的新文法，其中，非终结符、终结符和产生式进行了变更
     * @param: cfg为待化简的文法
     * @author: oyyy
     * @date: 2021/11/16 17:10
     */
    public CFG deleteUnreachableRules1(CFG cfg) {
        CFG newCfg = copyCfg(cfg);
        //清空newCfg的非终结符、终结符和产生式
        newCfg.Vt.clear();
        newCfg.Vn.clear();
        newCfg.production.clear();
        //将文法开始符放入newCfg的VN’中
        newCfg.Vn.add(newCfg.startSymbol);
        //将S放入Vn2中，对P的每一个产生式A->X1|X2|……Xk，若A∈Vn2，则将每个方案Xi中的全部非终结符放入Vn2中，终结符放入Vt2中
        //重复以上步骤直到Vn2、Vt2不再增大为止
        int change = 1;
        while (change == 1) {
            change = 0;
            for (int i = 0; i < cfg.production.size(); i++) {  //对于每一条产生式
                Rules rules = cfg.production.get(i);
                if (!newCfg.Vn.contains(rules.left))
                    continue;
                for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                    StringBuffer str = rules.right.get(j);//每个方案
                    for (int k = 0; k < str.length(); k++) {  //每个字符
                        if (str.charAt(k) == '@') {
                            break;  //进入下一方案
                        } else if (cfg.Vn.contains(str.charAt(k)) && !newCfg.Vn.contains(str.charAt(k))) {
                            change = 1;
                            newCfg.Vn.add(str.charAt(k));
                        } else if (cfg.Vt.contains(str.charAt(k)) && !newCfg.Vt.contains(str.charAt(k))) {
                            change = 1;
                            newCfg.Vt.add(str.charAt(k));
                        }
                    }
                }
            }
        }
        //test
        Set<Character> vn = newCfg.Vn;
        System.out.println("SimVn2 = " + vn);
        Set<Character> vt = newCfg.Vt;
        System.out.println("SimVt2 = " + vt);

        //更新产生式：将P中左右部仅含Vn2∪Vt2中符号的所有产生式放入P2中
        for (int i = 0; i < cfg.production.size(); i++) {
            Rules rules = cfg.production.get(i);
            if (!newCfg.Vn.contains(rules.left)) //如果产生式左边的字符不属于非终结符，本轮结束
                continue;
            ArrayList<StringBuffer> sr = new ArrayList<>();
            StringBuffer con = new StringBuffer();

            for (int j = 0; j < rules.right.size(); j++) {  //产生式右部
                StringBuffer str = rules.right.get(j);//每个方案
                con = new StringBuffer();
                for (int k = 0; k < str.length(); k++) {  //每个字符
                    if (newCfg.Vt.contains(str.charAt(k)) || newCfg.Vn.contains(str.charAt(k)) || str.charAt(k) == '@'){
                        con.append(str.charAt(k));
                        if (k < str.length() - 1) { //进入下一个字符
                            continue;
                        } else {  //将该方案加入产生式中
                            sr.add(con);
                            con = new StringBuffer();
                        }
                    } else {
                        break;   //进入下一方案
                    }
                }
            }
            Rules rul = new Rules(rules.left, sr);
            newCfg.production.add(rul);
        }
        ArrayList<Rules> production = newCfg.production;
        System.out.println("SimPro2 = " + production);
        return newCfg;
    }

    /**
     * description:化简文法
     * @param: cfg
     * @return com.oyyy.cfg.CFG
     * @author: oyyy
     * @date: 2021/11/21 19:39
     */
    public CFG simplifyRules(CFG cfg){
        cfg=deleteUnterminableRules(cfg);  //删除不可终止的
        cfg=deleteUnreachableRules(cfg);   //删除不可到达的
        return cfg;
    }
}
