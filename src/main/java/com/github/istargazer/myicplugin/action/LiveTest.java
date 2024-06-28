package com.github.istargazer.myicplugin.action;

import com.github.istargazer.myicplugin.util.ClassUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LiveTest extends AnAction {



    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (ClassUtil.isJavaFile(psiFile)) {
            return;
        }
        // 当前文件对于的模块
        @Nullable Module module = ModuleUtil.findModuleForFile(psiFile);

        PsiJavaFile javaFile = (PsiJavaFile) psiFile;

        List<String> list = new ArrayList<>();
        Project project = e.getProject();

        PsiMethod method = ClassUtil.getCurrentMethod(Objects.requireNonNull(e.getData(CommonDataKeys.EDITOR)),psiFile);

        Query<PsiReference> query = ReferencesSearch.search(method);

        query.forEach();


        PsiReference[] references = method.getReferences();
        StringBuilder sb = new StringBuilder("references is ");
        for (PsiReference pr : references) {
            sb.append(pr.resolve().getContainingFile().getName());
        }
        list.add(sb.toString());



        Messages.showMessageDialog(project, Arrays.toString(list.toArray()), "Message", Messages.getInformationIcon());
    }


}
