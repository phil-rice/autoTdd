package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.Test;

import com.thoughtworks.paranamer.CachingParanamer;

public class ParanamerConfigurationBehaviour {

    @Test
    public void shouldUseCachingParanamer() {
        assertThat(new ParanamerConfiguration().paranamer(), instanceOf(CachingParanamer.class));
    }
    
}
