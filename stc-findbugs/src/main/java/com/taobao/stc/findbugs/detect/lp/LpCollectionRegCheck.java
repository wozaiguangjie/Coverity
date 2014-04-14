package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.XField;

/**
 * 检查成员变量为 public static map/list/set
 * 
 * @author zhaoyuan.lizy
 */
public class LpCollectionRegCheck extends LpAbstractBytecodeScanningDetector {

    BugReporter bugReporter;

    private int flag = -1;

    private static String preClassName;

//    private JavaClass cls;

    public LpCollectionRegCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        flag = -1;
        if (STATIC_INITIALIZER_NAME.equals(getMethodName())) {
            super.visit(obj);
        }
        flag = -1;
    }

    /**
     * 每扫描一条字节码就会进入该方法
     * 
     * @param seen
     *            字节码的枚举值
     */
    @Override
    public void sawOpcode(int seen) {
        // 1、检测静态区是否存在 new map/list/set
        
        if (seen == INVOKESPECIAL) {
            try {
                String className = getClassConstantOperand();
                String methodName = getNameConstantOperand();

                if (className.equals("java/util/HashMap") || className.equals("java/util/HashSet")
                        || className.equals("java/util/ArrayList")) {

                    if ("<init>".equals(methodName)) {
                        if (PUTSTATIC == getNextOpcode()) {
                            flag = 1;
                        }
                    }
                }
            } catch (Exception e) {
            }
            return;
        }

        try {
            if (seen == PUTSTATIC && getPrevOpcode(1) == INVOKESPECIAL && flag == 1) {
                XField xField = getXFieldOperand();
                if (xField != null && xField.isPublic()) {
                    reportBugThis(LpTypeEnum.LP_CHECK_PUBLIC_COLLECTION_STATIC);
                    flag = -1;
                }
            }
        } catch (Exception e) {
        }

        // 2、检测静态区是否存在 Math或Random给final成员赋值
        // putstatic和getstatic对静态变量进行存取操作，如果putstatic操作的对象是final的，则将常量蜕变成变量
        try {
//            // 检测静态区是否存在 putstatic且该字段被public final修饰,基本类型封装类、枚举类除外
//            cls = getClassContext().getJavaClass();
//            // 2.1找到putstatic,该类不是枚举类，该类不是通过valueOf赋值
//            if (seen == PUTSTATIC && !cls.isEnum()&&!preMethodName.equals("valueOf")) {
//                XField xField = getXFieldOperand();
//                // 2.2如果是public且为final，则报bug
//                if (xField != null && xField.isPublic() && xField.isFinal()) {
//                    reportBugThis(LpTypeEnum.LP_CONSTANT_CHANGE_TO_VARIABLE);
//                }
//            }
            
            // 1.1、获取方法名称
            if(seen == INVOKESTATIC || seen == INVOKEVIRTUAL){
                preClassName = getClassConstantOperand();
            }
            
            // 限定只有math和random进行赋值时报错
            if (seen == PUTSTATIC && (getPrevOpcode(1) == INVOKESTATIC || getPrevOpcode(1) == INVOKEVIRTUAL)) {
                XField xField = getXFieldOperand();
                if ((preClassName.equals("java/lang/Math") || preClassName.equals("java/util/Random")) && xField != null
                        && xField.isPublic() && xField.isFinal()) {
                    reportBugThis(LpTypeEnum.LP_CONSTANT_CHANGE_TO_VARIABLE);
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
