#!/usr/bin/perl

use Math::Trig;
use POSIX;

print ";XXX \n";
print '(mc-registmotion 120 "XXX" COMPATIBLE #(';
print "\n#(\n";

my @steps;
while(<>){
  s/\t//g;
  chomp;
  my @data = split(",\s*");

  if($data[0] !~ /^\d/){
    print "; $data[0]\n";
    next;
  }
  
  push @steps, shift @data;
  
  my $frame = [];

  foreach (@data){
    push @{$frame}, $_;
  }
  
  print '#(' . join(" ", @{$frame}) . ")\n";
}

foreach my $frame (@{$list}){
}
print ") #(";
print join(" ", @steps);
print ")\n)\n)\n";

