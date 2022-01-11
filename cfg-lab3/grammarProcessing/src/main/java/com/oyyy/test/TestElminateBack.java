package com.oyyy.test;

import com.oyyy.cfg.*;
import org.junit.Test;

/**
 * @description:
 * @author: oyyy
 * @date: 2021/11/21 20:34
 */
public class TestElminateBack {
    @Test
    public void test1(){
        String [] str = new String [3];
        str[0]="S->af|Bd|c|@";
        str[1]="B->C|Cd";
        str[2]="C->ak";
        CFG cfg = new CFG(); //新建一个文法对象
        cfg=cfg.storeGrammar(str);
        CfgSimplify csgSim=new CfgSimplify();
        cfg=csgSim.deleteUnterminableRules(cfg);
        cfg=csgSim.deleteUnreachableRules(cfg);
        CfgLeftRecursion lr = new CfgLeftRecursion();
        cfg=lr.LeftRecursion(cfg);

        CfgEliminateBack comLeft = new CfgEliminateBack();
        comLeft.eliLeftCommon(cfg);
//        CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
//        cfgF.getFirstAndFollow(cfg);

    }
}
