
package com.taobao.stc.findbugs.detect.lp;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

/**
 * @author wuhui 良无限编码规则
 */
public class LpCodeReviewRule {

    /**
     * 检查是否生成SerialUID
     * @param interfaceName
     * @param cls
     * @return
     */
    public static boolean checkSerial(String interfaceName,JavaClass cls) {
        if (interfaceName.contains("Serializable") && !cls.isEnum()) {
            boolean haveSerialVersionUID = false;
            if (cls.getFields() != null) {
                for (Field f : cls.getFields()) {
                    if (f.getName().contains("serialVersionUID")) {
                        haveSerialVersionUID = true;
                        break;
                    }
                }
            }
            // 如果没有生成serialVersionUID 则报错
            return haveSerialVersionUID;
        }
        return Boolean.TRUE;
    }
    
    public static boolean needCheck(JavaClass source) {
        String fileName = source.getFileName();
        if (fileName.contains("test") || fileName.contains("Test")) {
            return false;
        }
        return true;
    }

}
