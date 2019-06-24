#!/bin/bash 

echo ""
echo "***update_to_github: enter..."
echo ""

TIME_FLAG=`date +"%y%m%d.%H%M%S"`

run_git_cmd(){
    #echo "run_git_cmd..."
    echo ""
    echo "# $*"
    $*
    echo ""
}

run_git_cmd "git status"
run_git_cmd "git add ."
run_git_cmd "git commit -m update_${TIME_FLAG}"
run_git_cmd "git push git@github.com:93873637/Android.git"
run_git_cmd "git status"

echo ""
echo "***update_to_github: exit."
echo ""
