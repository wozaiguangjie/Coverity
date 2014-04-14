package com.taobao.stc.findbugs.detect.lp;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author fucai.zhangfc
 */
public class LPoolSizeCheck extends LpAbstractBytecodeScanningDetector {
    BugReporter bugReporter;
    
    public LPoolSizeCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }
    @Override
    public void sawOpcode(int seen) {
        if (seen == INVOKESTATIC) {
            if ("java/util/concurrent/Executors".equals(getClassConstantOperand())) {
                if ("newFixedThreadPool".equals(getNameConstantOperand()) && "(I)Ljava/util/concurrent/ExecutorService;".equals(getSigConstantOperand())) {
                    reportBugThis(LpTypeEnum.LP_CHECK_SET_POOL_SIZE);
                }
            }
        }
    }
    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this)
                .addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
}
