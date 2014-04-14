package com.taobao.stc.findbugs.detect.lp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.bcel.Constants;
import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.charsets.UTF8;

/**
 * @author wuhui
 */
public class LpUtil implements Constants {

    public static boolean isBlank(String str) {
        if ("null".equals(str)) {
            return true;
        }
        return StringUtils.isBlank(str);
    }

    
    /**
     * 引用类型的store
     */
    public static boolean isStoreA(int seen) {
        if (seen == ASTORE || (seen >= ASTORE_0 && seen <= ASTORE_3 )) {
            return true;
        }
        return false;
    }
    
    /**
     * 引用类型的load
     */
    public static boolean isLoadA(int seen) {
        if (seen == ALOAD || (seen >= ALOAD_0 && seen <= ALOAD_3 )) {
            return true;
        }
        return false;
    }
    
    private static final List<Short> mulOrDivList = Arrays.asList(IMUL,LMUL,FMUL,DMUL,IDIV,LDIV,FDIV,DDIV);
    /**
     *  是否乘 除法 操作
     */
    public static boolean isMulOrDiv(int seen) {
        return mulOrDivList.contains(Short.valueOf(String.valueOf(seen)));
    }
    
    public static boolean isStore(int seen) {
        if (seen >= ISTORE && seen <= SASTORE) {
            return true;
        }
        return false;
    }

    public static boolean isLoad(int seen) {
        if (seen >= ILOAD && seen <= SALOAD) {
            return true;
        }

        return false;
    }

    /**
     * 判断 能拿到 方法体内 作用域起止(如:if { }) 范围的助记符操作
     * 
     * @param seen
     * @return
     */
    public static boolean haveBranchTarget(int seen) {
        if ((seen == GOTO) || (seen == GOTO_W) || (seen == IF_ACMPEQ) || (seen == IF_ACMPNE) || (seen == IF_ICMPEQ)
                || (seen == IF_ICMPGE) || (seen == IF_ICMPGT) || (seen == IF_ICMPLE) || (seen == IF_ICMPLT)
                || (seen == IF_ICMPNE) || (seen == IFEQ) || (seen == IFGE) || (seen == IFGT) || (seen == IFLE) || (seen == IFLT)
                || (seen == IFNE) || (seen == IFNONNULL) || (seen == IFNULL)) {
            return true;
        }
        return false;
    }

    public static void writerLog(Object... str) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("d:\\123000.txt"), true),
                    UTF8.charset));
            for (Object temp : str) {
                writer.write(String.valueOf(temp));
                writer.write("\r\n");
            }
        } catch (Exception e) {
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

}
