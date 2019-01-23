package org.jenkinsci.plugins.fitnesse.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import hudson.FilePath;

/** Test utilities */
public final class TestUtils
{

    /**
     * Collects and reads the output files present in a test Jenkins workspace
     * 
     * @param filenameOutputFormat configured output filename naming pattern
     * @param workspace Jenkins workspace
     * @return a map that associates each generated filename to its content
     * @throws IOException
     */
    public static Map<String, String> gatherWorkspaceOutputFiles(final String filenameOutputFormat, final FilePath workspace) throws IOException
    {
        final Function<String, String> readFile = filename -> {
            final Path absoluteFilename = Paths.get(workspace.getRemote(), filename);

            try (Stream<String> lines = Files.lines(absoluteFilename))
            {
                return lines.collect(Collectors.joining());
            } catch (final IOException e)
            {
                throw new RuntimeException("Error collecting build output files", e);
            }
        };

        return Files
                .list(Paths.get(workspace.getRemote()))
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(filename -> filename.matches(filenameOutputFormat.replace("%s", ".*")))
                .collect(Collectors.toMap(Function.identity(), readFile));
    }

    /**
     * Retrieves the workspace of a workflow run
     * 
     * @param run the run from which the workspace should be retrieved
     * @return the workspace that was used for this run, or {@code null} if not found
     */
    public static FilePath getWorkspace(final WorkflowRun run)
    {
        final FlowExecution execution = run.getExecution();

        if (execution == null)
        {
            return null;
        }

        final FlowGraphWalker walker = new FlowGraphWalker(execution);

        for (final FlowNode flowNode : walker)
        {
            if (flowNode instanceof StepStartNode)
            {
                final WorkspaceAction action = flowNode.getAction(WorkspaceAction.class);
                if (action != null)
                {
                    return action.getWorkspace();
                }
            }
        }

        return null;
    }

    private TestUtils()
    {

    }
}
