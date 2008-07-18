package com.androidnerds.tools.RemoteLogger;

import com.androidnerds.tools.RemoteLogger.LogProcessorInterface;

interface LogProcessorInterface
{
	boolean startTheCat( String serverAddress, String serverPort );
	boolean closeServer();
}