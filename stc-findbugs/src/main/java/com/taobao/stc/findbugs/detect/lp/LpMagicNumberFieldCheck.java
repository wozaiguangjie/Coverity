package com.taobao.stc.findbugs.detect.lp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.visitclass.PreorderVisitor;

/**
 * 检查非私有成员变量的魔术定义规则
 * 
 * @author zhaoyuan.lizy
 * @modify by Wuhui 2012-7-4 18:37
 */
public class LpMagicNumberFieldCheck extends PreorderVisitor implements Detector {

    private BugReporter bugReporter;

    private JavaClass cls;
    
    public LpMagicNumberFieldCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visitField(Field obj) {
        if (cls != null && !cls.isEnum()&&obj.isPublic()) {
            
            // 成员变量魔术检查
            if (checkDoublesChar(obj.getName())) {
                reportBugField(obj, LpTypeEnum.LP_MAGIC_NUMBER_FIELD_NAME);
            }
        }
    }

    public void visitClassContext(ClassContext classContext) {
        if (!LpCodeReviewRule.needCheck(classContext.getJavaClass())) {
            return;
        }
        cls = classContext.getJavaClass();
        cls.accept(this);

    }

    private void reportBugField(Field obj, String type) {
        bugReporter.reportBug(new BugInstance(this, type, HIGH_PRIORITY).addClass(cls).addField(
                new FieldAnnotation(cls.getClassName(), obj.getName(), obj.getSignature(), obj.isStatic())));
    }
    
    public void report() {
    }

    /**
     * 1.判断是否重复字符，单个字符也算重复 比如:String a = ""; 2.判断引用取名是否 前面字母连续重复后再以数字结尾，如：
     * String aaaa111 = "abc";
     */
    private static boolean checkDoublesChar(String str) {
        boolean result = false;
        if (str != null && str.length() >= 3) {
            int len = str.length();
            if (check_char_and_digit(str)) {
                int d_start = getIndexForDigitStart(str);
                str = str.substring(0, d_start);
                len = str.length();
            }
            char first_char = str.charAt(0);
            for (int i = 0; i < len; i++) {
                if (first_char == str.charAt(i)) {
                    result = true;
                } else {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获得连续字符中首次出现数字的下标
     */
    private static int getIndexForDigitStart(String str) {
        if (null != str && str.length() > 1) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                if (isNumeric(str.charAt(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private final static Pattern check_filed_name = Pattern.compile("^[a-zA-Z]+[0-9]+$");

    /**
     * 判断是否由字母+数字组合
     */
    private static boolean check_char_and_digit(String str) {
        if (str != null && str.length() > 1) {
            Matcher m = check_filed_name.matcher(str);
            return m.find();
        }
        return false;
    }

    private static boolean isNumeric(char ch) {
        return Character.isDigit(ch);
    }
    
//    public static void main(String[] args) {
//        System.out.println("char1111================>"+checkDoublesChar("AA"));
//        System.out.println("char1111================>"+checkDoublesChar("BBBBBB999999"));
//        System.out.println("char1111================>"+checkDoublesChar("a"));
//        System.out.println("char1111================>"+checkDoublesChar("aaaa999"));
//        System.out.println("char1111================>"+checkDoublesChar("zzzzzzzzz8"));
//        System.out.println("char1111================>"+checkDoublesChar("zzzzzz"));
//        System.out.println("char1111================>"+checkDoublesChar("z2222"));
//        System.out.println("char1111================>"+checkDoublesChar("abcsdf"));
//        System.out.println("char1111================>"+checkDoublesChar("kkkkA2222"));
//      }

}
