#!/usr/bin/perl

use Math::Trig;
use POSIX;

my @joints = (HeadYaw,HeadPitch,LShoulderPitch,LShoulderRoll,LElbowYaw,LElbowRoll,
              LHipYawPitch,LHipPitch,LHipRoll,LKneePitch,LAnklePitch,LAnkleRoll,
              RHipYawPitch,RHipPitch,RHipRoll,RKneePitch,RAnklePitch,RAnkleRoll,
              RShoulderPitch,RShoulderRoll,RElbowYaw,RElbowRoll);

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

foreach my $frame (@{$list}){
  print '1,' . join(",",@{$frame}) . "\n";
}

