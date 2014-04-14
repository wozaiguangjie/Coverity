package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * 1.检查 float == double 
 * 2.检查 (Float.parseFloat(price) * 100 ) 
 * 
 * @author wuHui
 */
public class LpF2DCheck extends LpAbstractBytecodeScanningDetector {

    BugReporter bugReporter;
    
    //parseFloat调用后需要计算  最近 3个PC内的指令是否 乘除法操作
    private int start = 0;

    public LpF2DCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        super.visit(obj);
        start = 0;
    }

    /**
     * 每扫描一条字节码就会进入该方法
     * 
     * @param seen
     *            字节码的枚举值
     */
    @Override
    public void sawOpcode(int seen) {
        
        //****** 检查 float==double 这种写法 start ******
        if (seen == DCMPL) {
            int prev1 = -1,prev2 = -1,prev3 = -1;
            try {
                 prev1 = getPrevOpcode(1);
            } catch (Exception e) {
            }
            
            try {
                 prev2 = getPrevOpcode(2);
            } catch (Exception e) {
            }
            
            try {
                 prev3 = getPrevOpcode(3);
            } catch (Exception e) {
            }
          
            if (prev1 == F2D || prev2 == F2D || prev3 == F2D) {
                reportBugThis(LpTypeEnum.LP_F2D_CHECK);
                return;
            }
        }
        //****** 检查 float==double 这种写法 end ******
        
        
        //****** 检查 Float.parseFloat * 100  这种写法 start ******
        //  invokestatic    #23; //Method java/lang/Float.parseFloat:(Ljava/lang/String;)F
        
        if(seen == INVOKESTATIC){
            try {
                String className = getClassConstantOperand();
                String methodName = getNameConstantOperand();
                if (className.equals("java/lang/Float") && methodName.equals("parseFloat") ) {
                    start=1;
                }
                if (className.equals("java/lang/Double") && methodName.equals("parseDouble") ) {
                    start=1;
                }
            } catch (Exception e) {
            }
            //这里return必须加，关系到下次用start值
            return;
        }
        
        if (start >= 1 && start <= 4 ) {
            start++;
            if (LpUtil.isMulOrDiv(seen)) {
                reportBugThis(LpTypeEnum.LP_FLOAT_PARSE_CHECK);
                start = 0;
                return;
            }
        }
        //****** 检查 Float.parseFloat * 100  这种写法 end ******

    }

    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
}
