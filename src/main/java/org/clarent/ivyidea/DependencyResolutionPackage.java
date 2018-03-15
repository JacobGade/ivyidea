package org.clarent.ivyidea;

import com.intellij.openapi.module.Module;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.clarent.ivyidea.resolve.problem.ResolveProblem;

import java.util.List;

public class DependencyResolutionPackage {
    private final Module module;
    private final List<ResolvedDependency> dependencies;
    private final List<ResolveProblem> problems;
    private final long resolveTime;
    private final long extractDependenciesTime;

    public DependencyResolutionPackage(Module module,
                                       List<ResolvedDependency> dependencies,
                                       List<ResolveProblem> problems, long resolveTime, long extractDependenciesTime) {
        this.module = module;
        this.dependencies = dependencies;
        this.problems = problems;
        this.resolveTime = resolveTime;
        this.extractDependenciesTime = extractDependenciesTime;
    }

    public List<ResolvedDependency> getDependencies() {
        return dependencies;
    }

    public List<ResolveProblem> getProblems() {
        return problems;
    }

    public Module getModule() {
        return module;
    }

    public long getResolveTime() {
        return resolveTime;
    }

    public long getExtractDependenciesTime() {
        return extractDependenciesTime;
    }
}
