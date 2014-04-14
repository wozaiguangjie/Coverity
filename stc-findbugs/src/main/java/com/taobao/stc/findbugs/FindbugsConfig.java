package com.taobao.stc.findbugs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import edu.umd.cs.findbugs.Priorities;

public class FindbugsConfig {
	
	private Logger logger;
	
	private List<File> workspaces = new ArrayList<File>();
	
	private Map<String, String> javaClassMap;
	
	private int minimumPriority = Priorities.NORMAL_PRIORITY;
	
	private List<String> reports = new ArrayList<String>();
	
	private List<Integer> ruleIds;
	

	public FindbugsConfig(List<File> dirs,Map<String, String> javaClassMap,List<Integer> ruleIds, Logger logger) {
		this.workspaces = dirs;
		this.javaClassMap = javaClassMap;
		this.ruleIds = ruleIds;
		this.logger = logger;
	}

	public List<File> getWorkspaces() {
		return workspaces;
	}

	public int getMinimumPriority() {
		return minimumPriority;
	}

	public void setMinimumPriority(int minimumPriority) {
		this.minimumPriority = minimumPriority;
	}

	public String getJavaFilePath(String path) {
		return javaClassMap.get(path);
	}

	public Logger getLogger() {
		return logger;
	}
	
	public void showClassMap(){
		logger.info("java class map:");
		for (String key : javaClassMap.keySet()) {
			logger.info(key+":"+javaClassMap.get(key));
		}
	}

	public List<String> getReports() {
		return reports;
	}

	public List<Integer> getRuleIds() {
		return ruleIds;
	}


}
	