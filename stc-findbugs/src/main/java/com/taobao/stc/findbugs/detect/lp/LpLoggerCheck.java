package com.taobao.stc.findbugs.detect.lp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * @author wuhui
 * 
 *检测log使用是否规范,必须先判断相应log是否打开
 * 
 */
public class LpLoggerCheck extends LpAbstractOpcodeStackDetector {
    BugReporter bugReporter;

    private int seenGuardClauseAt = -1;

    private int logBlockStart = 0;

    private int logBlockEnd = 0;
    
    private String logName = null;
    
    //用于存放方法的ex作用起 止域 key=start value=end 作用域是 >=start < end
    private Map<Integer, Integer> exhandler = null;
    
    private LocalVariableTable curMethodlvt;
    
    private Map<Integer, Integer> haveExLog = null;
    

    public LpLoggerCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code obj) {
        seenGuardClauseAt = -1;
        logBlockStart = 0;
        logBlockEnd = 0;
        
        Method m = getMethod();
        if (m != null) {
            curMethodlvt = m.getLocalVariableTable();
            if (curMethodlvt != null) {
                exhandler = new HashMap<Integer, Integer>();
                haveExLog = new HashMap<Integer, Integer>();
                for (LocalVariable lv : curMethodlvt.getLocalVariableTable()) {
                    
                    // 记录catch块的程序计数器范围
                    if(lv.getSignature().endsWith("Exception;")){
//                        if(lv.getSignature().contains("Exception")){
                        exhandler.put(lv.getStartPC(), lv.getStartPC()+lv.getLength());
                    }
                }
            }
        }
        
        super.visit(obj);
        seenGuardClauseAt = -1;
        logBlockStart = 0;
        logBlockEnd = 0;
        logName = null;
        exhandler = null;
        haveExLog = null;
    }
    
    @Override
    public void sawOpcode(int seen) {
        if (seen == INVOKEVIRTUAL || seen == INVOKEINTERFACE) {
            if (getNameConstantOperand().contains("isInfoEnabled") || getNameConstantOperand().contains("isDebugEnabled")
                    || getNameConstantOperand().contains("isWarnEnabled")) {
                seenGuardClauseAt = getPC(); //程序计数器的值
                logName = getNameConstantOperand().toLowerCase();
                return;
            }
        }

        if (seen == IFEQ && seenGuardClauseAt != -1 && (getPC() >= seenGuardClauseAt + 3 && getPC() < seenGuardClauseAt + 7)) {
            logBlockStart = getBranchFallThrough();// if代码块开始处 的程序计数器
            logBlockEnd = getBranchTarget();
            return;
        }
        
        // 检测 catch块中throw new Exception("xx");没有将异常抛出
        if(seen == INVOKESPECIAL){
            if (getClassConstantOperand().equals("java/lang/Exception")&&getNameConstantOperand().equals("<init>")){
                // methodSig应该是 (Ljava/lang/String;Ljava/lang/Throwable;)V
                String methodSig = getSigConstantOperand();
                if (StringUtils.isNotBlank(methodSig) && methodSig.indexOf("Throwable") < 0 && exhandler != null) {
                    for (Iterator<Entry<Integer, Integer>> it = exhandler.entrySet().iterator(); it.hasNext();) {
                        Entry<Integer, Integer> ey = it.next();
                        int start = ey.getKey();
                        int end = ey.getValue();
                        // 是否位于catch块中
                        if (getPC() >= start && getPC() < end) {
                            reportBug(LpTypeEnum.LP_CHECK_LOGGER_3);
                            break;
                        }
                    }
                }
            }
        }

        if (seen == INVOKEVIRTUAL || seen == INVOKEINTERFACE) {
            // 输出日志时是否只输出e,而不输出任何提示信息
            if (getNameConstantOperand().equals("error")) {
                try {
                    String methodSig = getSigConstantOperand();
                    
                    if (StringUtils.isNotBlank(methodSig)
                            && (methodSig.indexOf("Ljava/lang/Object") < 0 && methodSig.indexOf("Ljava/lang/String") < 0)) {
                        reportBug(LpTypeEnum.LP_CHECK_LOGGER);
                    }
                    
                    if (StringUtils.isNotBlank(methodSig) && methodSig.indexOf("Throwable") < 0 && exhandler != null) {
                        for (Iterator<Entry<Integer, Integer>> it = exhandler.entrySet().iterator(); it.hasNext();) {
                            Entry<Integer, Integer> ey = it.next();
                            int start = ey.getKey();
                            int end = ey.getValue();
                            if (getPC() >= start && getPC() < end) {
                                //如果该catch块 一直没有打出过堆栈信息  则报bug
                                if(haveExLog.get(start)==null){
                                    reportBug(LpTypeEnum.LP_CHECK_LOGGER_2);
                                    break;
                                }
                            }
                        }
                    }
                    
                    //记录下catch块中已经输出过e 的start 和 end 范围
                    if (StringUtils.isNotBlank(methodSig) && methodSig.indexOf("Throwable") > 0 && exhandler != null) {
                        for (Iterator<Entry<Integer, Integer>> it = exhandler.entrySet().iterator(); it.hasNext();) {
                            Entry<Integer, Integer> ey = it.next();
                            int start = ey.getKey();
                            int end = ey.getValue();
                            if (getPC() >= start && getPC() < end) {
                                haveExLog.put(start, end);
                                break;
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    // ignore
                }
                return;
            }

            if (getNameConstantOperand().equals("warn") || getNameConstantOperand().equals("info")
                    || getNameConstantOperand().equals("debug")) {

                // 是否已判断日志级别
                if (getPC() > logBlockStart && getPC() <= logBlockEnd+1) {
                    //判断是否 匹配logger.isWarnEnalbed(){   logger.info(....);
                    if (logName != null && !logName.contains(getNameConstantOperand())) {
                        reportBug(LpTypeEnum.LP_CHECK_LOGGER_1);
                        logName = null;
                    }
                    
                } else {
                    int nextOpcode = -1;
                    try{
                        nextOpcode =  getNextOpcode() ;
                    }catch(Exception e){
                    }
                    //解决如下场景
                    /*
                     *  for (String syncOrderConfig : syncOrderConfigList) {
                            if (taskManagedMap.containsKey("bbbb")) {
                                if (logger.isInfoEnabled()) {
                                    logger.info("platform:"+syncOrderConfig);
                                }
                                continue;//加了continue时会出现一种特殊的字节码情况，此种情况不报bug
                            } 
                            taskManagedMap.remove("aaaa");
                        }
                     */
                    if (nextOpcode!= -1 && nextOpcode != GOTO) {
                        reportBug(LpTypeEnum.LP_CHECK_LOGGER);
                        seenGuardClauseAt = -1;
                        logBlockStart = 0;
                        logBlockEnd = 0;
                        logName = null;
                    }
                }

            }
        }
    }
    
    private void reportBug(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this)
                .addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
    

}
