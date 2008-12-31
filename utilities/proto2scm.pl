#!/usr/bin/perl

# uasge
# perl utilities/proto2scm.pl HeadYaw HeadPitch camera RShoulderPitch RShoulderRoll RElbowYaw RElbowRoll
#  LShoulderPitch LShoulderRoll LElbowYaw LElbowRoll RHipYawPitch RHipRoll RHipPitch RKneePitch RAnklePitch RAnkleRoll
#  LHipYawPitch LHipRoll LHipPitch LKneePitch LAnklePitch LAnkleRoll < nao_robocup_webots6/protos/Nao.proto
#
#

use strict;

my @targets = @ARGV;

my $cur;

my $map = {};

my $state = "";

while(<STDIN>){
  my $line = $_;
  foreach my $t (@targets){
    if($line =~ m/name "$t"/){
      $cur = $t;
      $map->{$cur}->{name} = $cur;
      $state = "parse";
    }
  }

  if($state eq "parse" && $line =~ m/\}/){
    $state = "skip";
    printScheme($map->{$cur});
  }

  if(!exists $map->{$cur}->{translation} &&
    $line =~ m/translation ([+-.\d]+) ([+-.\d]+) ([+-.\d]+)/){
    $map->{$cur}->{translation}->{x} = $1 * 1000;
    $map->{$cur}->{translation}->{y} = $2 * 1000;
    $map->{$cur}->{translation}->{z} = $3 * 1000;
  }

  if(!exists $map->{$cur}->{axis} &&
    $line =~ m/rotation ([+-.\d]+) ([+-.\d]+) ([+-.\d]+) ([+-.\d]+)/){
    $map->{$cur}->{axis}->{x} = $1;
    $map->{$cur}->{axis}->{y} = $2;
    $map->{$cur}->{axis}->{z} = $3;
    if($4 != 0){
      $map->{$cur}->{angle} = $4;
    }
  }

  if($line =~ m/minPosition ([+-.\d]+)/){
    $map->{$cur}->{min} = $1;
  }

  if($line =~ m/maxPosition ([+-.\d]+)/){
    $map->{$cur}->{max} = $1;
  }

  if($line =~ m/mass ([+-.\d]+)/){
    $map->{$cur}->{mass} = $1;
  }

  #    translation -0.085 0.145 -0.02
  #      rotation 1 0 0 0
  #      minPosition -2.0944 # -120 degrees
  #      maxPosition 2.0944  # 120 degrees
  #      physics Physics {
  #        mass 0.053

}

sub printScheme{
  my $cur = shift;
#(RSole (sc-create-frame RSole '(
#                                     (translation . (0 -55 0))
#                                     (axis . (1 0 0))
#                                     )))
  my $trans = $cur->{translation};
  my $axis = $cur->{axis};
  print "($cur->{name} (sc-create-frame $cur->{name} '(\n";
  print " (translation . ($trans->{x} $trans->{y} $trans->{z}))\n" if(exists $cur->{translation});
  print " (axis . ($axis->{x} $axis->{y} $axis->{z}))\n" if(exists $cur->{axis});
  print " (max . $cur->{max})" if(exists $cur->{max});
  print " (min . $cur->{min})" if(exists $cur->{min});
  print " (mass . $cur->{mass})" if(exists $cur->{mass});
  print " (angle . $cur->{angle})" if(exists $cur->{angle});
  print ")))\n\n";
}
