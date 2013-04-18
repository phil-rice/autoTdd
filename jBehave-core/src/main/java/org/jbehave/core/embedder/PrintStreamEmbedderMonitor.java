package org.jbehave.core.embedder;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.ReportsCount;

/**
 * Monitor that reports to a {@link PrintStream}, defaulting to
 * {@link System.out}
 */
public class PrintStreamEmbedderMonitor extends NullEmbedderMonitor {
    private PrintStream output;

    public PrintStreamEmbedderMonitor() {
        this(System.out);
    }

    public PrintStreamEmbedderMonitor(PrintStream output) {
        this.output = output;
    }

    @Override
	public void batchFailed(BatchFailures failures) {
        print("Failed to run batch " + failures);
    }

    @Override
	public void beforeOrAfterStoriesFailed() {
        print("Failed to run before or after stories steps");
    }

    @Override
	public void embeddableFailed(String name, Throwable cause) {
        print("Failed to run embeddable " + name);
        printStackTrace(cause);
    }

    @Override
	public void embeddableNotConfigurable(String name) {
        print("Embeddable " + name + " must be an instance of " + ConfigurableEmbedder.class);
    }

    @Override
	public void embeddablesSkipped(List<String> classNames) {
        print("Skipped embeddables " + classNames);
    }

    @Override
	public void metaNotAllowed(Meta meta, MetaFilter filter) {
        print(meta + " excluded by filter '" + filter.asString() + "'");
    }

    @Override
	public void runningEmbeddable(String name) {
        print("Running embeddable " + name);
    }

    @Override
	public void runningStory(String path) {
        print("Running story " + path);
    }

    @Override
	public void storyFailed(String path, Throwable cause) {
        print("Failed to run story " + path);
        printStackTrace(cause);
    }

    @Override
	public void storiesSkipped(List<String> storyPaths) {
        print("Skipped stories " + storyPaths);
    }

    @Override
	public void storiesNotAllowed(List<Story> stories, MetaFilter filter, boolean verbose) {
        StringBuffer sb = new StringBuffer();
        sb.append(stories.size() + " stories excluded by filter: " + filter.asString() + "\n");
        if (verbose) {
            for (Story story : stories) {
                sb.append(story.getPath()).append("\n");
            }
        }
        print(sb.toString());
    }

    @Override
	public void runningWithAnnotatedEmbedderRunner(String className) {
        print("Running with AnnotatedEmbedderRunner '" + className + "'");
    }

    @Override
	public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        print("Annotated instance " + annotatedInstance + " if not of type " + type);
    }

    @Override
	public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        print("Generating reports view to '" + outputDirectory + "' using formats '" + formats + "'"
                + " and view properties '" + viewProperties + "'");
    }

    @Override
	public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        print("Failed to generate reports view to '" + outputDirectory + "' using formats '" + formats
                + "' and view properties '" + viewProperties + "'");
    }

    @Override
	public void reportsViewGenerated(ReportsCount count) {
        print("Reports view generated with " + count.getStories() + " stories (of which " + count.getStoriesPending()
                + " pending) containing " + count.getScenarios() + " scenarios (of which " + count.getScenariosPending() + " pending)");
        if (count.getStoriesNotAllowed() > 0 || count.getScenariosNotAllowed() > 0) {
            print("Meta filters excluded " + count.getStoriesNotAllowed() + " stories and  "
                    + count.getScenariosNotAllowed() + " scenarios");
        }
    }

    @Override
	public void reportsViewFailures(ReportsCount count) {
        print("Failures in reports view: " + count.getScenariosFailed() + " scenarios failed");
    }

    @Override
	public void reportsViewNotGenerated() {
        print("Reports view not generated");
    }

    @Override
	public void mappingStory(String storyPath, List<String> metaFilters) {
        print("Mapping story " + storyPath + " with meta filters " + metaFilters);
    }

    @Override
	public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        print("Generating maps view to '" + outputDirectory + "' using story maps '" + storyMaps + "'"
                + " and view properties '" + viewProperties + "'");
    }

    @Override
	public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
        print("Failed to generating maps view to '" + outputDirectory + "' using story maps '" + storyMaps + "'"
                + " and view properties '" + viewProperties + "'");
        printStackTrace(cause);
    }

    @Override
	public void generatingNavigatorView(File outputDirectory, Properties viewProperties) {
        print("Generating navigator view to '" + outputDirectory + "' using view properties '" + viewProperties + "'");
    }

    @Override
	public void navigatorViewGenerationFailed(File outputDirectory, Properties viewProperties, Throwable cause) {
        print("Failed to generating navigator view to '" + outputDirectory + "' using view properties '"
                + viewProperties + "'");
        printStackTrace(cause);
    }

    @Override
	public void navigatorViewNotGenerated() {
        print("Navigator view not generated, as the CrossReference has not been declared in the StoryReporterBuilder");
    }

    @Override
	public void processingSystemProperties(Properties properties) {
        print("Processing system properties " + properties);
    }

    @Override
	public void systemPropertySet(String name, String value) {
        print("System property '" + name + "' set to '" + value + "'");
    }

    @Override
	public void storyTimeout(Story story, StoryDuration storyDuration) {
        print("Story " + story.getPath() + " duration of " + storyDuration.getDurationInSecs()
                + " seconds has exceeded timeout of " + storyDuration.getTimeoutInSecs() + " seconds");
    }

    @Override
	public void usingThreads(int threads) {
        print("Using " + threads + " threads");
    }

    @Override
	public void usingExecutorService(ExecutorService executorService) {
        print("Using executor service " + executorService);
    }

    @Override
	public void usingControls(EmbedderControls embedderControls) {
        print("Using controls " + embedderControls);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    protected void print(String message) {
        Format.println(output, message);
    }

    protected void printStackTrace(Throwable e) {
        e.printStackTrace(output);
    }

}
