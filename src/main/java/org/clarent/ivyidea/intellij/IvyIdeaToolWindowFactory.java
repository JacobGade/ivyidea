package org.clarent.ivyidea.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class IvyIdeaToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Disposer.register(toolWindow.getDisposable(), console);
        Content content = ServiceManager.getService(ContentFactory.class)
                                        .createContent(console.getComponent(), "Console", true);
        toolWindow.getContentManager().addContent(content);
    }

    public static ConsoleView getConsole(Project project, ToolWindow toolWindow){
        CompletableFuture<ConsoleView> viewFuture = new CompletableFuture<>();
        ApplicationManager.getApplication()
                          .invokeAndWait(() ->{
                              final ContentManager contentManager = toolWindow.getContentManager();
                              if(contentManager.getContentCount() == 0) {
                                  new IvyIdeaToolWindowFactory().createToolWindowContent(project, toolWindow);
                              }
                              viewFuture.complete((ConsoleView) contentManager.getContent(0).getComponent());
                          });
        return viewFuture.join();
    }
}
