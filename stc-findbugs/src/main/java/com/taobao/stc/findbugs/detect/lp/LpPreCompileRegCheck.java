package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;
import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * 检查在使用正则表达式时是否进行了预编译，检查的方法有Pattern.compile(String)和Pattern.matches(String,
 * CharSequence)
 * 
 * @author fucai.zhangfc 2012-5-30下午8:25:16
 */
public class LpPreCompileRegCheck extends LpAbstractBytecodeScanningDetector {

    BugReporter bugReporter;

    private boolean isMethod = Boolean.FALSE;

    public LpPreCompileRegCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        String methodName = getMethodName();
        // this.get
        // 非静态初始化和初始化方法(以及去掉spring初始化的init方法和set方法)
        if (StringUtils.isNotBlank(methodName)
                && (!"<clinit>".equals(methodName) && !"<init>".equals(methodName) && !methodName.startsWith("init") && !methodName
                        .startsWith("set"))) {
            isMethod = Boolean.TRUE;
            super.visit(obj);
        } else {
            isMethod = Boolean.FALSE;
            return;
        }
    }

    @Override
    public void sawOpcode(int seen) {
        // 执行方法代码且调用静态方法
        if (seen == INVOKESTATIC && isMethod) {
            // 调用的是这种表达式类
            if ("java/util/regex/Pattern".equals(getClassConstantOperand())) {

                if ("compile".equals(getNameConstantOperand()) // 调用compile方法
                        || ("matches".equals(getNameConstantOperand()) && "(Ljava/lang/String;Ljava/lang/CharSequence;)Z"
                                .equals(getSigConstantOperand() // 调用参数为String,CharSequence的matches方法
                                ))) {
                    reportBugThis(LpTypeEnum.LP_CHECK_REG_PRE_COMPILE);
                }
            }
        }
    }

    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
}
