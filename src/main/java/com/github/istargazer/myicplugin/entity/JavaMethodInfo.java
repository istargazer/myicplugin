package com.github.istargazer.myicplugin.entity;

import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.List;

/**
 * 解析java方法内容
 */
public class JavaMethodInfo {
    private String modifier;
    private String returnType;
    private String name;
    private String parameters;
    private String body;

    private PsiMethod method;

    private List<PsiJavaFile> references;

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public PsiMethod getMethod() {
        return method;
    }

    public void setMethod(PsiMethod method) {
        this.method = method;
    }

    public List<PsiJavaFile> getReferences() {
        return references;
    }

    public void setReferences(List<PsiJavaFile> references) {
        this.references = references;
    }
}
