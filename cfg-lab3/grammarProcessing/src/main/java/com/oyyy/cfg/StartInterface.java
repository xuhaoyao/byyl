package com.oyyy.cfg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * @description:总控程序
 * @author: oyyy
 * @date: 2021/11/23 13:25
 */
public class StartInterface extends JFrame{

    CFG cfg=new CFG();
    CfgSimplify csgSim=new CfgSimplify();
    CfgLeftRecursion lr = new CfgLeftRecursion();
    CfgFirstAndFollow cfgF=new CfgFirstAndFollow();
    CfgEliminateBack comLeft = new CfgEliminateBack();
    LL1 ll=new LL1();

    public StartInterface()
    {
        JFrame frmMain = new JFrame("文法问题处理器");
        frmMain.setSize(650, 680);
        frmMain.setLocationRelativeTo(null);
        frmMain.setLayout(new FlowLayout());
        JPanel jp1 = new JPanel();
        JPanel jp2 = new JPanel();
        JPanel jp3 = new JPanel();
        JPanel jp4 = new JPanel();
        JPanel jp5 = new JPanel();
        JButton btnA = new JButton("化简文法");
        JButton btnB = new JButton("消除左公因子");
        JButton btnC = new JButton("消除左递归");
        JButton btnD = new JButton("求first集合follow集");
        JButton btnE = new JButton("存储文法");

        JLabel jl=new JLabel("请在下方输入产生式          ");    //创建一个标签
        JLabel jl2=new JLabel("输入要分析的句子：");    //创建一个标签
        JTextField sentence=new JTextField(15);    //创建文本框
        JButton analyse = new JButton("LL1(分析)");
        JLabel tip=new JLabel("\n"+"tips：请先点击存储文法再进行其它操作");    //创建一个标签
        JButton clear = new JButton("重置");
        JTextArea cfgInput = new JTextArea();
        cfgInput.setPreferredSize(new Dimension(160, 450));
        JTextArea res = new JTextArea();
        res.setPreferredSize(new Dimension(420, 450));
        res.setEditable(false);  //不可编辑
        JScrollPane jsp = new JScrollPane(res);
        jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JButton open = new JButton("打开");
        JButton save = new JButton("保存");
        JLabel jl4=new JLabel("                                                                 ");
        JLabel jl3=new JLabel("                                                                 ");
        //添加到JPanel
        jp1.add(btnE);
        jp1.add(btnA);
        jp1.add(btnC);
        jp1.add(btnB);
        jp1.add(btnD);
        jp2.add(jl);
        jp2.add(jl2);
        jp2.add(sentence);
        jp2.add(analyse);
        jp3.add(cfgInput);
        jp3.add(res);
        jp4.add(open);
        jp4.add(save);
        jp4.add(jl3);
        jp4.add(jl4);
        jp5.add(tip);
        jp5.add(clear);
        //将Panel加入JFrame
        frmMain.add(jp1);
        frmMain.add(jp5);
        frmMain.add(jp2);
        frmMain.add(jp3);
        frmMain.add(jp4);
        frmMain.setVisible(true);
        frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //按钮点击事件
        class ButtonListener implements ActionListener {// 定义内部类实现事件监听
            int flag=0;
            public void showRes(CFG cfg){
                res.append("文法开始符号：" + cfg.startSymbol + "\n");
                res.append("终结符：" + cfg.Vt.toString() + "\n");
                res.append("非终结符：" + cfg.Vn.toString() + "\n");
                res.append("文法产生式如下：" + "\n");
                for (int i = 0; i < cfg.production.size(); i++) {
                    Rules rules = cfg.production.get(i);
                    StringBuffer rule = new StringBuffer(rules.left+"->");
                    for (int j = 0; j < rules.right.size(); j++) {
                        rule.append(rules.right.get(j));//每个方案
                        if(j!=rules.right.size()-1)
                            rule.append("|");
                    }
                    res.append(rule.toString()+"\n");
                }
            }

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnE) { //存储文法
                    flag=1;
                    String[] strs = cfgInput.getText().split("\n");  //获取用户输入的内容
                    cfg=new CFG();
                    cfg = cfg.storeGrammar(strs);
                    res.setText("");
                    res.append("存储成功" + "\n");
                } else if (e.getSource() == open){   //打开文件
                    JFileChooser jf = new JFileChooser();
                    jf.showOpenDialog(frmMain);//显示打开的文件对话框
                    File f =  jf.getSelectedFile();//使用文件类获取选择器选择的文件
                    String path = f.getAbsolutePath();//返回路径名
                    //JOptionPane弹出对话框类，显示绝对路径名
//                    JOptionPane.showMessageDialog(frmMain, s, "标题",JOptionPane.INFORMATION_MESSAGE);
                    readFile(path);
                }else if(e.getSource()==save){ //保存
                    String path=null;
                    try {
                        JFileChooser jf = new JFileChooser();
                        jf.showOpenDialog(frmMain);//显示打开的文件对话框
                        File f =  jf.getSelectedFile();//使用文件类获取选择器选择的文件
                        path = f.getAbsolutePath();//返回路径名
                        write(cfgInput.getText(),path);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(frmMain, "已成功保存至"+path, "标题",JOptionPane.INFORMATION_MESSAGE);
                }
                else if (e.getSource() == btnA) {  //化简文法
                    if(flag==0)
                        JOptionPane.showMessageDialog(frmMain, "请先存储文法！", "警告",JOptionPane.INFORMATION_MESSAGE);
                    else{
                        cfg = csgSim.simplifyRules(cfg);
                        res.setText("");
                        res.append("化简文法结果：" + "\n"+"\n");
                        showRes(cfg);
                    }
                }else if(e.getSource()==btnC){  //消除左递归
                    if(flag==0)
                        JOptionPane.showMessageDialog(frmMain, "请先存储文法！", "警告",JOptionPane.INFORMATION_MESSAGE);
                    else{
                        cfg=lr.LeftRecursion(cfg);
                        res.setText("为顺消除左递归，已执行化简文法。"+"\n"+"\n");
                        res.append("消除左递归结果：" + "\n"+"\n");
                        showRes(cfg);
                    }
                }else if(e.getSource()==btnB){  //消除左公因子
                    if(flag==0)
                        JOptionPane.showMessageDialog(frmMain, "请先存储文法！", "警告",JOptionPane.INFORMATION_MESSAGE);
                    else{
                        cfg=comLeft.EliminteBack(cfg);
                        res.setText("为顺利消除左公共因子，已执行化简文法、和消除左递归。"+"\n"+"\n");
                        res.append("消除左公因子结果：" + "\n"+"\n");
                        showRes(cfg);
                    }
                }else if(e.getSource()==btnD){  //求first和follow集合
                    if(flag==0)
                        JOptionPane.showMessageDialog(frmMain, "请先存储文法！", "警告",JOptionPane.INFORMATION_MESSAGE);
                    else{
                        cfg=cfgF.getFirstAndFollow(cfg);
                        res.setText("为顺利求解first和follow集合，已执行化简文法、消除左公因子和消除左递归。"+"\n"+"\n");
                        showRes(cfg);
                        res.append("\n"+"非终结符" + "\t"+ "\t"+"First集"+ "\t"+ "\t"+"Follow集"+ "\n");
                        for(Character nonter:cfg.first.keySet()){
                            if(cfg.Vn.contains(nonter))
                                res.append(nonter + "\t"+ "\t"+cfg.first.get(nonter).toString()+"\t"+ "\t"+cfg.follow.get(nonter).toString()+"\n");
                        }
                    }
                }else if(e.getSource()==analyse){  //LL1分析
                    if(flag==0)
                        JOptionPane.showMessageDialog(frmMain, "请先存储文法！", "警告",JOptionPane.INFORMATION_MESSAGE);
                    else{
                        if(sentence.getText().length()==0){
                            res.setText("");
                            res.setText("请输入要分析的字符串！");
                        }else{
                            cfg=cfgF.getFirstAndFollow(cfg);
                            res.setText("");
                            res.setText("为顺利分析，已执行化简文法、消除左公因子、消除左递归和求first集follow集"+"\n"+"\n");
                            showRes(cfg);
                            cfg=ll.analyse(cfg,new StringBuffer(sentence.getText().trim()));
                            if(cfg.isLL1==false){
                                res.append("\n"+"不符合LL1文法，请重新输入文法！");
                                res.append("分析失败！");
                            }else{
                                res.append("\n"+"步骤"+ "\t"+"符号栈"+ "\t"+ "\t"+"输入串"+ "\t"+"动作"+"\n");
                                int number=1;
                                for(StringBuffer str:cfg.result){
                                    res.append(number+"\t"+str.toString());
                                    number++;
                                }
                                if(cfg.flag==true)
                                    res.append("分析成功！");
                                else
                                    res.append("分析失败！");
                            }
                        }
                    }
                }else if(e.getSource()==clear){  //全部重置
                    flag=0;
                    cfgInput.setText("");
                    res.setText("");
                    sentence.setText("");
                    cfg=new CFG();
                }
            }

            //将文法保存到文件
            public void write(String line,String path) throws IOException {
                File file = new File(path);
                FileWriter fw=new FileWriter(file);
                fw.write(line);
                fw.close();
            }

            //获取文件内容显示在textArea
            private void readFile(String path) {
                FileReader fr = null;
                BufferedReader br = null;
                try {
                    fr= new FileReader(path);
                    br=new BufferedReader(fr);
                    String str;
                    while((str=br.readLine())!=null){
                        cfgInput.append(str+"\n");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    try {
                        br.close();
                        fr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ButtonListener b = new ButtonListener();
        btnE.addActionListener(b);
        btnC.addActionListener(b);
        clear.addActionListener(b);
        btnB.addActionListener(b);
        btnD.addActionListener(b);
        btnA.addActionListener(b);
        btnE.addActionListener(b);
        open.addActionListener(b);
        save.addActionListener(b);
        analyse.addActionListener(b);
    }

    public static void main(String[] agrs)
    {
        new StartInterface();    //创建一个实例化对象
    }

}
