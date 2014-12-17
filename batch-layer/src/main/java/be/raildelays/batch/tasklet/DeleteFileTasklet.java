package be.raildelays.batch.tasklet;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;

/**
 * @author Almex
 * @since 1.2
 */
public class DeleteFileTasklet implements Tasklet, InitializingBean {

    private Resource source;
    private static final Logger LOGGER = LoggerFactory.getLogger(MoveFileTasklet.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(source, "The 'source' property must be provided");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        File file = source.getFile();

        LOGGER.info("Delete file from {}", file.getCanonicalPath());

        /*
         * If the destination file exists but is writable then it will be overwrite.
         * We must delete this on JVM shutdown in order to allow close() of ItemStream coming afterwards.
         */
        file.deleteOnExit();

        return RepeatStatus.FINISHED;
    }

    public void setSource(Resource source) {
        this.source = source;
    }
}

