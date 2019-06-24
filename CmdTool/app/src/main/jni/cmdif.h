#ifndef _CMD_IF_H_
#define _CMD_IF_H_

#ifndef IN
#define IN
#endif
#ifndef OUT
#define OUT
#endif

#define MAX_CMD_LEN    1024
#define MAX_OUTPUT_BUF_SIZE    16384
#define MAX_LINE_BUF_LEN    MAX_OUTPUT_BUF_SIZE

int exec_command(IN char *cmd, OUT char *output, IN int output_size);

#endif //_CMD_IF_H_
