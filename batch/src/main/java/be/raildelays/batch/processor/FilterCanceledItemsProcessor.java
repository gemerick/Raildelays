/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Almex
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package be.raildelays.batch.processor;

import be.raildelays.batch.bean.BatchExcelRow;
import be.raildelays.logging.Logger;
import be.raildelays.logging.LoggerFactory;
import org.apache.commons.lang.Validate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;

/**
 * Filter any canceled {@link BatchExcelRow}.
 *
 * @author Almex
 * @since 2.0
 */
public class FilterCanceledItemsProcessor implements ItemProcessor<BatchExcelRow, BatchExcelRow> {

    private static final Logger LOGGER = LoggerFactory.getLogger("Can", FilterCanceledItemsProcessor.class);

    @Override
    public BatchExcelRow process(final BatchExcelRow item) throws Exception {
        BatchExcelRow result = item;

        LOGGER.trace("item", item);

        if (item.isCanceled()) {
            LOGGER.debug("cancelled", item);
            result = null;
        }

        LOGGER.trace("result", result);

        return result; // To apply the ItemReader contract
    }

}
