
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>

#include "cmdif.h"

void usage()
{
    printf("[usage]:\n");
    printf("cmdif command_name [command_param1] [command_param2] ...\n");
    printf("\n");
}

int exec_command(IN char *cmd, OUT char *output, IN int output_size)
{
    char line[MAX_LINE_BUF_LEN];
    FILE *fp = NULL;
    
    fp = popen(cmd, "r");   
    if (fp == NULL)
    {
        sprintf(output, "popen cmd failed, error = %d\n", errno);
        return -1;
    }
    
    memset(line, 0, MAX_LINE_BUF_LEN);
    while(fgets(line, sizeof(line), fp) != NULL)
    {
        strcat(output, line);
    }
     
    pclose(fp); 
    return 0;
}

int main(int argc, char **argv)
{
    char cmd[MAX_CMD_LEN];
    char output[MAX_OUTPUT_BUF_SIZE];
    int i = 0;
    
    memset(cmd, 0, sizeof(cmd));
    memset(output, 0, sizeof(output));
    
    if (argc < 2) 
    {
        usage();
        return -1;
    }

    for (i=1; i<argc; i++) 
    {
        strcat(cmd, argv[i]);
        if (i != (argc - 1))
            strcat(cmd, " ");
    }
    printf("***cmd=\"%s\"\n", cmd);
    
    exec_command(cmd, output, sizeof(output));
    printf("***output:\n");
    printf("----------------\n");
    printf("%s\n", output);
    printf("----------------\n");
    return 0;
}
