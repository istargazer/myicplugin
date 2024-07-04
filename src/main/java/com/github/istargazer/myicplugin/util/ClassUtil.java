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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClassUtil {

    private static final FileType JAVA_FILE_TYPE = JavaFileType.INSTANCE;

    /**
     * 判断是否为java文件
     * @param file psi对象
     * @return true/false
     */
    public static boolean isJavaFile(PsiFile file) {
        return Objects.nonNull(file) && JAVA_FILE_TYPE.equals(file.getFileType());
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

    /**
     * 解析java类信息
     * @param file 文件
     * @return 类信息
     */
    public static JavaClassInfo parseClass(PsiJavaFile file) {
        JavaClassInfo result = new JavaClassInfo();
        result.setPackageName(file.getPackageName());
        result.setName(file.getName());
        result.setFullClassName(file.getPackageName() + "." + file.getName());
        result.setMethodInfoList(generateMethodInfo(file));
        return result;
    }

    /**
     * 解析类中的方法,不解析private类型方法
     * @param file 类文件
     * @return 方法列表
     */
    private static List<JavaMethodInfo> generateMethodInfo(PsiJavaFile file) {
        List<JavaMethodInfo> result = new ArrayList<>();
        PsiClass[] classes = file.getClasses();
        for (PsiClass clazz : classes) {
            PsiMethod[] methods = clazz.getAllMethods();
            for (PsiMethod m : methods) {
                // 不解析private类型方法
                if (!m.getModifierList().hasModifierProperty(PsiModifier.PRIVATE)) {
                    result.add(createJavaMethodInfo(m));
                }
            }
        }
        return result;
    }

    /**
     * 对方法进行解析，只获取当前方法的内容，不获取当前方法的引用
     * @param method 方法
     * @return 方法信息
     */
    public static JavaMethodInfo createJavaMethodInfo(PsiMethod method) {
        return createJavaMethodInfo(method, false);
    }

    /**
     * 解析方法
     * @param method 方法
     * @param needReference 是否解析方法包含的依赖类
     * @return 方法信息
     */
    public static JavaMethodInfo createJavaMethodInfo(PsiMethod method, boolean needReference) {
        JavaMethodInfo result = new JavaMethodInfo();
        result.setModifier(method.getModifierList().getText());
        PsiType psiType = method.getReturnType();
        if (Objects.nonNull(psiType)) {
            result.setReturnType(psiType.getPresentableText());
        } else {
            // 没有返回类型时，设置返回类型为 void
            result.setReturnType("void");
        }
        result.setName(method.getName());
        result.setParameters(method.getParameterList().getText());
        result.setSignature(String.format("%s %s %s %s",result.getModifier(), result.getReturnType(), result.getName(), result.getParameters()));
        result.setMethod(method);
        // 返回方法体内容，只包含大括号及内部内容
        if (Objects.nonNull(method.getBody())) {
            result.setBody(method.getBody().getText());
        } else {
            // 整个方法的内容，包含方法签名
            result.setBody(method.getText());
        }
        // 解析方法的依赖类
        if (needReference) {
            result.setReferences(resolveReference(method));
        }
        return result;
    }

    /**
     * 获取当前方法的依赖类
     * @param method 方法
     * @return 依赖java类
     */
    private static List<PsiJavaFile> resolveReference(PsiMethod method) {
        final List<PsiJavaFile> result = new ArrayList<>();
        final PsiFile psiFile = method.getContainingFile();
        // 获取当前方法所在类的模块
        final Module currentModule = getCurrentModuleByFile(psiFile);
        // 遍历方法中的所有依赖关系
        method.accept(new JavaRecursiveElementVisitor() {

            @Override
            public void visitReferenceElement(@NotNull PsiJavaCodeReferenceElement reference) {
                super.visitReferenceElement(reference);
                PsiElement element = reference.resolve();
                if (Objects.nonNull(element)) {
                    // 获取依赖的类
                    PsiFile file = element.getContainingFile();
                    // 保留是java类文件，且是当前模块内的文件
                    if (isJavaFile(file) && checkFileInModule(currentModule, file.getVirtualFile())) {
                       result.add((PsiJavaFile) file);
                    }
                }
            }
        });
        // 对依赖去重，过滤掉空对象和当前类
        return result.stream().distinct().filter(f -> Objects.nonNull(f) && !psiFile.equals(f)).toList();
    }

    /**
     * 通过方法中的引用的类，获取引用类的信息
     * @param method 方法
     * @return 引用类信息
     */
    public static List<JavaClassInfo> resolveMethodReferences(JavaMethodInfo method) {
        List<PsiJavaFile> list = method.getReferences();
        if(Objects.nonNull(list)) {
            return list.stream().map(ClassUtil::parseClass).toList();
        }
        return new ArrayList<>();
    }

    /**
     * 解析文件中的import引用
     * @param file java文件
     * @return 引用文件信息列表
     */
    public static List<JavaClassInfo> findCurrentModuleImportsInFile(PsiFile file) {
        List<JavaClassInfo> result = new ArrayList<>();
        if (!isJavaFile(file)) {
            return result;
        }
        final Module currentModule = getCurrentModuleByFile(file);
        List<PsiJavaFile> files = findCurrentModuleImportsInFile((PsiJavaFile) file, currentModule);
        for (PsiJavaFile javaFile : files) {
           result.add(parseClass(javaFile));
        }
        return result;
    }

    /**
     * 获取当前文件中依赖的当前模块的类文件
     * @param file 当前文件
     * @param module 当前文件对应的模块
     * @return 引用文件列表
     */
    private static List<PsiJavaFile> findCurrentModuleImportsInFile(PsiJavaFile file, final Module module) {
        // 获取引入包列表
        PsiImportList importList = file.getImportList();
        if (Objects.nonNull(importList)) {
            // 获取导入包数组
            PsiImportStatementBase[] imports = importList.getAllImportStatements();
            return Arrays.stream(imports)
                    .map(ClassUtil::getPsiJavaFileFromReference)
                    .filter(f -> Objects.nonNull(f) && checkFileInModule(module, f.getVirtualFile()))
                    .toList();
        }
        return new ArrayList<>();
    }

    /**
     * 通过引用获取引用类
     * @param importStatement 引用关系
     * @return 引用的类
     */
    private static PsiJavaFile getPsiJavaFileFromReference(PsiImportStatementBase importStatement) {
        PsiJavaCodeReferenceElement reference = importStatement.getImportReference();
        if (Objects.nonNull(reference)) {
            // 解析引用关系
            PsiElement pe = reference.resolve();
            if (Objects.nonNull(pe)) {
                // 拿到引用对应的类文件
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

    /**
     * 检查当前类文件是否为指定模块中的文件
     * @param module 指定模块
     * @param vFile 类文件
     * @return 是/否
     */
    public static boolean checkFileInModule(Module module, VirtualFile vFile) {
        return ModuleUtil.moduleContainsFile(module, vFile, false);
    }
}