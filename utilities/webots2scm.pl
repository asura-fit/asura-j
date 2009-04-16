#!/usr/bin/perl

use Math::Trig;
use POSIX;

my @joints = (LShoulderPitch,LShoulderRoll,LElbowYaw,LElbowRoll,LHipYawPitch,LHipRoll,LHipPitch,LKneePitch,LAnklePitch,LAnkleRoll,RHipYawPitch,RHipRoll,RHipPitch,RKneePitch,RAnklePitch,RAnkleRoll,RShoulderPitch,RShoulderRoll,RElbowYaw,RElbowRoll);

my %defaults = (LShoulderPitch => 110, RShoulderPitch => 110,
LShoulderRoll => 20, RShoulderRoll => -20,
LElbowYaw => -80, RElbowYaw => 80,
LElbowRoll => -90, RElbowRoll => 90);

my $c = 0;
my %head = map{ $_ => $c++; } split(",",scalar(<>));

my $list = [];
my @times;
my $lastTime = 0;
while(<>){
  my @data = split(",");
  my $frame = [];

  my $time = shift @data;
  $time =~ /(\d+):(\d+):(\d+)/;
  $time = ($1*60 + $2)*1000 + $3;
#  push @times, $time - $lastTime;
  push @times, $time;
  $lastTime = $time;

  shift @data; # skip frameNo

  foreach my $joint (@joints){
    if(exists $head{$joint}){
#      push @{$frame}, rad2deg($data[$head{$joint}]);
      push @{$frame}, sprintf("%.3f", $data[$head{$joint}]);
    }elsif(exists $defaults{$joint}){
      push @{$frame}, $defaults{$joint};
    }else{
      push @{$frame}, 0;
    }
  }
  push @{$list}, $frame;
}

print ";XXX \n";
print '(mc-registmotion 120 "XXX" TIMED #(';
print "\n#(\n";
foreach my $frame (@{$list}){
  print '#(' . join(" ",@{$frame}) . ")\n";
}
print ") #(";
print join(" ",@times);
print ")\n)\n)\n";

