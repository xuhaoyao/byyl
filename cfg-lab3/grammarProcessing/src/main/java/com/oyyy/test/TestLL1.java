package com.oyyy.test;

import com.oyyy.cfg.*;
import org.junit.Test;

/**
 * @description:测试LL1
 * @author: oyyy
 * @date: 2021/11/23 10:57
 */
public class TestLL1 {
    @Test
    public void testLL1(){
        String [] str = new String [3];
        str[0]="S->AbB|Bc";
        str[1]="A->aA|@";
        str[2]="B->d|e";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);

        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgLeftRecursion lr = new CfgLeftRecursion();
        cfg=lr.LeftRecursion(cfg);
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        cfgF.getFirstAndFollow(cfg);
        LL1 ll = new LL1();
        StringBuffer strw = new StringBuffer("abd");
        ll.analyse(cfg,strw);
    }

    @Test
    public void testLL2(){
        //文法规则含有二义性时，即Follow(A)和First(A)的交集不为空时
        String [] str = new String [4];
        str[0]="S->bB|Cc|Ak";
        str[1]="A->aAB|@";
        str[2]="B->a|d";
        str[3]="C->e|@";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);

        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgLeftRecursion lr = new CfgLeftRecursion();
        cfg=lr.LeftRecursion(cfg);
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        cfgF.getFirstAndFollow(cfg);
        LL1 ll = new LL1();
        ll.getSelect(cfg);
        ll.getPredict(cfg);
    }
}
