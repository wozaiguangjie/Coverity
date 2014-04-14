package com.taobao.stc.findbugs.detect.lp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author wuhui 1.int a = getOrderNum(); 本类方法getOrderNum可能会返回null时 不允许这种代码出现
 *         2.Integer a = getOrderNum(); 本类方法getOrderNum可能会返回null时 未判空 直接对 a 操作
 *         前做过一次判空，则必须检测所有使用a的地方是否都做过判空
 */
public class LpNpeMethodCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    // 统计本类的方法个数
    private int method_num = 0;

    private int next_pc = -1;

    private String needcheck_method_sig = null;

    // 记录当前访问到第几个方法
    private Integer method_count = 0;


    private Map<String, BugInstance> bugMap = new HashMap<String, BugInstance>();


    public LpNpeMethodCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    // 用于记录本类中有可能return null的方法的序号
    private List<String> reCanNullMethod = new ArrayList<String>();

    private void initM() {
        try {
            JavaClass javaClass = getClassContext().getJavaClass();
            Method[] methods = javaClass.getMethods();
            method_num = methods.length;
        } catch (Exception e) {
        }
    }

    @Override
    public void visit(Code obj) {
        if (!LpCodeReviewRule.needCheck(getClassContext().getJavaClass())) {
            return;
        }
        if (method_count == 0) {
            initM();
        }

        if (method_num == 0) {
            return;
        }

        method_count++;
        super.visit(obj);
        
        // 如果是访问完最后一个方法后 需要判断是否 报bug了
        if (method_num > 0 && method_count == method_num) {
            for (Iterator<Entry<String, BugInstance>> it = bugMap.entrySet().iterator(); it.hasNext();) {
                Entry<String, BugInstance> en = it.next();
                String index = en.getKey();
                BugInstance bug = en.getValue();
                if (reCanNullMethod.contains(index)) {
                    bugReporter.reportBug(bug);
                }
            }
            bugMap.clear();
            method_num = 0;
            method_count = 0;
        }
    }

    @Override
    public void sawOpcode(int seen) {

        try {
            if (seen == ACONST_NULL && getNextOpcode() == ARETURN) {
                // 如果该方法中有return null的情况 则记录下 这个方法的序号 以便后续做校验
                reCanNullMethod.add(getMethodName() + getMethod().getSignature());
            }
        } catch (Exception e) {
        }

        try {
            if ((seen == INVOKEVIRTUAL || seen == INVOKESTATIC) && getNextOpcode() == INVOKEVIRTUAL) {
                next_pc = getNextPC();
                needcheck_method_sig = getNameConstantOperand() + getSigConstantOperand();
            }
        } catch (Exception e) {
        }

        try {
            if (seen == INVOKEVIRTUAL && (getPrevOpcode(1) == INVOKEVIRTUAL || getPrevOpcode(1) == INVOKESTATIC)) {
                if ("java/lang/Integer".equals(getClassConstantOperand()) && getNameConstantOperand().equals("intValue")) {
                    if (next_pc > -1 && needcheck_method_sig != null && next_pc == getPC() && LpUtil.isStore(getNextOpcode())) {
                        // 代码中有 int a = getOrderNum(); 时准备预报bug实例
                        bugMap.put(needcheck_method_sig, createBug());
                    }
                }
            }
        } catch (Exception e) {
        }

    }

    private BugInstance createBug() {
        BugInstance bug = new BugInstance(this, LpTypeEnum.LP_NPE_METHOD_CHECK_1, HIGH_PRIORITY).addClassAndMethod(this)
                .addSourceLine(this, getPC());
        bug.addInt(getPC());
        return bug;
    }

}
