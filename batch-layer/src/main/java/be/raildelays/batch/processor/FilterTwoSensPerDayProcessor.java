package be.raildelays.batch.processor;

import be.raildelays.batch.bean.BatchExcelRow;
import be.raildelays.logger.Logger;
import be.raildelays.logger.LoggerFactory;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.beans.factory.InitializingBean;

/**
 * Filter items to get only two. One for departure and the other one for arrival.
 * The only remaining items are those which have the maximum delay for a given sens.
 *
 * @author Almex
 */
public class FilterTwoSensPerDayProcessor implements ItemProcessor<BatchExcelRow, BatchExcelRow>, InitializingBean {

    private ResourceAwareItemReaderItemStream<BatchExcelRow> outputReader;

    private static final Logger LOGGER = LoggerFactory.getLogger("2Ss", AggregateExpectedTimeProcessor.class);

    private ExecutionContext executionContext;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Validate.notNull(outputReader, "outputReader is mandatory");
    }

    @Override
    public BatchExcelRow process(final BatchExcelRow item) throws Exception {
        BatchExcelRow result = null;

        LOGGER.trace("item", item);

        try {
            outputReader.open(executionContext);

            try {
                BatchExcelRow matchingExcelRow = null;
                do {
                    matchingExcelRow = outputReader.read();

                    if (matchingExcelRow != null) {
                        if (new CompareToBuilder().append(item.getDate(), matchingExcelRow.getDate())
                                .append(item.getDepartureStation(), matchingExcelRow.getDepartureStation())
                                .append(item.getArrivalStation(), matchingExcelRow.getArrivalStation())
                                .toComparison() == 0) {
                            /**
                             * Here we know that we have a collision: we match the same date and the same sens.
                             * If the delay of the item is not greater than the one in the Excel sheet then we skip it.
                             */
                            if (item.getDelay() > matchingExcelRow.getDelay()) {
                                result = item;

                                if (matchingExcelRow.getIndex() != null) {
                                    result.setIndex(matchingExcelRow.getIndex());

                                    /**
                                     * Here, the delay of the item is greater than the matching Excel row.
                                     * We must replace the row currently in the Excel sheet with our item.
                                     */
                                    LOGGER.trace("replace_matching", matchingExcelRow);
                                } else {
                                    throw new IllegalArgumentException("We don't know the current index of this Excel row. We cannot replace it!");
                                }
                            }

                            /**
                             * We stop searching here. Either the result is found or we have to skip this item.
                             */
                            LOGGER.debug("stop_searching", result);

                            break;
                        } else if (item.getDate().before(matchingExcelRow.getDate())) {
                            /**
                             * We stop searching. We expect that the content of the Excel file is sorted by date.
                             * This clause should never happen if the data read are also sorted by date.
                             */
                            LOGGER.debug("item=_before_matching", matchingExcelRow);

                            break;
                        }
                    } else {
                        result = item;
                        result.setIndex(null);

                        /**
                         * In that case we reach the first empty row without matching any previous data.
                         * So, we have to add a new row to the Excel sheet.
                         */
                        LOGGER.debug("first_empty_row", item);

                        break;
                    }
                } while (matchingExcelRow != null);
            } finally {
                outputReader.close();
            }
        } catch (ItemStreamException e) {
            LOGGER.warn("Error when opening ResourceAwareItemReaderItemStream. Maybe the resource is not available yet.", e);
            result = item;
        }

        LOGGER.trace("result", result);

        return result;
    }

    public void setOutputReader(ResourceAwareItemReaderItemStream<BatchExcelRow> outputReader) {
        this.outputReader = outputReader;
    }
}
