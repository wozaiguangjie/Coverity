package com.taobao.stc.findbugs.detect.lp;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author wuhui
 * 
 * 检测方法体中 仅有 1个try语句存在时 finally中是否含有return语句，只检查一次
 * 
 */
public class LpFinallyReturnCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    // 用于存放finally 后产生的any异常类型的 target 和 to 的pc值
    private Map<Integer, Integer> target_end = null;

    public LpFinallyReturnCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        if (!LpCodeReviewRule.needCheck(getClassContext().getJavaClass())) {
            return;
        }
        initM();
        if (obj.getExceptionTable() != null) {
            for (CodeException e : obj.getExceptionTable()) {
                int cause = e.getCatchType();
                target_end = new HashMap<Integer, Integer>();
                if (cause == 0) {
                    target_end.put(e.getHandlerPC(), e.getEndPC());
                }
            }
        }

        if (target_end != null && target_end.size() > 0) {
            super.visit(obj);
        }
        
        initM();
    }

    private void initM() {
        target_end = null;
    }

    @Override
    public void sawOpcode(int seen) {
        if (target_end != null && target_end.get(getPC()) != null && seen == POP && getPrevOpcode(1) == GOTO) {
            reportBugThis();
        }
    }

    private void reportBugThis() {
        BugInstance bug = new BugInstance(this, LpTypeEnum.LP_CHECK_FINALLY, HIGH_PRIORITY).addClassAndMethod(this)
                .addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }

    // @DottedClassName
    // String causeName;
    // causeName = "java.lang.Throwable";
    // causeName =
    // Utility.compactClassName(getConstantPool().getConstantString(cause,
    // CONSTANT_Class), false);

}
