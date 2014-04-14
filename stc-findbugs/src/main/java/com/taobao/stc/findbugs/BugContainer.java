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

package com.taobao.stc.findbugs;

import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.TextUIBugReporter;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;

/**
 * @author zhangjin.jsf
 */
public class BugContainer extends TextUIBugReporter {
	
	protected final HashSet<BugInstance> seenAlready = new HashSet<BugInstance>();

    @Override
    protected void doReportBug(BugInstance bugInstance) {
        if (seenAlready.add(bugInstance)) {
            //printBug(bugInstance);
            notifyObservers(bugInstance);
        }
    }

    @Override
    public void reportQueuedErrors() {
        //ignore it
    }
    
    


    public Set<BugInstance> getAllBugs(){
        return seenAlready;
    }

	@Override
	public void finish() {
		
	}

	@Override
	public BugCollection getBugCollection() {
		return null;
	}

	@Override
	public void observeClass(ClassDescriptor classDescriptor) {
		
	}


}
