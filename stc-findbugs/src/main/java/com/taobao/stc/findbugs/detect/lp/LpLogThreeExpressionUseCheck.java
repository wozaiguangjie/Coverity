package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantString;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author wuhui
 * 
 *log记录时三元表达式误用 log.error("333333333333333333 "+ a == null ? "null" :
 * 
 */
public class LpLogThreeExpressionUseCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    private int log_error_start = -1;
    private int log_error_tostring = -1;
    private boolean log_error_ifnotnull = false;
    private boolean log_error_ifnotnull_2 = false;

    public LpLogThreeExpressionUseCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    /**
     * 每次进入字节码方法的时候调用，在每次进入新方法的时候清空标志位
     */
    @Override
    public void visit(Code obj) {
        if (!LpCodeReviewRule.needCheck(getClassContext().getJavaClass())) {
            return;
        }
        initFields();
        super.visit(obj);
    }

    private void initFields() {
        log_error_start = -1;
        log_error_tostring = -1;
        log_error_ifnotnull = false;
        log_error_ifnotnull_2 = false;
    }

    /**
     * 每扫描一条字节码就会进入该方法
     * 
     * @param seen
     *            字节码的枚举值
     */
    @Override
    public void sawOpcode(int seen) {

        if ((seen == GETFIELD || seen == GETSTATIC) && getNextOpcode() == NEW) {
            log_error_start = getPC();
        }

        if (seen == INVOKEVIRTUAL && getClassConstantOperand().equals("java/lang/StringBuilder")
                && getNameConstantOperand().equals("toString") && (log_error_start > -1 && getPC() > log_error_start)) {
            log_error_tostring = getPC();
        }

        if (seen == IFNONNULL && getNextOpcode() == LDC && !log_error_ifnotnull
                && (log_error_tostring > -1 && getPC() > log_error_tostring)) {
            log_error_ifnotnull = true;
        }

        if ( log_error_ifnotnull && seen == LDC && getPrevOpcode(1) == IFNONNULL ) {
            try {
                Constant c = getConstantRefOperand();
                if (c instanceof ConstantString) {
                    if ("null".equals(getStringConstantOperand()) || null == getStringConstantOperand()) {
                        log_error_ifnotnull_2 = true;
                    }
                }
            } catch (Exception e) {
            }
        }
        
        if (seen == INVOKEINTERFACE && log_error_ifnotnull && log_error_ifnotnull_2) {
            if (getClassConstantOperand().endsWith("/Logger") && getNameConstantOperand().equals("error")) {
                reportBugThis(LpTypeEnum.LP_CHECK_LOG_ERROR_MES);
                initFields();
            }
        }
    }

    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }

}
