package org.springframework.batch.item.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This implementation extends {@link org.springframework.batch.item.support.AbstractItemCountingItemStreamItemWriter}
 * and handle change of resource when we reach the <code>maxItemCount</code> and delegate to a
 * {@link org.springframework.batch.item.file.ResourceAwareItemWriterItemStream} the effective writing. The logic
 * to locate the resource is resolved by the {@link org.springframework.batch.item.support.ResourceLocator}.
 * <p>
 *   Subsequently to that, if the delegate is also an
 *   {@link org.springframework.batch.item.support.AbstractItemCountingItemStreamItemWriter} then it must have a
 *   <code>maxItemCount</code> lower than the one configured in this writer.
 *   <ul>
 *       <li><code>MultiResourceSupportItemWriter.maxItemCount</code> : is the max total number of items among all
 *       resources</li>
 *       <li><code>delegate.maxItemCount</code> : is the max number of items for one specific resource</li>
 *   </ul>
 * </p>
 * <p>
 *     If the {@link org.springframework.core.io.Resource} does not exists, it will be created by this writer.
 * </p>
 * <p>
 *   In case of restart this writer retrieve the last resource given by the last call to
 *   {@link org.springframework.batch.item.support.ResourceLocator#getResource(org.springframework.batch.item.ExecutionContext)}.
 * </p>
 *
 * @author Almex
 */
//FIXME If the delegate is not an AbstractItemCountingItemStreamItemWriter then we should change resource upon this.maxItemCount
public class MultiResourceSupportItemWriter<T> extends AbstractItemCountingItemStreamItemWriter<T> {

    private final static String RESOURCE_KEY = "resource";
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiResourceSupportItemWriter.class);
    protected ResourceLocator resourceLocator;
    private ResourceAwareItemWriterItemStream<? super T> delegate;
    private ExecutionContext executionContext;
    private Resource resource;
    private boolean opened = false;

    @Override
    public void open(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        super.open(executionContext);
    }

    @Override
    protected boolean doWrite(T item) throws Exception {
        List<T> items = new ArrayList<>();
        if (!opened) {
            File file = setResourceToDelegate();
            FileUtils.setUpOutputFile(file, false, true, false);
            Assert.state(file.canWrite(), "Output resource " + file.getAbsolutePath() + " must be writable");
            delegate.open(executionContext);
            opened = true;

            LOGGER.debug("Forced delegate to open a new file");
        }

        int beforeItemCount = -1;
        if (delegate instanceof AbstractItemCountingItemStreamItemWriter) {
            beforeItemCount = ((AbstractItemCountingItemStreamItemWriter) delegate).getCurrentItemCount();

            LOGGER.debug("beforeItemCount={}", beforeItemCount);
        }

        items.add(item);
        delegate.write(items);

        int afterItemCount = 0;
        if (delegate instanceof AbstractItemCountingItemStreamItemWriter) {
            afterItemCount = ((AbstractItemCountingItemStreamItemWriter) delegate).getCurrentItemCount();

            LOGGER.debug("afterItemCount={}", afterItemCount);
        }

        if (delegate instanceof AbstractItemCountingItemStreamItemWriter) {
            final int maxItemCount = ((AbstractItemCountingItemStreamItemWriter) delegate).getMaxItemCount();

            if (afterItemCount >= maxItemCount) {
                delegate.close();
                delegate.update(executionContext);
                opened = false;

                LOGGER.debug("Closing stream because we have reached maxItemCount={}", maxItemCount);
            }
        }

        return beforeItemCount < afterItemCount;
    }

    /**
     * Create output resource (if necessary) and point the delegate to it.
     */
    private File setResourceToDelegate() throws IOException {
        resource = resourceLocator.getResource(executionContext);

        delegate.setResource(resource);

        LOGGER.debug("Setting resource={} to delegate", resource != null ? resource.getFile().getAbsolutePath() : "null");

        return resource.getFile();
    }

    @Override
    protected void doOpen() throws Exception {
        if (executionContext.containsKey(getExecutionContextKey(RESOURCE_KEY))) {
            resource = new FileSystemResource(executionContext.getString(getExecutionContextKey(RESOURCE_KEY), ""));
            // It's a restart
            delegate.open(executionContext);
            delegate.setResource(resource);
            // We don't have to create the resource
            opened = true;


            LOGGER.trace("Stream is opened");
        } else {
            opened = false;

            LOGGER.trace("Stream is not opened yet");
        }
    }

    @Override
    protected void doClose() throws Exception {
        resource = null;
        setCurrentItemIndex(0);
        if (opened) {
            delegate.close();
            opened = false;

            LOGGER.trace("Stream is closed");
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        if (isSaveState()) {
            if (opened) {
                delegate.update(executionContext);
            }

            try {
                if (resource != null) {
                    executionContext.putString(getExecutionContextKey(RESOURCE_KEY), resource.getFile().getAbsolutePath());

                    LOGGER.trace("We store the resource={} into the execution context", resource.getFile().getAbsolutePath());
                }
            } catch (IOException e) {
                throw new ItemStreamException("Cannot get resource's absolute path", e);
            }
        }
    }

    public void setResourceLocator(ResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }

    public void setDelegate(ResourceAwareItemWriterItemStream<? super T> delegate) {
        this.delegate = delegate;
    }
}
