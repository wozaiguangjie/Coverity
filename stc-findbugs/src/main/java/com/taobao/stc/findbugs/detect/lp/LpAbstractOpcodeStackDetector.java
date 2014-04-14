package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * @author wuhui
 */
abstract public class  LpAbstractOpcodeStackDetector extends OpcodeStackDetector{

    @Override
    public void visit(Code obj) {
        if (!LpCodeReviewRule.needCheck(getClassContext().getJavaClass())) {
            return;
        } else {
            super.visit(obj);
        }
    }
    
}
