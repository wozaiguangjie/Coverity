package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author wuHui
 * 
 * 检测是否包含了printStackTrace()     
 * 
 */
public class LpExceptionCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    public LpExceptionCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        super.visit(obj);
    }

    @Override
    public void sawOpcode(int seen) {
        if (seen == INVOKEVIRTUAL) {
            
            if (getNameConstantOperand().contains("printStackTrace")) {
                BugInstance bug = new BugInstance(this, LpTypeEnum.LP_CHECK_EXCEPTION, HIGH_PRIORITY).addClassAndMethod(this)
                        .addSourceLine(this, getPC());
                bug.addInt(getPC());
                bugReporter.reportBug(bug);
            }
        }
    }
    
}
