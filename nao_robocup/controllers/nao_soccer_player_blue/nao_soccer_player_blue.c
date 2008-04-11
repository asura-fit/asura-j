/* this is an empty robot controller, the robot does nothing... */

#include <stdlib.h> /* definition of NULL */
#include <device/robot.h>

#define TIME_STEP 40

static int run(int ms) {
  /* just do nothing */
  return 10*TIME_STEP;
}

int main() {
  robot_live(NULL);       /* initialize */
  robot_run(run);
  return 0;
}
