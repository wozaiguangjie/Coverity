package com.taobao.stc.findbugs.detect.lp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 *         1.Integer a = getOrderNum(); 本类方法getOrderNum可能会返回null时 未判空 直接对 a 操作
 *         前做过一次判空，则必须检测所有使用a的地方是否都做过判空
 */
public class LpNpeMethodNullCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    private int store_next_pc1 = -1;
    
    private int mx = -1;

    /**
     * 用于记录作用域区间，因为判空的方法形式多样无法覆盖全，所以针对NPE 只要调用对象的方法行 处于某个作用域范畴就不报BUG
     */
    private Map<Integer, Integer> sectionMap = new HashMap<Integer, Integer>();

    /**
     * 用于方法 对应 的store，key是store值, value为name
     */
    private Map<Integer, String> bugMap1 = new HashMap<Integer, String>();

    /**
     * 用于临时记录store时pc 对应的上次操作的方法的名称
     */
    private Map<Integer, String> bugMap_temp = new HashMap<Integer, String>();

    /**
     * key为load 的pc +"|"+ index， value是BugInstance
     */
    private Map<String, BugInstance> bugMap2 = new HashMap<String, BugInstance>();

    public LpNpeMethodNullCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    // 用于记录本类中有可能return null的方法的序号
    private Set<String> reCanNullMethod = new HashSet<String>();

    @Override
    public void visit(Code obj) {
        if (!LpCodeReviewRule.needCheck(getClassContext().getJavaClass())) {
            return;
        }
        if (mx == -1) {
            try {
                JavaClass javaClass = getClassContext().getJavaClass();
                Method[] methods = javaClass.getMethods();
                for (Method m : methods) {
                    Code code = m.getCode();
                    String m1 = m.getName() + m.getSignature();
                    if (code != null && code.toString().contains("aconst_null\n")) {
                        String[] str = code.toString().split("aconst_null\n");
                        for (String s : str) {
                            String substring = s.substring(2, 15);
                            if (substring.contains("areturn")) {
                                reCanNullMethod.add(m1);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                return;
            } finally {
                mx++;
            }
        }

        super.visit(obj);
        bugReport();
    }

    private void bugReport() {
        try {
            for (Iterator<Entry<String, BugInstance>> it = bugMap2.entrySet().iterator(); it.hasNext();) {
                Entry<String, BugInstance> en = it.next();
                String pc_index = en.getKey();
                BugInstance bug = en.getValue();
                String[] split = pc_index.split("@");

                int pc = Integer.valueOf(split[0]);
                Integer index = Integer.valueOf(split[1]);

                if (bugMap1.get(index) != null && reCanNullMethod.contains(bugMap1.get(index))) {
                    // 如果load时调用的方法时曾经store过的方法 且会return null的
                    // 那么要继续做检查，判断load的PC值是否处于某个作用域
                    boolean repotbug = true;
                    for (Iterator<Entry<Integer, Integer>> ite = sectionMap.entrySet().iterator(); ite.hasNext();) {
                        Entry<Integer, Integer> ey = ite.next();
                        int s = ey.getKey();
                        int e = ey.getValue();
                        if (pc >= s && pc <= e) {
                            // 只要有一个作用域囊括了它那就不用报bug
                            repotbug = false;
                            break;
                        }
                    }
                    if (repotbug) {
                        bugReporter.reportBug(bug);
                    }
                }
            }
        } catch (NumberFormatException e) {
        }
        
        bugMap1.clear();
        bugMap2.clear();
        sectionMap.clear();
        bugMap_temp.clear();
    }

    @Override
    public void sawOpcode(int seen) {
        // ------1.把所有方法操作缓存起来
        try {
            if (seen >= INVOKEVIRTUAL && seen <= INVOKEINTERFACE && LpUtil.isStoreA(getNextOpcode())) {
                String key = getNameConstantOperand() + getSigConstantOperand();
                store_next_pc1 = getNextPC();
                bugMap_temp.put(store_next_pc1, key);
            }
        } catch (Exception e) {
        }

        // ------2.把所有方法操作后的store的序号 缓存起来
        try {
            // 7: invokevirtual #17; //Method getB:()Ljava/lang/Integer;
            // 10: astore_3 用于存储 3
            if (getPC() == store_next_pc1 && LpUtil.isStoreA(seen)
                    && (getPrevOpcode(1) >= INVOKEVIRTUAL && getPrevOpcode(1) <= INVOKEINTERFACE)) {
                bugMap1.put(getRegisterOperand(), bugMap_temp.get(getPC()));
                bugMap_temp.clear();
            }
        } catch (Exception e) {
        }

        try {
            // 3. 把load的 3存起来 ,并创建预报bug实例 ，方便后续比较
            // 26: aload_3
            // 27: invokevirtual #21; //Method
            // java/lang/Integer.toString:()Ljava/lang/String;
            if (LpUtil.isLoadA(seen) && (getNextOpcode() >= INVOKEVIRTUAL && getNextOpcode() <= INVOKEINTERFACE)) {
                bugMap2.put(getPC() + "@" + getRegisterOperand(), createBug());
            }
        } catch (Exception e) {
        }

        // 4. 把所有方法体内的作用域缓存起来
        if (LpUtil.haveBranchTarget(seen)) {
            int start = getBranchFallThrough();// 作用域开始处
            int end = getBranchTarget();
            sectionMap.put(start, end);
        }

    }

    private BugInstance createBug() {
        BugInstance bug = new BugInstance(this, LpTypeEnum.LP_NPE_METHOD_CHECK_2, HIGH_PRIORITY).addClassAndMethod(this)
                .addSourceLine(this, getPC());
        bug.addInt(getPC());
        return bug;
    }

}
