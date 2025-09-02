/**
 * MIT License

Copyright (c) 2015  Rob Terpilowski

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.sumzerotrading.marketdata.ib;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.ib.IbUtils;
import com.sumzerotrading.marketdata.IQuoteEngine;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.util.QuoteUtil;

/**
 *
 * @author Rob Terpilowski
 */
public class IBLevel1QuoteProcessor extends IBQuoteProcessor<Level1QuoteData> {

    public IBLevel1QuoteProcessor(BlockingQueue<Level1QuoteData> queue, IQuoteEngine quoteEngine) {
        super(queue, quoteEngine);
    }

    @Override
    protected void processData(Level1QuoteData data) {
        if (data.getSize().value().doubleValue() > 0) {
            processTickSize(data);
        } else {
            processTickPrice(data);
        }
    }

    protected void processTickPrice(Level1QuoteData data) {
        QuoteType quoteType;

        quoteType = IbUtils.getQuoteType(data.getField());
        // This is a quote type we don't care about.
        if (quoteType == null || QuoteType.UNKNOWN == quoteType) {
            return;
        }

        BigDecimal formattedPrice = QuoteUtil.getBigDecimalValue(data.getTicker(), data.getPrice());
        processQuote(data.getTicker(), quoteType, formattedPrice);

    }

    protected void processTickSize(Level1QuoteData data) {
        QuoteType quoteType;
        Ticker ticker = data.getTicker();
        int size = data.getSize().value().intValue();

        quoteType = IbUtils.getQuoteType(data.getField());
        if (quoteType == null || quoteType == QuoteType.UNKNOWN) {
            return;
        }

        if (ticker.getInstrumentType() == InstrumentType.STOCK) {
            size = size * 100;
        }
        BigDecimal formattedValue = QuoteUtil.getBigDecimalValue(size);
        processQuote(ticker, quoteType, formattedValue);
    }

    protected void processQuote(Ticker ticker, QuoteType quoteType, BigDecimal formattedValue) {
        Map<QuoteType, BigDecimal> quoteMap = new HashMap<>();
        quoteMap.put(quoteType, formattedValue);
        Level1Quote quote = new Level1Quote(ticker, getTime(), quoteMap);

        quoteEngine.fireLevel1Quote(quote);
    }

    protected ZonedDateTime getTime() {
        return ZonedDateTime.now();
    }

}
