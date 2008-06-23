#!/usr/bin/perl

use Math::Trig;
use POSIX;

my @joints = (HeadYaw,HeadPitch,LShoulderPitch,LShoulderRoll,LElbowYaw,LElbowRoll,LHipYawPitch,LHipPitch,LHipRoll,LKneePitch,LAnklePitch,LAnkleRoll,RHipYawPitch,RHipPitch,RHipRoll,RKneePitch,RAnklePitch,RAnkleRoll,RShoulderPitch,RShoulderRoll,RElbowYaw,RElbowRoll);

my $c = 0;
my %head = map{ $_ => $c++; } split(",",scalar(<>));

my $list = [];
while(<>){
  my @data = split(",");
  my $frame = [];
  foreach my $joint (@joints){
    if(exists $head{$joint}){
      push @{$frame}, floor(rad2deg($data[$head{$joint}]) + 0.5);
    }else{
      push @{$frame}, 0;
    }
  }
  push @{$list}, $frame;
}

print ";XXX \n";
print '(mc-registmotion 120 "XXX" LINER #(';
print "\n#(\n";
foreach my $frame (@{$list}){
  print '#(' . join(" ",@{$frame}) . ")\n";
}
print ") #(";
print join(" ",map { 1; } @{$list});
print ")\n)\n)\n";

