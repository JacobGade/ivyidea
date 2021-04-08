package org.clarent.ivyidea.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class IvyIdeaToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Disposer.register(toolWindow.getDisposable(), console);
        Content content = ServiceManager.getService(ContentFactory.class)
                                        .createContent(console.getComponent(), "Console", true);
        toolWindow.getContentManager().addContent(content);
    }

    public static ConsoleView getConsole(ToolWindow toolWindow){
        return (ConsoleView) toolWindow.getContentManager().getContent(0).getComponent();
    }
}
