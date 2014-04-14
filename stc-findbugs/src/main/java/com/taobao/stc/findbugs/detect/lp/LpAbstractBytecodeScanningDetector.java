package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BytecodeScanningDetector;

/**
 * @author wuhui
 */
abstract public class LpAbstractBytecodeScanningDetector extends BytecodeScanningDetector{

    @Override
    public void visit(Code obj) {
        if (!LpCodeReviewRule.needCheck(getClassContext().getJavaClass())) {
            return;
        } else {
            super.visit(obj);
        }
    }
}
