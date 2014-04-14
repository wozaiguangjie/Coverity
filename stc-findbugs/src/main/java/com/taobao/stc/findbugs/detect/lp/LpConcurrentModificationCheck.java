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
 * List.subList得到的新集合后，不允许对原集合做结构性更改，如增加和删除元素
 * 
 * @author azheng
 */
public class LpConcurrentModificationCheck extends LpAbstractOpcodeStackDetector {

    private Map<Integer, Boolean> loadMap = null;

    private Map<Integer, Integer> storeMap = null;

    private Integer flag = -1;

    private Integer index = -1;

    private LocalVariableTable curMethodlvt;

    BugReporter bugReporter;

    public LpConcurrentModificationCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        Method m = getMethod();
        if (m != null) {
            curMethodlvt = m.getLocalVariableTable();
            if (curMethodlvt != null) {
                loadMap = new HashMap<Integer, Boolean>();
                storeMap = new HashMap<Integer, Integer>();
            }
        }
        super.visit(obj);
        storeMap = null;
        loadMap = null;
        flag = -1;
    }

    @Override
    public void sawOpcode(int seen) {
        try {
            if (seen == INVOKEINTERFACE && getClassConstantOperand().equals("java/util/List")
                    && getNameConstantOperand().equals("subList")) {
                flag = getNextPC();
                OpcodeStack.Item invokedOn = stack.getItemMethodInvokedOn(this);
                index = invokedOn.getRegisterNumber();
                loadMap.put(index, Boolean.FALSE);
            }
        } catch (Exception e) {
        }
        try {
            if (isRegisterStore() && getPC() == flag && index != -1) {
                if (getPrevOpcode(1) == INVOKEINTERFACE) {
                    storeMap.put(getRegisterOperand(), index);
                    flag = -1;
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
                        int loadIndex = invokedOn.getRegisterNumber();
                        if (loadMap != null && loadMap.get(loadIndex) != null) {
                            loadMap.put(loadIndex, Boolean.TRUE);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        try {
            if (isRegisterLoad()) {
                Integer storekey = getRegisterOperand();
                Integer loadkey = storeMap.get(storekey);
                if (loadMap.get(loadkey) != null && loadMap.get(loadkey) == Boolean.TRUE) {
                    reportBugThis(LpTypeEnum.LP_CHECK_CONCURRENT_MODIFICATION);
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
