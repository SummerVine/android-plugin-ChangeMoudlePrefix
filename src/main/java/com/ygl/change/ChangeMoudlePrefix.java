package com.ygl.change;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class ChangeMoudlePrefix extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String modulePath = Messages.showInputDialog(project,
                "输入组件的路径(结尾不需要带/)",
                "请输入你组件项目的绝对路径",
                Messages.getQuestionIcon());
        String sourePrefix = Messages.showInputDialog(project,
                "输入资源前缀(不需要带_)",
                "请输入资源前缀名",
                Messages.getQuestionIcon());


        if (modulePath != null && modulePath.trim().length() > 0 && sourePrefix != null && sourePrefix.trim().length() > 0) {
            ChangePrefixImpl4Mac.startRenameTask(modulePath, sourePrefix);
            Messages.showMessageDialog(project,
                    modulePath + "的资源前缀" + sourePrefix + "修改完毕!",
                    "完成",
                    Messages.getInformationIcon());
        } else {
            Messages.showMessageDialog(project,
                    "必须传入组件路径和资源前缀名",
                    "关闭",
                    Messages.getWarningIcon());
        }

    }
}
