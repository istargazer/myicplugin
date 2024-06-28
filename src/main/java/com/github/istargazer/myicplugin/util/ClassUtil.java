package com.github.istargazer.myicplugin.util;

import com.github.istargazer.myicplugin.entity.JavaClassInfo;
import com.github.istargazer.myicplugin.entity.JavaMethodInfo;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClassUtil {

    private static final FileType JAVA_FILE_TYPE = JavaFileType.INSTANCE;

    /**
     * 判断是否为java文件
     * @param file psi对象
     * @return true/false
     */
    public static boolean isJavaFile(PsiFile file) {
        return Objects.nonNull(file) && JAVA_FILE_TYPE.equals(file);
    }

    /**
     * 获取当前方法
     * @param editor 编辑器
     * @param psiFile 当前文件
     * @return 方法
     */
    public static PsiMethod getCurrentMethod(Editor editor, PsiFile psiFile) {
        PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
        return PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    }

    private static JavaClassInfo parseClass(PsiJavaFile file) {
        JavaClassInfo result = new JavaClassInfo();
        result.setPackageName(file.getPackageName());
        result.setFullClassName(file.getPackageName() + "." + file.getName());
        return result;
    }

    private static List<JavaMethodInfo> generateMethodInfo(PsiJavaFile file) {
        List<JavaMethodInfo> result = new ArrayList<>();
        PsiClass[] classes = file.getClasses();
        for (PsiClass clazz : classes) {
            PsiMethod[] methods = clazz.getAllMethods();
            for (PsiMethod m : methods) {
                if (!m.getModifierList().hasModifierProperty(PsiModifier.PRIVATE)) {
                    result.add(createJavaMethodInfo(m, false));
                }
            }
        }
        return result;
    }

    private static JavaMethodInfo createJavaMethodInfo(PsiMethod method, boolean needReference) {
        JavaMethodInfo result = new JavaMethodInfo();
        result.setModifier(method.getModifierList().getText());
        result.setReturnType(method.getReturnType().getPresentableText());
        result.setName(method.getName());
        result.setParameters(method.getParameterList().getText());
        result.setMethod(method);
        if (needReference) {
            result.setReferences(resolveReference(method));
        }
        return result;
    }

    private static List<PsiJavaFile> resolveReference(PsiMethod method) {
        List<PsiJavaFile> result = new ArrayList<>();

        return result;
    }

    private static List<PsiJavaFile> findCurrentModuleImportsInFile(PsiJavaFile file, final Module module) {
        // 获取引入包列表
        PsiImportList importList = file.getImportList();
        if (Objects.nonNull(importList)) {
            PsiImportStatementBase[] imports = importList.getAllImportStatements();
            return Arrays.stream(imports)
                    .map(ClassUtil::getPsiJavaFileFromReference)
                    .filter(f -> Objects.nonNull(f) && checkFileInModule(module, f.getVirtualFile()))
                    .toList();
        }
        return new ArrayList<>();
    }

    private static PsiJavaFile getPsiJavaFileFromReference(PsiImportStatementBase importStatement) {
        PsiJavaCodeReferenceElement reference = importStatement.getImportReference();
        if (Objects.nonNull(reference)) {
            PsiElement pe = reference.resolve();
            if (Objects.nonNull(pe)) {
                PsiFile pf = pe.getContainingFile();
                if (JAVA_FILE_TYPE.equals(pf.getFileType())) {
                    return (PsiJavaFile) pf;
                }
            }
        }
        return null;
    }

    /**
     * 获取当前文件所在的模块
     * @param file 文件
     * @return 模块
     */
    private static Module getCurrentModuleByFile(PsiFile file) {
        return ModuleUtil.findModuleForFile(file);
    }

    private static boolean checkFileInModule(Module module, VirtualFile vFile) {
        return ModuleUtil.isModuleFile(module, vFile);
    }
}
