#!/usr/bin/perl
###############################################################################
##
## $Id$
##
## Copyright (C) 2006 Josh Guilfoyle <jasta@devtcg.org>
##
## Total hackjob to generate specialized RemoteCallbackList classes from a
## custom AIDL.  Example usage:
##
## $ ./aidl-cblistsub.pl < IFooListener.aidl > IFooListenerCallbackList.java
##
###############################################################################

use strict;
use Data::Dumper;

###############################################################################

local $/;
my $f = <>;

# Remove comments.
$f =~ s/\/\/.*$//m;
$f =~ s/\/\*.*?\*\///s;

# Extract main interface block.
my ($pkg) = $f =~ m/^\s*package\s+([a-z0-9\._]+);$/mi;
my ($iname, $def) = $f =~ m/interface\s+([a-z0-9_]+)\s*{(.*?)}/si;
die unless $pkg;
die unless $iname;
die unless $def;

# Split methods.
my @parts = split /;/, $def;
my @methods;

foreach (@parts)
{
	s/^\s+//;
	s/\s+$//;
	next unless length($_);

	my $m = {};

	# Decompose prototype.
	my $typeAndToken;
	($typeAndToken, $m->{arglist}) =
	  $_ =~ m/^([a-z0-9_\s]+)\s*\((.*?)\)$/msi;
	die unless $typeAndToken;
	die unless $m->{arglist};

	@$m{qw/ret name/} = sepTypeAndToken($typeAndToken);
	die unless $m->{ret};
	die unless $m->{name};

	$m->{argnames} = join(', ',
	  map { [sepTypeAndToken($_)]->[1] }
	    (split /\s*,\s*/, $m->{arglist}));

	push @methods, $m;
}

my $name = $iname . 'CallbackList';

print qq#package $pkg;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class $name extends
  RemoteCallbackList<$iname>
{
	public $name()
	{
		super();
	}
#;

foreach my $m (@methods)
{
	print qq#
	public $m->{ret} broadcast$m->{name}($m->{arglist})
	{
		int N = beginBroadcast();

		for (int i = 0; i < N; i++)
		{
			try {
				getBroadcastItem(i).$m->{name}($m->{argnames});
			} catch (RemoteException e) {}
		}

		finishBroadcast();
	}
#;
}

print qq#}
#;

###############################################################################

sub sepTypeAndToken
{
	return $_[0] =~ m/^([\w\s]+)\s+([a-z0-9_]+)$/mi;
}
