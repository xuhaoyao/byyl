package com.oyyy.test;

import com.oyyy.cfg.CFG;
import com.oyyy.cfg.CfgLeftRecursion;
import com.oyyy.cfg.CfgSimplify;
import org.junit.Test;

/**
 * @description:
 * @author: oyyy
 * @date: 2021/11/21 9:44
 */
public class TestLeftRecursion {
    @Test
    public void testDirLeftRecursion1(){
        //测试直接左递归
        String [] str = new String [1];
        str[0]="P->Pa|Pb|Pc|Pd|e|f|g|h";

        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);

        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);

        CfgLeftRecursion lr = new CfgLeftRecursion();
        cfg=lr.DirectLeftRecursion(cfg);
    }

    @Test
    public void testDirLeftRecursion2(){
        //测试直接左递归
        String [] str = new String [2];
        str[0]="S->Sa|A";
        str[1]="A->Ab|c";

        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);

        CfgLeftRecursion lr = new CfgLeftRecursion();
        cfg=lr.DirectLeftRecursion(cfg);
    }

    @Test
    public void testInDirLeftRecursion1(){
        //测试左递归
        String [] str = new String [2];
        str[0]="A->Ba|Aa|c|@";
        str[1]= "B->Bb|Ab|d";

        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);

        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);

        CfgLeftRecursion lr = new CfgLeftRecursion();
        cfg=lr.LeftRecursion(cfg);
    }

    @Test
    public void testInDirLeftRecursion2(){
        //测试间接左递归
        String [] str = new String [3];
        str[0]="A->B|@";
        str[1]="C->Ba";
        str[2]="B->CA|g";

        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);


        CfgLeftRecursion lr = new CfgLeftRecursion();
        cfg=lr.LeftRecursion(cfg);
    }
}
