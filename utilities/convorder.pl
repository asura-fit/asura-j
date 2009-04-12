#!/usr/bin/perl
# 関節定義の順番を変更する.
# ALMotionの順番にあわせるのに使用. 手抜き実装.

use strict;
use warnings;
my @map = (0,1,2,3,4,5,6,8,7,9,10,11,12,14,13,15,16,17,18,19,20,21);

while(<>){
  my $l = $_;
  if($l =~ /\#\([\d\-]+(\s+[\d\-]+){21}\)/){
    $l =~ /\#\(([\d\-\s]+)\)/;
    my @values = split(" +", $1);
    my @mapped = ();
    for(my $i = 0; $i < @values; $i++){
      $mapped[$map[$i]] = $values[$i];
    }
    print "    #(". join(" ", @mapped) . ")\n";
  }else{
  print $l;
  }
}
