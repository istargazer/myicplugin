package com.github.istargazer.myicplugin.entity;

import java.util.List;

/**
* 解析java类内容
 */
public class JavaClassInfo {
    private String packageName;
    private String fullClassName;
    private List<JavaMethodInfo> methodInfoList;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public List<JavaMethodInfo> getMethodInfoList() {
        return methodInfoList;
    }

    public void setMethodInfoList(List<JavaMethodInfo> methodInfoList) {
        this.methodInfoList = methodInfoList;
    }

    @Override
    public String toString() {
        return "JavaClassInfo{" +
                "packageName='" + packageName + '\'' +
                ", fullClassName='" + fullClassName + '\'' +
                ", methodInfoList=" + methodInfoList +
                '}';
    }
}
