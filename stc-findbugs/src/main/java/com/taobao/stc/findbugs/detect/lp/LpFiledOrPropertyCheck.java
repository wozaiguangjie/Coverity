package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.visitclass.PreorderVisitor;

/**
 * @author wuHui class类成员属性等加载时的检查
 */
public class LpFiledOrPropertyCheck extends PreorderVisitor implements Detector {

    private BugReporter bugReporter;

    private JavaClass   cls;
    
    private int   flag_service_name = 0;
    
    private int   flag_dao_name = 0;

    public LpFiledOrPropertyCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }
    
    public void visitClassContext(ClassContext classContext) {
        
        if (!LpCodeReviewRule.needCheck(classContext.getJavaClass())) {
            return;
        }

        cls = classContext.getJavaClass();
        cls.accept(this);
       
        if (cls.getPackageName() == null || cls.getPackageName().length() == 0) {
            reportBugJavaClass(LpTypeEnum.LP_CHECK_PACKGENAME);
        }

        // 2.是否生成serialVersionUID
        // 3.接口命名检查
        if (cls.getInterfaceNames() != null && cls.getInterfaceNames().length > 0) {
            for (String str : cls.getInterfaceNames()) {
                // 2.是否生成serialVersionUID
                if (!LpCodeReviewRule.checkSerial(str, cls)) {
                    reportBugJavaClass(LpTypeEnum.LP_CHECK_SERIA);
                }
            }
        }
        
        
        if (cls.getInterfaceNames() != null && cls.getInterfaceNames().length == 1) {
            String[] instr = cls.getInterfaceNames();
            
            //检查1对1接口实现类的类名 是否以Impl结尾，抽象类不检查
            if (instr[0].endsWith("Service") && !cls.getClassName().endsWith("Impl") && !cls.isAbstract()) {
                flag_service_name = 1;
            }
            
            if (instr[0].endsWith("Service") && cls.getClassName().endsWith("Impl") && !cls.isAbstract()
                    && flag_service_name == 1) {
                flag_service_name = 2;
                reportBugJavaClass(LpTypeEnum.LP_CHECK_SERVICE_NAME);
            }
            
            if ((instr[0].endsWith("Dao") || instr[0].endsWith("DAO")) && !cls.getClassName().endsWith("Impl")
                    && !cls.isAbstract()) {
                flag_dao_name = 1;
            }
            
            if ((instr[0].endsWith("Dao") || instr[0].endsWith("DAO")) && cls.getClassName().endsWith("Impl")
                    && !cls.isAbstract() && flag_dao_name == 1) {
                flag_dao_name = 2;
                reportBugJavaClass(LpTypeEnum.LP_CHECK_SERVICE_CLASS_NAME);
            }
            
        }
        
        JavaClass[] superClasses = null;
        try {
            superClasses = cls.getSuperClasses();
        } catch (ClassNotFoundException e) {
        }
        if (superClasses != null && superClasses.length > 0) {
            for (JavaClass jc : superClasses) {
                if (jc.getInterfaceNames() != null && jc.getInterfaceNames().length > 0) {
                    for (String str : jc.getInterfaceNames()) {
                        // 2.是否生成serialVersionUID
                        if (!LpCodeReviewRule.checkSerial(str, cls)) {
                            reportBugJavaClass(LpTypeEnum.LP_CHECK_SERIA);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void visitField(Field obj) {
        
        if (!LpCodeReviewRule.needCheck(cls)) {
            return;
        }

        String type = obj.getType().toString();

        // 4.是否使用线程不安全对象
        try {
            if (type.equals("java.text.SimpleDateFormat")) {
                reportBugField(obj, LpTypeEnum.LP_CHECK_THREAD_SAFE_OBJ);
                return;
            }
        } catch (Exception e) {
            //ignore
        }

        // 7.检查ThreadLocal是否final static
        if (type.contains("ThreadLocal")) {
            if (!(obj.isStatic() && obj.isFinal())) {
                reportBugField(obj, LpTypeEnum.LP_CHECK_FINAL_STATIC_FIELD);
            }
            return;
        }

//        if (obj.isStatic() && obj.isPublic()) {
//            if (!obj.isFinal()) {
//                reportBugField(obj, LpTypeEnum.LP_CHECK_FINAL_STATIC_FIELD);
//            }
//        }

        // 8.检查Logger 是否使用sf4j的log
        //        if (type.endsWith(".Log") || type.endsWith(".Logger")) {
        //            if (!type.contains("slf4j") || !obj.isStatic() || !obj.isFinal()) {
        //                reportBugField(obj, LpTypeEnum.LP_CHECK_LOGGER);
        //            }
        //            return;
        //        }

    }

    private void reportBugField(Field obj, String type) {
        bugReporter.reportBug(new BugInstance(this, type, HIGH_PRIORITY).addClass(cls).addField(
                new FieldAnnotation(cls.getClassName(), obj.getName(), obj.getSignature(), obj
                        .isStatic())));
    }

    private void reportBugJavaClass(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClass(cls);
        bugReporter.reportBug(bug);
    }

    public void report() {
    }


}
