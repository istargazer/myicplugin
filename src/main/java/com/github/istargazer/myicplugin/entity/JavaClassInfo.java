package com.github.istargazer.myicplugin.entity;

import java.util.List;

/**
* 解析java类内容
 */
public class JavaClassInfo {
    /**
     * class package name
     */
    private String packageName;
    /**
     * class name
     */
    private String name;
    /**
     * class package name + . + class name
     */
    private String fullClassName;
    /**
     *
     */
    private List<JavaMethodInfo> methodInfoList;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                ", name='" + name + '\'' +
                ", fullClassName='" + fullClassName + '\'' +
                ", methodInfoList=" + methodInfoList +
                '}';
    }
}
