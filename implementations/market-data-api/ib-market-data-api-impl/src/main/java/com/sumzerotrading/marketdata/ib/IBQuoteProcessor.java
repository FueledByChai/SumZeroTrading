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

import java.util.concurrent.BlockingQueue;

import com.sumzerotrading.marketdata.IQuoteEngine;

/**
 *
 * @author Rob Terpilowski
 */
public abstract class IBQuoteProcessor<T> implements Runnable {

    protected IQuoteEngine quoteEngine;
    protected BlockingQueue<T> quoteBlockingQueue;
    protected volatile boolean shouldRun = true;
    // protected Logger logger = Logger.getLogger( IBQuoteProcessor.class );
    protected Thread thread = new Thread(this, getClass().getName() + "-Thread");
    protected boolean started = false;

    public IBQuoteProcessor(BlockingQueue<T> queue, IQuoteEngine quoteEngine) {
        this.quoteBlockingQueue = queue;
        this.quoteEngine = quoteEngine;
    }

    public void startProcessor() {
        if (!started) {
            started = true;
            thread.start();
        }
    }

    public void stopProcessor() {
        if (started) {
            started = false;
            shouldRun = false;
        }
    }

    public void run() {
        while (shouldRun) {
            try {
                processData(quoteBlockingQueue.take());
            } catch (Exception ex) {
                // logger.error(ex, ex);
                ex.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return started;
    }

    protected abstract void processData(T data);

}
