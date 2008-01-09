package org.devtcg.asyncservice;

import org.devtcg.asyncservice.IAsyncServiceCounter;

interface IAsyncService
{
	/* Start the asynchronous counting sequence.  The service will count to `to', pausing
	 * 1 second between each interval. */
	void startCount(int to, IAsyncServiceCounter callback);
}
