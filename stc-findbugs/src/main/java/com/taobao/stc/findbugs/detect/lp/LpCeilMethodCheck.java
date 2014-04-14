/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.taobao.stc.findbugs.detect.lp;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

/**
 * 检查调用Math.ceil方法时传入的参数是否为int类型，当传入int类型参数时，执行结果等于参数值，调用无任何意义
 * 
 * @author alonso
 */
public class LpCeilMethodCheck extends LpAbstractOpcodeStackDetector {

    BugReporter bugReporter;

    public LpCeilMethodCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.umd.cs.findbugs.bcel.OpcodeStackDetector#sawOpcode(int)
     */
    @Override
    public void sawOpcode(int seen) {
        if (seen == INVOKESTATIC) {
            String claz = this.getClassConstantOperand();
            String method = this.getNameConstantOperand();
            if ("java/lang/Math".equals(claz) && "ceil".equals(method)) {
                int opCode = getPrevOpcode(1);
                if (I2D == opCode) {
                    this.reportBugThis(LpTypeEnum.LP_CHECK_MATH_CEIL);
                }
            }
        }
    }

    private void reportBugThis(String type) {
        BugInstance bug = new BugInstance(this, type, HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC());
        bug.addInt(getPC());
        bugReporter.reportBug(bug);
    }
}
