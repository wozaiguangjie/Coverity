package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author wuhui
 * 
 * 用于检测是否代码中是否包含：System.out和System.error System.gc
 * 
 */
public class LpSystemMethodCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    private String ldcClazz = "";
    
    public LpSystemMethodCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    /**
     * 每次进入字节码方法的时候调用，在每次进入新方法的时候清空标志位
     */
    @Override
    public void visit(Code obj) {
        super.visit(obj);
    }

    /**
     * 每扫描一条字节码就会进入该方法
     * 
     * @param seen
     *            字节码的枚举值
     */
    @Override
    public void sawOpcode(int seen)  {
        
//        Method m = getMethod();
//        LocalVariableTable lvt = m.getLocalVariableTable();
//        if (lvt != null) {
//            try{
//                if ((seen >= ASTORE && seen <= ASTORE_3) || (seen >= ALOAD && seen <= ALOAD_3)) {
//                    LocalVariable lv = LVTHelper.getLocalVariableAtPC(lvt, getRegisterOperand(), getPC());
//                    if (lv != null) {
//                        String signature = lv.getSignature();
//                    }
//                }
//            }catch(Exception e){
//            }
//        }
        
        
        String methodName = getMethodName();
        if (STATIC_INITIALIZER_NAME.equals(methodName) || CONSTRUCTOR_NAME.equals(methodName)) {
            //使用log时必须匹配当前class
            if (seen == LDC && getNextOpcode() == INVOKESTATIC) {
                try {
                    ldcClazz = getClassConstantOperand().replace("/", ".");
                } catch (Exception e) {
                }
            }

            if (seen == INVOKESTATIC && getClassConstantOperand().equals("org/slf4j/LoggerFactory")
                    && getNameConstantOperand().equals("getLogger")
                    && getSigConstantOperand().equals("(Ljava/lang/Class;)Lorg/slf4j/Logger;")) {

                if (getPrevOpcode(1) == LDC) {
                    if (!getClassContext().getJavaClass().getClassName().equals(ldcClazz)) {
                        reportBugThis(LpTypeEnum.LP_CHECK_LOGGER);
                    }
                }

            }
        }

        if (seen == GETSTATIC || seen == INVOKESTATIC) {
            if (getClassConstantOperand().equals("java/lang/System")
                    && (getNameConstantOperand().equals("out") || getNameConstantOperand().equals("err") || getNameConstantOperand()
                            .contains("gc"))) {
                reportBugThis(LpTypeEnum.LP_CHECK_SYSTEM_METHOD);
            }
        }
        
    }

    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
    
}
