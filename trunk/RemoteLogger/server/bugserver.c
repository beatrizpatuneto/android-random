/**
 * This is a really really really simple server application to listen for bug reports.
 */

#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <errno.h>
#include <getopt.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#define PORT_NUMBER 2342

void prog_version();
void usage();

enum {
	HELP_OPTION = 1,
	VERSION_OPTION,
	PORT_OPTION
};

static char const shortopts[] = "vhp:";

static struct option const longopts[] = {
	{ "version", 0, 0, VERSION_OPTION },
	{ "help", 0, 0, VERSION_OPTION },
	{ "port", 0, 0, PORT_OPTION }
};

int main( int argc, char **argv )
{
	int c;
	while( ( c = getopt_long( argc, argv, shortopts, longopts, NULL ) ) != -1 ) {
		switch( c ) {
			case 'p':
				runProg( optarg );
			case 'h':
				usage();
				return 0;
			case 'v':
				prog_version();
				return 0;
			default:
				runProg( PORT_NUMBER );
		}
	}
	return 0;
}
int runProg( int port )
{
	int fd = -1, result;
	int programResult = EXIT_FAILURE;

	result = socket( AF_INET, SOCK_STREAM, 0 );

	if( result == -1 ) {
		fprintf( stderr, "could not grab a socket. error: %d / %s\n" , errno, strerror( errno ) );
		goto bailout;
	}
	fd = result;

	int yes = 1;
	result = setsockopt( fd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof( int ) );

	if( result == -1 ) {
		fprintf( stderr, "could not set sockopt to reuseaddr. %d / %s\n", errno, strerror( errno ) );
		goto bailout;
	}

	//bind to the port.
	{
		struct sockaddr_in address;
		//address.sin_len = sizeof( struct sockaddr_in );
		address.sin_family = AF_INET;
		address.sin_port = htons( PORT_NUMBER );
		address.sin_addr.s_addr = htonl( INADDR_ANY );
		memset( address.sin_zero, 0, sizeof( address.sin_zero ) );

		result = bind( fd, ( struct sockaddr * )&address, sizeof( address ) );
		if( result == -1 ) {
			fprintf( stderr, "could not bind socket. error: %d / %s\n", errno, strerror( errno ) );
			goto bailout;
		}
	}

		result = listen( fd, 8 );
		if( result == -1 ) {
			fprintf( stderr, "listen failed. error: %d / %s\n", errno, strerror( errno ) );
			goto bailout;
		}

		while( 1 ) {
			struct sockaddr_in address;
			socklen_t addressLength = sizeof( address );
			int remoteSocket;
			char buffer[8092];
			
			result = accept( fd, ( struct sockaddr * )&address, &addressLength );

			if( result == -1 ) {
				fprintf( stderr, "accept failed. error: %d / %s\n", errno, strerror( errno ) );
				continue;
			}

			printf( "accepted connection from %s:%d\n", inet_ntop( AF_INET, &address.sin_addr, buffer, sizeof( buffer ) ), ntohs( address.sin_port ) );
			remoteSocket = result;

			while( 1 ) {
				result = read( remoteSocket, buffer,  8092);

				if( result == 0 ) {
					//EOF
					break;
				} else if( result == -1 ) {
					fprintf( stderr, "could not read from the remote host. error %d / %s\n", errno, strerror( errno ) );
					break;
				} else {
					//hey hey we have a result to play with.
					//write out the streaming text to a file.
					buffer[result] = '\000';
					printf( "%s", buffer );
					printf( "\n" );
				}
			}
			
			close( remoteSocket );
		}
		
		programResult = EXIT_SUCCESS;

bailout:
	close( fd );
	return ( programResult );

}

void usage()
{
	//print out the program information.
	printf( "Bugserver Version 0.01 - Copyright (c) 2008 NovakLabs\n" );
	printf( "Usage: bugserver [OPTION]... files\n\n" );
	printf( "-p Specifiy a port to listen on.\n" );
	printf( "-v View the version and license info.\n" );
	printf( "-h View this screen.\n\n" );
	printf( "View the logcat output from a connected android device..\n\n" );
	printf( "Report bugs to remotelogger-bugs@novaklabs.com\n" );
}

void prog_version()
{
	printf( "bugserver (The friendly android log reader) Version 0.01\n" );
	printf( "Copyright (c) 2008, NovakLabs\n\n" );
	printf( "This program comes with NO WARRANTY, to the extent permitted by law.\n" );
	printf( "You may redistribute copies of this program\n" );
	printf( "under the terms of the GNU General Public License.\n" );
	printf( "For more information about these matters, see the file named COPYING.\n\n" );
	printf( "Written and Maintained By Mike Novak\n" );
}
