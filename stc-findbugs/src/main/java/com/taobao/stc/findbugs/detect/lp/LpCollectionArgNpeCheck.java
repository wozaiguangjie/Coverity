package com.taobao.stc.findbugs.detect.lp;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * 检查 collection 作为入参 没判空就直接操作的NPE检查
 * 
 * @author wuHui
 */
public class LpCollectionArgNpeCheck extends LpAbstractBytecodeScanningDetector {

    BugReporter bugReporter;

    private List<Integer> list = null;

    private int loadIndex = -1;
    
    private int invokeInterfacePC = -1;

    public LpCollectionArgNpeCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        list = new ArrayList<Integer>();
        super.visit(obj);
        list = null;
        loadIndex = -1;
    }

    @Override
    public void sawOpcode(int seen) {
        try {
            if (LpUtil.isLoadA(seen)) {
                loadIndex = getRegisterOperand();
                list.add(loadIndex);
                if (getNextOpcode() == INVOKEINTERFACE) {
                    invokeInterfacePC = getNextPC();
                }
            }
        } catch (Exception e1) {
        }

        try {
            if (getPC() == invokeInterfacePC && seen == INVOKEINTERFACE && LpUtil.isLoadA(getPrevOpcode(1))) {

                String className = getClassConstantOperand();
                String methodName = getNameConstantOperand();

                if (className.equals("java/util/List") && (methodName.equals("size") || methodName.equals("iterator"))) {
                    int n = 0;
                    for (Integer i : list) {
                        if (i.equals(loadIndex)) {
                            n++;
                        }
                    }
                    // 如果aload 次数为 1次 则 需要报bug
                    if (n == 1) {
                        reportBugThis(LpTypeEnum.LP_COLLECTION_NULL_NPE_CHECK);
                        return;
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
