#ifndef _CMD_IF_H_
#define _CMD_IF_H_

#ifndef IN
#define IN
#endif
#ifndef OUT
#define OUT
#endif

int exec_command(IN char *cmd, OUT char *output, IN int output_size);

#endif //_CMD_IF_H_
