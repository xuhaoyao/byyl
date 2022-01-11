package com.oyyy.test;

import com.oyyy.cfg.CFG;
import com.oyyy.cfg.CfgFirstAndFollow;
import com.oyyy.cfg.CfgSimplify;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:测试类，以下测试不涉及左公因子和左递归
 * @author: oyyy
 * @date: 2021/11/14 17:12
 */
public class TestSimAndFirAndFol {

    /**
     * description:测试文法存储
     * @param:
     * @return void
     * @author: oyyy
     * @date: 2021/11/14 17:28
     */
    @Test
    public void testCfgSimplify(){
        String [] str = new String [6];
        str[0]="S->Be";
        str[1]="B->Ce|Af";  //B->Ce不可终止
        str[2]="A->Ae|e";
        str[3]="C->Cf";  //不可终止
        str[4]="D->f";  //不可到达
        str[5]="F->F";  //有害规则

        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);

        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
    }

    /**
     * description:first集合计算的测试用例1
     * @param:
     * @return void
     * @author: oyyy
     * @date: 2021/11/18 16:32
     */
    @Test
    public void testGetFirst1(){
        String [] str = new String [4];
        str[0]="S->AB";
        str[1]="A->Ba";
        str[2]="B->Cb";
        str[3]="C->ef";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        cfgF.getFirst(cfg);
        cfgF.getFollow(cfg);
    }

    /**
     * description:first集合计算的测试用例2
     * @param:
     * @return void
     * @author: oyyy
     * @date: 2021/11/18 16:32
     */
    @Test
    public void testGetFirst2(){
        String [] str = new String [5];
        str[0]="S->AB|CD";
        str[1]="A->aB|dD";
        str[2]="B->cC|bD";
        str[3]="C->ef|gh";
        str[4]="D->i|j";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        cfgF.getFirst(cfg);
        cfgF.getFollow(cfg);
    }

    /**
     * description:first集合计算的测试用例3
     * @param:
     * @return void
     * @author: oyyy
     * @date: 2021/11/18 16:33
     */
    @Test
    public void testGetFirst3(){
        String [] str = new String [5];
        str[0]="S->ABC|D";
        str[1]="A->aB|@";
        str[2]="B->@|cC";
        str[3]="C->eC|@";
        str[4]="D->i|j";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        cfgF.getFirst(cfg);
        cfgF.getFollow(cfg);
    }

    @Test
    public void testGetFollow1(){
        String [] str = new String [5];
        str[0]="E->TA";
        str[1]="A->+TA|@";
        str[2]="T->FB";
        str[3]="B->*FB|@";
        str[4]="F->(E)|i";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.simplifyRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        cfgF.getFirst(cfg);
        cfgF.getFollow(cfg);
    }

    @Test
    public void testGetFollow2(){
        String [] str = new String [4];
        str[0]="S->ABC";
        str[1]="A->aB|bB";
        str[2]="B->cC|@";
        str[3]="C->ef|gh";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
        cfgF.getFirstAndFollow(cfg);
    }

}
