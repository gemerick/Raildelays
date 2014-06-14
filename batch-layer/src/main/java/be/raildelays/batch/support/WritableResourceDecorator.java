package be.raildelays.batch.support;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

/**
 * @author Almex
 */
public interface WritableResourceDecorator extends WritableResource {

    void setOutputDirectory(Resource outputDirectory);

    Resource getOutputDirectory();

}