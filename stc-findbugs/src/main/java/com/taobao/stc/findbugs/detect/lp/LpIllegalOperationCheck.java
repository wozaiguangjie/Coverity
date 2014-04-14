package com.taobao.stc.findbugs.detect.lp;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;

/**
 * Array.asList得到的集合不允许增加和删除元素
 * 
 * @author azheng
 */
public class LpIllegalOperationCheck extends LpAbstractOpcodeStackDetector {

    // value = true 表示 list 不可修改
    private Map<Integer, Boolean> storeMap = null;

    private Integer flag = -1;

    private LocalVariableTable curMethodlvt;

    BugReporter bugReporter;

    public LpIllegalOperationCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        Method m = getMethod();
        if (m != null) {
            curMethodlvt = m.getLocalVariableTable();
            if (curMethodlvt != null) {
                storeMap = new HashMap<Integer, Boolean>();
            }
        }
        super.visit(obj);
        storeMap = null;
    }

    @Override
    public void sawOpcode(int seen) {
        try {
            if (seen == INVOKESTATIC && getClassConstantOperand().equals("java/util/Arrays")
                    && getNameConstantOperand().equals("asList")) {
                flag = getNextPC();
            }
        } catch (Exception e) {
        }
        try {
            if (isRegisterStore()) {
                if (flag != -1 && getPC() == flag && getPrevOpcode(1) == INVOKESTATIC) {
                    storeMap.put(getRegisterOperand(), Boolean.FALSE);
                    flag = -1;
                } else {
                    storeMap.remove(getRegisterOperand());
                }
            }
        } catch (Exception e) {
        }

        try {
            if (seen == INVOKEINTERFACE) {
                if ("java/util/List".equals(getClassConstantOperand())) {
                    String methodName = getNameConstantOperand();
                    if (methodName.contains("add") || methodName.contains("addAll") || methodName.contains("remove")
                            || methodName.contains("removeAll") || methodName.contains("clear")
                            || methodName.contains("retainAll")) {
                        OpcodeStack.Item invokedOn = stack.getItemMethodInvokedOn(this);
                        int index = invokedOn.getRegisterNumber();
                        if (storeMap.get(index) != null) {
                            reportBugThis(LpTypeEnum.LP_CHECK_ILLEGAL_OPERATION);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

    }

    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
}
