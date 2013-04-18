package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jbehave.core.io.ResourceLoader;
import org.junit.Test;

public class ExamplesTableFactoryBehaviour {

    private String tableAsString = "|one|two|\n|11|22|\n";

    @Test
    public void shouldCreateExamplesTableFromTableInput() {
        // Given
        ExamplesTableFactory factory = new ExamplesTableFactory();
        
        // When        
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);
        
        // Then
        assertThat(examplesTable.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldCreateExamplesTableFromResourceInput() {
        // Given
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ExamplesTableFactory factory = new ExamplesTableFactory(resourceLoader);
        
        // When
        String resourcePath = "/path/to/table";
        when(resourceLoader.loadResourceAsText(resourcePath)).thenReturn(tableAsString);
        ExamplesTable examplesTable = factory.createExamplesTable(resourcePath);
        
        // Then
        assertThat(examplesTable.asString(), equalTo(tableAsString));
    }

}
