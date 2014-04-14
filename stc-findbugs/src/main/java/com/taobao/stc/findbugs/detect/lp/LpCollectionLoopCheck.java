package com.taobao.stc.findbugs.detect.lp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * 1.检查 collection 作为入参 没判空就直接操作的NPE检查
 * 
 * @author wuHui
 */
public class LpCollectionLoopCheck extends LpAbstractBytecodeScanningDetector {

    BugReporter bugReporter;

    private LocalVariableTable curMethodlvt;
    
    /**
     * 当前方法的name+singure
     */
    private String nameSingure = null;

    /**
     * key = slot@name@length,数组0位置 为start  1位置为end 组合为作用域范围
     */
    private Map<String, Integer[]> map = null;

    public LpCollectionLoopCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        Method m = getMethod();
        if (m != null) {
            nameSingure = m.getName()+m.getSignature();
            map =  new HashMap<String, Integer[]>();
            curMethodlvt = m.getLocalVariableTable();
            if (curMethodlvt != null) {
                for (LocalVariable lv : curMethodlvt.getLocalVariableTable()) {
                    // 如果本地变量表中有使用过迭代器 则 记录 it的作用域
                    if ("Ljava/util/Iterator;".equals(lv.getSignature())) {
                        Integer[] ins = {lv.getIndex(),lv.getIndex()+lv.getLength()};
                        map.put(lv.getIndex()+"@"+lv.getName()+"@"+lv.getLength(), ins);
                    }

                }
            }
        }
        super.visit(obj);
    }
    
    private int slot =-1;
    private int nextPC =-1;
    private boolean flag =false;
    private Integer[] value = null;
    

    @Override
    public void sawOpcode(int seen) {
        
        try {
            if (getNextOpcode()== INVOKEINTERFACE && isRegisterLoad() || LpUtil.isLoad(seen)) {
                slot = getRegisterOperand();
                nextPC= getNextPC();
                return;
            }
        } catch (Exception e) {
        }
        
        
        try {
            if (INVOKEINTERFACE == seen && getPC() == nextPC) {
                String className = getClassConstantOperand();
                String methodName = getNameConstantOperand();
                if (className.equals("java/util/Iterator") && methodName.equals("remove")) {
                    for (Iterator<Entry<String, Integer[]>> it = map.entrySet().iterator(); it.hasNext();) {
                        Entry<String, Integer[]> en = it.next();
                        String key = en.getKey();
                        if (key.contains(slot + "@")) {
                            value = en.getValue();
                            //如果进行remove操作的it的临时变量 在迭代器 循环作用域内 则 需要进一步判断是否 接下去进行遍历操作
                            if (getPC() > value[0] && getPC() < value[1]) {
                                flag=true;
                            }
                        }
                    }
                }else{
                    slot = -1;
                    nextPC = -1;
                }
            }
        } catch (Exception e1) {
        }
        
        try {
            if (flag && (seen >= INVOKEVIRTUAL && seen <= INVOKEINTERFACE) && (getPC() > value[0] && getPC() < value[1])) {
                String methodName = getNameConstantOperand();
                String singure = getSigConstantOperand();
                
                if (nameSingure.equals((methodName + singure)) && getClassConstantOperand().equals(getClassName())) {
                    reportBugThis(LpTypeEnum.LP_LOOP_RECURSIVE_CHECK);
                    slot = -1;
                    nextPC = -1;
                    flag = false;
                    value = null;
                    return;
                }
            }
        } catch (Exception e1) {
        }
        
    }

    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
}
