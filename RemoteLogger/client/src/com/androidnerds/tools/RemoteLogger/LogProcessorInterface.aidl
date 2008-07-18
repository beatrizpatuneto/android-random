package com.androidnerds.tools.RemoteLogger;

import com.androidnerds.tools.RemoteLogger.LogProcessorInterface;

interface LogProcessorInterface
{
	void startTheCat( String serverAddress, String serverPort );
	void closeServer();
}