#!/usr/bin/perl

use Math::Trig;
use POSIX;

my @joints = (HeadYaw,HeadPitch,LShoulderPitch,LShoulderRoll,LElbowYaw,LElbowRoll,LHipYawPitch,LHipPitch,LHipRoll,LKneePitch,LAnklePitch,LAnkleRoll,RHipYawPitch,RHipPitch,RHipRoll,RKneePitch,RAnklePitch,RAnkleRoll,RShoulderPitch,RShoulderRoll,RElbowYaw,RElbowRoll);

my %defaults = (LShoulderPitch => 110, RShoulderPitch => 110,
LShoulderRoll => 20, RShoulderRoll => -20,
LElbowYaw => -80, RElbowYaw => 80,
LElbowRoll => -90, RElbowRoll => 90);

my $c = 0;
my %head = map{ $_ => $c++; } split(",",scalar(<>));

my $list = [];
while(<>){
  my @data = split(",");
  my $frame = [];
  foreach my $joint (@joints){
    if(exists $head{$joint}){
      push @{$frame}, rad2deg($data[$head{$joint}]);
    }elsif(exists $defaults{$joint}){
      push @{$frame}, $defaults{$joint};
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

