package com.taobao.stc.findbugs.detect.lp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author wuhui
 * 
 */
public class LpArgsUnUseCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    private int args_num = 0;

    private Set<Integer> args_use_num = null;

    private List<Integer> args_wait_use = null;

    private LocalVariableTable curMethodlvt;
    
    List<String> list = Arrays.asList("closeWrite","wapperBySimpleImage");

    public LpArgsUnUseCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        
        if (!LpCodeReviewRule.needCheck(getClassContext().getJavaClass())) {
            return;
        }

        try {
            //只检查 private 的  非static 非native 且2个参数以上的 方法
            if (!getMethod().isPrivate() || getMethod().isNative() || getMethod().isStatic()) {
                return;
            }
        } catch (Exception e1) {
            return;
        }

        args_num = getNumberMethodArguments();

        if (args_num < 2) {
            return;
        }

        if (args_num > 1) {
            args_use_num = new HashSet<Integer>();
            args_wait_use = new ArrayList<Integer>();
            Method m = getMethod();
            if (m != null) {
                if (list.contains(m.getName())) {
                    curMethodlvt = null;
                    args_wait_use = null;
                    args_use_num = null;
                    args_num = 0;
                    return;
                }
                curMethodlvt = m.getLocalVariableTable();
                if (curMethodlvt != null) {
                    int n = 1;
                    for (LocalVariable lv : curMethodlvt.getLocalVariableTable()) {
                        if (lv.getIndex() > 0 && n >= 1 && n <= args_num) {
                            args_wait_use.add(lv.getIndex());
                            n++;
                        }

                    }
                }
            }
            // 开始遍历字节码
            super.visit(obj);

            for (Integer i : args_wait_use) {
                // 如果已经load过的index没有包含 参数的index 那么报bug
                if (!args_use_num.contains(i)) {
                    reportBug();
                    break;
                }
            }
        }

        curMethodlvt = null;
        args_wait_use = null;
        args_use_num = null;
        args_num = 0;
    }

    @Override
    public void sawOpcode(int seen) {
        // 如果是load 则把load 的local的value 的 index放进set中
        try {
            if (isRegisterLoad() || LpUtil.isLoad(seen)) {
                int registerOperand = getRegisterOperand();
                // 进入这里 入参个数 都是 args_num > 1 的 方法
                args_use_num.add(registerOperand);
            }
        } catch (Exception e) {
        }
    }
    
    private void reportBug() {
        BugInstance bug = new BugInstance(this, LpTypeEnum.LP_ARGS_UN_USE_CHECK, HIGH_PRIORITY).addClassAndMethod(this)
                .addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
       
    }

}
